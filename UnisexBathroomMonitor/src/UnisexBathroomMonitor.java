/*This is the instruction of problem 8 of homework 4 of the course ID1217 Concurrent Programming.
 *This program simulate an unisexbathroom which can be used by any number of men or any number of women, 
 *but not at the same time. 
 *This multithreaded program provides a fair solution to this problem using java monitor
 *To compile : javac  UnisexBathroomMonitor.java
 *To run: java UnisexBathroomMonitor numberOfManThread numberOfWomanThread
 *Alternative : run program in eclipse*/
public class UnisexBathroomMonitor {
	final static int number_man_Max = 10;
	final static int number_woman_Max = 10;
	static Man[] men;   //array for threads
	static Woman[] women;  //array for threads
	final static boolean doorforwoman = true;  //permit to woman
	final static boolean doorforman = false;   //permit to man
	final static boolean open = true;   //entrance open
	final static boolean Close = false;  // entrance close
	static Monitor monitor;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int number_man = number_man_Max; //default number of man thread
		int number_woman = number_woman_Max; //default number of woman thread
		
		//command line check
		if(args.length > 2) 
		{
			int input_number_man = Integer.parseInt(args[1]);
			int input_number_woman = Integer.parseInt(args[2]);
			if( input_number_man < number_man_Max && input_number_man > 0 ) /*input number of worker should be bigger than 0, and smaller than max worker number*/
			{
				number_man = input_number_man;
			}
			if( input_number_woman < number_woman_Max && input_number_woman > 0 ) /*input number of worker should be bigger than 0, and smaller than max worker number*/
			{
				number_woman = input_number_woman;
			}
		}
		
		//create concurrent object
		monitor = new Monitor(0, doorforwoman, open);
		/*create woman thread*/
		women = new Woman[number_woman];
		for(int i = 0; i < number_woman; i++)
		{
			women[i] = new Woman(i);
		}
		/*create man thread*/
		men = new Man[number_man];
		for(int i = 0; i < number_man; i++)
		{
			men[i] = new Man(i);
		}
		/*start woman thread and man thread separately*/
		for(int i = 0; i < number_woman; i++)
		{
			women[i].start();
		}
		for(int i = 0; i < number_man; i++)
		{
			men[i].start();
		}
		
		//controller in main thread, guarantee that woman and man take wash in turn
		while(true)
		{
			try {
				monitor.open_door_for_woman();
				Thread.sleep(100);
				monitor.close();
				
				monitor.open_door_for_man();
				Thread.sleep(100);
				monitor.close();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	private static class Woman extends Thread
	{
		long sleep = (long) (Math.random()*1000);
		long washing = (long) (Math.random()*1000);
		int id;
		int numberInBathroom;
		Woman(int id)
		{
			this.id = id;
		}
		public void run()
		{
			while(true)
			{
				//System.out.println("woman thread " + id +" is running");
				try {
					sleep(sleep); // sleep random time
					numberInBathroom = monitor.require_key_woman(id);// get enter chance
					sleep(washing);
					
					numberInBathroom = monitor.finishwash_woman(id);// let others to come in
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			
		}
	}

	private static class Man extends Thread
	{
		long sleep = (long) (Math.random()*1000);
		long washing = (long) (Math.random()*100);
		int numberInBathroom;
		int id;
		Man(int id)
		{
			this.id = id;
		}
		public void run()
		{
			while(true)
			{
				
				//System.out.println("man thread " + id +" is running");
				try {
					sleep(sleep); // sleep random time
					numberInBathroom = monitor.require_key_man(id);// get enter chance
					
					sleep(washing);
					
					numberInBathroom = monitor.finishwash_man(id);// let others to come in
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	
	
	public static class Monitor{
		int numberInBathroom; //record of numbers of person in bathroom
		boolean door;  //conditional variable for checking that if the door is opening for woman or man
		boolean iscloseforanyone; //variable for checking if door is opening or closed
		boolean finishwashing = true; //control if people in bathroom finish washing, if they finished, the door can open to another gender
		
		//constructor
		public Monitor(int numberInBathroom, boolean door, boolean open)
		{
			this.numberInBathroom = numberInBathroom;
			this.door = door;
			this.iscloseforanyone = open;
		}
		
		
		public synchronized int require_key_woman(int id)  
		{
			while(door == doorforman || iscloseforanyone == Close ) //door is not open for woman or door is closed for anyone
			{
				try {
					wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}//the door is open for woman, if the door is closed for anyone, no one can come in
			numberInBathroom++;
			if(numberInBathroom == 1) //the first one in bathroom will get the finish key
			{
				finishwashing = false; 
			}
			System.out.println("woman " + id + " enters the bathroom");
			System.out.println(numberInBathroom + " women in bathroom");
			notifyAll();
			return numberInBathroom;
		}
		
		public synchronized int finishwash_woman(int id)  
		{
			numberInBathroom--;
			if(numberInBathroom == 0)  //the last one in bathroom will release the finish key
			{
				finishwashing = true;
				//System.out.println("************** SEX EXCHANGE***********");
			}
			System.out.println("woman " + id + " leaves the bathroom");
			System.out.println(numberInBathroom + " women in bathroom");
			notifyAll();
			return numberInBathroom;
		}
		
		public synchronized int require_key_man(int id)  
		{
			while(door == doorforwoman || iscloseforanyone == Close ) //door is close or this turn is for man
			{
				try {
					wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			numberInBathroom++;
			if(numberInBathroom == 1)//the first one in bathroom will get the finish key
			{
				finishwashing = false;
			}
			System.out.println("man " + id + " enters the bathroom");
			System.out.println(numberInBathroom + " men in bathroom");
			notifyAll();
			return numberInBathroom;
		}
		
		public synchronized int finishwash_man(int id)  
		{
			numberInBathroom--;
			if(numberInBathroom == 0) //the last one in bathroom will release the finish key
			{
				finishwashing = true;
			}
			System.out.println("man " + id + " leaves the bathroom");
			System.out.println(numberInBathroom + " men in bathroom");
			notifyAll();
			return numberInBathroom;
		}
		
		
		
		public synchronized void close()  
		{
			iscloseforanyone = Close;
			System.out.println("!entrance is closed for anyone");
			notifyAll();
		}
		
		public synchronized void open_door_for_woman()  
		{
			while(finishwashing == false)  //wait for man finish
			{
				try {
					wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			door = doorforwoman; //set permit to woman
			iscloseforanyone = open;  //open entrance
			System.out.println("**********************woman***********************");
			notifyAll();
				
		}
		
		public synchronized void open_door_for_man() 
		{
			while(finishwashing != true) //check if woman finish
			{
				try {
					wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			door = doorforman;  //set permit to man
			iscloseforanyone = open; //open entrance
			System.out.println("********************man********************");
			notifyAll();
		}
		
		
	}


	
	
	
	
}
