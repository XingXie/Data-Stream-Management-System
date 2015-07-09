package stream.vis.data;

import java.util.List;
import java.util.ArrayList;

public class Operator implements EntityProperty {
    /// Unique id for an operator.  This is an index into the ops[] array 
    /// of a query plan.
    private int id;
    
    /// Unique identifier for an operator.  This is the identifier assigne
    /// by the server - used for display purposes
    private String serverId;
    
    /// Name of an operator (e.g., select, project)
    private String name;
    
    /// Long name of the operator (e.g., Selection, Projection, ..)
    private String longName;

    /// ids of the input operators
    private int[] inputs;
    
    /// true if the operator outputs a stream
    boolean bStream;
    
    /// Query plan to which this operator is part of
    QueryPlan plan;
    
    /// The list of properties ..
    List properties;
    
    public Operator (int id, String serverId, String name, String longName,
		     int[] inputs, QueryPlan plan, boolean bstr) {
	this.id = id;
	this.serverId = serverId;
	this.name = name;
	this.longName = longName;
	this.inputs = inputs;
	this.bStream = bstr;
	this.plan = plan;
	this.properties = new ArrayList ();
    }
    
    public void addProperty (String name, String value) {
	properties.add (new Property (name, value));
    }
    
    public int getId () {
	return id;
    }
    
    public String getServerId () {
	return serverId;
    }
    
    public String getName () {
	return name;
    }
    
    public String getLongName () {
	return longName;
    }
    
    public int getNumInputs () {
	return inputs.length;
    }
    
    public int getInput (int index) {
	return inputs [index];
    }
    
    public boolean outputsStream () {
	return bStream;
    }
    
    public QueryPlan getPlan () {
	return plan;
    }    
    
    //------------------------------------------------------------
    // EntityProperty methods
    //------------------------------------------------------------
    
    public int getNumProperties () {
	// the first two properties are operator specific ..
	return properties.size() + 3;
    }
    
    public String getKey (int index) {
	assert (index < properties.size() + 3);
	
	if (index == 0)
	    return "Type";
	if (index == 1)
	    return "Name";
	if (index == 2)
	    return "Id";
	
	Property p = (Property)properties.get(index - 3);
	return p.name;
    }
    
    public String getValue (int index) {
	assert (index < properties.size() + 3);
	
	if (index == 0)
	    return "Operator";
	if (index == 1)
	    return longName;
	if (index == 2)
	    return serverId;
	
	Property p = (Property)properties.get(index - 3);
	return p.value;
    }
    
    private class Property {
	
	Property (String n, String v) {
	    name = n;
	    value = v;
	}
	
	String name;
	String value;
    }
}
