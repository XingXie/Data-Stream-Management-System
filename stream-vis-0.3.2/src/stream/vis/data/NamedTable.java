package stream.vis.data;

import stream.vis.util.*;
import java.util.List;
import java.util.ArrayList;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.xml.sax.*;

/**
 * A named table is a stream or a relation with a name and containing named
 * attributes.  A named table is either a base table (an input stream or
 * relation) or an intermediate table produced by a subquery.
 */

public class NamedTable implements Schema {
    
    /// Name of the table
    private String tableName;
    
    /// List of named attributes
    private List attrs;
    
    /// Is it a stream or a relation?
    private boolean isStream;
    
    /// Is it a base table or an intermediate table
    private boolean isBase;
    
    /// If !isBase, then the query that produces this table
    private Query source;
    
    /// the input id for sending data to server
    private int inputId;

    public NamedTable (String tableName, boolean isStream, boolean isBase) {
	this.tableName = tableName;
	this.isStream = isStream;
	this.isBase = isBase;
	this.attrs = new ArrayList ();
    }
    
    //------------------------------------------------------------
    // Set methods
    //------------------------------------------------------------
    
    public void addAttr (String name, int type) {
	attrs.add (new Attr (name, type));
    }
    
    public void addAttr (String name, int type, int len) {
	attrs.add (new Attr (name, type, len));
    }
    
    public void addAttr (Attr attr) {
	attrs.add (attr);
    }
    
    public void setSource (Query qry) {
	assert (!isBase);	
	source = qry;
    }   
    
    public void setInputId (int id) {
	inputId = id;
    }

    //------------------------------------------------------------
    // Get methods
    //------------------------------------------------------------
    
    public String getTableName () {
	return tableName;
    }
    
    public boolean isStream () {
	return isStream;
    }
    
    public boolean isBase () {
	return isBase;
    }
    
    public int getNumAttrs () {
	return attrs.size();
    }
    
    public String getAttrName (int index) {
	return ((Attr)attrs.get(index)).name;
    }
    
    public int getAttrType (int index) {
	return ((Attr)attrs.get(index)).type;
    }
    
    public int getAttrLen (int index) {
	return ((Attr)attrs.get(index)).len;
    }
    
    public String toString () {
	return tableName + " (" + getAttrEncoding() + ")";
    }
    
    public String getAttrEncoding () {
	StringBuffer strBuf = new StringBuffer ();
	
	for (int a = 0 ; a < attrs.size() ; a++) {
	    strBuf.append (attrs.get(a));
	    if (a < attrs.size() - 1)
		strBuf.append (", ");
	}
	
	return strBuf.toString ();
    }
 
    public int getInputId () {
	return inputId;
    }

    //------------------------------------------------------------
    // To script
    //------------------------------------------------------------
    public String toScript () {
	StringBuffer strBuf = new StringBuffer();

	strBuf.append("<Table>\n");

	strBuf.append("<Name>");
	strBuf.append(tableName);
	strBuf.append("</Name>\n");

	strBuf.append("<isStream>");
	strBuf.append(isStream);
	strBuf.append("</isStream>\n");

	strBuf.append("<isBase>");
	strBuf.append(isBase);
	strBuf.append("</isBase>\n");

	for(int i=0; i<getNumAttrs(); i++) {
	    	strBuf.append("<Attr>\n");

	    	strBuf.append("<Name>");
		strBuf.append(getAttrName(i));
		strBuf.append("</Name>\n");

	    	strBuf.append("<Type>");
		strBuf.append(getAttrType(i));
		strBuf.append("</Type>\n");		

	    	strBuf.append("<Len>");
		strBuf.append(getAttrLen(i));
		strBuf.append("</Len>\n");		
		
		strBuf.append("</Attr>\n");
	}

	strBuf.append("</Table>\n");

	return strBuf.toString();
    } 

    //------------------------------------------
    //generate Table from part of script xml
    //------------------------------------------
    public static NamedTable genTableFromScript(Node node) 
	throws FatalException {

	NodeList childs = node.getChildNodes ();
	String tableName="", bVal;
	boolean isStream=false, isBase=false;
	List attrList = new ArrayList();

	for (int n = 0 ; n < childs.getLength () ; n++) {
	    Node child = childs.item (n);
	    
	    if (child.getNodeType () != Node.ELEMENT_NODE)
		continue;
	    
	    if (child.getNodeName().equals ("Name")) {
		tableName = XmlHelper.getText (child);
		continue;
	    }
	    
	    if (child.getNodeName().equals ("isStream")) {
		bVal = XmlHelper.getText (child);
		if(bVal.equals("true")) isStream=true;
		else isStream = false;
		continue;
	    }
	    
	    if (child.getNodeName().equals ("isBase")) {
		bVal = XmlHelper.getText (child);
		if(bVal.equals("true")) isBase=true;
		else isBase = false;
		continue;
	    }
	    
	    if (child.getNodeName().equals ("Attr")) {
		Attr attr = generateAttr(child);
		attrList.add(attr);
		continue;
	    }
	}

	NamedTable table = new NamedTable(tableName, isStream, isBase);
	    
	for(int i=0; i<attrList.size(); i++) 
	    table.addAttr((Attr)attrList.get(i));

	return table;
    }

    private static Attr generateAttr(Node node) 
	throws FatalException {
	NodeList children = node.getChildNodes ();
	String attrName="";
	int type=0, len=0;		    
		    
	for (int j = 0 ; j < children.getLength () ; j++) {
	    Node child = children.item (j);
		
	    if (child.getNodeType () != Node.ELEMENT_NODE)
		continue;
	    
	    if (child.getNodeName().equals ("Name")) {
		attrName = XmlHelper.getText (child);
		continue;
	    }
	    
	    if (child.getNodeName().equals ("Type")) {
		type = Integer.parseInt(XmlHelper.getText (child));
		continue;
	    }

	    if (child.getNodeName().equals ("Len")) {
		len = Integer.parseInt(XmlHelper.getText (child));
		continue;
	    }
	}
	    
	Attr attr = new Attr(attrName, type, len);
	return attr;
    }

    //------------------------
    // inner class Attr
    //------------------------
    private static class Attr {
	public String name;
	public int type;
	public int len;
	
	public Attr (String name, int type) {
	    this (name, type, 0);
	}
	
	public Attr (String name, int type, int len) {
	    this.name = name;
	    this.type = type;
	    this.len  = len;
	}
	
	/**
	 * Encode the attribute to a string.
	 */
	public String toString () {
	    StringBuffer encoding = new StringBuffer (name);
	    
	    // white space
	    encoding = encoding.append (" ");
	    
	    if (type == Types.INTEGER) 
		encoding = encoding.append ("int");
	    
	    else if (type == Types.FLOAT)
		encoding = encoding.append ("float");
	    
	    else if (type == Types.CHAR) {
		encoding = encoding.append ("char(");
		encoding = encoding.append (len);
		encoding = encoding.append (')');
	    }
	    
	    else if (type == Types.BYTE)
		encoding = encoding.append ("byte");
	    return encoding.toString();
	}
    }    
}
