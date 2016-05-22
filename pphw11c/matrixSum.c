/* matrix summation using pthreads

   features: uses a barrier; the Worker[0] computes
             the total sum from partial sums computed by Workers
             and prints the total sum to the standard output

   usage under Linux:
     gcc matrixSum.c -lpthread
     a.out size numWorkers

*/
#ifndef _REENTRANT 
#define _REENTRANT 
#endif 
#include <pthread.h>
#include <stdlib.h>
#include <stdio.h>
#include <stdbool.h>
#include <time.h>
#include <sys/time.h>
#define MAXSIZE 10000  /* maximum matrix size */
#define MAXWORKERS 10   /* maximum number of workers */

pthread_mutex_t barrier;  /* mutex lock for the barrier */
pthread_cond_t go;        /* condition variable for leaving */
int numWorkers;           /* number of workers */ 
int numArrived = 0;       /* number who have arrived */
int nextRow = 0; /*task bag*/
long totals=0;

/* a reusable counter barrier */
void Barrier() {
  pthread_mutex_lock(&barrier);
  numArrived++;
  if (numArrived == numWorkers) {
    numArrived = 0;
    pthread_cond_broadcast(&go);
  } else
    pthread_cond_wait(&go, &barrier);
  pthread_mutex_unlock(&barrier);
}

/* timer */
double read_timer() {
    static bool initialized = false;
    static struct timeval start;
    struct timeval end;
    if( !initialized )
    {
        gettimeofday( &start, NULL );
        initialized = true;
    }
    gettimeofday( &end, NULL );
    return (end.tv_sec - start.tv_sec) + 1.0e-6 * (end.tv_usec - start.tv_usec);
}

double start_time, end_time; /* start and end times */
int size, stripSize;  /* assume size is multiple of numWorkers */
int sums[MAXWORKERS]; /* partial sums */
int matrix[MAXSIZE][MAXSIZE]; /* matrix */
pthread_mutex_t minmutex; /*mutex for min element*/
pthread_mutex_t maxmutex; /*mutex for max element*/
pthread_mutex_t summutex; /*mutex for sum*/
pthread_mutex_t taskbagmutex; /*mutex for bag of tasks*/
long sum = 0; /*sum of matrix*/
int Max,Min,NumberOfMaxRow,NumberOfMaxColumn,NumberOfMinRow, NumberOfMinColumn;/*store max and min elements and theirs positions*/
void *Worker(void *);

/* read command line, initialize, and create threads */
int main(int argc, char *argv[]) {

  int i, j;
  long l; /* use long in case of a 64-bit system */
  pthread_attr_t attr;
  pthread_t workerid[MAXWORKERS];


  /* set global thread attributes */
  pthread_attr_init(&attr);
  pthread_attr_setscope(&attr, PTHREAD_SCOPE_SYSTEM);

  /* initialize mutex and condition variable */
  pthread_mutex_init(&barrier, NULL);
  pthread_mutex_init(&minmutex, NULL);
  pthread_mutex_init(&maxmutex, NULL);
  pthread_mutex_init(&summutex, NULL);
  pthread_cond_init(&go, NULL);
  if (pthread_mutex_init(&taskbagmutex, NULL) != 0)
  {
      printf("\n mutex init failed\n");
      return 1;
  }

  /* read command line args if any */
  size = (argc > 1)? atoi(argv[1]) : MAXSIZE;
  numWorkers = (argc > 2)? atoi(argv[2]) : MAXWORKERS;
  if (size > MAXSIZE) size = MAXSIZE;
  if (numWorkers > MAXWORKERS) numWorkers = MAXWORKERS;
  stripSize = size/numWorkers;

  /* initialize the matrix */
  srand(time(NULL));
  for (i = 0; i < size; i++) {
	  for (j = 0; j < size; j++) {
          matrix[i][j] = rand()%99; /*initial rand number*/
	  }
  }
  /* initialize the matrix min and max value*/
  Max = matrix[0][0];
  Min = matrix[0][0];

  /* print the matrix */
#ifdef DEBUG1
  for (i = 0; i < size; i++) {
	  printf("[ ");
	  for (j = 0; j < size; j++) {
	    printf(" %d", matrix[i][j]);
	  }
	  printf(" ]\n");
  }
#endif


  /* do the parallel work: create the workers */
  start_time = read_timer();
  for (l = 0; l < numWorkers; l++)
    pthread_create(&workerid[l], &attr, Worker, (void *) l);
  /*wait for all the workers to join into the main thread*/
  for (i = 0; i < numWorkers; i++)
  {
	  int ret = pthread_join(workerid[i], NULL);
	  printf ("Completed join with thread %d, ret=%d\n", i, ret);
  }
  end_time = read_timer();
  printf("the sum of matrix is %ld\n", sum);
  printf("The max element is %d in row %d column %d\n", Max, NumberOfMaxRow, NumberOfMaxColumn);
  printf("The min element is %d in row %d column %d\n", Min, NumberOfMinRow, NumberOfMinColumn);
  printf("The execution time is %g sec\n", end_time - start_time);

#ifdef DEBUG
  for (i = 0; i < size; i++) {
  	  for (j = 0; j < size; j++) {
  	    totals += matrix[i][j];
  	  }
    }
  printf("test : the totals is %ld\n",totals);
#endif
}

/* Each worker sums the values, and find the max and min value, in
 * the rows which are allowed to be worked on by bag of tasks.
 */
void *Worker(void *arg) {
  long myid = (long) arg; /*workers ID*/
  long total = 0; /*local sum of rows*/
  long j = 0;
  int max, min, numberOfMaxRow,numberOfMaxColumn,numberOfMinRow, numberOfMinColumn; /*local extreme values*/
  int row;

#ifdef DEBUG
  printf("worker %ld (pthread id %ld) has started\n", myid, pthread_self());
#endif

  /*initialization*/
  total = 0;
  max = matrix[0][0];
  min = matrix[0][0];

  while(true)
  {
	  pthread_mutex_lock(&taskbagmutex);/*get a task*/
	  row = nextRow;
	  nextRow++;
	  pthread_mutex_unlock(&taskbagmutex);
	  if(row >= size) /*task bag is empty*/
	  {
		  break;
	  }
	  else
	  {
		  for (j = 0; j < size; j++)
		  {
		  	total += matrix[row][j];

		  	if(matrix[row][j] > max) /*calculate the local max of worker*/
		  	{
		  		max = matrix[row][j];
		  		numberOfMaxRow = row;
		  		numberOfMaxColumn = j;
		  	}else if(matrix[row][j] < min) /*calculate the local min of worker*/
		  	{
		  		min = matrix[row][j];
		  		numberOfMinRow = row;
		  		numberOfMinColumn = j;
		  	}/*else equal*/
		  }
	  }
  }
  pthread_mutex_lock(&summutex); /*calculate sum of matrix as a critical section*/
  sum += total;
  pthread_mutex_unlock(&summutex);


  if(Max < max) /*uppdate the Max if needed*/
  {
	pthread_mutex_lock(&maxmutex);
	if(Max < max)
	{
		Max = max;
		NumberOfMaxRow = numberOfMaxRow;
		NumberOfMaxColumn = numberOfMaxColumn;
	}
	pthread_mutex_unlock(&maxmutex);
  }else if(Min > min) /*uppdate the Min if needed*/
  {
	pthread_mutex_lock(&minmutex);
	if(Min > min)
	{
		Min = min;
		NumberOfMinRow = numberOfMinRow;
		NumberOfMinColumn = numberOfMinColumn;
	}
	pthread_mutex_unlock(&minmutex);
   }
  pthread_exit(NULL);


  /*task 2 only has difference in funktion worker compared with task 3
  void *Worker(void *arg) {
  long myid = (long) arg;
  long total, i, j, first, last;
  int max, min, numberOfMaxRow,numberOfMaxColumn,numberOfMinRow, numberOfMinColumn;

  first = myid*stripSize;
  last = (myid == numWorkers - 1) ? (size - 1) : (first + stripSize - 1);
  total = 0;
  max = matrix[0][0];
  min = matrix[0][0];
  for (i = first; i <= last; i++){
    for (j = 0; j < size; j++)
    {
      total += matrix[i][j];
      if(matrix[i][j] > max)
      {
    	  max = matrix[i][j];
    	  numberOfMaxRow = i;
    	  numberOfMaxColumn = j;
      }else if(matrix[i][j] < min)
	  {
    	  min = matrix[i][j];
    	  numberOfMinRow = i;
    	  numberOfMinColumn = j;
	  }
    }
  }
  pthread_mutex_lock(&summutex);
  sum += total;
  printf("worker %ld sum is %ld, total is %ld\n", myid, sum, total);
  pthread_mutex_unlock(&summutex);


  if(Max < max)
  {
	  pthread_mutex_lock(&maxmutex);
	  Max = max;
	  NumberOfMaxRow = numberOfMaxRow;
	  NumberOfMaxColumn = numberOfMaxColumn;
	  pthread_mutex_unlock(&maxmutex);
  }else if(Min > min)
  {
	  pthread_mutex_lock(&minmutex);
	  Min = min;
	  NumberOfMinRow = numberOfMinRow;
	  NumberOfMinColumn = numberOfMinColumn;
	  pthread_mutex_unlock(&minmutex);
  }
  printf("thread %ld is terminated\n",myid);
  pthread_exit(NULL);
  }*/

  /*task 1*/
  /*sums[myid] = total;
  Barrier();
  if (myid == 0) {
    total = 0;
    for (i = 0; i < numWorkers; i++)
      total += sums[i];

    end_time = read_timer();

    printf("The total is %ld\n", total);
    printf("The max element is %d in row %d column %d\n", max, numberOfMaxRow, numberOfMaxColumn);
    printf("The min element is %d in row %d column %d\n", min, numberOfMinRow, numberOfMinColumn);
    printf("The execution time is %g sec\n", end_time - start_time);
  }
*/
}
