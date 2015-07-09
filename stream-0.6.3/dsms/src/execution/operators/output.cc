#ifndef _OUTPUT_
#include "execution/operators/output.h"
#endif

#ifndef _DEBUG_
#include "common/debug.h"
#endif

/// debug
#include <iostream>
#include <sstream>
#include <stdlib.h>
#include <stdio.h>
//add by xing:
#include <cstring>
#include <time.h>

#include <fstream>
using namespace std;

using namespace Execution;



#define UNLOCK_INPUT_TUPLE(t) (inStore -> decrRef ((t)))

Output::Output(unsigned int _id, std::ostream& _LOG)
	: LOG (_LOG)
{
	id              = _id;
	inputQueue      = 0;
	inStore         = 0;
	numAttrs        = 0;
	output          = 0;
#ifdef _DM_
	lastInputTs     = 0;
#endif
}

Output::~Output() {
	ofstream myfile1;
	myfile1.open ("stream-0.6.3/endtime.txt",ios::app|std::ios::out); // open the file
	myfile1 << "id: " <<id<< endl;
	myfile1 << "hour: " << current1->tm_hour << endl;
	myfile1 << "mins: " << current1->tm_min << endl;
	myfile1 << "sec: " << current1->tm_sec << endl;
	myfile1 << "milli: " << msec1 << endl;
	myfile1.flush();
	myfile1.close();

}

int Output::setInputQueue (Queue *inputQueue)
{
	ASSERT (inputQueue);
	
	this -> inputQueue = inputQueue;
	return 0;
}

int Output::setInStore (StorageAlloc *store)
{
	ASSERT (store);

	this -> inStore = store;
	return 0;
}

int Output::setQueryOutput (Interface::QueryOutput *output)
{
	ASSERT (output);

	this -> output = output;
	return 0;
}

int Output::addAttr (Type type, unsigned int len, Column inCol)
{
	if (numAttrs == MAX_ATTRS)
		return -1;
	
	attrs [numAttrs].type = type;
	attrs [numAttrs].len = len;	
	inCols [numAttrs] = inCol;
	
	numAttrs ++;
	return 0;
}

int Output::initialize()
{
	int rc;
	ASSERT (output);
	ASSERT (numAttrs);

	// Xing: test
//	printf("I am in initialize of output.cc and numAttrs: %d \n", numAttrs);
	if ((rc = output -> setNumAttrs (numAttrs)) != 0)
		return rc;
//	printf("I am in initialize of output.cc and after setNumAttrs, rc=%d \n",rc);
	
	for (unsigned int a = 0 ; a < numAttrs ; a++) {
		if ((rc = output -> setAttrInfo (a, attrs[a].type, attrs[a].len))
			!= 0)
			return rc;
	}
//	printf("I am in initialize of output.cc and after setAttrInfo, rc=%d \n",rc);
	if ((rc = output -> start()) != 0)
		return rc;
	// compute the offsets of the attributes
	unsigned int offset = DATA_OFFSET;
	for (unsigned int a = 0 ; a < numAttrs ; a++) {
		offsets [a] = offset;

		switch (attrs [a].type) {
		case INT:   offset += INT_SIZE;      break;
		case FLOAT: offset += FLOAT_SIZE;    break;
		case BYTE:  offset += BYTE_SIZE;     break;
		case CHAR:  offset += attrs [a].len; break;
		// Xing LUB type
//		case LUB:   offset += LUB_SIZE;      break;
		default:
			return -1;
		}
	}	
	tupleLen = offset;
	//cout <<"I am in initialize of output.cc and going out of it"<< endl;
	return 0;
}

int Output::run (TimeSlice timeSlice)
{
	int rc;
	Element inputElement;
	Tuple inputTuple;
	unsigned int numElements;	
	char effect;
	// Xing
	char LUBlevel[4];

#ifdef _MONITOR_
	startTimer ();
#endif


	numElements = timeSlice;

	for (unsigned int e = 0 ; e < numElements ; e++) {

		// Get the next element
		if (!inputQueue -> dequeue (inputElement)){
			// Xing: write to flie
		//	myfile1.flush();
			break;
		}



		ASSERT (lastInputTs <= inputElement.timestamp);
		
#ifdef _DM_
		lastInputTs = inputElement.timestamp;
#endif
		
		// Ignore heartbeats
		if (inputElement.kind == E_HEARTBEAT)
			continue;
		
		inputTuple = inputElement.tuple;
		
		// Output timestamp
		memcpy(buffer, &inputElement.timestamp, TIMESTAMP_SIZE);
		// Xing test
	//	int tsbuffer;
	//	tsbuffer = snprintf (buffer, MAX_BUFFER_SIZE, "%d", inputElement.timestamp);
	//	printf("output.cc: inputelement with timestamp: %d \n", inputElement.timestamp);
	//	printf("output.cc: buffer with timestamp: %s \n", buffer);
				
		// Output effect: integer 1 for PLUS, integer 2 for MINUS
		effect = (inputElement.kind == E_PLUS)? '+' : '-';
		// 	Xing test
	//	int signbuffer;
	//	signbuffer = snprintf (buffer+tsbuffer, MAX_BUFFER_SIZE-1-tsbuffer, "%c", effect);
		memcpy(buffer + EFFECT_OFFSET, &effect, 1);
		
		// Xing test
	//	printf("output.cc: inputelement with sign: %c \n", effect);
	//	printf("output.cc: buffer with sign: %s \n", buffer);

		// Output the remaining attributes
		// Xing: Change the last out put
		printf("output.cc: numAttrs: %d \n", numAttrs);
		for (unsigned int a = 0 ; a < numAttrs ; a++) {
		//	cout<<"offsets[a]: "<< offsets[a]<<endl;
			switch (attrs[a].type) {
			case INT:
				// Xing: Handle level
				//LUB special print
				if(a== (numAttrs-1) ){

					snprintf (LUBlevel, sizeof(LUBlevel), "%d", inputElement.level);
					//xing test
			//		snprintf (buffer+offsets[a], MAX_BUFFER_SIZE-offsets[a], "%s", LUBlevel);
					memcpy (buffer + offsets[a],
											LUBlevel,
										INT_SIZE);
					//test
					printf("output.cc: INT attr (level): %s \n", buffer + offsets [a]);
				}
				else{
					//test
			//		int intbuffer;
			//		intbuffer = snprintf (buffer+offsets[a], MAX_BUFFER_SIZE-offsets[a], "%d", 99);
					memcpy (buffer + offsets[a],
						&(ICOL(inputTuple, inCols [a])),
						INT_SIZE);
				//	printf("output.cc: ICOL: %s \n", &(ICOL(inputTuple, inCols [a])));
				//	printf("output.cc: INT attr: %s \n", buffer + offsets [a]);
				}
				break;
				

			case FLOAT:
				memcpy (buffer + offsets[a],
						&(FCOL(inputTuple, inCols [a])),
						FLOAT_SIZE);
				break;
				
			case BYTE:
					buffer [offsets[a]] = BCOL(inputTuple,
										   inCols[a]);
				break;
				
			case CHAR:
			//	cout <<"I am in run of output.cc CHAR switch" << endl;
				strncpy (buffer + offsets [a],
						 (CCOL(inputTuple, inCols [a])),
						 attrs [a].len);
				printf("output.cc: Char attr: %s \n", CCOL(inputTuple, inCols [a]));
				break;
				
				// Added by Xing
	//		case LUB:

			//	LUBlevel << " " << inputElement.level << endl;


	//			LUBlevel[0] = '0'+ inputElement.level;
	//			LUBlevel[1] = '\0';


	//			strncpy (buffer + offsets [a],
	//									 LUBlevel,1);


			//	strcat (buffer+offsets[a],"2");
			//	cout<<"LUBlevelstring value: "<< LUBlevelstr<<endl;
			//	LUBlevelint = &inputElement.level;
			//	memcpy (buffer + offsets[a],LUBlevelint,sizeof(int));
			//	strncpy (buffer + offsets [a],
			//							LUBlevel,
			//							 1);

			//	LUBlevelstr.copy(buffer+offsets[a],4, 0);


			/*	strncpy (buffer + offsets [a],
						 (CCOL(LUBlevel, 0)),
										 1);
*/
			//	memcpy (buffer + offsets[a],(int *)LUBlevel,LUB_SIZE);
			//	printf("buffer + offset %s \n", buffer + offsets[a]);

		//		break;
				
			default:
				return -1;
			}
		}
		

		// Xing: Add LUB level


		UNLOCK_INPUT_TUPLE(inputTuple);
	//cout << "output.cc before the putNext, and tuplelen size:" <<  tupleLen << endl;

		// Output the tuple
		//xing: test
		printf("output.cc: buffer after constuction: %s \n", buffer);

		if ((rc = output -> putNext (buffer, tupleLen)) != 0)
			return rc;

		now1 = time(0);
		current1 = localtime(&now1);
		gettimeofday(&detail_time1,NULL);
		msec1 = detail_time1.tv_usec/1000;
		// Xing: Output test
		/*cout << "hour: " << current1->tm_hour << endl;
		cout << "mins: " << current1->tm_min << endl;
		cout << "sec: " << current1->tm_sec << endl;
		cout << "milli: " << msec1 << endl;
*/

	}
	// No intput tuples


#ifdef _MONITOR_
	stopTimer ();
#endif




	return 0;
}

