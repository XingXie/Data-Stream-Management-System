/**
 * @file      row_win.cc
 * @date      Aug. 26, 2004
 * @brief     Implements the row window
 */
#include <stdio.h>
#include <string.h>
#include <time.h>
#include <iostream>
#include <fstream>

using namespace std;
#ifndef _DEBUG_
#include "common/debug.h"
#endif

#ifndef _ROW_WIN_
#include "execution/operators/row_win.h"
#endif

using namespace Execution;
//using std::endl;

#define LOCK_INPUT_TUPLE(t) (inStore -> addRef ((t)))

// Xing: Set timing flag
//static 	int timeflag = 0;
RowWindow::RowWindow (unsigned int id, std::ostream &_LOG)
	: LOG (_LOG)
{
	this -> id             = id;
	this -> inputQueue     = 0;
	this -> outputQueue    = 0;
	this -> windowSize     = 0;
	this -> windowLevel     = 0;
	this -> winSynopsis    = 0;
	this -> lastInputTs    = 0;
	this -> lastOutputTs   = 0;
	this -> bStalled       = 0;
	this -> inStore        = 0;
	this -> numProcessed   = 0;
	this -> timeflag =0;
}

RowWindow::~RowWindow () {}

int RowWindow::setInputQueue (Queue *inputQueue) 
{
	ASSERT (inputQueue);
	
	this -> inputQueue = inputQueue;
	return 0;
}

int RowWindow::setOutputQueue (Queue *outputQueue)
{
	ASSERT (outputQueue);
	
	this -> outputQueue = outputQueue;
	return 0;
}

int RowWindow::setWindowSize (unsigned int windowSize)
{
	ASSERT (windowSize > 0);
	
	this -> windowSize = windowSize;
//	printf("In logopcc mke_row_window windowSize = %u \n"+ this -> windowSize); // Tested by Xing

	return 0;
}

//Added by Xing
int RowWindow::setWindowLevel (unsigned int level)
{
	this -> windowLevel = level;
	//std::cout <<  level << endl;
	//std::cout <<  windowLevel << endl;
	return 0;
}

int RowWindow::setWindowSynopsis (WindowSynopsis *winSynopsis) 
{
	ASSERT (winSynopsis);
	
	this -> winSynopsis = winSynopsis;
	return 0;
}

int RowWindow::setInStore (StorageAlloc *store)
{
	ASSERT (store);

	this -> inStore = store;
	return 0;
}

int RowWindow::run (TimeSlice timeSlice)
{
	int rc;
	unsigned int buffers; // added by Xing
	unsigned int oldTupleLevel; // added by Xing
	int flag; // added by Xing
	//struct timeval detail_time; // added by Xing

	// Xing: start timing
		struct timeval detail_time;
		ofstream myfile;
		time_t now;
		struct tm *current;
		int msec;


	unsigned int stubId;
	unsigned int numElements;

	Element inElement;
	Tuple oldestTuple;
	Timestamp oldestTupleTs;
	Element outElement;

#ifdef _MONITOR_
	startTimer ();
#endif							
	
	// We have a stall & aren't able to clear it
	if (bStalled && !clearStall()) {
#ifdef _MONITOR_
		stopTimer ();
		logOutTs (lastOutputTs);
#endif									
		return 0;
	}
  //  timeflag = 0; // added by Xing
	numElements = timeSlice;





	for (unsigned int e = 0 ; e < numElements ; e++) {



		flag = 0; // Xing: reset the flag
		// We skip processing if we find the output queue full
		if (outputQueue -> isFull())
			break;

		// Input queue empty?
		if (!inputQueue -> dequeue (inElement))
			break;
		
		// Xing: Start timing
				if(timeflag == 0){
									myfile.open ("stream-0.6.3/starttime.txt", ios::app|std::ios::out);
									now = time(0);
									current = localtime(&now);
									myfile << "id: " << id << endl;
									myfile << "hour: " << current->tm_hour << endl;
									myfile << "mins: " << current->tm_min << endl;
									myfile << "sec: " << current->tm_sec << endl;
									gettimeofday(&detail_time,NULL);
									msec = detail_time.tv_usec/1000;

								   myfile << "milli: " << msec << endl;
									timeflag = 1;
									myfile.flush();
									myfile.close();
								}


		lastInputTs = inElement.timestamp;

		// Ignore heartbeats
		if (inElement.kind == E_HEARTBEAT)
			continue;
		
		// Our input should be a stream, so can't have MINUSes		
		ASSERT (inElement.kind == E_PLUS);
		
		// Xing: We should add level filtering here
		 memcpy (&buffers, inElement.tuple, TIMESTAMP_SIZE);
		// printf ("tuple level: %d \n", buffers);

	 //    if (windowLevel == 24 and buffers !=2 and buffers != 4) continue;
		 switch(windowLevel){
		 case 1:
		 case 2:
		 case 3:
		 case 4:
			 if (windowLevel != buffers) {
				    	 if ((rc = winSynopsis -> deleteUnqualifiedTuple(inElement.tuple))!= 0)
				    	 			return rc;

				    	 flag = 1;
			 }
 	    	 break;
		 case 12:
		 	if (buffers != 1 and buffers != 2) {
		 				 if ((rc = winSynopsis -> deleteUnqualifiedTuple(inElement.tuple))!= 0)
		 			 	 			return rc;

		 			   	 flag = 1;
		 	 }
		  	 break;
		 case 13:
		 	if (buffers != 1 and buffers != 3) {
		 		 		 if ((rc = winSynopsis -> deleteUnqualifiedTuple(inElement.tuple))!= 0)
		 		 		 			return rc;

		 		 		 flag = 1;
		 	 }
		 	 break;
		 case 14:
				if (buffers != 1 and buffers != 4) {
				 		 		 if ((rc = winSynopsis -> deleteUnqualifiedTuple(inElement.tuple))!= 0)
				 		 		 			return rc;

				 		 		 flag = 1;
				 	 }
				 	 break;
		 case 23:
				 	if (buffers != 2 and buffers != 3) {
				 		 		 if ((rc = winSynopsis -> deleteUnqualifiedTuple(inElement.tuple))!= 0)
				 		 		 			return rc;

				 		 		 flag = 1;
				 	 }
				 	 break;

		 case 24:
				 	if (buffers != 2 and buffers != 4) {
				 		 		 if ((rc = winSynopsis -> deleteUnqualifiedTuple(inElement.tuple))!= 0)
				 		 		 			return rc;

				 		 		 flag = 1;
				 	 }
				 	 break;

		 case 34:
				 	if (buffers != 3 and buffers !=4) {
				 		 		 if ((rc = winSynopsis -> deleteUnqualifiedTuple(inElement.tuple))!= 0)
				 		 		 			return rc;

				 		 		 flag = 1;
				 	 }
				 	 break;

		 case 123:
				 	if (buffers == 4) {
				 		 		 if ((rc = winSynopsis -> deleteUnqualifiedTuple(inElement.tuple))!= 0)
				 		 		 			return rc;

				 		 		 flag = 1;
				 	 }
				 	 break;
		 case 124:
						 	if (buffers == 3) {
						 		 		 if ((rc = winSynopsis -> deleteUnqualifiedTuple(inElement.tuple))!= 0)
						 		 		 			return rc;

						 		 		 flag = 1;
						 	 }
						 	 break;
		 case 234:
						 	if (buffers == 1) {
						 		 		 if ((rc = winSynopsis -> deleteUnqualifiedTuple(inElement.tuple))!= 0)
						 		 		 			return rc;

						 		 		 flag = 1;
						 	 }
						 	 break;

		 default:
			 flag = 0;
			 break;
		 }

/*	     if (windowLevel != buffers) {
	    	 if ((rc = winSynopsis -> deleteUnqualifiedTuple(inElement.tuple))!= 0) {
	    	 			return rc;
	    	 		}
	    	 continue;
	     }
*/
		if (flag) continue; // unqualified input tuple


		
		// Insert the input tuple into the synopsis
		if ((rc = winSynopsis -> insertTuple (inElement.tuple,
											  inElement.timestamp)) != 0) {
			return rc;
		}
		LOCK_INPUT_TUPLE (inElement.tuple);
		
		// Xing: forward the same element onwards .. But don't forget Minus tuples later
		outputQueue -> enqueue (inElement);
		lastOutputTs = inElement.timestamp;
		
		// We expire the oldest element from the window if we have
		// processed more than windowSize elements
		ASSERT (numProcessed <= windowSize);
		if (numProcessed == windowSize) {
			
			// get the oldest tuple
			rc = winSynopsis -> getOldestTuple (oldestTuple,
												oldestTupleTs);			
			if (rc != 0) return rc;
			
			outElement.kind = E_MINUS;
			outElement.tuple = oldestTuple;
			outElement.timestamp = inElement.timestamp;
			
			// Xing: output level info for Minus tuple
			memcpy (&oldTupleLevel, oldestTuple, INT_SIZE);
			outElement.level = oldTupleLevel;

			// delete the oldest element
			rc = winSynopsis -> deleteOldestTuple ();
			if (rc != 0) return rc;
			
			if (!outputQueue -> enqueue (outElement)) {
				bStalled = true;
				stalledElement = outElement;
				break;
			}
			
			// Note: no need to update lastOutputTs
		}
		
		else {
			numProcessed ++;
		}
	}
	
	// Hearbeat generation
	if (!outputQueue -> isFull() && (lastOutputTs < lastInputTs)) {
		outputQueue -> enqueue (Element::Heartbeat (lastInputTs));
	}

#ifdef _MONITOR_
	stopTimer ();
	logOutTs (lastOutputTs);
#endif
	
	return 0;
}

bool RowWindow::clearStall () {
	ASSERT (bStalled);
	
	return (bStalled = !outputQueue -> enqueue (stalledElement));
}
