This is the instruction of problem 1 of homework 2 of the course ID1217 Concurrent Programming.

The program computes a sum of matrix elements, finds maximum and minimum element of matrix in parallel using openmp.

This program is compiled and only need to run directly.
The makefile of this program is generated automatically by eclipse. 

To build the project 
build project using eclipse 
Alternative: go into the directory /matrixsum-omp/Debug
command line input is : make

To run the project
run project directly in eclipse
in terminal: ./matrixsum-omp.exe sizeofMatrix numberofthread 
the second command argument control how many threads will be used( max 10 thread)
if run without the second argument, or a argument bigger than 10, the project will run in default 10 threads.