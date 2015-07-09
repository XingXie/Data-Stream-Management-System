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
    
    //Pramod
    public static  FileWriter fstream_pra = null;
    public static  BufferedWriter out_pra = null;
    
    public QueryResultReader (Socket socket, QueryResult result) 
	throws IOException {
	this.socket = socket;	
	this.in = new BufferedReader 
	    (new InputStreamReader (socket.getInputStream()),
	     1000000);
	
	this.result = result;
	this.schema = result.getSchema ();
	bStopped = false;
	
	//Pramod
	try{
		fstream_pra = new FileWriter("out.txt");
		out_pra = new BufferedWriter(fstream_pra);
	}
	catch(IOException ex){
		System.out.println (ex.toString());
		System.out.println ("Error Creating a file");
	}
	System.out.println ("###########################################QueryResultReader.java###########################################\n");
	
	
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
        if (line.trim().length() == 0) {
//            System.out.println("Line of len == 0");
            continue;
        } else {
            System.out.println("Line: " + line);
        }

        Tuple t;
        try {
            t = getTuple(line);
        } catch (FatalException e) {
            continue;
        }
        result.insert (t);
		numTuples++;
			if(numTuples==100000){
				try{
					out_pra.write("Total Number of tuples sent is "+numTuples+"\n");
					out_pra.write("End time is "+System.currentTimeMillis()+"\n");
				}catch(IOException ex){
					System.out.println (ex.toString());
					System.out.println ("Error Creating a file");
				}
			}
			if(numTuples==200000){
				try{
					out_pra.write("Total Number of tuples sent is "+numTuples+"\n");
					out_pra.write("End time is "+System.currentTimeMillis()+"\n");
				}catch(IOException ex){
					System.out.println (ex.toString());
					System.out.println ("Error Creating a file");
				}
			}
			if(numTuples==300000){
				try{
					out_pra.write("Total Number of tuples sent is "+numTuples+"\n");
					out_pra.write("End time is "+System.currentTimeMillis()+"\n");
				}catch(IOException ex){
					System.out.println (ex.toString());
					System.out.println ("Error Creating a file");
				}
			}
			if(numTuples==400000){
				try{
					out_pra.write("Total Number of tuples sent is "+numTuples+"\n");
					out_pra.write("End time is "+System.currentTimeMillis()+"\n");
				}catch(IOException ex){
					System.out.println (ex.toString());
					System.out.println ("Error Creating a file");
				}
			}
			if(numTuples==500000){
				try{
					out_pra.write("Total Number of tuples sent is "+numTuples+"\n");
					out_pra.write("End time is "+System.currentTimeMillis()+"\n");
				}catch(IOException ex){
					System.out.println (ex.toString());
					System.out.println ("Error Creating a file");
				}
			}
			if(numTuples==600000){
				try{
					out_pra.write("Total Number of tuples sent is "+numTuples+"\n");
					out_pra.write("End time is "+System.currentTimeMillis()+"\n");
				}catch(IOException ex){
					System.out.println (ex.toString());
					System.out.println ("Error Creating a file");
				}
			}
			if(numTuples==700000){
				try{
					out_pra.write("Total Number of tuples sent is "+numTuples+"\n");
					out_pra.write("End time is "+System.currentTimeMillis()+"\n");
				}catch(IOException ex){
					System.out.println (ex.toString());
					System.out.println ("Error Creating a file");
				}
			}
			if(numTuples==800000){
				try{
					out_pra.write("Total Number of tuples sent is "+numTuples+"\n");
					out_pra.write("End time is "+System.currentTimeMillis()+"\n");
				}catch(IOException ex){
					System.out.println (ex.toString());
					System.out.println ("Error Creating a file");
				}
			}
			if(numTuples==900000){
				try{
					out_pra.write("Total Number of tuples sent is "+numTuples+"\n");
					out_pra.write("End time is "+System.currentTimeMillis()+"\n");
				}catch(IOException ex){
					System.out.println (ex.toString());
					System.out.println ("Error Creating a file");
				}
			}
			if(numTuples==1000000){
				try{
					out_pra.write("Total Number of tuples sent is "+numTuples+"\n");
					out_pra.write("End time is "+System.currentTimeMillis()+"\n");
					out_pra.close();
				}catch(IOException ex){
					System.out.println (ex.toString());
					System.out.println ("Error Creating a file");
				}
			}
	    }	    
	    
	    // cleanup
	    in.close ();
	    socket.close ();	    
	}
	
	catch (IOException e) {
	    System.out.println (e.toString());	    
	}
//	catch (FatalException e) {
//	    System.out.println (e.toString());
//	}
    }
    
    /**
     * Stop the execution of the reader thread - this is called from
     * within the Client (Swing thread)
     */
    
    public void stopReader () {	    
		System.out.println ("###########################################PAUSE IN QUERY RESULT READER###########################################\n");
		try{
			out_pra.write("End time is "+System.currentTimeMillis()+"\n");
			out_pra.close();
		}catch(IOException ex){
			System.out.println (ex.toString());
			System.out.println ("Error Creating a file");
		}
		bStopped = true;
    }
    
    private Tuple getTuple (String line) throws FatalException {
	Tuple tuple = new Tuple ();
//    System.out.println("line: " + line);
	// The line is a comma separated list of attributes
	String[] attrs = line.split (",");

//    System.out.println("attrs.length: " + attrs.length + " :: " + (schema.getNumAttrs () + 2));
	// Sanity check
	if (attrs.length != schema.getNumAttrs () + 2) {
//        System.out.println("attrs.length != schema.getNumAttrs () + 2");
	    throw new FatalException ("Corrupted query result");	
    }
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
        System.out.println("1NumberFormatException");
        throw new FatalException ("Corrupted query result");
	}
    }
    
    private int getTimestamp (String tstamp) throws FatalException {
	try {
	    return Integer.parseInt(tstamp);
	}
	catch (NumberFormatException e) {
        System.out.println("2NumberFormatException");
        throw new FatalException ("Corrupted query result");
	}
    }
    
    private int getSign (String s) throws FatalException {
	if (s.length() != 1)  {
        System.out.println("s.length() != 1");
        throw new FatalException ("Corrupted query result");
    }
	if (s.charAt (0) == '+')
	    return Tuple.PLUS;
	if (s.charAt (0) == '-')
	    return Tuple.MINUS;
    System.out.println("getSign end");
    throw new FatalException ("Corrupted query result");
    }
}
