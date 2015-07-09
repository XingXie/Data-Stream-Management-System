package stream.vis.data;

import java.io.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import java.util.List;
import java.util.ArrayList;
import stream.vis.util.FatalException;

/**
 * An unnamed table is just like a named table but without the table name
 * and the attribute names.  When the client registers a query with
 * the server the server returns an unnamed table.
 */

public class UnnamedTable implements Schema {    
    /// Does the table represent a stream or a relation.
    private boolean isStream;
    
    /// List of attributes  
    List attrs;
    
    private static final String SCHEMA = "schema";
    
    private static final String TYPE = "type";
    
    private static final String COLUMN = "column";
    
    private static final String LEN = "len";
    
    private static final String INT_STR = "int";
    
    private static final String FLOAT_STR = "float";
    
    private static final String CHAR_STR = "char";
    
    private static final String BYTE_STR = "byte";
    
    private static final String STREAM = "stream";
    
    private static final String RELATION = "relation";
    
    UnnamedTable (String xmlStr) throws FatalException { 
	attrs = new ArrayList ();
	
	Document doc = getDocument (xmlStr);
	
	// Sanity check
	Element root = doc.getDocumentElement ();
	if (!root.getNodeName().equals (SCHEMA))
	    throw new FatalException ("Invalid schema");
	
	// Determine if it is a stream or a relation
	isStream = determineIsStream (root);
	
	NodeList nodeList = root.getChildNodes ();
	
	// Iterate over the children ...
	for (int n = 0 ; n < nodeList.getLength () ; n++) {
	    Node curNode = nodeList.item (n);

	    // ... Ignote non-element nodes
	    if (curNode.getNodeType () != Node.ELEMENT_NODE)
		continue;
	    
	    // ignore nodes that are not <Column ..>
	    if (!curNode.getNodeName().equals (COLUMN))
		continue;

	    attrs.add (getAttr (curNode));
	}
	
	if (attrs.size () == 0)
	    throw new FatalException ("Invalid schema");
    }
    
    public boolean isStream () {
	return isStream;
    }
    
    public int getNumAttrs () {
	return attrs.size ();
    }
    
    public int getAttrType (int index) {
	return ((Attr)attrs.get(index)).type;
    }
    
    public int getAttrLen (int index) {
	return ((Attr)attrs.get(index)).len;
    }    
    
    Document getDocument (String xml) throws FatalException {
	Document document;
	
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
  	factory.setIgnoringComments(true);
  	factory.setIgnoringElementContentWhitespace(true);
	
	try {
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    document = builder.parse 
		(new ByteArrayInputStream (xml.getBytes()));
	}
	catch (ParserConfigurationException e) {
	    throw new FatalException ("Unknown exception");
	}
	catch (SAXException e) {
	    throw new FatalException ("Unknown exception");
	}
	catch (IOException e) {
	    throw new FatalException ("Unknown exception");
	}

	return document;
    }
    
    boolean determineIsStream (Element root) throws FatalException {
	String type = root.getAttribute (TYPE);
	
	if (type == null)
	    throw new FatalException ("Invalid schema");
	if (type.equals (STREAM))
	    return true;
	if (type.equals (RELATION))
	    return false;
	throw new FatalException ("Invalid schema");	
    }
    
    Attr getAttr (Node n) throws FatalException {	
	Node typeAttr = n.getAttributes().getNamedItem(TYPE);
	
	if (typeAttr == null)
	    throw new FatalException ("Invalid schema");
	
	String typeStr = typeAttr.getNodeValue ();
	int type;
	
	if (typeStr.equals (INT_STR))
	    type = Types.INTEGER;
	else if (typeStr.equals (FLOAT_STR))
	    type = Types.FLOAT;
	else if (typeStr.equals (CHAR_STR))
	    type = Types.CHAR;
	else if (typeStr.equals (BYTE_STR))
	    type = Types.BYTE;
	else 
	    throw new FatalException ("Invalid schema");
	
	if (type == Types.CHAR) {
	    Node lenAttr = n.getAttributes().getNamedItem (LEN);
	    if (lenAttr == null) throw new FatalException ("Invalid schema");
	    
	    String lenStr = lenAttr.getNodeValue ();
	    int len;
	    try {
		len = Integer.parseInt (lenStr);
	    }
	    catch (NumberFormatException e) {
		throw new FatalException ("Invalid schema");
	    }
	    
	    return new Attr (Types.CHAR, len);
	}
	else {
	    return new Attr (type);
	}
    }
    
    private class Attr {
	/// Type of the attribute
	public int type;
	
	/// The length of the attribute
	public int len;
	
	public Attr (int type) {
	    this (type, 0);
	}
	
	public Attr (int type, int len) {
	    this.type = type;
	    this.len = len;
	}
    }
}
