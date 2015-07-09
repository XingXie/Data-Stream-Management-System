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
// the default time slot for level 1 ops
int timeslot = 100;
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
		printf("Trusted RR scheduler is destroyed \n");
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

	 //-----------Normal method: no high and low level consideration

 if (numTimeUnits == 0) {
		while (!bStop) {
			for (unsigned int o = 0 ; opset[o].serverID!=-1 and o < numOpSet ; o++) {
				// Add delete progress
				if(serverIdForDelete == opset[o].serverID){
					cout<< "FOUND server ID: " << opset[o].serverID << endl;
								for (unsigned int k = 0 ; opset[o].ops[k]; k++){
									opset[o].ops[k] = 0;
									numOps--;
								}
								opset[o].serverID = -1;
								serverIdForDelete = -1;
								numOpSet--;
					if(numOpSet == 0)
						this->stop();
					cout<< "after deletion: numOps value: " << numOps << endl;
						cout<< "after deletion: numOpSet value: " << numOpSet << endl;
				}

				for (unsigned int k = 0 ; opset[o].ops[k]; k++){
				if ((rc = opset[o].ops[k] -> run(timeSlice)) != 0) {
					return rc;
				}
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


//-----------------normal method

/*
	// The basic algorithm 1: Every level has identical running time:

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
//------------------------------end of basic algorithm: Time-slot RR

/*
	// Time-slot RR with borrow mechanism
	// Xing: Flag indicates if all runable server instances has no input in current time slot
	bool inputExist = false;
	long unsigned int timeslotEnd;
	long unsigned int myTimeslot;
	bool recomputeTimeslotEnd = true;


	if (numTimeUnits == 0) {
				while (!bStop) {
					if(Tflag){
						gettimeofday(&tv,0);
						start = localtime(&tv.tv_sec);
						// Note that we put a day as the maximum execution time: be careful of 23 and 0 hours.
						mistart = start->tm_hour*3600000 +
								start->tm_min* 60000 + start->tm_sec*1000 + tv.tv_usec/1000;
						myTimeslot = mistart + 250;
						printf ("while: myTimeslot: %lu \n",  myTimeslot);
						if(recomputeTimeslotEnd){
						timeslotEnd = mistart + 1000;
						currentLevel = 1;
						recomputeTimeslotEnd = false;
						printf ("while: current time slot end milliseconds: %lu \n",  timeslotEnd);
						}
					}
			//	 printf ("current start milliseconds: %lu \n",  mistart);
				 Tflag = false;
				 inputExist = false;
				 printf ("entering the for running loop with currentLevel %d\n", currentLevel);
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
							printf("the time slot is not for current server \n");
							if(numOpSet == 1){
								myTimeslot = myTimeslot + 250;
								currentLevel ++;
								printf("numOpSet == 1 and myTimeslot is updated = %ld \n", myTimeslot);
								break;
							}

							else
								continue;
						}

						printf("execution: current level %d \n", currentLevel);

						for (unsigned int k = 0; opset[o].ops[k]; k++){
						if ((rc = opset[o].ops[k] -> run(timeSlice)) != 0) {
							// Xing: we might have special code 9
							if(rc == 9){
								printf("I got the rc code = 9 \n" );
								if(currentLevel < 4){
								inputExist = false;
								}
								break;
							}
							else
								return rc;
						}
						// There is some inputs in current level
						inputExist = true;
						Tflag = true;
						printf("under execution, current time = %ld \n", getCurrentTime());

						if(getCurrentTime() >= myTimeslot){
						// set the out flagmistart
							printf("level changes in loop1 \n");
						outloop = true;
						break;
						}

						}
						// All ops from one server is done, or because level is changed
						if(outloop){
							// Since the op runs our of time slot, break it
							break;
						}
						// No break, we keep the current level, try next server ops
						// If it runs to the end the time slot is not changed
						if(o == (numOpSet-1)){
							printf ("o== numOpSet-1 \n");
							// No input in current level
							if(!inputExist){
								if(currentLevel == 4){
									printf("currentLevel=4 and has no inputs \n");
									if(getCurrentTime()<=timeslotEnd){
										//idle for highest level
										usleep((timeslotEnd - getCurrentTime())*1000);
									}
									printf ("after idle, the current time is %ld milliseconds.\n", getCurrentTime());
									recomputeTimeslotEnd = true;
									Tflag = true;
								}
								else{
									myTimeslot = myTimeslot + 250;
									currentLevel ++;
								}
							}
							// There is some inputs but we have rest of time slot
							else{
								printf("I want to waste the time slot in level %d since I have inputs \n", currentLevel);
								if(currentLevel != 4){

								if(getCurrentTime()<= myTimeslot){
									// idle the rest time in current time slot
									usleep((myTimeslot - getCurrentTime())*1000);
								}
								printf ("currentLevel = %d, after waste, The current time %ld milliseconds.\n", currentLevel, getCurrentTime());
								}
								else{
									printf("currentLevel=4 and has some inputs! \n");
									printf("before idle, myTimeslot = %ld! \n", myTimeslot);
									printf("before idle, current timeslotEnd = %ld! \n", timeslotEnd);
									printf("before idle, current times = %ld! \n", getCurrentTime());
									if(getCurrentTime()<=timeslotEnd){
										//idle for highest level
										usleep((timeslotEnd - getCurrentTime())*1000);
									}
									printf("after idle, myTimeslot = %ld! \n", myTimeslot);
									printf("after idle, current timeslotEnd = %ld! \n", timeslotEnd);
									printf ("after idle, the current time is %ld milliseconds.\n", getCurrentTime());
									recomputeTimeslotEnd = true;
									Tflag = true;
								}
							}

						}


					} // end of running all server loop
					printf("end of run \n");
					// If it goes to the end
					if(!inputExist){
						if(currentLevel == 4){
							printf("outside run: currentLevel=4 and has no inputs \n");
							while(getCurrentTime()<=timeslotEnd){
								//idle for highest level
								;
							}
							printf ("after idle, the current time is %ld milliseconds.\n", getCurrentTime());
							printf("Tflag set to true \n");

							Tflag = true;
							recomputeTimeslotEnd = true;
						}
						else{
							currentLevel ++;
							printf("outside run: level++, the current level: %d \n", currentLevel);
						}
					}
					// There is some inputs but we have rest of time slot
					else{
						printf("outside run: the level is updated to %d and do nothing \n", currentLevel);
					}

				} // end of while
				printf("bstop is true \n");
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

// Get the duration time
long unsigned int RoundRobinScheduler::getCurrentTime(){
	timeval tv3;
	tm * end3;
	unsigned long int miend3;

	gettimeofday(&tv3,0);
	end3 = localtime(&tv3.tv_sec);
	miend3 =  end3->tm_hour*3600000 +
	end3->tm_min* 60000 + end3->tm_sec*1000 + tv3.tv_usec/1000;
//	printf ("The current time %ld milliseconds.\n", miend3);
	return miend3;
}
// Measure level changes
int RoundRobinScheduler::levelChange(long unsigned int duration){
	if(duration>=timeslot){

	//	if((duration-timeslot) <= 50){
	//		printf("I am in the sleepy motion \n");
	//		usleep((duration-timeslot)*1000);
	//	}
		switch(currentLevel){
		case 1:
			timeslot = 200;
			currentLevel++;
			break;
		case 2:
			timeslot = 300;
			currentLevel++;
			break;
		case 3:
			timeslot = 400;
			currentLevel++;
			break;
		case 4:
			timeslot = 100;
			currentLevel = 1;
			break;
		default:
			printf("the level is out from 1 to 4");

		}
	//	printf ("levelchange method: current level after level change: %d.\n", currentLevel);
		Tflag = true;
		return 1;
	}
	else
		return 0;
}
