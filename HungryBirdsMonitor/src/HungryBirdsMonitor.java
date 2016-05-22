/*This is the instruction of problem 5 of homework 4 of the course ID1217 Concurrent Programming.
 *problem description: Given are n baby birds and one parent bird. 
 *The baby birds eat out of a common dish that initially contains W worms. 
 *Each baby bird repeatedly takes a worm, eats it, sleeps for a while, takes another worm, 
 *and so on. If the dish is empty, the baby bird who discovers the empty dish chirps real loud 
 *to awaken the parent bird. The parent bird flies off and gathers W more worms, 
 *puts them in the dish, and then waits for the dish to be empty again. 
 *This pattern repeats forever.
 *To compile : javac  HungryBirdsMonitor.java
 *To run: java HungryBirdsMonitor numberOfBabyBirds
 *Alternative : run program in eclipse*/

public class HungryBirdsMonitor {
	static Monitor monitor;
	final static int number_babybird_Max = 5;
	final static int number_parentBird = 1;
	static BabyBird[] babyBirds;
	static ParentBird momBird;
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
		monitor = new Monitor(100);
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
		int worm;
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
					worm = 100 - monitor.require_eat();   //get permit to eat
					System.out.println("bird " + id + " eat the " + worm + " worm");
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
				monitor.require_fill();; //wake by baby bird and fulfill the dish
				System.out.println("dear birds, food is prepared");	
			}
		}
	}

	public static class Monitor
	{
		int worms; //shared variable honey
		public Monitor(int worms){   //Construct
			this.worms = worms;
		}
		
		
		public synchronized int require_eat()  
		{
			while( worms == 0)  //worms is not available, wait for fulfill again
			{
				try {
					wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			worms--; //worms is available
			notifyAll(); //tell others to eat or to fill
			return worms;
		}
		
		public synchronized void require_fill()
		{
			while( worms != 0) // worms do not need to fill
			{
				try {
					wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			worms = 100;  // fulfill worms
			notifyAll();
		}
		
	}
}
