/*This is the instruction of problem 6 of homework 4 of the course ID1217 Concurrent Programming.
 *problem description:Given are n honeybees and a hungry bear. 
 *They share a pot of honey. The pot is initially empty; its capacity is H portions of honey. 
 *The bear sleeps until the pot is full, then eats all the honey and goes back to sleep. 
 *Each bee repeatedly gathers one portion of honey and puts it in the pot; 
 *the bee who fills the pot awakens the bear.
 *To compile : javac  BearBeeMonitor.java
 *To run: java BearBeeMonitor numberOfHoneybeeThread
 *Alternative : run program in eclipse*/

public class BearBeeMonitor {

	final static int number_Honeybee_Max = 5;
	final static int number_Bear = 1;
	final static int full = 100;
	static Honeybee[] honeybee;
	static Bear bear;
	static Monitor monitor;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int number_bees = number_Honeybee_Max;  // default number of honeybee
		if(args.length > 1) 
		{
			int input_number_bees = Integer.parseInt(args[1]);
			if( input_number_bees < number_Honeybee_Max && input_number_bees > 0 ) /*input number of worker should be bigger than 0, and smaller than max worker number*/
			{
				number_bees = input_number_bees;
			}
		}
		
		monitor = new Monitor(0);
		honeybee = new Honeybee[number_bees];
		/*create and start threads for Honeybee*/
		for(int i = 0; i < number_bees; i++)
		{
			honeybee[i] = new Honeybee(i);
			honeybee[i].start();
		}
		/*create and start threads for bear*/
		bear = new Bear(0);
		bear.start();
	}

	//producer threads
		private static class Honeybee extends Thread
		{
			long sleep = (long) (Math.random()*1000);
			int id;
			int honey;
			Honeybee(int id)
			{
				this.id = id;
			}
			public void run()
			{
				while(true)
				{
					try {
						honey = monitor.require_fill(); //get permit to visit critical section
						System.out.println("bee " + id + " fill pot to " + honey + " %.");
						sleep(sleep);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			}
		
		}

		//consumer thread
		private static class Bear extends Thread
		{
			int id;
			Bear(int id)
			{
				this.id = id;
			}
			
			public void run()
			{
				while(true)
				{
					monitor.require_eat(); //get permit to eat honey
					System.out.println("ohhh! I love honey! I want eat more!");
					
				}
			}
		}
		
		public static class Monitor
		{
			int pot; //shared variable honey
			public Monitor(int pot){   //Construct
				this.pot = pot;
			}
			
			
			public synchronized void require_eat()  
			{
				while( pot != full)  //honey is not available, wait for fulfill 
				{
					try {
						wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				pot = 0; //eat out
				notifyAll(); //tell bees to fill
			}
			
			public synchronized int require_fill()
			{
				while( pot == full) // honey do not need to fill
				{
					try {
						wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				pot++;  // fulfill honey
				notifyAll(); //notify other bees and bear
				return pot;
			}
			
		}

}
