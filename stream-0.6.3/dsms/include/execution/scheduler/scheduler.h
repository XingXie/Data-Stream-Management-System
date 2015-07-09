#ifndef _SCHEDULER_
#define _SCHEDULER_

#ifndef _OPERATOR_
#include "execution/operators/operator.h"
#endif

namespace Execution {
	class Scheduler {
	public:
		/**
		 * Add a new operator to schedule
		 */
		virtual int addOperator (Operator *op, int severId, int querylevel) = 0;
		
		/**
		 * Schedule the operators for a prescribed set of time units.
		 */		
		virtual int run(long long int numTimeUnits) = 0;
		
		virtual int stop () = 0;
		virtual int resume () = 0;

		//Xing:
		virtual int requestRun(long long int numTimeUnits) = 0;
		virtual int requestStop(int serverId) = 0;
		//virtual long unsigned int getDuration(long unsigned int mistart) = 0;
		//virtual int levelChange(long unsigned int duration) = 0;
	};
}

#endif
