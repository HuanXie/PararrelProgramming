/*This is the instruction of problem 4 of homework 3 of the course ID1217 Concurrent Programming.
 *This program simulate an unisexbathroom which can be used by any number of men or any number of women, 
 *but not at the same time. 
 *This multithreaded program provides a fair solution to this problem using only semaphores for synchronization
 *To compile : javac  UnisexBathroom.java
 *To run: java UnisexBathroom numberOfManThread numberOfWomanThread
 *Alternative : run program in eclipse*/
import java.util.concurrent.Semaphore;


public class UnisexBathroom {

	final static int number_man_Max = 10;
	final static int number_woman_Max = 10;
	static Man[] men;
	static Woman[] women;
	static Semaphore womansKey = new Semaphore(0, true); //open bathroom for woman, release before require
	static Semaphore mansKey = new Semaphore(0, true); //open bathroom for man, release before require
	static Semaphore washingmutex = new Semaphore(0, true); //
	static Semaphore finishWashing = new Semaphore(1, true);
	static int numberInBathroom = 0;
	public static void main(String[] args) throws InterruptedException {
		int number_man = number_man_Max; //default number of man thread
		int number_woman = number_woman_Max; //default number of woman thread
		
		
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
		
		/*put UnisexBathroom into use*/
		while(true)
		{
			womansKey.release();//bathroom open for woman,lady first!
			washingmutex.release();
			System.out.println("bathroom is opening for woman.");
			Thread.sleep(100);
			womansKey.acquire(); //bathroom close for other women
			System.out.println("!!!!!entrance is closed!!!!!!!!");
			washingmutex.acquire();//waiting for them to finish, exchange sex
			
			System.out.println("************** SEX EXCHANGE***********");
			
			mansKey.release();
			washingmutex.release();
			System.out.println("bathroom is opening for man.");
			Thread.sleep(100);
			mansKey.acquire(); //bathroom close for other men
			System.out.println("!!!!!!!entrance is closed!!!!!!!!");
			washingmutex.acquire();//waiting for them to finish, exchange sex
			
			System.out.println("************** SEX EXCHANGE***********");
			
		}
	}
	
	private static class Man extends Thread
	{
		long sleep = (long) (Math.random()*1000);
		long washing = (long) (Math.random()*100);
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
					mansKey.acquire(); // get enter chance
					numberInBathroom ++;  
					System.out.println("man " + id + " enters the bathroom");
					System.out.println(numberInBathroom + " men in bathroom");
					if(numberInBathroom == 1) // the first in bathroom get the mutex semaphore
					{
						washingmutex.acquire();
					}
					mansKey.release(); // let others to come in
					sleep(washing);  //washing time
					finishWashing.acquire(); //lock to protect the critical section
					numberInBathroom--;
					System.out.println("man " + id + " leaves the bathroom");
					System.out.println(numberInBathroom + " men in bathroom");
					if(numberInBathroom == 0)
					{
						washingmutex.release(); // the last leaving bathroom release the mutex semaphore
					}
					finishWashing.release();
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private static class Woman extends Thread
	{
		long sleep = (long) (Math.random()*1000);
		long washing = (long) (Math.random()*100);
		int id;
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
					womansKey.acquire();// get enter chance
					System.out.println("woman " + id + " enters the bathroom");
					numberInBathroom ++;
					System.out.println(numberInBathroom + " women in bathroom");
					if(numberInBathroom == 1)
					{
						washingmutex.acquire();// the first in bathroom get the mutex semaphore
					}
					womansKey.release();// let others to come in
					sleep(washing);//washing time
					finishWashing.acquire();//lock to protect the critical section
					numberInBathroom--;
					System.out.println("woman " + id + " leaves the bathroom");
					System.out.println(numberInBathroom + " women in bathroom");
					if(numberInBathroom == 0)
					{
						washingmutex.release();// the last leaving bathroom release the mutex semaphore
					}
					finishWashing.release();
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			
		}
	}
}
