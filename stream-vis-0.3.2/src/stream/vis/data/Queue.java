package stream.vis.data;

public class Queue implements EntityProperty {
    /// Unique id for a queue.  This is an index into the 
    /// queues[] array of QueryPlan i.e., queryPlan[id] == this
    private int id;
    
    /// identifier assigned by the server.  This is used to talk to 
    /// the server about this queue
    private String serverId;
    
    /// Id of the src operator
    private int srcOp;
    
    /// Id of the destination operator
    private int destOp;
    
    /// Query plan to which this queue belongs
    private QueryPlan plan;
    
    public Queue (int id, String serverId, int srcOp, int destOp, 
		  QueryPlan plan) {
	this.id = id;
	this.serverId = serverId;
	this.srcOp = srcOp;
	this.destOp = destOp;
	this.plan = plan;
    }
    
    public int getId () {
	return id;
    }
    
    public String getServerId () {
	return serverId;
    }
    
    public int getSrc () {
	return srcOp;
    }
    
    public int getDest () {
	return destOp;
    }
    
    public QueryPlan getPlan () {
	return plan;
    }

    public int getNumProperties () {
	return 4;
    }
    
    private String[] propertyKeys = {
	"Type",
	"Id",
	"Source",
	"Dest"
    };    
    
    public String getKey (int index) {
	return propertyKeys[index];
    }
    
    public String getValue (int index) {
	if (index == 0)
	    return "Queue";
	if (index == 1)
	    return serverId;
	if (index == 2)
	    return plan.getOp(srcOp).getServerId();
	if (index == 3)
	    return plan.getOp(destOp).getServerId();
	return "";
    }
}
