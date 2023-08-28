CS301 - DATABASES


** For establishing connection, localhost: 5432, database name: railways, user: postgres, password: postgresql

1) In the client.java file, the number of files present in the Input directory of the folder are counted.
2) If the number of input files are 'n', then the number of pools are taken to be n/3.
   eg. 12 files, 4 pools
3) The hardcoded value of threads is taken, which is 3. So, each pool will be expected to have 3 threads except for the first pool
   whose number of threads will be equal to the total number of pools.

4) The database consists of majorly three tables and unknown number of dynamically created tables.
5) Relation 'train_stats' stores the information regarding the train no, date and the no. of AC and SL coaches and net seats available.
6) Relation 'ac_composition' stores information regarding berth type of a given berth number in an AC coach.
7) Relation 'sl_composition' stores information regarding berth type of a given berth number in an SL coach.
8) Whenever a booking is done for a train on a particular date, a new table is created if it doesn't exist, with name '<train_no>_<date>'.
9) The details of the passengers, pnr numbers and their seats and coach information are all stored in the respective tables mentiones in 8).
10) The procedure 'add_train' inserts a new train into table 'train_stats' if the train no. is valid, i.e., 4 digits.
11) The procedure 'booking_ticket' books the ticket if seats are available.