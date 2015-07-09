#ifndef _ROUND_ROBIN_
#define _ROUND_ROBIN_

#ifndef _SCHEDULER_
#include "execution/scheduler/scheduler.h"
#endif

namespace Execution {
	class RoundRobinScheduler : public Scheduler {
	private:
		static const unsigned int MAX_OPS = 100;

		/// Max op sets can be scheduled
		static const unsigned int MAXOPSET = 100;
		
		// Operators that we are scheduling
		//Operator *ops [MAX_OPS];
		
		// Number of operators that we have to schedule
		unsigned int numOps;
		
		unsigned int numOpSet;

		bool bStop;
		
		// Already running?
		bool brun;

		// added by Xing for testing
		struct OpSet {
					int queryLevel;
					Operator *ops[MAX_OPS];
					int serverID;
					} opset[MAXOPSET];

		int serverIdForDelete;

	public:
		RoundRobinScheduler ();
		virtual ~RoundRobinScheduler ();
		
		//Xing: findEmptyOpSet
	//	int getEmptyOpset();
	//	int getServerID(int setIndex);


		//Xing: Methods for adding and deleting ops
		int addQueryOpSet(OpSet opset);
		int deleteQueryOpSet(int serverID);

		// Inherited from Scheduler
		int addOperator (Operator *op, int serverId, int querylevel);
		int run (long long int numTimeUnits);
		int stop ();
		int resume ();

		//Xing:
		int requestRun(long long int numTimeUnits);

		// Xing
		int requestStop(int serverId);
		long unsigned int getDuration(long unsigned int mistart);
		long unsigned int getCurrentTime();
		int levelChange(long unsigned int duration);

	};
}

#endif
