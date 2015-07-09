package stream.vis.data;

public class Synopsis implements EntityProperty {
    /// Unique id for a store.  This is the index into the 
    /// syns [] array of the owning query plan
    private int id;
    
    /// id assigned by the server.  Used to refer to this synopsis when
    /// talking to the server
    private String serverId;
    
    /// Name for the synopsis 
    private String name;
    
    /// The operator who owns this store object
    private int ownOp;
    
    /// The type of synopsis - one of LEFT, RIGHT, INPUT, OUTPUT
    private int synType;
    
    /// The store which allocated tuples to this synopsis
    private int store;
    
    /// The query plan to which we belong
    private QueryPlan plan;

    // Type of synopses
    
    /// Synopsis corresponding to the left input
    public static final int LEFT  = 0;
    
    /// Synopsis corresponding to the right input
    public static final int RIGHT = 1;
    
    /// Synopsis corresponding to the only input 
    public static final int CENTER = 2;
    
    /// Synopsis corresponding to the output
    public static final int OUTPUT = 3;
        
    public Synopsis (int id, String serverId, String name, 
		     int ownOp, int synType, int store,
		     QueryPlan plan) {
	this.id = id;
	this.serverId = serverId;
	this.ownOp = ownOp;
	this.name = name;
	this.synType = synType;
	this.store = store;
	this.plan = plan;
    }
    
    //------------------------------------------------------------
    // Get methods
    //------------------------------------------------------------
    
    public String getServerId () {
	return serverId;
    }
    
    public int getOwnOp () {
	return ownOp;
    }
    
    public int getStore () {
	return store;
    }
    
    public String getName () {
	return name;
    }
    
    public int getType () {
	return synType;
    }
    
    public int getNumProperties () {
	return 5;
    }
    
    private String[] propertyKeys = {
	"Type",
	"Name",
	"Id",
	"Owner",
	"Store"
    };
    
    public String getKey (int index) {
	return propertyKeys [index];
    }
    
    public String getValue (int index) {
	if (index == 0)
	    return "Synopsis";
	if (index == 1)
	    return name;
	if (index == 2)
	    return serverId;
	if (index == 3)
	    return plan.getOp(ownOp).getServerId();
	if (index == 4)
	    return plan.getStore (store).getServerId();
	// should never come
	return "";
    }
}
