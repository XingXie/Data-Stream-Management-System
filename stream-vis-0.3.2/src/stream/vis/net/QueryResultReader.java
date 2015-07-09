package stream.vis.net;

import java.net.*;
import java.io.*;
import stream.vis.data.*;
import stream.vis.util.FatalException;

public class QueryResultReader extends Thread {
    /// Socket
    Socket socket;

    /// Input stream reader
    BufferedReader in;
    
    /// the query result
    QueryResult result;
    
    /// The schema of the query result
    Schema schema;
    
    /// Has the reader been stopped by the client ?
    boolean bStopped;
    
    int numTuples = 0;
    
    public QueryResultReader (Socket socket, QueryResult result) 
	throws IOException {
	this.socket = socket;	
	this.in = new BufferedReader 
	    (new InputStreamReader (socket.getInputStream()),
	     1000000);
	
	this.result = result;
	this.schema = result.getSchema ();
	bStopped = false;
    }
    
    public void run () {
	String line;
	
	try {
	    while (!bStopped) {
		// Next tuple
		line = in.readLine ();
		
		// End of the stream
		if  (line == null)
		    break;
		
		// Parse the line to get the tuple
		result.insert (getTuple (line));
		numTuples++;
	    }	    
	    
	    // cleanup
	    in.close ();
	    socket.close ();	    
	}
	
	catch (IOException e) {
	    System.out.println (e.toString());	    
	}
	catch (FatalException e) {
	    System.out.println (e.toString());
	}
    }
    
    /**
     * Stop the execution of the reader thread - this is called from
     * within the Client (Swing thread)
     */
    
    public void stopReader () {	    
	bStopped = true;
    }
    
    private Tuple getTuple (String line) throws FatalException {
	Tuple tuple = new Tuple ();
	
	// The line is a comma separated list of attributes
	String[] attrs = line.split (",");
	
	// Sanity check
	if (attrs.length != schema.getNumAttrs () + 2)
	    throw new FatalException ("Corrupted query result");	
	
	// Get the timestamp field
	tuple.setTimestamp (getTimestamp (attrs [0]));
	
	// Get the sign
	tuple.setSign (getSign (attrs [1]));	
	
	// Construct the data attributes
	for (int a = 2 ; a < attrs.length ; a++)
	    tuple.add (getAttribute (attrs[a], schema.getAttrType (a-2)));
	
	return tuple;
    } 
    
    private Object getAttribute (String attrVal, int type) 
	throws FatalException{
	
	try {
	    if (type == Types.INTEGER)
		return new Integer (attrVal);
	    if (type == Types.FLOAT)
		return new Float (attrVal);
	    if (type == Types.BYTE)
		return new Character (attrVal.charAt (0));
	    // type == String
	    return attrVal;
	}
	catch (NumberFormatException e) {
	    throw new FatalException ("Corrupted query result");
	}
    }
    
    private int getTimestamp (String tstamp) throws FatalException {
	try {
	    return Integer.parseInt(tstamp);
	}
	catch (NumberFormatException e) {
	    throw new FatalException ("Corrupted query result");	    
	}
    }
    
    private int getSign (String s) throws FatalException {
	if (s.length() != 1)
	    throw new FatalException ("Corrupted query result");
	if (s.charAt (0) == '+')
	    return Tuple.PLUS;
	if (s.charAt (0) == '-')
	    return Tuple.MINUS;
	throw new FatalException ("Corrupted query result");
    }
}
