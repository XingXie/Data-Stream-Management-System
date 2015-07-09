package stream.vis.net;

import stream.vis.data.*;
import stream.vis.util.FatalException;
import java.util.*;
import java.io.*;
import java.net.*;

public class SystimeTableFeeder implements TableFeeder {
    
    /// sleep time 0.1 second
    private static final int SLEEP_TIME=100;
    
    /// Table source file
    private String fileName;

    /// The (base) table which we are streaming
    private NamedTable table;
    
    /// Are we looping the file
    private boolean isLooped;
    
    
    /// Socket ...
    private Socket socket;
    
    /// Input stream
    private BufferedReader in;
    
    /// Output stream
    private PrintWriter out;
    
    
    /// The rate at which we feed the tuples (tuples/sec)
    private int rate = 1;
    
    /// State: Sequence: START (RUNNING|PAUSED)* END TERMINATE. 
    /// Actually, RUNNING follows START and precedes END ...
    private int state;
    
    /// Number of tuples sent so far to the server
    private int numTuplesSent;
    
    /// If state == RUNNING, number of tuples sent since the last time 
    /// we entered RUNNING state or we changed the rate (whichever came
    /// later)
    private int  intervalNumTuplesSent;
    
    /// Time at which table input was started (start() function was called)
    private long startTime;
    
    /// If state == RUNNING, the time at which we last entered this state
    /// or changed rate (whichever is later)
    private long intervalStartTime;
    
    /// Timer to schedule us periodically
    private Timer timer;
        
    /// Timestamp of the last output tuple or heartbeat sent to server
    private int lastOutputTs;
    
    private static final int START = 0;
    private static final int RUNNING = 1;
    private static final int PAUSED = 2;
    private static final int END = 3;
    private static final int TERMINATED = 4;
    
    public SystimeTableFeeder (NamedTable table, 
			       Socket socket, 
			       String fileName,
			       boolean isLooped) throws IOException {
	
	this.socket = socket;
	this.table = table;
	this.isLooped = isLooped;
	
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
	startTime = System.currentTimeMillis();
	
	state = RUNNING;
	intervalStartTime = startTime;
	intervalNumTuplesSent = 0;
	
	lastOutputTs = 0;
	
	sendSchema();
	
	timer = new Timer ();
	timer.schedule(new FeederTask(), 1, SLEEP_TIME);
    }
    
    public int getNumTuplesSent() {
	return numTuplesSent;
    }
    
    public void pause () {
	assert (state == RUNNING || state == END);
	
	synchronized (this) {
	    if (state == RUNNING) {
		state = PAUSED;
	    }
	}
    }
    
    public void unpause () {
	assert (state == PAUSED || state == END);
	
	synchronized (this) {
	    if (state == END || state == TERMINATED)
		return;
	    
	    state = RUNNING;
	}
	
	intervalNumTuplesSent = 0;
	intervalStartTime = System.currentTimeMillis();
    }
    
    public void setRate(int newRate) {
	assert (state == START || state == RUNNING || 
		state == END || state == PAUSED);
	
	if (state == END)
	    return;
	
	if(rate != newRate) {
	    rate = newRate;
	    
	    if (state == RUNNING) {
		intervalStartTime = System.currentTimeMillis();
		intervalNumTuplesSent = 0;
	    }
	}
    }
    
    /// Terminate the sending of tuples
    public void terminate () {
	if (state == RUNNING || state == END || state == PAUSED)
	    timer.cancel ();
	
	synchronized (this) {
	    state = TERMINATED;
	}
	
	try {
	    socket.close();
	}
	catch (IOException e) {
	    System.out.println ("Error closing socket");
	}
    }
    
    private void sendTuples () {
	assert (state == RUNNING || state == PAUSED || state == END || 
		state == TERMINATED);

	if (state == TERMINATED)
	    return;
	
	// Number seconds since start
	int curTs = getCurTs();
	
	if (state == RUNNING) {
	    
	    int n = getNumTuples ();
	    
	    for (; n > 0; n--) {
		String line;
		
		try {
		    line = in.readLine ();		
		}
		
		catch (IOException e) {
		    System.out.println ("Error reading from " + fileName);
		    processEOF();
		    break;
		}
		
		// End of file
		if (line == null) {
		    processEOF ();
		    if (state == END)
			break;
		    
		    continue;
		}
		
		out.println (curTs + "," + line);
		
		numTuplesSent++;
		intervalNumTuplesSent++;
		lastOutputTs = curTs;
	    }	    
	}
	
	// Send a heartbeat if we have not sent any tuple with curTs - we
	// send a heartbeat even in PAUSED or END state
	if (curTs > lastOutputTs) {
	    out.println (curTs);
	    lastOutputTs = curTs;
	}
	
        out.flush();
    }
    
    private int getNumTuples () {
	long elapsedTimeMs = System.currentTimeMillis() - intervalStartTime;
	int desired = (int)
	    (elapsedTimeMs * rate / 1000 - intervalNumTuplesSent);
	
	// correct rate = rate / 10
	int max = (rate/10 + 1) * 12 / 10;
	
	if (desired > max)
	    return max;
	
	return desired;
    }   
    
    private int getCurTs() {
	return (int)((System.currentTimeMillis () - startTime)/1000);
    }
    
    private void processEOF () {
	try {
	    in.close ();
	}
	catch (IOException e) {
	    System.out.println ("Error closing " + fileName);
	}
	
	if (isLooped) {
	    try {
		in = new BufferedReader (new FileReader (fileName));
	    }
	    catch (FileNotFoundException e) {
		System.out.println ("Error opening " + fileName);
		synchronized (this) {
		    if (state != TERMINATED) {
			state = END;
		    }
		}
	    }
	}
	
	else {
	    synchronized (this) {
		if (state != TERMINATED)
		    state = END;
	    }
	}
    }
    
    private class FeederTask extends TimerTask {	
    	public void run() {
	    sendTuples ();
	}
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

    public boolean isAppTimestamped () {
	return false;
    }
}
