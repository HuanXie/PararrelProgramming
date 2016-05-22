package pphw16a;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
/*This parallel multithreaded program finds all the palindromic words in file "words"
 * and write the palindromic words to a results file "result.txt"*/
public class pWord {
	static int maxWorker = 10; /*default number of thread*/
	final static int size = 25143; /*Quantity of words*/
	static String[] wordList = new String[size]; /*read sequentially from original file*/
	final static Lock lock = new ReentrantLock(); /*lock of bag of tasks*/
	static int tasks = 1;/*number of row begin with 1, task begin with 1*/
	final static Lock resultslock = new ReentrantLock();/*lock for result*/
	static Thread thread[];/*thread array*/
	static LinkedList<String> resultsList = new LinkedList<String>(); /*output result list*/
	static int[] staticsofThread; /*to save number of palindromic words found by each thread*/
	final static String outputFileName = "result.txt";/*result file*/

	public static void main(String[] args) throws IOException {
		
		/*check the command line argument*/
		if(args.length > 1)
		{
			int numworker = Integer.parseInt(args[1]);
			if( numworker < maxWorker && numworker > 0 ) /*input number of worker should be bigger than 0, and smaller than max worker number*/
			{
				maxWorker = numworker;
			}
		}
		thread = new Thread[maxWorker]; 
		staticsofThread = new int[maxWorker];
		
		BufferedReader reader = new BufferedReader(new FileReader("words"));
		
		/*Sequentially read words from file "words" */
		for(int i = 0; i < size; i++)
		{
			wordList[i] = reader.readLine();
		}
		reader.close();
		
		long startTime = System.currentTimeMillis(); /*start time*/
		/*create threads*/
		for(int j = 0; j< maxWorker; j++)
		{
			worker work= new worker(j);
			thread[j]= new Thread(work);
			thread[j].start();
			//System.out.println("thread "+ j + "is starting");
		}
		
		/*join the threads when work is done*/
		for(int x = 0; x < maxWorker; x++)
		{
			try {
				thread[x].join();
				//System.out.println("work thread"+ x+ " joined");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		/*calculate total palindromic words*/
		int sum = 0;
		for(int l = 0; l < maxWorker; l++)
		{
			sum = sum + staticsofThread[l]; 
			System.out.println("thread" + l + " find " + staticsofThread[l] + " palindromic words");
		}
		System.out.println("total " + sum + " palindromic words found");
		Writer writer = null;

		/*Sequentially write the result in file "result.txt"*/
		try {
		    writer = new BufferedWriter(new OutputStreamWriter(
		          new FileOutputStream(outputFileName), "utf-8"));
		    writer.write(resultsList.toString());
		    writer.close();
		} catch (IOException ex) {
		  // report
		} finally {
		   try {writer.close();} catch (Exception ex) {/*ignore*/}
		}
		
		long endTime   = System.currentTimeMillis();
		double totalTime = (double)(endTime - startTime)/1000; /*total execution time*/
		System.out.println("the execution time is: " +totalTime +"s");
	}
	
	/*thread class*/
	static class worker implements Runnable
	{
		int id;
		public worker(int id)
		{
			this.id = id;
		}
		
		@Override
		public void run() 
		{
			int row;
			while(true)
			{
				lock.lock();   //lock bag of tasks
				row = tasks;    //get a row to work on
				tasks++;
				lock.unlock();
				if(row > size)  //after the last row 
				{
					break;
				}
				else
				{
					checkWords(row, id); // check if words in list[row] has a palindromic word
				}
			}
		}
	
		public void checkWords(int row, int id)
		{
			/*from the index of word to be checked to the end of word list, try to find palindromic word*/
			for(int i = row-1; i< wordList.length; i++)
			{
				
				if (isOk(wordList[row - 1], wordList[i]))/*true when palindromic word is found*/
				{
					/*save the words par in result list synchronously*/
					resultslock.lock();
					resultsList.add(wordList[row-1]);
					resultsList.add(wordList[i]);
					resultsList.add("\n");
					staticsofThread[id]++;
					resultslock.unlock();
					break;
				}//else no palindromic word found
			}
		}

		/*help funtion to check whether the two strings are palindromic words*/
		private boolean isOk(String string1, String string2) {
			if (string1.length() == string2.length()) {
				int length = string1.length();
				for (int i = 0; i < length; ++i) {
					char a = string1.charAt(i);
					char b = string2.charAt(length - 1 - i);
					if (a != b && Math.abs(a-b) != 'a'-'A') /*if exists difference*/
					{
						return false;
					}
				}
				/*if all the characters are the same*/
				return true; 
			}
			return false;
		}
	}

}

