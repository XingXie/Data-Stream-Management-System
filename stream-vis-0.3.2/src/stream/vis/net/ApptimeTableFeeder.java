package stream.vis.net;

import stream.vis.data.*;
import stream.vis.util.FatalException;
import java.util.*;
import java.io.*;
import java.net.*;

/**
 * A TableFeeder reads from a file and sends the contents of the 
 * file to an OutputStream.
 */

public class ApptimeTableFeeder implements TableFeeder {
    
    /// sleep time 1 second
    private static final int SLEEP_TIME=1000;
    
    /// Socket ...
    private Socket socket;
    
    /// Table source file
    private String fileName;
    
    /// The (base) table which we are streaming
    private NamedTable table;
    
    /// Input stream
    private BufferedReader in;
    
    /// Output stream
    private PrintWriter out;
    
    /// The rate (tuples/sec) at which we send tuple to server
    private int rate;
    
    /// State: START RUNNING (PAUSED.RUNNING)* END
    private int state;
    
    /// Number of tuples sent so far to the server
    private int numTuplesSent;  
    
    /// Total time (in msec) elapsed since start not counting paused times
    /// and the time since we reached RUNNING state last
    private long elapsedTime;
    
    /// If state == RUNNING, the time we entered this state (last)
    private long intervalStartTime;
    
    /// The ts of the last tuple / heartbeat we sent to the server
    private int lastOutputTs;
    
    /// Timer who schedules us
    private Timer timer;
    
    // states ...
    private static final int START = 0;
    private static final int RUNNING = 1;
    private static final int PAUSED = 2;
    private static final int END = 3;
    private static final int TERMINATED = 4;
    
    public ApptimeTableFeeder (NamedTable table, 
			       Socket socket, 
			       String fileName) throws IOException {
	
	this.socket = socket;
	this.table = table;
	this.out = new PrintWriter 
	    (new BufferedOutputStream(socket.getOutputStream()));
	
	this.fileName = fileName;
	File f = new File (fileName);
	in = new BufferedReader(new FileReader(f)); 
	state = START;
    }
    
    public void start () {
	assert (state == START);
	
	numTuplesSent = 0;
	elapsedTime = 0;
	
	state = RUNNING;
	intervalStartTime = System.currentTimeMillis();
	
	lastOutputTs = 0;
	
	sendSchema();
	
	timer = new Timer ();
	timer.schedule(new FeederTask(), 1, SLEEP_TIME);
    }
    
    public synchronized void terminate () {
	if (state == RUNNING || state == END || state == PAUSED)
	    timer.cancel ();
	state = TERMINATED;
	
	out.close ();
	try {
	    socket.close();
	}
	catch (IOException e) {
	    System.out.println ("TableFeeder: Error closing socket");
	}
    }
    
    public void setRate (int rate) {}
    
    public synchronized int getNumTuplesSent() {
	return numTuplesSent;
    }
    
    public synchronized void pause () {
	assert (state == RUNNING || state == END);
	
	if (state == RUNNING) {
	    state = PAUSED;
	    elapsedTime += System.currentTimeMillis() - intervalStartTime;
	}
    }
    
    public synchronized void unpause () {
	assert (state == PAUSED || state == END);
	
	if (state == END)
	    return;
	
	state = RUNNING;
	intervalStartTime = System.currentTimeMillis();
    }

    public boolean isAppTimestamped () {
	return true;
    }
    
    private synchronized void sendTuples () {
	assert (state == RUNNING || state == END || 
		state == PAUSED || state == TERMINATED);

	if (state == PAUSED || state == TERMINATED)
	    return;
	
	int curTs = getCurTs ();
	
	// No more tuples - but send a heartbeat if necessary
	if (state == END) {
	    if (curTs > lastOutputTs) {
		out.println (curTs);
		lastOutputTs = curTs;
	    }	    
	    out.flush ();
	}
	
	if (state == RUNNING) {
	    
	    while (true) {
		try {
		    String tuple = peepNextLine ();
		    
		    if (tuple == null) {
			state = END;
			break;
		    }
		    
		    int tupleTs = getTupleTs (tuple);
		    
		    if (tupleTs > curTs)
			break;
		    
		    tuple = readNextLine ();
		    out.println (tuple);
		    numTuplesSent++;
		    lastOutputTs = tupleTs;
		}
		catch (IOException e) {
		    System.out.println ("Error reading from file");
		    state = END;
		    break;
		}
	    }
	}

	// Send a heartbeat if possible
	if (curTs > lastOutputTs) {
	    out.println (curTs);
	    lastOutputTs = curTs;
	}

	out.flush();
    }
    
    //------------------------------------------------------------
    // Logic to do lookahead(1) reading of lines
    //------------------------------------------------------------
    
    private String lastLine = null;
    
    private String readNextLine () throws IOException {
	String nextLine;
	
	if (lastLine != null) {
	    nextLine = lastLine;
	    lastLine = null;
	}
	else {
	    nextLine = in.readLine();
	}
	
	return nextLine;
    }
    
    private String peepNextLine () throws IOException {
	if (lastLine != null)
	    return lastLine;
	lastLine = in.readLine();
	return lastLine;
    }

    private class FeederTask extends TimerTask {	
    	public void run() {
	    sendTuples ();
	}
    }
    
    private int getCurTs () {
	long totalElapsedTime = elapsedTime + 
	    System.currentTimeMillis() - intervalStartTime;
	return (int)(totalElapsedTime/1000);
    }
    
    private int getTupleTs (String tuple) {
	return Integer.parseInt(tuple.split(",")[0]);
    }

    private void sendSchema () {
	StringBuffer strBuf = new StringBuffer ();
	
	// timestamp column
	strBuf.append ('i');
	
	// sign column
	if (!table.isStream()) {
	    strBuf.append (",b");
	}
	
	for (int a = 0 ; a < table.getNumAttrs() ; a++) {
	    strBuf.append (',');
	    
	    switch (table.getAttrType (a)) {
	    case Types.INTEGER:
		strBuf.append ('i');
		break;

	    case Types.FLOAT:
		strBuf.append ('f');
		break;
		
	    case Types.CHAR:
		strBuf.append ('c');
		strBuf.append (table.getAttrLen(a));
		break;
		
	    case Types.BYTE:
		strBuf.append ('b');
		break;
		
	    default:
		assert (false);
		break;
	    }
	}
	
	String schema = strBuf.toString();	
	out.println (schema);
    }
}

    
