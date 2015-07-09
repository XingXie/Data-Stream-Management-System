package stream.vis.util;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import java.io.*;

// A pure helper class 
// for processing XML files
 
public class XmlHelper {
	
    public static String getText (Node n) throws FatalException {
	Node child = n.getFirstChild ();

	// Has to be a text node
	if (!child.getNodeName().equals ("#text")) 
	    throw new FatalException ("Invalid script");
	
	return child.getNodeValue().trim();
    }

    public static Document getDocument (File file) 
	throws FatalException {
	Document doc;
	
	try {
	    InputStream fileInput = new FileInputStream (file);
	    doc = getDocument(fileInput);
	    return doc;
	} 
	catch (FileNotFoundException e) {
	    throw new FatalException ("xml file not found");
	}
	catch (IOException e) {
	    throw new FatalException ("xml file i/o error");
	}
    }
    
    public static Document getDocument(InputStream fileInput) 
	throws FatalException {
	
	Document document;
	
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	factory.setIgnoringComments(true);
	factory.setIgnoringElementContentWhitespace(true);
	
	try {
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    document = builder.parse (new InputSource (fileInput));
	    return document;
	}
	
	catch (ParserConfigurationException e) {
	    throw new FatalException ("Unknown exception");
	}
	catch (SAXException e) {
	    throw new FatalException ("Unknown exception");
	}
	catch (IOException e) {
	    throw new FatalException ("xml file i/o exception");
	}
    }    

}
