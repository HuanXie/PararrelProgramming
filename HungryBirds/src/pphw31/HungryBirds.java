/*This is the instruction of problem 1 of homework 3 of the course ID1217 Concurrent Programming.
 *problem description: Given are n baby birds and one parent bird. 
 *The baby birds eat out of a common dish that initially contains W worms. 
 *Each baby bird repeatedly takes a worm, eats it, sleeps for a while, takes another worm, 
 *and so on. If the dish is empty, the baby bird who discovers the empty dish chirps real loud 
 *to awaken the parent bird. The parent bird flies off and gathers W more worms, 
 *puts them in the dish, and then waits for the dish to be empty again. 
 *This pattern repeats forever.
 *To compile : javac  HungryBirds.java
 *To run: java HungryBirds numberOfBabyBirds
 *Alternative : run program in eclipse*/
package pphw31;


import java.util.concurrent.Semaphore;

public class HungryBirds {
	final static int number_babybird_Max = 5;
	final static int number_parentBird = 1;
	static BabyBird[] babyBirds;
	static ParentBird momBird;
	/*This solution is fair, because fairness of Semaphore toEat is set true, the
	 * semaphore guarantees that threads invoking any of the acquire() methods are selected to obtain permits in the order in
	 * which their invocation of those methods was processed
	 * (first-in-first-out; FIFO).
	 * Thus babybirds avoid starvation */
	static Semaphore toEat = new Semaphore(1, true); //require before release, fairness is set true;
	static Semaphore tofill = new Semaphore(0);//release before require
	static int worms = 100;
	static int numberEatingBirds = 0;
	static int count = 0;
	public static void main(String[] args) {
		int number_babybird = number_babybird_Max;  // default number of baby bird
		if(args.length > 1) 
		{
			int input_number_bird = Integer.parseInt(args[1]);
			if( input_number_bird < number_babybird_Max && input_number_bird > 0 ) /*input number of worker should be bigger than 0, and smaller than max worker number*/
			{
				number_babybird = input_number_bird;
			}
		}
		
		babyBirds = new BabyBird[number_babybird];
		/*create threads for baby bird*/
		for(int i = 0; i < number_babybird; i++)
		{
			babyBirds[i] = new BabyBird(i);
			babyBirds[i].start();
		}
		momBird = new ParentBird(0);// create mom bird thread and start
		momBird.start();

	}
	
	/*consumer thread, each bird eat one worm*/
	private static class BabyBird extends Thread
	{
		long sleep = (long) (Math.random()*1000);
		int id;
		BabyBird(int id)
		{
			this.id = id;
		}
		public void run()
		{
			//System.out.println("baby bird thread " + id + "begin to run");
			while(true)
			{
				try {
					toEat.acquire(); //get permit to eat
					worms --; 
					count++; //for debug
					System.out.println("bird " + id + " eat the " + count + " worm");
					if(worms == 0) // the bird who eats the last worm need to wake mum bird
					{
						count = 0;
						tofill.release(); //call mom bird
						System.out.println("mom!! we have nothing to eat!! ");
					}else
					{
						toEat.release(); //call other birds to eat worms
					}
					sleep(sleep);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
	
	}

	/*producer thread to fulfill dish*/
	private static class ParentBird extends Thread
	{
		int id;
		ParentBird(int id)
		{
			this.id = id;
		}
		
		public void run()
		{
			//System.out.println("mom bird thread " + id + "begin to run");
			while(true)
			{
				try {
					tofill.acquire(); //wake by baby bird and fulfill the dish
					worms = 100;
					System.out.println("dear birds, food is prepared");
					toEat.release(); // tell baby bird to eat
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
	}

}
