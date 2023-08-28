import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.sql.Date;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.*;
import java.util.*;

class ConnectDB
{
    Connection c = null;
    public Connection connect(){
    try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/railways","postgres", "postgresql");
            c.setAutoCommit(false);
            //c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            //System.exit(0);
        }
    System.out.println("Opened database successfully");
    return c;
    }

    public void close(){
        try{
            c.close();
        } catch (Exception e){
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
    }
}

class TrainAddition
{
    public String addTrain(Connection c, Vector<String>tokens){
    String query="CALL add_train (?,?,?,?,?);";
    String responseQuery="";
                    Date date= new Date(0);
                    try{
                        CallableStatement stmt=c.prepareCall(query);
                    stmt.setInt(1,Integer.parseInt(tokens.get(0)));
                    stmt.setDate(2,date.valueOf(tokens.get(1)));
                    stmt.setInt(3,Integer.parseInt(tokens.get(2)));
                    stmt.setInt(4,Integer.parseInt(tokens.get(3)));
                    stmt.registerOutParameter(5,Types.VARCHAR);

                    stmt.executeUpdate();
                    c.commit();
                    responseQuery=stmt.getString(5);
                    //System.out.println(count + " row/s affected");
                    }catch (Exception e){
                            System.err.println( e.getClass().getName()+": "+ e.getMessage() );
                            //System.exit(0);
                    }
                    return responseQuery;
    }
}


class TicketBooking
{
    public String ticketBooking(Connection c, Vector<String>tokens, int num_tokens){
    String responseQuery="";
    String query= "CALL booking_ticket (?,?,?,?,?,?);";
                    Date date= new Date(0);
                    int num_passengers=Integer.parseInt(tokens.get(0));
                    String names="";
                    
                    for(int i=0;i<num_passengers;i++){
                        names=names+tokens.get(i+1)+" ";
                    }
                    try{
                    CallableStatement stmt=c.prepareCall(query);
                    stmt.setInt(1,Integer.parseInt(tokens.get(num_tokens -3)));
                    stmt.setDate(2,date.valueOf(tokens.get(num_tokens-2)));
                    stmt.setString(3,tokens.get(num_tokens-1));
                    stmt.setInt(4,Integer.parseInt(tokens.get(0)));
                    stmt.setString(5,names);
                    stmt.registerOutParameter(6,Types.VARCHAR);

                    stmt.executeUpdate();
                    c.commit();
                    responseQuery=stmt.getString(6);
                    //printWriter.println(responseQuery);
                    stmt.close();
                    }catch (Exception e){
                            System.err.println( e.getClass().getName()+": "+ e.getMessage() );
                            //System.exit(0);
                    }
            return responseQuery;
    }
}

class PrintTicket
{
    public Vector<String> printTicket(Connection c, Vector<String>tokens, int num_tokens,String pnr_no){
        String table_name="\""+tokens.get(num_tokens-3) + "_"+tokens.get(num_tokens-2)+"\"";
        //System.out.println(table_name);
        String query = "SELECT * FROM "+table_name+" WHERE pnr= '"+ pnr_no + "';";
        //System.out.println(query);
        Vector<String>output=new Vector<String>();
        try{
            Statement stmt=c.createStatement();
            ResultSet rs=stmt.executeQuery(query);
            String userData="";
            userData="Train no: "+tokens.get(num_tokens-3);
            output.add(userData);
            userData="Date: "+tokens.get(num_tokens-2);
            output.add(userData);
            userData="PNR No.: "+pnr_no;
            output.add(userData);
            //System.out.println("lololo");
            while(rs.next()){
                userData = rs.getString(2)+" "+rs.getString(3)+" "+rs.getInt(4)+" "+rs.getString(5);
                //System.out.println(userData);
                output.add(userData);
            }
            //System.out.println("nononono");
            stmt.close();

        }catch(Exception e){
            System.err.println( e.getClass().getName()+": "+ e.getMessage() );
            //System.exit(0);
        }

        return output;
    }
    
}

class QueryRunner implements Runnable
{
    //  Declare socket for client access
    protected Socket socketConnection;

    public QueryRunner(Socket clientSocket)
    {
        this.socketConnection =  clientSocket;
    }

    public void run()
    {
      try
        {
            //  Reading data from client
            ConnectDB conn=new ConnectDB();
            Connection c=conn.connect();
            InputStreamReader inputStream = new InputStreamReader(socketConnection
                                                                  .getInputStream()) ;
            BufferedReader bufferedInput = new BufferedReader(inputStream) ;
            OutputStreamWriter outputStream = new OutputStreamWriter(socketConnection
                                                                     .getOutputStream()) ;
            BufferedWriter bufferedOutput = new BufferedWriter(outputStream) ;
            PrintWriter printWriter = new PrintWriter(bufferedOutput, true) ;
            String clientCommand = "" ;
            String responseQuery = "" ;
            // Read client query from the socket endpoint
            clientCommand = bufferedInput.readLine(); 
            while( ! clientCommand.equals("#"))
            {
                
                System.out.println("Recieved data <" + clientCommand + "> from client : " 
                                    + socketConnection.getRemoteSocketAddress().toString());

                

                int num_tokens=0;
                Vector<String> tokens = new Vector<String>();
                Vector<String> passenger_details=new Vector<String>();
                String pnr="";
                responseQuery="";
                StringTokenizer st = new StringTokenizer(clientCommand," ");  
                while (st.hasMoreTokens()) {  
                    String s=st.nextToken();  
                    s=s.replace(",","");
                    tokens.add(s);
                    num_tokens++;
                }

                if(num_tokens==4)
                {
                    //System.out.println("train adding");
                    TrainAddition newTrain=new TrainAddition();
                    responseQuery=newTrain.addTrain(c,tokens);
                }
                else
                {
                    //System.out.println("ticket booking");
                    TicketBooking newTicket=new TicketBooking();
                    pnr=newTicket.ticketBooking(c,tokens,num_tokens);
                    //System.out.println(pnr);
                    responseQuery=pnr;
                    //System.out.println(responseQuery);
                    if(!responseQuery.equals("no seats") && !responseQuery.equals("no train")){
                        //System.out.println(responseQuery+"bi");
                        PrintTicket ticket=new PrintTicket();
                        passenger_details=ticket.printTicket(c,tokens,num_tokens,responseQuery);
                    }
                }
                /*******************************************
                         Your DB code goes here
                ********************************************/
                
                // Dummy response send to client
                //responseQuery = "******* Dummy result ******";      
                //  Sending data back to the client
                //System.out.println(responseQuery+"jj");
                if(responseQuery.equals("invalid train no")){
                    printWriter.println("Train cannot be added, invalid train no. !");
                }
                else if(responseQuery.equals("success")){
                    printWriter.println("Train added successfully");
                }
                else if(responseQuery.equals("train already exists")){
                    printWriter.println("Train already exists !");
                }
                else if(responseQuery.equals("no seats")){
                    //System.out.println(responseQuery+"kkk");
                    printWriter.println("Ticket booking failed, seats not available !");
                }
                else if(responseQuery.equals("no train")){
                    //System.out.println(responseQuery+"iii");
                    printWriter.println("Train has not yet been launched in the system for booking !");
                }
                else{
                    //System.out.println(responseQuery+"ooo");
                    //printWriter.println(responseQuery);
                    for(int i=0;i<passenger_details.size();i++){
                        printWriter.println(passenger_details.get(i));
                    }
                }
                printWriter.println("");
                pnr="";
                responseQuery="";
                // Read next client query
                clientCommand = bufferedInput.readLine(); 
            }
            inputStream.close();
            bufferedInput.close();
            outputStream.close();
            bufferedOutput.close();
            printWriter.close();
            socketConnection.close();
            conn.close();
        }
        catch(IOException e)
        {
            return;
        }
    }
}

/**
 * Main Class to controll the program flow
 */
public class ServiceModule 
{
    // Server listens to port
    static int serverPort = 7008;
    // Max no of parallel requests the server can process
    static int numServerCores = 5 ;         
    //------------ Main----------------------
    public static void main(String[] args) throws IOException 
    {    
        // Creating a thread pool
        ExecutorService executorService = Executors.newFixedThreadPool(numServerCores);
        
        try (//Creating a server socket to listen for clients
        ServerSocket serverSocket = new ServerSocket(serverPort)) {
            Socket socketConnection = null;
            
            // Always-ON server
            while(true)
            {
                System.out.println("Listening port : " + serverPort 
                                    + "\nWaiting for clients...");
                socketConnection = serverSocket.accept();   // Accept a connection from a client
                System.out.println("Accepted client :" 
                                    + socketConnection.getRemoteSocketAddress().toString() 
                                    + "\n");
                //  Create a runnable task
                Runnable runnableTask = new QueryRunner(socketConnection);
                //  Submit task for execution   
                executorService.submit(runnableTask);   
            }
        }
    }
}

