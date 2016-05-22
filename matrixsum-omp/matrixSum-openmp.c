/* matrix summation using OpenMP

   usage with gcc (version 4.2 or higher required):
     gcc -O -fopenmp -o matrixSum-openmp matrixSum-openmp.c 
     ./matrixSum-omp size numWorkers

*/
#include <omp.h>

double start_time, end_time;

#include <stdio.h>
#define MAXSIZE 10000   /* maximum matrix size */
#define MAXWORKERS 4   /* maximum number of workers */

int numWorkers;
int size; 
int matrix[MAXSIZE][MAXSIZE];
void rememberMax(int localmax, int numberOfRow, int numberofColumn);
void rememberMin(int localmin, int numberOfRow, int numberofColumn);
int Max,Min,NumberOfMaxRow,NumberOfMaxColumn,NumberOfMinRow, NumberOfMinColumn;

/* read command line, initialize, and create threads */
int main(int argc, char *argv[]) {
  int i, j;
  long total =0;
  Max=Min=NumberOfMaxRow=NumberOfMaxColumn=NumberOfMinRow=NumberOfMinColumn = 0;


  /* read command line args if any */
  size = (argc > 1)? atoi(argv[1]) : MAXSIZE;
  numWorkers = (argc > 2)? atoi(argv[2]) : MAXWORKERS;
  if (size > MAXSIZE) size = MAXSIZE;
  if (numWorkers > MAXWORKERS) numWorkers = MAXWORKERS;

  omp_set_num_threads(numWorkers);

  /* initialize the matrix */
  for (i = 0; i < size; i++) {
    //  printf("[ ");
	  for (j = 0; j < size; j++) {
      matrix[i][j] = rand()%99;
      //	  printf(" %d", matrix[i][j]);
	  }
	  //	  printf(" ]\n");
  }

  /* initialize the matrix min and max value*/
    Max = matrix[0][0];
    Min = matrix[0][0];

  start_time = omp_get_wtime();
  /* do the parallel work*/
#pragma omp parallel for reduction (+:total) private(j)
  for (i = 0; i < size; i++)
    for (j = 0; j < size; j++){
      total += matrix[i][j];
      if(matrix[i][j] > Max)
      {
#pragma omp critical
    	  {
    		  if(matrix[i][j] > Max)/*uppdate the Max if needed*/
    		  {
    			  Max = matrix[i][j];
    			  NumberOfMaxRow = i;
    			  NumberOfMaxColumn = j;
    		  }
    	  }
      }else if(matrix[i][j] < Min) /*calculate the local min of worker*/
	  {
#pragma omp critical
    	  {
    		  if(matrix[i][j] < Min)/*uppdate the Min if needed*/
    		  {
    			  Min = matrix[i][j];
    			  NumberOfMinRow = i;
    			  NumberOfMinColumn = j;
    		  }
    	  }
	  }
    }
// implicit barrier

  end_time = omp_get_wtime();

  printf("the total is %ld\n", total);
  printf("it took %g seconds\n", end_time - start_time);
  printf("the max element is %d\nin row %d\ncolumn %d\n", Max, NumberOfMaxRow, NumberOfMaxColumn );
  printf("the min element is %d\nin row %d\ncolumn %d\n", Min, NumberOfMinRow, NumberOfMinColumn);

}

