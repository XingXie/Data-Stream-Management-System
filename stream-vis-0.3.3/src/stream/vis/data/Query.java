package stream.vis.data;

import stream.vis.util.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.xml.sax.*;

public class Query {
    
    /// The text of the query
    private String queryString;
    
    /// Does this query produce a visible output (a query could be view
    /// in which case it does not produce output
    private boolean hasOutput;
    
    /// Does this query correspond to a monitor
    private boolean isMonitor;
    
    /// Valid only if hasOutput == true.  the outputId is sent by the 
    /// server when the query is registered and is used to connect to
    /// the server to receive output.
    private int outputId;
    
    /// Is the query output named - (is the query a view)
    private boolean isNamed;
    
    /// If the query is named, then the named table that it produces
    private NamedTable namedTable;
    
    /// The the query is unnamed, then the unnamed table that it produces
    /// the unnamed table has basically the attribute types and the 
    /// type of output (stream/relation) that the query produces    
    private UnnamedTable unnamedTable;
    
    /// the id of the query assigned by the server
    private int queryId;
    
    public Query (String queryString, boolean hasOutput, boolean isNamed) {
	assert (queryString != null);
	
	this.queryString = queryString;
	this.hasOutput = hasOutput;
	this.isNamed = isNamed;
	this.isMonitor = false;
	this.namedTable = null;
	this.unnamedTable = null;
	this.outputId = -1;
	this.queryId = -1;
    }
    
    //------------------------------------------------------------
    // Set methods
    //------------------------------------------------------------
    public void setNamedTable (NamedTable table) {
	assert (isNamed);
	
	this.namedTable = table;
    }
    
    public void setUnnamedTable (UnnamedTable table) {	
	assert (!isNamed);
	
	this.unnamedTable = table;
    }
    
    public void setOutputId (int id) {
	// Not set already:
	assert (this.outputId == -1);
	
	this.outputId = id;
    }
    
    public void setQueryId (int qid) {
	// Not set already:
	assert (this.queryId == -1);
	
	this.queryId = qid;
    }
    
    public void setMonitor (boolean isMon) {
	this.isMonitor = isMon;
    }
    
    //------------------------------------------------------------
    // Get methods
    //------------------------------------------------------------
    
    public boolean isMonitor () {
	return isMonitor;
    }
    
    public String getString () {
	return queryString;
    }
    
    public boolean isNamed() {
	return isNamed;
    }
    
    public boolean hasOutput () {
	return hasOutput;
    }
    
    public NamedTable getNamedTable () {
	assert (isNamed);	
	return namedTable;
    }
    
    public int getQueryId () {
	// should have been set..
	assert (queryId != -1);	
	return queryId;
    }
    
    public Schema getSchema () {
	assert ((isNamed && namedTable != null) ||
		(!isNamed && unnamedTable != null));
	if (isNamed)
	    return namedTable;
	else
	    return unnamedTable;
    }
    
    public int getOutputId () {
	assert (outputId != -1);
	return outputId;
    }

    //------------------------------------------------------------
    // To script
    //------------------------------------------------------------
    public String toScript () {
	StringBuffer strBuf = new StringBuffer();

	strBuf.append("<Query>\n");

	strBuf.append("<QueryString>");
	strBuf.append(queryString);
	strBuf.append("</QueryString>\n");

	strBuf.append("<isNamed>");
	strBuf.append(isNamed);
	strBuf.append("</isNamed>\n");

	strBuf.append("<hasOutput>");
	strBuf.append(hasOutput);
	strBuf.append("</hasOutput>\n");

	strBuf.append("</Query>\n");

	return strBuf.toString();
    } 

    //------------------------------------------
    // generate Query from part of script xml
    //------------------------------------------
    public static Query genQueryFromScript(Node node) 
	throws FatalException {

	NodeList childs = node.getChildNodes ();
	String queryString="", bVal;
	boolean hasOutput=false, isNamed=false;

	for (int n = 0 ; n < childs.getLength () ; n++) {
	    Node child = childs.item (n);
	    
	    if (child.getNodeType () != Node.ELEMENT_NODE)
		continue;
	    
	    if (child.getNodeName().equals ("QueryString")) {
		queryString = XmlHelper.getText (child);
		continue;
	    }
	    
	    if (child.getNodeName().equals ("hasOutput")) {
		bVal = XmlHelper.getText (child);
		if(bVal.equals("true")) hasOutput=true;
		else hasOutput = false;
		continue;
	    }
	    
	    if (child.getNodeName().equals ("isNamed")) {
		bVal = XmlHelper.getText (child);
		if(bVal.equals("true")) isNamed=true;
		else isNamed = false;
		continue;
	    }
	}

	Query query = new Query(queryString, hasOutput, isNamed);
	    
	return query;
    }
}
