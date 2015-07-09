package stream.vis.data;

import java.util.ArrayList;

public class QueryResult {   
    /// The query for which we are the result
    Query query;
    
    /// Only the last WINDOW tuples are remembered
    private static final int WINDOW = 1000;
    
    /// The current window of tuples.  We will use the array list as 
    /// a circular buffer - we will let grow until WINDOW and then 
    /// start using it as a circular buffer
    private ArrayList tuples;
    
    /// The index of the oldest tuple in the window.  Note that 'first'
    /// along with tuples.size() completely captures the state of the 
    /// circular buffer.
    private int first;
    
    /// The number of tuples inserted into the result - this could be
    /// more than what we have remembered
    private int numTuples;
    
    public QueryResult (Query q) {
	
	assert (q.hasOutput());	
	query = q;		
	tuples = new ArrayList ();
	first = 0;
	numTuples = 0;
    }
    
    public Query getQuery () {
	return query;
    }
    
    public Schema getSchema () {
	return query.getSchema ();
    }
    
    /**
     * Insert a new tuple into the query result.  This method is called
     * from a QueryResultReader thread, which is different from the thread
     * that uses the result for display - hence the synchronization
     */
    
    public synchronized void insert (Tuple tuple) {
	assert (first >= 0 && first < WINDOW);
	
	if (tuples.size () < WINDOW) {
	    tuples.add (tuple);	    
	}
	else {
	    tuples.set (first, tuple);
	    first++;
	    if (first == WINDOW)
		first = 0;
	}
	numTuples ++;
    }
    
    public int getNumTuples () {
	return numTuples;
    }
    
    public int getNumAvailableTuples () {
	if (numTuples > WINDOW)
	    return WINDOW;
	else
	    return numTuples;
    }
    
    public int getWindowSize () {
	return WINDOW;
    }
    
    /**
     * The the tuple at position 'pos' from the end of the stream
     * pos = 0 gets the newest tuple (if it exists).  Returns null
     * if no such tuple exists.
     *
     * We don't make this synchronized because the user code will do 
     * synchronization in a non-monitor fashion -- 
     */
    public Tuple get (int pos) {
	assert (tuples.size() <= WINDOW);
	assert (pos < tuples.size());
	
	if (tuples.size() < WINDOW)
	    return ((Tuple)tuples.get (tuples.size() - pos - 1));
	
	pos = first - pos - 1;
	if (pos < 0)
	    pos += WINDOW;
	
	if (pos >= tuples.size())
	    return null;
	
	return ((Tuple)(tuples.get (pos)));
    }
}
