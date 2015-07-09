package stream.vis.data;

import stream.vis.net.*;
import stream.vis.util.FatalException;
import stream.vis.util.NonFatalException;
import stream.vis.util.Error;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.net.Socket;
import java.io.*;

public class Client {
    
    /// Connection to the server
    ServerConnection serverConn;
    
    /// State of the client
    private int state;
    
    /// Registered tables (stream or relations) - these could be base
    /// tables or intermediate tables
    private List tables;
    
    /// Registered Queries
    private List queries;
    
    /// Registered monitors
    private List monitors;
    
    /// Queries corresponding to monitors
    private List monQueries;
    
    /// The query plan
    private QueryPlan plan;
    
    /// Mapping from queries to results
    private Map queryResults;
    
    /// Mapping from queries to result reader threads
    private Map queryResultReaders;
    
    /// Mapping from tables to tableFeeders
    private Map tableFeeders;
    
    //------------------------------------------------------------
    // States of the client: 
    //
    // State transitions: INIT --> (APPSPEC|SCRIPT)* --> PL
    //------------------------------------------------------------
    
    /// Initial state - no connection established with the server yet
    public static final int INIT = 0;
    
    /// Application is being specified by the user interactively
    public static final int USERAPPSPEC = 1;
    
    /// Application is being specified using a script
    public static final int SCRIPTAPPSPEC = 2;    
    
    /// Plan has been generated - user cannot specify any more streams/rels
    public static final int PLANGEN = 3;
    
    /// Query execution has started
    public static final int RUN = 4;
    
    //------------------------------------------------------------
    // Client Event - listeners ...
    //------------------------------------------------------------
    
    /// The list of components listening to the client events
    private List listeners;
    
    public Client () {
	tables       = new ArrayList ();
	queries      = new ArrayList ();
	monitors     = new ArrayList ();
	monQueries   = new ArrayList ();
	plan         = null;
	queryResults = new HashMap ();
	queryResultReaders = new HashMap ();
	tableFeeders = new HashMap ();
	listeners    = new ArrayList ();
	state        = INIT;
    }
    
    public void setServerInfo (String host, int port) throws FatalException {
	assert (state == INIT);
	
	serverConn = new ServerConnection (host, port);	
	
	BeginAppRet ret = serverConn.beginApp ();
	if (ret.errorCode != 0) {
	    throw new FatalException ("Server returned error code");
	}
	
	// change the state
	state = USERAPPSPEC;
	bcastStateChange (INIT, USERAPPSPEC);
    }
    
    public void beginScriptSpec () {
	assert (state == USERAPPSPEC);	
	state = SCRIPTAPPSPEC;
	bcastStateChange (USERAPPSPEC, SCRIPTAPPSPEC);
    }
    
    public void endScriptSpec () {
	assert (state == SCRIPTAPPSPEC);
	state = USERAPPSPEC;
	bcastStateChange (SCRIPTAPPSPEC, USERAPPSPEC);
    }
    
    /// Register a new stream 
    public void registerBaseTable (NamedTable table) 
	throws FatalException, NonFatalException {
	
	assert (state == USERAPPSPEC || state == SCRIPTAPPSPEC);
	
	/// Command to register the table with the server
	String command = getRegisterTableCommand (table);
	
	/// Register ...
	RegInputRet ret = serverConn.registerInput (command);
	
	if (ret.errorCode != 0) {
	    // Known error (e.g., parse error)
	    if (Error.isKnownError (ret.errorCode)) {
		throw new NonFatalException 
		    (Error.getErrorMesg (ret.errorCode));
	    }
	    
	    else {	    
		throw new FatalException ("Server returned error code");
	    }
	}
	
	table.setInputId(ret.inputId);
	tables.add (table);	
	
	bcastRegisterBaseTable (table);
    }
    
    /// Register a query
    
    public void registerQuery (Query qry) 
	throws FatalException, NonFatalException {
	
	assert (state == USERAPPSPEC || state == SCRIPTAPPSPEC);
	
	RegQueryRet ret;
	
	if (qry.hasOutput()) {
	    ret = serverConn.registerOutQuery (qry.getString());
	    if (ret.errorCode != 0) {
		if (Error.isKnownError (ret.errorCode)) {
		    throw new NonFatalException 
			(Error.getErrorMesg (ret.errorCode));
		}
		else {
		    throw new FatalException ("Server returned error code");
		}
	    }
	    qry.setQueryId (ret.queryId);
	    qry.setOutputId (ret.outputId);
	}
	
	else {
	    ret = serverConn.registerQuery (qry.getString ());
	    if (ret.errorCode != 0) {
		if (Error.isKnownError (ret.errorCode)) {
		    throw new NonFatalException 
			(Error.getErrorMesg (ret.errorCode));
		}
		else {
		    throw new FatalException ("Server returned error code");
		}
	    }	    
	    qry.setQueryId (ret.queryId);
	}
	
	UnnamedTable unnamedTable = new UnnamedTable (ret.schema);
	if (!qry.isNamed()) 
	    qry.setUnnamedTable (unnamedTable);
	
	// If query is named, the schema is set in registerView() when
	// the user enters the view details	
	queries.add (qry);	
	bcastQueryAdded (qry, unnamedTable);
    }
    
    /**
     * Make the output of 
     */
    public void registerView (Query qry, NamedTable table) 
	throws FatalException, NonFatalException {
	assert (state == USERAPPSPEC || state == SCRIPTAPPSPEC);
	assert (qry.isNamed());
	assert (!table.isBase());
	
	qry.setNamedTable (table);
	table.setSource (qry);
	
	// Command to register the table with the server
	String command = getRegisterTableCommand (table);
	
	RegViewRet ret = 
	    serverConn.registerView (command, qry.getQueryId ());
	if (ret.errorCode != 0) {
	    if (Error.isKnownError (ret.errorCode)) {
		throw new NonFatalException 
		    (Error.getErrorMesg (ret.errorCode));
	    }
	    else {
		throw new FatalException ("Server returned error code");
	    }
	}
	
	tables.add (table);	
	bcastViewAdded (qry, table);	
    }
    
    /// Get plan
    public void genPlan () throws FatalException {
	assert (state == USERAPPSPEC);
	
	// Send end application command
	EndAppRet eret = serverConn.endApp ();
	if (eret.errorCode != 0) 
	    throw new FatalException ("Server returned error code");
	
	// Send the generate plan command to the server
	GenPlanRet ret = serverConn.genPlan ();	
	if (ret.errorCode != 0)
	    throw new FatalException ("Server returned error code");
	
	plan = new QueryPlan (ret.planString);	
	bcastPlanGen (plan);
	
	state = PLANGEN;
	bcastStateChange (USERAPPSPEC, PLANGEN);
    }
    
    /// Execute
    public void run () throws FatalException {
	assert (state == PLANGEN);	
	setupQueryResults ();
	
	state = RUN;
	bcastStateChange (PLANGEN, RUN);
	
	ExecRet ret = serverConn.execute ();
	if (ret.errorCode != 0)
	    throw new FatalException ("Server returned error code");
    }
    
    public QueryResult registerMonitor (Monitor monitor) 
	throws FatalException {
	RegMonRet ret;
	String monQryStr;
	Query monQry;
	
	assert (state == PLANGEN || state == RUN);       
	
	// create the monitor query
	monQry = new Query (monitor.getMonitorQuery (), true, false);
	monQry.setMonitor (true);
	
	// register the monitor
	ret = serverConn.registerMonitor (monQry.getString());
	if (ret.errorCode != 0) {
	    throw new FatalException ("Server returned error code");
	}
	monQry.setQueryId (ret.monId);
	monQry.setOutputId (ret.outputId);
	
	// schema of the query 
	UnnamedTable schema = new UnnamedTable (ret.schema);
	monQry.setUnnamedTable (schema);
	
	// the update query plan
	assert (plan != null);
	plan.updatePlan (ret.planString);
	
	monitors.add (monitor);
	monQueries.add (monQry);
	
	// set up the output of the query
	try {
	    QueryResult res = new QueryResult (monQry);
	    queryResults.put (monQry, res);
	    Socket sock = serverConn.establishOutputConnection (ret.outputId); 
	    QueryResultReader resReader = new QueryResultReader (sock, res);
	    queryResultReaders.put (monQry, resReader);
	    resReader.start ();
	    
	    bcastMonitorAdded (monitor, monQry, res, plan);
	    
	    return res;
	}
	catch (IOException e) {
	    throw new FatalException ("IO Exception");
	}	
    }
    
    public void reset () throws FatalException {	
	// Init state has no server connection
	assert (state != INIT);
	
	// Reset the server
	ResetRet ret = serverConn.reset ();
	if (ret.errorCode != 0)
	    throw new FatalException ("Server returned error code");
	
	// Stop all table feeders
	for (int t = 0 ; t < tables.size() ; t++) {
	    NamedTable table = (NamedTable)tables.get (t);
	    
	    if (!table.isBase()) 
		continue;
	    
	    TableFeeder feeder = (TableFeeder)tableFeeders.get (table);

	    if (feeder != null)
		feeder.terminate ();
	}
	
	// Stop all the query readers
	for (int q = 0 ; q < queries.size() ; q++) {
	    Query qry = (Query)queries.get(q);
	    
	    if (!qry.hasOutput())
		continue;
	    
	    QueryResultReader reader = 
		(QueryResultReader)queryResultReaders.get (qry);
	    
	    if (reader != null)
		reader.stopReader ();
	}
	
	for (int m = 0 ; m < monQueries.size() ; m++) {
	    Query qry = (Query) monQueries.get(m);
	    
	    QueryResultReader reader = 
		(QueryResultReader)queryResultReaders.get (qry);
	    
	    if (reader != null)
		reader.stopReader ();
	}
	
	// Clear state
	tables.clear ();
	queries.clear ();
	plan = null;
	queryResults.clear ();
	queryResultReaders.clear ();
	tableFeeders.clear ();
	monitors.clear ();
	monQueries.clear ();

	bcastResetEvent ();
	
	BeginAppRet bret = serverConn.beginApp ();
	if (bret.errorCode != 0) {
	    throw new FatalException ("Server returned error code");
	}
	int oldState = state;
	
	state = USERAPPSPEC;
	bcastStateChange (oldState, state);	
    }
    
    public void softReset () throws FatalException {
	List baseTables;
	
	// Get the list of base tables
	baseTables = new ArrayList ();
	for (int t = 0 ; t < tables.size () ; t++) {
	    NamedTable table = (NamedTable)tables.get(t);
	    if (table.isBase())
		baseTables.add (table);
	}
	
	reset ();
	
	// re-register all base tables	
	try {
	    for (int t = 0 ; t < baseTables.size () ; t++) {
		NamedTable table = (NamedTable)baseTables.get(t);
		registerBaseTable (table);
	    }
	}
	catch (NonFatalException e) {
	    throw new FatalException ("Unexpected error");
	}
	
	baseTables = null;
    }    

    public void end () {
	try {
	    if (state != INIT) {
		reset ();
		
		TerminateRet ret = serverConn.terminate ();
		if (ret.errorCode != 0)
		    System.out.println ("Server returned error code");
	    }
	}
	
	catch(FatalException e) {
	    System.out.println (e.toString());
	}
	
	// exit
	System.exit (0);
    }

    public TableFeeder createTableFeeder (NamedTable table, String fileName,
					  boolean bLoop, boolean bAppTs)
	throws FatalException {
	
	TableFeeder feeder;
	
	/// create corrsponding TableFeeder
        try {
	    Socket sock = 
		serverConn.establishInputConnection(table.getInputId()); 
	    
	    if (bAppTs) {
		feeder = new ApptimeTableFeeder (table, sock, fileName);
	    }
	    else {
		feeder = new SystimeTableFeeder (table, sock, fileName, bLoop);
	    }
	    
	    tableFeeders.put (table, feeder);
	    return feeder;
	}
	
	catch (Exception e) {
	    e.printStackTrace();
	    throw new FatalException 
		("Error establishing input conn w/ Server");
	}
    }
    
    public void destroyTableFeeder (NamedTable table) throws
	FatalException {

	try {
	    TableFeeder feeder = (TableFeeder)tableFeeders.get(table);

	    if(feeder != null) feeder.terminate();

	    tableFeeders.remove(table);
	}
	catch (Exception e) {
	    e.printStackTrace();
	    throw new FatalException 
		("Error terminating input conn w/ Server");
	}
    }

    /// Get the list of registered streams
    public List getRegisteredTables () {
	return tables;
    }
    
    public List getRegisteredQueries () {
	return queries;
    }
    
    public QueryResult getQueryResult (Query query) {
	assert (query.hasOutput());	
	return (QueryResult)queryResults.get (query);
    }
    
    public int getState () {
	return state;
    }
    
    public void addListener (ClientListener listener) {
	listeners.add (listener);
    }
    
    public void removeListener (ClientListener listener) {
	listeners.remove (listener);
    }							  
    
    private void setupQueryResults () throws FatalException {
	
	try {
	    for (int q = 0 ; q < queries.size() ; q++) {
		Query qry = (Query)queries.get(q);
		
		if (qry.hasOutput ()) {
		    QueryResult res = new QueryResult (qry);
		    queryResults.put (qry, res);		
		    bcastQueryResultAvailable (res);
		    
		    Socket sock = serverConn.establishOutputConnection 
			(qry.getOutputId());
		    QueryResultReader resReader = 
			new QueryResultReader (sock, res);
		    queryResultReaders.put (qry, resReader);
		    resReader.start ();
		}
	    }
	}
	catch (IOException e) {
	    throw new FatalException ("IO Exception");
	}
    }
    
    private void bcastStateChange (int oldState, int newState) {
	ClientListener listener;
	for (int l = 0 ; l < listeners.size() ; l++) {
	    listener = (ClientListener)listeners.get(l);
	    listener.stateChanged (oldState, newState);
	}
    }
    
    private void bcastRegisterBaseTable (NamedTable table) {
	ClientListener listener;
	for (int l = 0 ; l < listeners.size() ; l++) {
	    listener = (ClientListener)listeners.get(l);
	    listener.baseTableAdded (table);
	}
    }
    
    private void bcastQueryAdded (Query query, UnnamedTable unnamedTable) {
	ClientListener listener;
	for (int l = 0 ; l < listeners.size() ; l++) {
	    listener = (ClientListener)listeners.get(l);
	    listener.queryAdded (query, unnamedTable);
	}
    }
    
    private void bcastViewAdded (Query query, NamedTable table) {
	ClientListener listener;
	for (int l = 0 ; l < listeners.size() ; l++) {
	    listener = (ClientListener)listeners.get(l);
	    listener.viewAdded (query, table);
	}
    }
    
    private void bcastPlanGen (QueryPlan plan) {
	ClientListener listener;
	for (int l = 0 ; l < listeners.size() ; l++) {
	    listener = (ClientListener)listeners.get(l);
	    listener.planGenerated (plan);
	}
    }
    
    private void bcastQueryResultAvailable (QueryResult result) {
	ClientListener listener;
	for (int l = 0 ; l < listeners.size() ; l++) {
	    listener = (ClientListener)listeners.get(l);
	    listener.queryResultAvailable (result);
	}
    }
    
    private void bcastMonitorAdded (Monitor mon, Query qry, QueryResult res, 
				    QueryPlan plan) {
	ClientListener listener;
	for (int l = 0 ; l < listeners.size() ; l++) {
	    listener = (ClientListener)listeners.get(l);
	    listener.monitorAdded (mon, qry, res, plan);
	}
    }
	
    
    public void bcastResetEvent () {
	ClientListener listener;
	List listenersCopy = new ArrayList ();
	
	// We work with a copy since listeners list is modified when
	// listener.resetEvent() is called for some listeners.
	for (int l = 0 ; l < listeners.size() ; l++) {
	    listenersCopy.add (listeners.get(l));
	}
	
	for (int l = 0 ; l < listenersCopy.size() ; l++) {
	    listener = (ClientListener)listenersCopy.get(l);
	    listener.resetEvent ();
	}
	
	listenersCopy = null;
    }    
    
    private String getRegisterTableCommand (NamedTable table) {
	if (table.isStream ())
	    return "Register Stream " + table + ";";
	else
	    return "Register Relation " + table + ";";
    }
}
