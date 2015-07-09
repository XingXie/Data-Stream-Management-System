package stream.vis.data;

public class Monitor {
    
    //------------------------------------------------------------
    // Types of Monitors:
    // 
    // Each monitor type corresponds to a property in SysStream
    // These codes are (&should be) identical to the ones
    // declared in sys_stream.h of the server code
    //------------------------------------------------------------
    
    /// Fraction of time used by an operator
    public static final int SS_OP_TIME = 0;
    
    /// Rate of tuple flow in a queue
    public static final int SS_QUEUE_RATE = 1;
    
    /// Last enqueued timestamp in a queue
    public static final int SS_QUEUE_TS = 2;
    
    /// Join selectivity
    public static final int SS_JOIN_SEL = 3;
    
    /// Number of tuples in synopsis
    public static final int SS_SYN_CARD = 4;
    
    /// Number of pages used by store
    public static final int SS_STORE_SIZE = 5;
    
    //------------------------------------------------------------
    // Types of Entities: codes for entities - operators, queues,
    // synopses, stores, that occur in a plan.  These codes should
    // be consistent with the codes in sys_stream.h
    //------------------------------------------------------------
    
    /// Operator
    public static final int OP    = 0;
    
    /// Queue
    public static final int QUEUE = 1;
    
    /// Synopsis
    public static final int SYN   = 2;
    
    /// Store
    public static final int STORE = 3;
    
    //------------------------------------------------------------
    // Granularity of timestamps : should be consistent with 
    // sys_stream.h
    //------------------------------------------------------------
    
    public static final int TIME_PER_SEC = 5;
    
    //------------------------------------------------------------
    // An aggregatable monitor is one whose value can be averaged
    // over a time window to smooth out the property
    //------------------------------------------------------------
    
    private static boolean isAggregatable [] = {
	true, 
	true, 
	false,
	true, 
	true, 
	true
    };
    
    public static boolean isAggr (int monId) {	
	assert (monId >= 0 && monId < isAggregatable.length);
	return isAggregatable [monId];
    }
    
    //------------------------------------------------------------
    // Depending on the type of the monitor, each monitored property
    // is either an int or a float, and they have to refer to
    // different attributes of SysStream accordingly
    //------------------------------------------------------------
    
    private static String attr [] = {
	"Fval",
	"Fval",
	"Ival",
	"Fval",
	"Ival",
	"Ival"
    };    
    
    //------------------------------------------------------------
    // Each monitor type corresponds to some entity.  E.g., SS_OP_TIME
    // measures time used by an operator, so corresponds to
    // operator entity.  The following is the mapping from    
    // monitor types to entity types
    //------------------------------------------------------------
    
    private static int entityOf [] = {OP, QUEUE, QUEUE, OP, SYN, STORE};
    
    //------------------------------------------------------------
    // Monitor state
    //------------------------------------------------------------
    
    /// Type of the monitor [0..5]
    private int monitorId;
    
    /// Entity type: entityOf[type]
    private int entityType;
    
    /// (Server side) Id for the particular entity (instance) being monitored
    private String entityId;
    
    /// If isAggr[monitorId], the size of window over which we aggregate
    private int winSize;
    
    public Monitor (int monId, String entityId) {
	// Valid monitor id?
	assert (monId >= 0 && monId <= 5);

	this.monitorId = monId;
	this.entityType = entityOf [monId];
	this.entityId = entityId;
	
	// This constructor should be used only for non-aggr monitors
	assert (!isAggregatable[monId]);
	
	this.winSize = 0;
    }
    
    public Monitor (int monId, String entityId, int winSize) {
	// Valid monitor id?
	assert (monId >= 0 && monId <= 5);
	
	this.monitorId = monId;
	this.entityType = entityOf [monId];
	this.entityId = entityId;
	
	// This constructor should be used only for aggr monitors
	assert (isAggregatable[monId]);
	
	this.winSize = winSize;	
    }
    
    public String getMonitorQuery () {
	String query;
	
	if (isAggregatable [monitorId]) {
	    query =
		"Rstream (" +
		"Select Avg(" + attr [monitorId] + ")\n" +  
		"From SysStream [Range " + winSize + "]\n" +
		"Where Type = " + entityType + " and " +
		"Id = " + entityId + " and " +
		"Property = " + monitorId +
		");";	    
	}
	
	else {
	    query =
		"Select " + attr[monitorId] + "\n" + 
		"From SysStream\n" +
		"Where Type = " + entityType + " and " + 
		"Id = " + entityId + " and " + 
		"Property = " + monitorId + ";";
	}
	
	return query;
    }
}
