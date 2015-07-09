#ifndef _PLAN_MGR_IMPL_
#include "metadata/plan_mgr_impl.h"
#endif

#ifndef _OUTPUT_
#include "execution/operators/output.h"
#endif
#include <stdio.h>
using namespace Metadata;

using Execution::Output;
using Interface::QueryOutput;

int PlanManagerImpl::inst_output (Physical::Operator *op)
{
	int rc;
	TupleLayout *tupleLayout;
	QueryOutput *qryOutput;
	Output *outputOp;
	
	ASSERT (op);
	ASSERT (op -> kind == PO_OUTPUT);
	

	qryOutput = queryOutputs [op -> u.OUTPUT.outputId];

	outputOp = new Output (op -> id, LOG);

	if ((rc = outputOp -> setQueryOutput (qryOutput)) != 0)
		return rc;

	tupleLayout = new TupleLayout (op);
	
//	printf("inst_output.cc: numAttrs: %d \n", op->numAttrs);

	for (unsigned int a = 0 ; a < op -> numAttrs ; a++) {
		if ((rc = outputOp -> addAttr (op -> attrTypes [a],
									   op -> attrLen [a],
									   tupleLayout -> getColumn (a))) !=
			0)
			return rc;
	}
	
	if ((rc = outputOp -> initialize ()) != 0)
		return rc;
	
	delete tupleLayout;
	
	op -> instOp = outputOp;
	
//	printf("inst_output.cc: I am getting out \n");

	return 0;
}
