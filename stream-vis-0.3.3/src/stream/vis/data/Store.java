package stream.vis.data;

public class Store implements EntityProperty {
    /// Unique id for a store.  This is the index into the 
    /// stores [] array of the owning query plan
    private int id;
    
    /// id assigned by the server.  Used to refer to this store when
    /// talking to the server
    private String serverId;
    
    /// Name of the store - one of RelStore, WindowStore, PwindowStore,
    /// LineageStore, SimpleStore
    private String name;
    
    /// The operator who owns this store object
    private int ownOp;
    
    /// the query plan to which we belong
    private QueryPlan plan;
    
    public Store (int id, String serverId, String name, int ownOp,
		  QueryPlan plan) {
	this.id = id;
	this.serverId = serverId;
	this.name = name;
	this.ownOp = ownOp;
	this.plan = plan;
    }
    
    public int getOwnOp () {
	return ownOp;
    }
    
    public String getName () {
	return name;
    }
    
    public String getServerId () {
	return serverId;
    }

    public int getNumProperties () {
	return 4;
    }

    private String[] propertyKeys = {
	"Type",
	"Name",
	"Id",
	"Owner"
    };
    
    public String getKey (int index) {
	return propertyKeys [index];
    }
    
    public String getValue (int index) {
	if (index == 0)
	    return "Store";
	if (index == 1)
	    return name;
	if (index == 2)
	    return serverId;
	if (index == 3)
	    return plan.getOp(ownOp).getServerId();
	
	// should never come
	return "";
    }
}
