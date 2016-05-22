/*This is the instruction of problem 2 of homework 3 of the course ID1217 Concurrent Programming.
 *problem description:Given are n honeybees and a hungry bear. 
 *They share a pot of honey. The pot is initially empty; its capacity is H portions of honey. 
 *The bear sleeps until the pot is full, then eats all the honey and goes back to sleep. 
 *Each bee repeatedly gathers one portion of honey and puts it in the pot; 
 *the bee who fills the pot awakens the bear.
 *To compile : javac  BearAndHoneybee.java
 *To run: java BearAndHoneybee numberOfHoneybeeThread
 *Alternative : run program in eclipse*/
import java.util.concurrent.Semaphore;

public class BearAndHoneybee {

	final static int number_Honeybee_Max = 5;
	final static int number_Bear = 1;
	static Honeybee[] honeybee;
	static Bear bear;
	/*This solution is fair, because fairness of Semaphore toFill is set true, the
	 * semaphore guarantees that threads invoking any of the acquire() methods are selected to obtain permits in the order in
	 * which their invocation of those methods was processed
	 * (first-in-first-out; FIFO).
	 * Thus bees avoid starvation */
	static Semaphore toFill = new Semaphore(1, true); //require before release, fairness is set true;
	static Semaphore toEat = new Semaphore(0);//release before require
	static int pot = 0;
	final static int full = 100;
	public static void main(String[] args) {
		int number_bees = number_Honeybee_Max;  // default number of honeybee
		if(args.length > 1) 
		{
			int input_number_bees = Integer.parseInt(args[1]);
			if( input_number_bees < number_Honeybee_Max && input_number_bees > 0 ) /*input number of worker should be bigger than 0, and smaller than max worker number*/
			{
				number_bees = input_number_bees;
			}
		}
		
		
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
		Honeybee(int id)
		{
			this.id = id;
		}
		public void run()
		{
			while(true)
			{
				try {
					toFill.acquire(); //get permit to visit critical section
					pot ++;
					System.out.println("bee " + id + " does it work");
					System.out.println("pot is " + pot + "%"+ " full");
					if(pot == full) //the bee who fulfill the pot should call bear to eat
					{
						toEat.release();
						System.out.println("bear!! wake and eat honey!! ");
					}else//otherwise let other bees continue to fulfill the pot
					{
						toFill.release(); 
					}
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
				try {
					toEat.acquire(); //get permit to eat honey
					pot = 0; //eat up honey
					System.out.println("ohhh! I love honey! I want eat more!");
					toFill.release(); //tell bee to work again
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
	}

}
