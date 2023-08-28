import java.util.concurrent.ExecutorService ;
import java.util.concurrent.Executors   ;
import java.util.concurrent.TimeUnit;
import java.io.IOException  ;
import java.io.File;

public class client
{
    public static void main(String args[])throws IOException
    {

        File inputDirectory=new File(".\\Input");
        int inputFiles=inputDirectory.list().length;
        System.out.println("Number of files in input folder are: "+inputFiles);

        
    
        /**************************/
        int firstLevelThreads = inputFiles/3-1  ;   // Indicate no of users 
        /**************************/
        //Creating a thread pool
       
        ExecutorService executorService = Executors.newFixedThreadPool(firstLevelThreads);
        
        for(int i = 0; i < firstLevelThreads; i++)
        {
            Runnable runnableTask = new invokeWorkers();    //  Pass arg, if any to constructor sendQuery(arg)
            executorService.submit(runnableTask) ;
        }

        executorService.shutdown();
        try
        {    // Wait for 8 sec and then exit the executor service
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS))
            {
                executorService.shutdownNow();
            } 
        } 
        catch (InterruptedException e)
        {
            executorService.shutdownNow();
        }
    }
}