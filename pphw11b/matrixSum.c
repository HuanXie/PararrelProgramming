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
pthread_mutex_t minmutex;
pthread_mutex_t maxmutex;
pthread_mutex_t summutex;
long sum=0;
int Max,Min,NumberOfMaxRow,NumberOfMaxColumn,NumberOfMinRow, NumberOfMinColumn;
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
  pthread_cond_init(&go, NULL);
  pthread_mutex_init(&summutex, NULL);

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

#ifdef DEBUG
  for (i = 0; i < size; i++) {
  	  for (j = 0; j < size; j++) {
  	    totals += matrix[i][j];
  	  }
    }
  printf("the totals is %ld\n",totals);
#endif

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
  for (l = 0; l < numWorkers; l++) {
    pthread_create(&workerid[l], &attr, Worker, (void *) l);
  }

  for (i = numWorkers -1; i >= 0; i--)
  {
	  int ret = pthread_join(workerid[i], NULL);
	  printf ("Completed join with thread %d, ret=%d\n", i, ret);
  }
  printf("the sum of matrix is %ld\n", sum);
  printf("The max element is %d in row %d column %d\n", Max, NumberOfMaxRow, NumberOfMaxColumn);
  printf("The min element is %d in row %d column %d\n", Min, NumberOfMinRow, NumberOfMinColumn);
  /*pthread_exit(NULL);*/
}

/* Each worker sums the values in one strip of the matrix.
   After a barrier, worker(0) computes and prints the total */
void *Worker(void *arg) {
  long myid = (long) arg;
  long total, i, j, first, last;
  int max, min, numberOfMaxRow,numberOfMaxColumn,numberOfMinRow, numberOfMinColumn;

#ifdef DEBUG
  printf("worker %ld (pthread id %ld) has started\n", myid, pthread_self());
#endif

  /* determine first and last rows of my strip */
  first = myid*stripSize;
  last = (myid == numWorkers - 1) ? (size - 1) : (first + stripSize - 1);/*if max row number == numworkers*/
  /* sum values in my strip */
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
	  }/*else equal*/
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

  /*del uppgifter 1*/
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
