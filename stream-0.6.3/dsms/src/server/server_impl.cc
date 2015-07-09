/**
 * @file       server_impl.cc
 * @date       May 22, 2004
 * @brief      Implementation of the STREAM server.
 */

/// debug
#include <iostream>
#include <fstream>
// Add by Xing
#include <cstring>
#include <stdio.h>
#include <stdlib.h>

#include <time.h>

using namespace std;


#ifndef _ERROR_
#include "interface/error.h"
#endif

#ifndef _DEBUG_
#include "common/debug.h"
#endif

#ifndef _SERVER_IMPL_
#include "server/server_impl.h"
#endif

#ifndef _PARSER_
#include "parser/parser.h"
#endif

#ifndef _NODES_
#include "parser/nodes.h"
#endif

#ifndef _QUERY_
#include "querygen/query.h"
#endif

#ifndef _LOGOP_
#include "querygen/logop.h"
#endif

#ifndef _ROUND_ROBIN_
#include "execution/scheduler/round_robin.h"
#endif

#ifndef _CONFIG_FILE_READER_
#include "server/config_file_reader.h"
#endif

#ifndef _PARAMS_
#include "server/params.h"
#endif

#ifdef _DM_
#include "querygen/query_debug.h"
#include "parser/nodes_debug.h"
#include "querygen/logop_debug.h"
#endif

using namespace std;

// Helper functions ...
static int registerRelation (NODE *parseTree,
							 Metadata::TableManager *tableMgr,
							 unsigned int &relId);

static int registerStream (NODE *parseTree,
						   Metadata::TableManager *tableMgr,
						   unsigned int &strId);


ServerImpl::ServerImpl(ostream &_LOG)
	: LOG (_LOG)
{
	state             = S_INIT;
	tableMgr          = 0;
	qryMgr            = 0;
	planMgr           = 0;
	scheduler         = 0;
	serverId		  = -1;
	// Xing
	startflag		  = 0;
	// Xing: flags for sharing and storages
	completeShareFlag = false;
	subsumeShareFlag = false;
	
	// Xing: initialize the flags
	ssWinSpec        = false;
    ssSelect         = false;
    ssProject        = false;
    ssCode           = -1;
    logPlanBufferIndex = 0;

	myfile1.open ("stream-0.6.3/sharedifftime.txt",ios::app|std::ios::out); // open the file


	// Set default values of various server params
	MEMORY            = MEMORY_DEFAULT;
	QUEUE_SIZE        = QUEUE_SIZE_DEFAULT;
	SHARED_QUEUE_SIZE = SHARED_QUEUE_SIZE_DEFAULT;
	INDEX_THRESHOLD   = INDEX_THRESHOLD_DEFAULT;
	SCHEDULER_TIME    = SCHEDULER_TIME_DEFAULT;
	CPU_SPEED         = CPU_SPEED_DEFAULT;
	
	pthread_mutex_init (&mutex, NULL);
	pthread_cond_init (&mainThreadWait, NULL);
	pthread_cond_init (&interruptThreadWait, NULL);
}

ServerImpl::~ServerImpl()
{
	if (logPlanGen)
		delete logPlanGen;

	if (semInterpreter)
		delete semInterpreter;
	
	if (tableMgr)
		delete tableMgr;
	
	if (planMgr)
		delete planMgr;
	
	if (qryMgr)
		delete qryMgr;

//	if (scheduler)
	//	delete scheduler;
}


/**
 * Begin the specification of an application.
 *
 * Server should be in INIT state when this method id called.  It moves to
 * S_APP_SPEC state when it returns from this method.  
 *
 * Also server starts up:
 *
 * 1. Table manager to manage named streams and relations (input &
 *    derived)
 */

int ServerImpl::beginAppSpecification() 
{
	// State transition
	if(state != S_INIT)
		return INVALID_USE_ERR;
	state = S_APP_SPEC;
	
	// Table manager
	tableMgr = new Metadata::TableManager();
	if(!tableMgr)
		return INTERNAL_ERR;

	// Query Manager
	qryMgr = new Metadata::QueryManager(LOG);
	if (!qryMgr)
		return INTERNAL_ERR;
	
	// Plan manager
	planMgr = Metadata::PlanManager::newPlanManager(tableMgr, LOG);
	if(!planMgr)
		return INTERNAL_ERR;
	
	// Semantic Interpreter
	semInterpreter = new SemanticInterpreter(tableMgr);
	if(!semInterpreter)
		return INTERNAL_ERR;
	
	// Logical plan generator
	logPlanGen = new LogPlanGen (tableMgr);
	if (!logPlanGen)
		return INTERNAL_ERR;
	
	return 0;
}

/**
 * Register a table (stream or a relation):
 *
 * The information about a table is encoded as a string.  This string is
 * first parsed using the parser.
 */

int ServerImpl::registerBaseTable(const char *tableInfo,
								  unsigned int tableInfoLen,
								  Interface::TableSource *input)
{
	int rc;
	NODE  *parseTree;
	unsigned int tableId;

	// Tables can be registered only in S_APP_SPEC mode (before
	// endApplicationSpec() has been called)
	if (state != S_APP_SPEC)
		return INVALID_USE_ERR;	
	
	// Sanity check
	if (!tableInfo)
		return INVALID_PARAM_ERR;
	
	// Parse table info
	parseTree = Parser::parseCommand(tableInfo, tableInfoLen);
	if(!parseTree)
		return PARSE_ERR;
	
	// Store the parsed info with the table manager
	if(parseTree -> kind == N_REL_SPEC) {
		if ((rc = registerRelation (parseTree, tableMgr, tableId)) != 0)
			return rc;
	}
	else if(parseTree -> kind == N_STR_SPEC) {
		if ((rc = registerStream (parseTree, tableMgr, tableId)) != 0)
			return rc;
	}
	
	// Unknown command: parse error
	else {
		return PARSE_ERR;
	}
	
	// Inform the plan manager about the new table
	if ((rc = planMgr -> addBaseTable (tableId, input)) != 0)
		return rc;
	
	return 0;	
}

// Xing: save logical plan
void ServerImpl::saveLogPlan(Logical::Operator logPlan){
	logPlanBuffer[logPlanBufferIndex] = logPlan;
	logPlanBufferIndex ++;
}


/**
 *  [[ To be implemented ]] 
 */ 
int ServerImpl::registerQuery (const char *querySpec, 
							   unsigned int querySpecLen,
							   Interface::QueryOutput *output,
							   unsigned int &queryId)
{	
	NODE *parseTree;
	Semantic::Query semQuery;
	Logical::Operator *logPlan;
	int rc;
	

	int completeShareID, subsumeShareID;
	// Reset the flags
	completeShareFlag = false;
	subsumeShareFlag = false;
	ssWinSpec        = false;
    ssSelect         = false;
    ssProject        = false;
    ssCode			 = -1;

	// Reset the ids
	completeShareID = -1;
	subsumeShareID = -1;

	if(startflag == 0){
	now1 = time(0);
	current1 = localtime(&now1);
	gettimeofday(&detail_time1,NULL);
	msec1 = detail_time1.tv_usec/1000;
	myfile1 << "start time in sec: " << current1->tm_sec << endl;
	myfile1 << "start time in milsec: " << msec1 << endl;
	startflag = 1;
	}
	// This method can be called only in S_APP_SPEC state
	if (state != S_APP_SPEC)
		return INVALID_USE_ERR;
	
	// Query String -> Parse Tree
	parseTree = Parser::parseCommand(querySpec, querySpecLen);
	if (!parseTree) {
		return PARSE_ERR;
	}
	

	// Parse Tree -> Semantic Query
	if((rc = semInterpreter -> interpretQuery (parseTree, semQuery)) != 0) {
		return rc;
	}


	/// Debug
#ifdef _DM_
	//printQuery (cout, semQuery, tableMgr);
#endif

//	cout << "before register: the predicate in semQuery: " << semQuery.preds[0].right->u.sval << endl;
//	if (flag == 1)
//		cout << "before register: qryMgr->semQueryBuffer[0]: " << qryMgr->semQueryBuffer[0].preds[0].right->u.sval << endl;

	// At this point there are no errors in the query (syntactic or
	// semantic), so we register the query and get an internal id for it
	// Xing: query manager should store the semQuery for share checking
	if ((rc = qryMgr -> registerQuery (querySpec, querySpecLen, queryId, semQuery))
		!= 0) {
		return rc;
	}

//	cout << "after register: qryMgr->semQueryBuffer[0]: " << qryMgr->semQueryBuffer[0].preds[0].right->u.sval << endl;

	// Xing: with the id, we can check if there is previous queries can be complete
	// or partial sharing
	for(int a = 0; a < queryId;a++)
		if(sharingAnalysis(semQuery, qryMgr->semQueryBuffer[a])){
			cout << "the current queryId: " << queryId << " found sharable query in queryId: " << a << endl;
			if(completeShareFlag){
				completeShareID = a;
				cout << "--->It is complete sharing " << endl;
			}

			if(subsumeShareFlag){
				subsumeShareID = a;
	//			cout << "--->It is subsume sharing " << endl;
			}

			break;
		}

	// Xing: No need to create logPlan if completed shared, but for subsume sharing we still need to create unique logical ops
	// SemanticQuery -> logical plan
	if(!completeShareFlag){
		// No complete sharing, then try subsume sharing
		if(subsumeShareFlag){
			// Decide the ssCode
			if(ssWinSpec)
				ssCode = 1;
		    if(ssSelect)
		    	ssCode = 2;
		    if(ssProject)
		    	ssCode = 3;
	//		cout << "--->Subsume sharing: the subsumeShareID: " << subsumeShareID <<
	//				" and the ssCode: " << ssCode << endl;

			if ((rc = logPlanGen ->genSubsumeShareLogPlan (semQuery, logPlan, subsumeShareID, ssCode)) != 0) {
				return rc;
			}
			saveLogPlan(*logPlan);

		}
		// Not subsume sharing, we need to create the whole plan
		else{
		cout << "no sharing plans for current queryId: " << queryId << endl;
		if ((rc = logPlanGen -> genLogPlan (semQuery, logPlan)) != 0) {
			return rc;
		}
		saveLogPlan(*logPlan);
		}
	}
	
#ifdef _DM_
	//cout << endl << endl << logPlan << endl;
#endif
	
	// Xing: If shared, add the share plan
	// Logical Plan -> Physical Plan (stored with plan manager)
	if(completeShareFlag){
//		cout << "we share queryID " << completeShareID << "'s plan with current plan. " << endl;
		if((rc=planMgr ->addCompleteSharePlan (queryId,  completeShareID, output))!=0){
			return rc;
		}
	}

	if(!completeShareFlag and !subsumeShareFlag){
		cout << "no sharing and generate the physical plan " << endl;
	if ((rc = planMgr -> addLogicalQueryPlan (queryId, logPlan, output)) != 0){
		return rc;
	}
	}
	
	// Xing: The subsume physical plan generation
	if(subsumeShareFlag and !completeShareFlag){
	//	cout << "subsumeSharing plan generation" << endl;

		if((rc=planMgr ->addSubsumeShareLogicalQueryPlan (queryId, logPlan, output, subsumeShareID, ssCode))!=0){
			return rc;
		}
	}

#ifdef _DM_
	//cout << endl << endl;
	//planMgr -> printPlan();
#endif
	
	// Xing: sharing timing
	now2 = time(0);
	current2 = localtime(&now2);
	gettimeofday(&detail_time2,NULL);
	msec2 = detail_time2.tv_usec/1000;



	return 0;
}

#ifdef _SYS_STR_
	
/**
 * Register a monitor query with the server.
 */

int ServerImpl::registerMonitor (const char *monitorSpec,
								 unsigned int monitorSpecLen,
								 Interface::QueryOutput *output,
								 unsigned int &monitorId)
{
	NODE *parseTree;
	Semantic::Query semQuery;
	Logical::Operator *logPlan;
	int rc;

	ASSERT (output);

	LOG << "Server: trying to interrupt exec ...";
	
	// Interrupt the server execution to register the monitor
	interruptExecution ();

	LOG << "done" << endl;
	
	// If we are not in the interrupted state, it means that
	// the server was not executing when we tried to interrupt
	if (state != S_INT) {
		LOG << "Register monitor when server not executing" << endl;		
		return 0;
	}

	LOG << "parsing..." << endl;
	
	// Query String -> Parse Tree
	parseTree = Parser::parseCommand(monitorSpec, monitorSpecLen);
	if (!parseTree) {
		resumeExecution ();
		return PARSE_ERR;
	}

	LOG << "Seminterp..." << endl;
	
	// Parse Tree -> Semantic Query
	if((rc = semInterpreter -> interpretQuery (parseTree, semQuery)) != 0) {
		resumeExecution ();
		return rc;
	}	

	LOG << "log plan..." << endl;
	
	// SemanticQuery -> logical plan
	if ((rc = logPlanGen -> genLogPlan (semQuery, logPlan)) != 0) {
		resumeExecution ();
		return rc;
	}

	LOG << "register query" << endl;
	
	// At this point there are no errors in the monitor query (syntactic or
	// semantic), so we register the query and get an internal id for it
	// Xing: Sharing analysis with semQuery save
	if ((rc = qryMgr -> registerQuery (monitorSpec, monitorSpecLen,
									   monitorId, semQuery)) != 0) {
		resumeExecution ();
		return rc;
	}

	LOG << "add monitor" <<endl;
	
	if ((rc = planMgr -> addMonitorPlan (monitorId, logPlan,
										 output, scheduler)) != 0)
		return rc;

	LOG << "resume exec..." << endl;
	
	resumeExecution ();

	LOG << "done" << endl;
	
	return 0;
}

#endif


int ServerImpl::getQuerySchema (unsigned int queryId,
								char *schemaBuf,
								unsigned int schemaBufLen) {

	int rc;

#ifndef _SYS_STR_
	if (state != S_APP_SPEC)
		return INVALID_USE_ERR;
#endif
	
	// Xing: We plus the bLUB info, and reset the flag
	rc = planMgr -> getQuerySchema (queryId, schemaBuf, schemaBufLen);
	//bLUB = false;
	return rc;
}

int ServerImpl::registerView(unsigned int queryId,
							 const char *tableInfo,
							 unsigned int tableInfoLen)
{
	int rc;
	NODE  *parseTree;
	unsigned int tableId;	
	
	// This method can be called only in S_APP_SPEC state
	if (state != S_APP_SPEC)
		return INVALID_USE_ERR;

	// Sanity check
	if (!tableInfo)
		return INVALID_PARAM_ERR;

	// Parse table info
	parseTree = Parser::parseCommand(tableInfo, tableInfoLen);
	if(!parseTree)
		return PARSE_ERR;
	
	// Store the parsed info with the table manager
	if(parseTree -> kind == N_REL_SPEC) {
		if ((rc = registerRelation (parseTree, tableMgr, tableId)) != 0)
			return rc;
	}
	
	else if(parseTree -> kind == N_STR_SPEC) {
		if ((rc = registerStream (parseTree, tableMgr, tableId)) != 0)
			return rc;
	}
	
	// Unknown command: parse error
	else {
		return PARSE_ERR;
	}
	
	// Indicate the mapping from query -> tableId to the plan manager
	if ((rc = planMgr -> map (queryId, tableId)) != 0)
		return rc;
	
	return 0;
}

/**
 * [[ to be implemented ]]
 */ 
int ServerImpl::endAppSpecification(int serverId, Execution::Scheduler *trustedRR, int querylevel)
{
	int rc;

	// Xing: write diff time to file
	myfile1 << "end time in sec: " << current2->tm_sec << endl;
	myfile1 << "end time in milsec: " << msec2 << endl;
//	myfile1 << "milli: " << msec2 - msec1 << endl;
	myfile1.flush();
	myfile1.close();

	if (state != S_APP_SPEC)
		return INVALID_USE_ERR;
	
//	cout<< "server_impl.cc: enter optimize plan"  << endl;

	if ((rc = planMgr -> optimize_plan ()) != 0)
		return rc;

//	cout<< "server_impl.cc: enter add_aux structures"  << endl;
	if ((rc = planMgr -> add_aux_structures ()) != 0)
		return rc;

//	cout<< "server_impl.cc: enter instantiate"  << endl;

	if ((rc = planMgr -> instantiate ()) != 0)
		return rc;
	//Xing: pointer to trusted scheduler
	scheduler = trustedRR;
	// Xing: Update serverID
	this->serverId = serverId;

//	cout<< "server_impl.cc: I am just before plan-initScheduler"  << endl;
	if ((rc = planMgr -> initScheduler (scheduler, serverId, querylevel)) != 0){

		return rc;
	}
	state = S_PLAN_GEN;
	
	return 0;
}

/**
 * Get the plan for execution.  Should be called after endAppSpec()
 * method is called
 */
int ServerImpl::getXMLPlan (char *planBuf, unsigned int planBufLen)
{
#ifdef _SYS_STR_
	if (state != S_PLAN_GEN && state != S_EXEC)
		return INVALID_USE_ERR;
#else
	if (state != S_PLAN_GEN)
		return INVALID_USE_ERR;
#endif
	
	return planMgr -> getXMLPlan (planBuf, planBufLen);
}

/**
 * [[ To be implemented ]]
 */
int ServerImpl::beginExecution()
{
	int rc;

	if (state != S_PLAN_GEN)
		return INVALID_USE_ERR;
		
	// Transition from S_PLAN_GEN to S_EXEC (Critical code)
	pthread_mutex_lock (&mutex);

	// normal sequence
	if (state == S_PLAN_GEN) {
		state = S_EXEC;
	}

	// the execution has been interrupted already 
	else if (state == S_INT) {
		// wake up the interrupting thread
		pthread_cond_signal (&interruptThreadWait);
		
		// sleep until the interruption is over
		pthread_cond_wait (&mainThreadWait, &mutex);
		
		// Resume ..
		state = S_EXEC;
	}
	
	else if (state == S_END) {
		// do nothing
	}
	
	pthread_mutex_unlock (&mutex);	
	
	while (state == S_EXEC) {
		/* Xing: old implementation. We need to make it run in the same scheduler
		 * if ((rc = scheduler -> run (SCHEDULER_TIME)) != 0)
			return rc;
		 */

		if ((rc = scheduler->requestRun(SCHEDULER_TIME)) != 0)
					return rc;
		
		ASSERT (state == S_EXEC || state == S_INT || state == S_END);
		pthread_mutex_lock (&mutex);
		
		// natural termination
		if (state == S_EXEC) {
			state = S_END;
		}
		
		// interrupted by interruptExecution()
		else if (state == S_INT) {			
			// wake up the interrupting thread
			pthread_cond_signal (&interruptThreadWait);
			
			// sleep until the interruption is over
			pthread_cond_wait (&mainThreadWait, &mutex);
			
			// Resume ..
			state = S_EXEC;
		}
		
		// terminated by stopExecution()
		else if (state == S_END) {
			// do nothing
		}
		
		pthread_mutex_unlock (&mutex);
	}
	
	ASSERT (state == S_END);
	
	if ((rc = planMgr -> printStat()) != 0)
		return rc;   
	
	return 0;
}

int ServerImpl::stopExecution ()
{
	
	ASSERT (state == S_PLAN_GEN || state == S_EXEC || state == S_END);
	pthread_mutex_lock (&mutex);
	// Xing: Send request for stop. It will delete all related info like ops, level, serverID etc.
	if (state == S_EXEC) {	
		scheduler -> requestStop (serverId);
	}
	state = S_END;
	pthread_mutex_unlock (&mutex);
	
	return 0;
}

int ServerImpl::interruptExecution ()
{
	ASSERT (state == S_PLAN_GEN || state == S_EXEC || state == S_END);
	
	pthread_mutex_lock (&mutex);
	
	if (state == S_EXEC) {		
		scheduler -> requestStop (serverId);
		state = S_INT;
		stateWhenInterrupted = S_EXEC;
		
		// wait for the main scheduler thread to give you control
		pthread_cond_wait (&interruptThreadWait, &mutex);
	}
	
	else if (state == S_PLAN_GEN) {
		stateWhenInterrupted = S_PLAN_GEN;
		state = S_INT;
	}
	
	pthread_mutex_unlock (&mutex);
	return 0;
}

int ServerImpl::resumeExecution ()
{
	int rc;
	
	ASSERT (state == S_INT);
	
	pthread_mutex_lock (&mutex);
	
	// This does not actually run the scheduler, but just resets its state
	// so that the next run() call runs without returning immediately.
	if ((rc = scheduler -> resume ()) != 0)
		return rc;
	state = stateWhenInterrupted;

	ASSERT (state == S_EXEC || state == S_PLAN_GEN);
	
	pthread_cond_signal (&mainThreadWait);
	pthread_mutex_unlock (&mutex);
	
	return 0;
}

static int registerRelation (NODE *parseTree,
							 Metadata::TableManager *tableMgr,
							 unsigned int &relId)
{	
	int rc;
	
	const char   *relName;
	NODE         *attrList;	
	NODE         *attrSpec;
 	const char   *attrName;
	Type          attrType;
	unsigned int  attrLen;
	
	// Relation name
	relName = parseTree -> u.REL_SPEC.rel_name;
	ASSERT(relName);	
	
	if((rc = tableMgr -> registerRelation(relName, relId)) != 0)
		return rc;
	
	attrList = parseTree -> u.REL_SPEC.attr_list;
	ASSERT(attrList);
	
	// Process each attribute in the list
	while (attrList) {		
		ASSERT(attrList -> kind == N_LIST);		
		
		// Attribute specification
		attrSpec = attrList -> u.LIST.curr;
		
		ASSERT(attrSpec);
		ASSERT(attrSpec -> kind == N_ATTR_SPEC);
		
		attrName = attrSpec -> u.ATTR_SPEC.attr_name;
		attrType = attrSpec -> u.ATTR_SPEC.type;

		switch (attrType) {
		case INT:
			attrLen = INT_SIZE; break;			
		case FLOAT:
			attrLen = FLOAT_SIZE; break;
		case CHAR:
			attrLen = attrSpec -> u.ATTR_SPEC.len; break;			
		case BYTE:
			attrLen = BYTE_SIZE; break;
		default:
			ASSERT (0);
			break;
		}
		
		if((rc = tableMgr -> addTableAttribute(relId, attrName, 
											   attrType, attrLen)) != 0) {
			tableMgr -> unregisterTable (relId);
			return rc;
		}
		
		// next attribute
		attrList = attrList -> u.LIST.next;
	}
	
	return 0;
}

static int registerStream (NODE *parseTree,
						   Metadata::TableManager *tableMgr,
						   unsigned int &strId)
{
	int rc;
	
	const char   *strName;
	NODE         *attrList;	
	NODE         *attrSpec;
 	const char   *attrName;
	Type          attrType;
	unsigned int  attrLen;

	// Stream name
	strName = parseTree -> u.STR_SPEC.str_name;
	ASSERT(strName);
	
	if((rc = tableMgr -> registerStream(strName, strId)) != 0)
		return rc;
	
	attrList = parseTree -> u.STR_SPEC.attr_list;
	ASSERT(attrList);
	
	// Process each attribute in the list
	while (attrList) {
		
		ASSERT(attrList -> kind == N_LIST);
		
		// Attribute specification
		attrSpec = attrList -> u.LIST.curr;
		
		ASSERT(attrSpec);
		ASSERT(attrSpec -> kind == N_ATTR_SPEC);
		
		attrName = attrSpec -> u.ATTR_SPEC.attr_name;
		attrType = attrSpec -> u.ATTR_SPEC.type;
		
		switch (attrType) {
		case INT:
			attrLen = INT_SIZE; break;			
		case FLOAT:
			attrLen = FLOAT_SIZE; break;
		case CHAR:
			attrLen = attrSpec -> u.ATTR_SPEC.len; break;			
		case BYTE:
			attrLen = BYTE_SIZE; break;
		default:
			ASSERT (0);
			break;
		}
		
		if((rc = tableMgr -> addTableAttribute(strId, attrName, 
											   attrType, attrLen)) != 0) {
			tableMgr -> unregisterTable (strId);
			return rc;
		}
		
		// next attribute
		attrList = attrList -> u.LIST.next;
	}
	
	return 0;
}

Server *Server::newServer(std::ostream& LOG) 
{
	return new ServerImpl(LOG);
}

int ServerImpl::setConfigFile (const char *configFile)
{
	int rc;
	ConfigFileReader *configFileReader;
	ConfigFileReader::Param param;
	ConfigFileReader::ParamVal val;
	bool bValid;
	
	configFileReader = new ConfigFileReader (LOG);
	
	if ((rc = configFileReader -> setConfigFile (configFile)) != 0)
		return rc;
	
	while (true) {
		
		if ((rc = configFileReader -> getNextParam (param, val, bValid))
			!= 0)
			return rc;
		
		if (!bValid)
			break;
		
		switch (param) {
		case ConfigFileReader::MEMORY_SIZE:
			MEMORY = (unsigned int)val.ival;
			break;
			
		case ConfigFileReader::QUEUE_SIZE:
			QUEUE_SIZE = (unsigned int)val.ival;
			break;
			
		case ConfigFileReader::SHARED_QUEUE_SIZE:
			SHARED_QUEUE_SIZE = (unsigned int)val.ival;
			break;
			
		case ConfigFileReader::INDEX_THRESHOLD:
			INDEX_THRESHOLD = val.dval;
			break;
			
		case ConfigFileReader::RUN_TIME:
			SCHEDULER_TIME = (unsigned int)val.lval;
			break;

		case ConfigFileReader::CPU_SPEED:
			CPU_SPEED = (unsigned int)val.lval;
			break;
			
		default:
			break;
		}
	}

	delete configFileReader;
	
	return 0;
}

// Xing: sharing analysis
bool ServerImpl::sharingAnalysis(Semantic::Query semQuery1, Semantic::Query semQuery2){
	// sharing Checking flags, two predicate flags, which is reused in subsume sharing checking
	bool cflag, sflag, cpredflagA, cpredflagB;
	// This boolean tells if we need to do more subsume sharing analysis
	bool ssMore;

	// For subsume index project use
	int ssIndex;


	// Query type
	if(semQuery1.kind != semQuery2.kind){
		cout<< "sharing analysis: kind different"<< endl;
		return false;
	}

	// Keyword distinct
	if(semQuery1.bDistinct != semQuery2.bDistinct){
		cout<< "sharing analysis: distinct different"<< endl;
		return false;
	}


	// Check from clause
	if(semQuery1.numRefTables != semQuery2.numRefTables){
			cout<< "sharing: analysis: number of tables are different"<< endl;
			return false;
		}

	cflag = true;

	for(int i=0; i<=semQuery1.numRefTables-1;i++){
		cflag = false;
		for (int j=0;j <= semQuery2.numRefTables-1;j++){
			if(semQuery1.refTables[i] == semQuery2.refTables[j]){
				cflag = true;
				break;
			}
		}
		if(!cflag){
			cout<< "sharing: analysis: no matching table id: "<< semQuery1.refTables[i] <<endl;
			return false;
		}
	}

	// Check window spec
	for(int i=0; i<=semQuery1.numRefTables-1;i++){
		cflag = false;
		if(semQuery1.winSpec[i].type == semQuery2.winSpec[i].type){
			switch(semQuery1.winSpec[i].type){
			case RANGE:
				if(semQuery1.winSpec[i].u.RANGE.timeUnits == semQuery2.winSpec[i].u.RANGE.timeUnits){
					cflag = true;
				}
				break;
			case ROW:
				if(semQuery1.winSpec[i].u.ROW.numRows == semQuery2.winSpec[i].u.ROW.numRows and
						semQuery1.winSpec[i].u.ROW.level == semQuery2.winSpec[i].u.ROW.level){
					cflag = true;
				}
				break;
			case PARTITION:
				if(semQuery1.winSpec[i].u.PARTITION.numPartnAttrs == semQuery2.winSpec[i].u.PARTITION.numPartnAttrs and
						semQuery1.winSpec[i].u.PARTITION.numRows == semQuery2.winSpec[i].u.PARTITION.numRows){
					for(int j=0;j<=semQuery1.winSpec[i].u.PARTITION.numPartnAttrs-1;j++){
						cflag = false;
						if(semQuery1.winSpec[i].u.PARTITION.attrs[j].attrId != semQuery2.winSpec[i].u.PARTITION.attrs[j].attrId or
								semQuery1.winSpec[i].u.PARTITION.attrs[j].varId != semQuery2.winSpec[i].u.PARTITION.attrs[j].varId){
							break;
						}
						cflag = true;
					}

				}
				break;
			case NOW:
			case UNBOUNDED:
				cflag = true;
				break;
			default: break;

			}
		}

		if(!cflag){
			cout<< "sharing analysis: no matching window spec id: "<< i <<endl;
			return false;
		}

	}

	// Winspecs are identical
	ssWinSpec = true;
	ssCode = 1;
	subsumeShareFlag = true;
	ssMore = true;


	// -----------------------------------------Check where clause
	cflag = true;
	if(semQuery1.numPreds != semQuery2.numPreds){
				cout<< "sharing: analysis: complete sharing: number of predicates are different"<< endl;
				cflag = false;
			}

	cpredflagA = true;
	cpredflagB = true;
	// Complete sharing checking
	if(cflag and semQuery1.numPreds>=1){

	// Check the predicates like A op B
	for(int i=0; i<=semQuery1.numPreds-1;i++){
		cflag = false;

		for (int j=0;j<=semQuery2.numPreds-1;j++){

			cpredflagA = false;
			cpredflagB = false;
			// First we check the op
			if(semQuery1.preds[i].op  == semQuery2.preds[j].op){
			//	cout<< "sharing analysis: semQuery1.preds[i].op  == semQuery2.preds[j].op " << endl;
			//	cout<< "sharing analysis: semQuery1.preds[i].left->type =  "<< semQuery1.preds[i].left->type <<
			//								" and semQuery2.preds[j].left->type = " << semQuery2.preds[j].left->type << endl;
				// Second we check the left element A
				if(semQuery1.preds[i].left->type == semQuery2.preds[j].left->type){
			//		cout<< "sharing analysis: semQuery1.preds[i].left->type == semQuery2.preds[j].left->type " << endl;
					if(semQuery1.preds[i].left->exprType == semQuery2.preds[j].left->exprType){
				//		cout<< "semQuery1.preds[i].left->exprType == semQuery2.preds[j].left->exprType " << endl;
						// We go into the expression in different exprTypes:
						switch (semQuery1.preds[i].left->exprType){
						// Attribute reference
						case Semantic::E_ATTR_REF:
//							cout<< "sharing: analysis: pred left Semantic::E_ATTR_REF: " << endl;
//							cout<< "semQuery1.preds[i].left->u.attr.attrId: " << semQuery1.preds[i].left->u.attr.attrId << endl;
//							cout<< "semQuery2.preds[j].left->u.attr.attrId: " << semQuery2.preds[j].left->u.attr.attrId << endl;
//							cout<< "semQuery1.preds[i].left->u.attr.varId: " << semQuery1.preds[i].left->u.attr.varId << endl;
//							cout<< "semQuery2.preds[j].left->u.attr.varId: " << semQuery2.preds[j].left->u.attr.varId << endl;

							// Xing: CUrrently we don'r care the order of tables
							if(semQuery1.preds[i].left->u.attr.attrId ==
									semQuery2.preds[j].left->u.attr.attrId){
//								cout<< "sharing: analysis: pred left Semantic::E_ATTR_REF: attrId is equal" << endl;
//								if(semQuery1.preds[i].left->u.attr.varId ==
//										semQuery2.preds[j].left->u.attr.varId){
//									cout<< "sharing: analysis: pred left Semantic::E_ATTR_REF: varId is equal" << endl;
																cpredflagA = true;
//								}
							}



							break;

						// Constant Value, we don't need checking since it is already decided by type and exprType
						case Semantic::E_CONST_VAL:
						//	cout<< "sharing: analysis: pred left Semantic::E_CONST_VAL: " << endl;
							switch(semQuery1.preds[i].left->type){
							case INT:
								if(semQuery1.preds[i].left->u.ival == semQuery2.preds[j].left->u.ival){
									cpredflagA = true;
								}

								break;

							case FLOAT:
								if(semQuery1.preds[i].left->u.fval != semQuery2.preds[j].left->u.fval){
									cpredflagA = true;
								}

								break;

							case CHAR:
								if(strcmp(semQuery1.preds[i].left->u.sval, semQuery2.preds[j].left->u.sval)== 0){
									cpredflagA = true;
								}

								break;

							case BYTE:
								if(semQuery1.preds[i].left->u.bval == semQuery2.preds[j].left->u.bval){
									cpredflagA = true;
								}

								break;

							default: break;

							}

							break;

						// The aggregation: A complicated case AVG(Table1.weight) == AVG(Table2.weight) with Table1 = Table2
						case Semantic::E_AGGR_EXPR:
							if(semQuery1.preds[i].left->u.AGGR_EXPR.attr.attrId ==
									semQuery2.preds[j].left->u.AGGR_EXPR.attr.attrId){
								if(semQuery1.preds[i].left->u.AGGR_EXPR.attr.varId ==
									semQuery2.preds[j].left->u.AGGR_EXPR.attr.varId){
									if(semQuery1.preds[i].left->u.AGGR_EXPR.fn ==
										semQuery2.preds[j].left->u.AGGR_EXPR.fn)
									cpredflagA = true;
								}

							}
							break;
						// For now we didn't consider complex expression
						case Semantic::E_COMP_EXPR:
							if(semQuery1.preds[i].left->u.COMP_EXPR.op ==
									semQuery2.preds[j].left->u.COMP_EXPR.op){
								cpredflagA = true;
							}
							break;

						default: break;

						}
					}
				}

				// Third we check the right element B
//				cout<< "semQuery1.preds[i].right->type: " <<  semQuery1.preds[i].right->type << endl;
//				cout<< "semQuery2.preds[j].right->type: " <<  semQuery2.preds[j].right->type << endl;
				if(semQuery1.preds[i].right->type == semQuery2.preds[j].right->type){
				//	cout<< "semQuery1.preds[i].right->type == semQuery2.preds[j].right->type " << endl;
					if(semQuery1.preds[i].right->exprType == semQuery2.preds[j].right->exprType){
					//	cout<< "semQuery1.preds[i].right->exprType == semQuery2.preds[j].right->exprType " << endl;
						// We go into the expression in different exprTypes:
						switch (semQuery1.preds[i].right->exprType){
						// Attribute reference
						case Semantic::E_ATTR_REF:
						//	cout<< "sharing: analysis: pred right Semantic::E_ATTR_REF: " << endl;
							if(semQuery1.preds[i].right->u.attr.attrId ==
									semQuery2.preds[j].right->u.attr.attrId){
								if(semQuery1.preds[i].right->u.attr.varId ==
										semQuery2.preds[j].right->u.attr.varId)
								cpredflagB = true;
							}
							break;

						// Constant Value, we don't need checking since it is already decided by type and exprType
						case Semantic::E_CONST_VAL:
						//	cout<< "sharing: analysis: pred right Semantic::E_CONST_VAL: " << endl;
							switch(semQuery1.preds[i].right->type){
							case INT:
						//		cout<< "sharing: analysis: pred right Semantic::E_CONST_VAL: in INT " << endl;
								if(semQuery1.preds[i].right->u.ival == semQuery2.preds[j].right->u.ival){
							//		cout<< "semQuery1.preds[i].right->u.ival: " << semQuery1.preds[i].right->u.ival << endl;
							//		cout<< "semQuery2.preds[j].right->u.ival: " << semQuery2.preds[j].right->u.ival << endl;
									cpredflagB = true;
								}
								//Xing: for test
								else{
									cout<< "sharing: analysis: pred right Semantic::E_CONST_VAL: int values no matching " << endl;
									cout<< "semQuery1.preds[i].right->u.ival: " << semQuery1.preds[i].right->u.ival << endl;
									cout<< "semQuery2.preds[j].right->u.ival: " << semQuery2.preds[j].right->u.ival << endl;
								}

								break;

							case FLOAT:
							//	cout<< "sharing: analysis: pred right Semantic::E_CONST_VAL: in FLOAT " << endl;
								if(semQuery1.preds[i].right->u.fval == semQuery2.preds[j].right->u.fval){
									cpredflagB = true;
								}

								break;

							case CHAR:
							//	cout<< "sharing analysis: pred right Semantic::E_CONST_VAL: in char " << endl;
							//	cout << "the semQuery1.preds[i].right->u.sval: " << semQuery1.preds[i].right->u.sval << endl;
							//	cout << "the semQuery2.preds[j].right->u.sval: " << semQuery2.preds[j].right->u.sval << endl;

								if(	strcmp(semQuery1.preds[i].right->u.sval, semQuery2.preds[j].right->u.sval)== 0){
									cpredflagB = true;
								}

				//				 Xing: For testing
								else{
									cout<< "char no matchingin  pred right Semantic::E_CONST_VAL: in char " << endl;
									cout<< "semQuery1.preds[i].right->u.sval: " << semQuery1.preds[i].right->u.sval << endl;
									cout<< "semQuery2.preds[j].right->u.sval: " << semQuery2.preds[j].right->u.sval << endl;

								}

								break;

							case BYTE:
								if(semQuery1.preds[i].right->u.bval == semQuery2.preds[j].right->u.bval){
									cpredflagB = true;
								}

								break;

							default: break;

							}

							break;

						// The aggregation: A complicated case AVG(Table1.weight) == AVG(Table2.weight) with Table1 = Table2
						case Semantic::E_AGGR_EXPR:
							if(semQuery1.preds[i].right->u.AGGR_EXPR.attr.attrId ==
									semQuery2.preds[j].right->u.AGGR_EXPR.attr.attrId){
								if(semQuery1.preds[i].right->u.AGGR_EXPR.attr.varId ==
									semQuery2.preds[j].right->u.AGGR_EXPR.attr.varId){
									if(semQuery1.preds[i].right->u.AGGR_EXPR.fn ==
										semQuery2.preds[j].right->u.AGGR_EXPR.fn)
									cpredflagB = true;
								}

							}
							break;
						// For now we didn't consider complex expression
						case Semantic::E_COMP_EXPR:
							if(semQuery1.preds[i].right->u.COMP_EXPR.op ==
									semQuery2.preds[j].right->u.COMP_EXPR.op){
								cpredflagB = true;
							}
							break;

						default: break;

						}
					}
				}

			}
			// We find the matching predicate expression
			if(cpredflagA and cpredflagB){
				cflag = true;
				sflag = true;
				ssSelect = true;
				ssCode = 2;
			//	cout << "sharing analysis: complete sharing in where clause:" << endl;
				break;
			}
		}
		if(!cflag){
		//	cout<< "sharing analysis: complete sharing cannot be done: where clause no match" <<endl;
			break;
		}
	}

	}

	// subsume sharing checking
	if(!cflag and semQuery1.numPreds> semQuery2.numPreds){
	// Check the predicates like A op B
	for(int i=0; i<=semQuery2.numPreds-1;i++){
		sflag = false;

		for (int j=0;j<=semQuery1.numPreds-1;j++){

			cpredflagA = false;
			cpredflagB = false;
			// First we check the op
			if(semQuery2.preds[i].op  == semQuery1.preds[j].op){
			//	cout<< "sharing analysis: semQuery1.preds[i].op  == semQuery2.preds[j].op " << endl;
			//	cout<< "sharing analysis: semQuery1.preds[i].left->type =  "<< semQuery1.preds[i].left->type <<
			//								" and semQuery2.preds[j].left->type = " << semQuery2.preds[j].left->type << endl;
				// Second we check the left element A
				if(semQuery2.preds[i].left->type == semQuery1.preds[j].left->type){
			//		cout<< "sharing analysis: semQuery1.preds[i].left->type == semQuery2.preds[j].left->type " << endl;
					if(semQuery2.preds[i].left->exprType == semQuery1.preds[j].left->exprType){
				//		cout<< "semQuery1.preds[i].left->exprType == semQuery2.preds[j].left->exprType " << endl;
						// We go into the expression in different exprTypes:
						switch (semQuery2.preds[i].left->exprType){
						// Attribute reference
						case Semantic::E_ATTR_REF:
//							cout<< "sharing: analysis: pred left Semantic::E_ATTR_REF: " << endl;
//							cout<< "semQuery1.preds[i].left->u.attr.attrId: " << semQuery1.preds[i].left->u.attr.attrId << endl;
//							cout<< "semQuery2.preds[j].left->u.attr.attrId: " << semQuery2.preds[j].left->u.attr.attrId << endl;
//							cout<< "semQuery1.preds[i].left->u.attr.varId: " << semQuery1.preds[i].left->u.attr.varId << endl;
//							cout<< "semQuery2.preds[j].left->u.attr.varId: " << semQuery2.preds[j].left->u.attr.varId << endl;

							// Xing: CUrrently we don'r care the order of tables
							if(semQuery2.preds[i].left->u.attr.attrId ==
									semQuery1.preds[j].left->u.attr.attrId){
//								cout<< "sharing: analysis: pred left Semantic::E_ATTR_REF: attrId is equal" << endl;
//								if(semQuery1.preds[i].left->u.attr.varId ==
//										semQuery2.preds[j].left->u.attr.varId){
//									cout<< "sharing: analysis: pred left Semantic::E_ATTR_REF: varId is equal" << endl;
																cpredflagA = true;
//								}
							}



							break;

						// Constant Value, we don't need checking since it is already decided by type and exprType
						case Semantic::E_CONST_VAL:
						//	cout<< "sharing: analysis: pred left Semantic::E_CONST_VAL: " << endl;
							switch(semQuery2.preds[i].left->type){
							case INT:
								if(semQuery2.preds[i].left->u.ival == semQuery1.preds[j].left->u.ival){
									cpredflagA = true;
								}

								break;

							case FLOAT:
								if(semQuery2.preds[i].left->u.fval != semQuery1.preds[j].left->u.fval){
									cpredflagA = true;
								}

								break;

							case CHAR:
								if(strcmp(semQuery2.preds[i].left->u.sval, semQuery1.preds[j].left->u.sval)== 0){
									cpredflagA = true;
								}

								break;

							case BYTE:
								if(semQuery2.preds[i].left->u.bval == semQuery1.preds[j].left->u.bval){
									cpredflagA = true;
								}

								break;

							default: break;

							}

							break;

						// The aggregation: A complicated case AVG(Table1.weight) == AVG(Table2.weight) with Table1 = Table2
						case Semantic::E_AGGR_EXPR:
							if(semQuery2.preds[i].left->u.AGGR_EXPR.attr.attrId ==
									semQuery1.preds[j].left->u.AGGR_EXPR.attr.attrId){
								if(semQuery2.preds[i].left->u.AGGR_EXPR.attr.varId ==
									semQuery1.preds[j].left->u.AGGR_EXPR.attr.varId){
									if(semQuery2.preds[i].left->u.AGGR_EXPR.fn ==
										semQuery1.preds[j].left->u.AGGR_EXPR.fn)
									cpredflagA = true;
								}

							}
							break;
						// For now we didn't consider complex expression
						case Semantic::E_COMP_EXPR:
							if(semQuery2.preds[i].left->u.COMP_EXPR.op ==
									semQuery1.preds[j].left->u.COMP_EXPR.op){
								cpredflagA = true;
							}
							break;

						default: break;

						}
					}
				}

				// Third we check the right element B
//				cout<< "semQuery1.preds[i].right->type: " <<  semQuery1.preds[i].right->type << endl;
//				cout<< "semQuery2.preds[j].right->type: " <<  semQuery2.preds[j].right->type << endl;
				if(semQuery2.preds[i].right->type == semQuery1.preds[j].right->type){
				//	cout<< "semQuery1.preds[i].right->type == semQuery2.preds[j].right->type " << endl;
					if(semQuery2.preds[i].right->exprType == semQuery1.preds[j].right->exprType){
					//	cout<< "semQuery1.preds[i].right->exprType == semQuery2.preds[j].right->exprType " << endl;
						// We go into the expression in different exprTypes:
						switch (semQuery2.preds[i].right->exprType){
						// Attribute reference
						case Semantic::E_ATTR_REF:
						//	cout<< "sharing: analysis: pred right Semantic::E_ATTR_REF: " << endl;
							if(semQuery2.preds[i].right->u.attr.attrId ==
									semQuery1.preds[j].right->u.attr.attrId){
								if(semQuery2.preds[i].right->u.attr.varId ==
										semQuery1.preds[j].right->u.attr.varId)
								cpredflagB = true;
							}
							break;

						// Constant Value, we don't need checking since it is already decided by type and exprType
						case Semantic::E_CONST_VAL:
						//	cout<< "sharing: analysis: pred right Semantic::E_CONST_VAL: " << endl;
							switch(semQuery2.preds[i].right->type){
							case INT:
						//		cout<< "sharing: analysis: pred right Semantic::E_CONST_VAL: in INT " << endl;
								if(semQuery2.preds[i].right->u.ival == semQuery1.preds[j].right->u.ival){
							//		cout<< "semQuery1.preds[i].right->u.ival: " << semQuery1.preds[i].right->u.ival << endl;
							//		cout<< "semQuery2.preds[j].right->u.ival: " << semQuery2.preds[j].right->u.ival << endl;
									cpredflagB = true;
								}
								//Xing: for test
								else{
									cout<< "sharing: analysis: pred right Semantic::E_CONST_VAL: int values no matching " << endl;
									cout<< "semQuery2.preds[i].right->u.ival: " << semQuery2.preds[i].right->u.ival << endl;
									cout<< "semQuery1.preds[j].right->u.ival: " << semQuery1.preds[j].right->u.ival << endl;
								}

								break;

							case FLOAT:
							//	cout<< "sharing: analysis: pred right Semantic::E_CONST_VAL: in FLOAT " << endl;
								if(semQuery2.preds[i].right->u.fval == semQuery1.preds[j].right->u.fval){
									cpredflagB = true;
								}

								break;

							case CHAR:
							//	cout<< "sharing analysis: pred right Semantic::E_CONST_VAL: in char " << endl;
							//	cout << "the semQuery1.preds[i].right->u.sval: " << semQuery1.preds[i].right->u.sval << endl;
							//	cout << "the semQuery2.preds[j].right->u.sval: " << semQuery2.preds[j].right->u.sval << endl;

								if(	strcmp(semQuery2.preds[i].right->u.sval, semQuery1.preds[j].right->u.sval)== 0){
									cpredflagB = true;
								}

				//				 Xing: For testing
								else{
									cout<< "char no matchingin  pred right Semantic::E_CONST_VAL: in char " << endl;
									cout<< "semQuery2.preds[i].right->u.sval: " << semQuery2.preds[i].right->u.sval << endl;
									cout<< "semQuery1.preds[j].right->u.sval: " << semQuery1.preds[j].right->u.sval << endl;

								}

								break;

							case BYTE:
								if(semQuery2.preds[i].right->u.bval == semQuery1.preds[j].right->u.bval){
									cpredflagB = true;
								}

								break;

							default: break;

							}

							break;

						// The aggregation: A complicated case AVG(Table1.weight) == AVG(Table2.weight) with Table1 = Table2
						case Semantic::E_AGGR_EXPR:
							if(semQuery2.preds[i].right->u.AGGR_EXPR.attr.attrId ==
									semQuery1.preds[j].right->u.AGGR_EXPR.attr.attrId){
								if(semQuery2.preds[i].right->u.AGGR_EXPR.attr.varId ==
									semQuery1.preds[j].right->u.AGGR_EXPR.attr.varId){
									if(semQuery2.preds[i].right->u.AGGR_EXPR.fn ==
										semQuery1.preds[j].right->u.AGGR_EXPR.fn)
									cpredflagB = true;
								}

							}
							break;
						// For now we didn't consider complex expression
						case Semantic::E_COMP_EXPR:
							if(semQuery2.preds[i].right->u.COMP_EXPR.op ==
									semQuery1.preds[j].right->u.COMP_EXPR.op){
								cpredflagB = true;
							}
							break;

						default: break;

						}
					}
				}

			}
			// We find the matching predicate expression
			if(cpredflagA and cpredflagB){
				cout << "subsume sharing analysis: where clause: Find one sharable" << endl;
				sflag = true;
				ssSelect = true;
				ssCode = 2;
				break;
			}
		}
		if(!sflag){
			cout<< "sharing analysis: subsume sharing cannot be done: there is one condition in where clause not match" <<endl;
			ssMore = false;
			break;
		}
	}
	if(sflag){
		cout << "subsume sharing in where condition can be done" << endl;
		ssSelect = true;
		ssCode = 2;
		return true;
	}

	}



//	// Select (where clause) is same
//	ssSelect = true;
//	ssCode = 2;


	// Check optional group by clause
	if(semQuery1.numGbyAttrs != semQuery2.numGbyAttrs){
				cout<< "sharing: analysis: number of attributes in group-by are different"<< endl;
				return false;
			}
	if(semQuery1.numGbyAttrs>=1){
	for(int i=0; i<=semQuery1.numGbyAttrs-1;i++){
//		cout<< "semQuery1.gbyAttrs[i].attrId: "<< semQuery1.gbyAttrs[i].attrId << endl;
//		cout<< "semQuery2.gbyAttrs[i].attrId: "<< semQuery2.gbyAttrs[i].attrId << endl;
//		cout<< "semQuery1.gbyAttrs[i].varId: "<< semQuery1.gbyAttrs[i].varId << endl;
//		cout<< "semQuery2.gbyAttrs[i].varId: "<< semQuery2.gbyAttrs[i].varId << endl;
		if(semQuery1.gbyAttrs[i].attrId != semQuery2.gbyAttrs[i].attrId or
				semQuery1.gbyAttrs[i].varId != semQuery2.gbyAttrs[i].varId){
			cout<< "sharing analysis: no matching group by "<<endl;
			return false;
		}
	}
	}

	// Check select clause
	if(semQuery1.numProjExprs != semQuery2.numProjExprs){
			cout<< "sharing analysis: number of projection attributes are different"<< endl;
			cflag = false;
		}
//	cflag = true;

	//  Complete sharing analysis: Check all attributes
	// For current stage, we do not handle sharing between weight and sid, weight
	if(cflag){
	for(int i=0;i<=semQuery1.numProjExprs-1;i++){
		cflag = false;
	if(semQuery1.projExprs[i]->type == semQuery2.projExprs[i]->type){
		if(semQuery1.projExprs[i]->exprType == semQuery2.projExprs[i]->exprType){
			// We go into the expression in different exprTypes:
			switch (semQuery1.projExprs[i]->exprType){
			// Attribute reference
			case Semantic::E_ATTR_REF:
		//		cout<<"Sharing analysis: Selection check: E_ATTR_REF" <<endl;
				if(semQuery1.projExprs[i]->u.attr.attrId ==
						semQuery2.projExprs[i]->u.attr.attrId){
		//			cout<<"Sharing analysis: attrId is same" <<endl;
					if(semQuery1.projExprs[i]->u.attr.varId ==
							semQuery2.projExprs[i]->u.attr.varId){
			//			cout<<"Sharing analysis: varId is same" <<endl;
						cflag = true;
					}
				}
				break;

			// Constant Value, we don't need checking since it is already decided by type and exprType
			case Semantic::E_CONST_VAL:
			//	cout<<"Sharing analysis: Selection check: E_CONST_VAL" <<endl;
				cflag = true;
				break;

			// The aggregation: A complicated case AVG(Table1.weight) == AVG(Table2.weight) with Table1 = Table2
			case Semantic::E_AGGR_EXPR:
		//		cout<<"Sharing analysis: Selection check: E_AGGR_EXPR" <<endl;
				if(semQuery1.projExprs[i]->u.AGGR_EXPR.attr.attrId ==
						semQuery2.projExprs[i]->u.AGGR_EXPR.attr.attrId){
					if(semQuery1.projExprs[i]->u.AGGR_EXPR.attr.varId ==
							semQuery2.projExprs[i]->u.AGGR_EXPR.attr.varId){
						if(semQuery1.projExprs[i]->u.AGGR_EXPR.fn ==
								semQuery2.projExprs[i]->u.AGGR_EXPR.fn)
						cflag = true;
					}

				}
				break;
			// For now we didn't consider complex expression
			case Semantic::E_COMP_EXPR:
				cout<<"Sharing analysis: Selection check: E_COMP_EXPR" <<endl;
				if(semQuery1.projExprs[i]->u.COMP_EXPR.op ==
						semQuery2.projExprs[i]->u.COMP_EXPR.op){
					cflag = true;
				}
				break;

			default: break;

			}
		}
	}

	if(!cflag){
		cout<< "sharing analysis: no matching attribute: " <<endl;
		return false;
	}


	}
	}

	// check if subsume sharing is possible
	else{
		// Current number of query output attributes must smaller than sharing query
		if(semQuery1.numProjExprs > semQuery2.numProjExprs){
			cout<< "sharing analysis: number of projection attributes in current query is bigger than the sharing one"<< endl;
		}

		else{
		ssIndex = 0;
		// The last attribute is level, we don't need to check that
		for(int i=0;i<semQuery1.numProjExprs-1;i++){
			ssProject = false;
			for(int j=ssIndex; j< semQuery2.numProjExprs-1; j++, ssIndex++){



		if(semQuery1.projExprs[i]->type == semQuery2.projExprs[j]->type){
			if(semQuery1.projExprs[i]->exprType == semQuery2.projExprs[j]->exprType){
				// We go into the expression in different exprTypes:
				switch (semQuery1.projExprs[i]->exprType){
				// Attribute reference
				case Semantic::E_ATTR_REF:
			//		cout<<"Sharing analysis: Selection check: E_ATTR_REF" <<endl;
					if(semQuery1.projExprs[i]->u.attr.attrId ==
							semQuery2.projExprs[j]->u.attr.attrId){
			//			cout<<"Sharing analysis: attrId is same" <<endl;
						if(semQuery1.projExprs[i]->u.attr.varId ==
								semQuery2.projExprs[j]->u.attr.varId){
				//			cout<<"Sharing analysis: varId is same" <<endl;
							ssProject = true;
							ssCode = 3;
						}
					}
					break;

				// Constant Value, we don't need checking since it is already decided by type and exprType
				case Semantic::E_CONST_VAL:
				//	cout<<"Sharing analysis: Selection check: E_CONST_VAL" <<endl;
					ssProject = true;
					ssCode = 3;
					break;

				// The aggregation: A complicated case AVG(Table1.weight) == AVG(Table2.weight) with Table1 = Table2
				case Semantic::E_AGGR_EXPR:
			//		cout<<"Sharing analysis: Selection check: E_AGGR_EXPR" <<endl;
					if(semQuery1.projExprs[i]->u.AGGR_EXPR.attr.attrId ==
							semQuery2.projExprs[j]->u.AGGR_EXPR.attr.attrId){
						if(semQuery1.projExprs[i]->u.AGGR_EXPR.attr.varId ==
								semQuery2.projExprs[j]->u.AGGR_EXPR.attr.varId){
							if(semQuery1.projExprs[i]->u.AGGR_EXPR.fn ==
									semQuery2.projExprs[j]->u.AGGR_EXPR.fn)
								ssProject = true;
								ssCode = 3;
						}

					}
					break;
				// For now we didn't consider complex expression
				case Semantic::E_COMP_EXPR:
					cout<<"Sharing analysis: Selection check: E_COMP_EXPR" <<endl;
					if(semQuery1.projExprs[i]->u.COMP_EXPR.op ==
							semQuery2.projExprs[j]->u.COMP_EXPR.op){
						ssProject = true;
						ssCode = 3;
					}
					break;

				default: break;

				}

			}
		}
		if(ssProject)
			break;

		}

		if(!ssProject){
			cout<< "subsume sharing analysis: no matching attribute: " <<endl;
			return false;
		}


		}


	//	cout << "subsume sharing analysis: subsume sharing projection!" << endl;
		}
	}

	if(cflag)
		completeShareFlag = true;

	// If the winSpec is true, at least we can subsume share something
	if(ssWinSpec)
		subsumeShareFlag  = true;

	if(subsumeShareFlag or completeShareFlag)
		return true;
	else
		return false;
}
