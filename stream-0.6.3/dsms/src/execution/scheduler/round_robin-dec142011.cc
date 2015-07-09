/**
 * @file       round_robin.cc
 * @date       June 4, 2004
 * @brief      Implementation of the round robin scheduler
 */

/// debug
#include <iostream>
#include <fstream>
#include <time.h>
#include <sys/time.h>
#include "execution/operators/output.h"
using namespace std;

#ifndef _DEBUG_
#include "common/debug.h"
#endif

#ifndef _ROUND_ROBIN_
#include "execution/scheduler/round_robin.h"
#endif

#include "net_server/include/command_conn.h"

using namespace Execution;

//extern int CURRENTSERVERID;
// tested by Xing: original 100000
static const TimeSlice timeSlice = 100000;
// edited by Xing:global variables for secure RR,
// which is the threshold value for 1 second time slot
int timeslot = 950;
int currentLevel = 1;
bool Tflag = true;

RoundRobinScheduler::RoundRobinScheduler()
{
	this -> numOps = 0;	
	this ->numOpSet = 0;
	bStop = false;
	brun = false;
	for(int i=0; i< MAXOPSET;i++){
		opset[i].serverID = -1;
	}
	serverIdForDelete = -1;
}

RoundRobinScheduler::~RoundRobinScheduler() {
	//added by Xing
	//	printf("Trusted RR scheduler is destroyed \n");
}

/* original addOps
int RoundRobinScheduler::addOperator (Operator *op)
{
	if (numOps == MAX_OPS)
		return -1;	
	ops [numOps ++] = op;
	
	return 0;
}
*/
//edited by Xing
int RoundRobinScheduler::addOperator (Operator *op, int serverId, int querylevel)
{
//	cout<< "I am in rr.cc returning serverID" << serverId << endl;
//	cout<< "I am in rr.cc returning querylevel" <<querylevel << endl;
	bool bexisted = false;
	int availableSet = 101;
	int currentSet = -1;
	if (numOps == MAX_OPS*MAXOPSET)
		return -1;
	// detect if there is existed server instance
	for(int i=0; i< MAXOPSET; i++){
		if(opset[i].serverID == serverId){
			bexisted = true;
			currentSet = i;
			break;
		}
		if(opset[i].serverID == -1 and i< availableSet){
			availableSet = i;
		}
	}

	if(bexisted){
		for (int j=0; j< MAX_OPS; j++){
			if(!opset[currentSet].ops[j]){
				opset[currentSet].ops[j] = op;
			    numOps ++;
			    break;
			}
			if(j == MAX_OPS){
				cout << "The maximum op per server is over 100" << endl;
				return -1;
			}
		}
	}
	else{
		if(availableSet > 100){
			printf("No available op set! \n");
			return -1;
		}
		else{
		opset[availableSet].serverID = serverId;
		opset[availableSet].queryLevel = querylevel;
		opset[availableSet].ops[0] = op;
		numOps ++;
		numOpSet ++;
		}

	}
//	cout<< "I am in rr.cc just before return 0: numOps value: " << numOps << endl;
//	cout<< "I am in rr.cc just before return 0: numOpSet value: " << numOpSet << endl;
    this->resume();
	return 0;
}



int RoundRobinScheduler::run (long long int numTimeUnits)
{
	int rc;
	timeval tv;
	tm * start;
	long unsigned int mistart, duration;
	bool outloop = false;
	brun = true;


	// added by Xing:
//	printf("server %d RR is ready \n", CURRENTSERVERID);
	//LOG << "RR scheduler is running " << endl;

	// numtimeunits == 0 signal for scheduler to run forever (until stopped)
	///

	 //Normal method: no high and low level consideration
	 if (numTimeUnits == 0) {
		while (!bStop) {
			for (unsigned int o = 0 ; opset[o].serverID!=-1 and o < numOpSet ; o++) {
				// Add delete progress
				if(serverIdForDelete == opset[o].serverID){
				//	cout<< "FOUND server ID: " << opset[o].serverID << endl;
								for (unsigned int k = 0 ; opset[o].ops[k]; k++){
									opset[o].ops[k] = 0;
									numOps--;
								}
								opset[o].serverID = -1;
								serverIdForDelete = -1;
								numOpSet--;
					if(numOpSet == 0)
						this->stop();
				//	cout<< "after deletion: numOps value: " << numOps << endl;
				//		cout<< "after deletion: numOpSet value: " << numOpSet << endl;
				}

				for (unsigned int k = 0 ; opset[o].ops[k]; k++)
				if ((rc = opset[o].ops[k] -> run(timeSlice)) != 0) {
					return rc;
				}
			}
			//Xing: everytime a loop is done, return the server ID
			// printf("server %d RR finishes a loop \n", serverID);
		}
	}
	
	else {		
		for (long long int t = 0 ; (t < numTimeUnits) && !bStop ; t++) {
			for (unsigned int o = 0 ; opset[o].serverID!=-1 and o < numOpSet ; o++) {
				for (unsigned int k = 0 ; opset[o].ops[k] && k <= numOps; k++)
				if ((rc = opset[o].ops[k] -> run(timeSlice)) != 0) {
					return rc;
				}
			}
		}
	}




	// The basic algorithm 1: Every level has identical running time:
	/*
	if (numTimeUnits == 0) {
			while (!bStop) {
				if(Tflag){
					gettimeofday(&tv,0);
					start = localtime(&tv.tv_sec);
					// Note that we put a day as the maximum execution time: be careful of 23 and 0 hours.
					mistart = start->tm_hour*3600000 +
							start->tm_min* 60000 + start->tm_sec*1000 + tv.tv_usec/1000;
				}
		//	 printf ("current start milliseconds: %lu \n",  mistart);
			 Tflag = false;
				for (unsigned int o = 0; opset[o].serverID!=-1 and o < numOpSet ; o++) {
					// Add delete progress
					if(serverIdForDelete == opset[o].serverID){
						cout<< "FOUND server ID: " << opset[o].serverID << endl;
						// perform possible deletion
						for (unsigned int k = 0 ; opset[o].ops[k]; k++){
										opset[o].ops[k] = 0;
										numOps--;
									}
									opset[o].serverID = -1;
									serverIdForDelete = -1;
									numOpSet--;
									cout<< "after deletion: numOps value: " << numOps << endl;
									cout<< "after deletion: numOpSet value: " << numOpSet << endl;

				  if(numOpSet == 0){
							this->stop();
							break;
						}
					}
					outloop = false;
					// check the time slot for idle or process
					if(opset[o].queryLevel != currentLevel){
					//	printf("idle: current level %d \n", currentLevel);
						continue;
					}

				//	printf("execution: current level %d \n", currentLevel);

					for (unsigned int k = 0; opset[o].ops[k]; k++){
					if ((rc = opset[o].ops[k] -> run(timeSlice)) != 0) {
						return rc;
					}
					duration = getDuration(mistart);
					if(levelChange(duration)){
					// set the out flag
					outloop = true;
					break;
					}
					// when an op finishes the job, check for current time slot
					//duration = getDuration(mistart);
					//if(levelChange(duration)){
					//   break;
				//	}

					}

					if(outloop){
						// Since the op runs our of time slot, break it
						break;
					}

				}
				if(!outloop){
				duration = getDuration(mistart);
				levelChange(duration);
				}

			}
		}

		else {
			for (long long int t = 0 ; (t < numTimeUnits) && !bStop ; t++) {
				for (unsigned int o = 0 ; opset[o].serverID!=-1 and o < numOpSet ; o++) {
					for (unsigned int k = 0 ; opset[o].ops[k] && k <= numOps; k++)
					if ((rc = opset[o].ops[k] -> run(timeSlice)) != 0) {
						return rc;
					}
				}
			}
		}
	*/
	
	return 0;


}



int RoundRobinScheduler::stop ()
{   //Added by xing
	//extern struct timeval detail_time1; // added by Xing
/*
	extern	time_t now1;
	extern	struct tm *current1;
	extern	int msec1;
	ofstream myfile1;
	myfile1.open ("stream-0.6.2/endtime.txt"); // open the file
	myfile1 << "hour: " << current1->tm_hour << endl;
	myfile1 << "mins: " << current1->tm_min << endl;
	myfile1 << "sec: " << current1->tm_sec << endl;
	myfile1 << "milli: " << msec1 << endl;
	myfile1.flush();
	myfile1.close();
*/
	bStop = true;
	return 0;
}

int RoundRobinScheduler::resume ()
{
	bStop = false;
	return 0;
}

int RoundRobinScheduler::requestRun(long long int numTimeUnits){
	if(!brun){
		this->run(numTimeUnits);
	}
}

int RoundRobinScheduler::requestStop(int serverId){
	//int rc;
	//if((rc = this->stop())!=0)
	//	return rc;
	serverIdForDelete = serverId;

//	for(int o = 0; o< MAXOPSET; o++){
//	if(opset[o].serverID != -1 && opset)
//	}

/*	for (unsigned int o = 0 ; o < numOpSet ; o++) {
		if(opset[o].serverID == serverId){
			cout<< "FOUND server ID: " << opset[o].serverID << endl;
			for (unsigned int k = 0 ; opset[o].ops[k] && k <= numOps; k++){
				opset[o].ops[k] = 0;
				numOps--;
			}
			opset[o].serverID = -1;
			numOpSet--;
			break;

		}

	}
*/
//	if((rc = this->resume())!=0)
//			return rc;

	return 0;
}

// Get the duration time
long unsigned int RoundRobinScheduler::getDuration(long unsigned int mistart){
	timeval tv;
	tm * end;
	unsigned long int miend, duration;
	gettimeofday(&tv,0);
	end = localtime(&tv.tv_sec);
	miend =  end->tm_hour*3600000 +
	end->tm_min* 60000 + end->tm_sec*1000 + tv.tv_usec/1000;
	// printf ("The end %ld milliseconds.\n", miend);
	return miend - mistart;
}
// Measure level changes
int RoundRobinScheduler::levelChange(long unsigned int duration){
	if(duration>=timeslot){
	//	printf ("The difference %lu milliseconds.\n", duration);

		if((duration-timeslot) <= 50){
	//		printf("I am in the sleepy motion \n");
			usleep((duration-timeslot)*1000);
		}
		if(currentLevel == 4)
			currentLevel = 1;
		else
			currentLevel++;

	//	printf ("current level: %d.\n", currentLevel);
		Tflag = true;
		return 1;
	}
	else
		return 0;
}
