package stream.vis.data;

import java.util.List;
import java.util.ArrayList;

/**
 * A signed, timestamped tuple.
 */

public class Tuple {    
    /// The timestamp of the tuple
    private int timestamp;
    
    /// The sign of the tuple
    private int sign;
    
    /// The list of attributes
    List attrs;
    
    /// PLUS sign constant
    public static final int PLUS = 0;
    
    /// MINUS sign constant
    public static final int MINUS = 1;
    
    public Tuple () {
	timestamp = -1; 
	sign = -1;
	attrs = new ArrayList ();
    }
    
    //------------------------------------------------------------
    // Set methods
    //------------------------------------------------------------
    
    public void setTimestamp (int tstamp) {
	assert (tstamp >= 0);
	
	timestamp = tstamp;
    }
    
    public void setSign (int s) {
	assert (s == PLUS || s == MINUS);	
	sign = s;
    }
    
    public void add (Object attr) {
	attrs.add (attr);
    }
    
    //------------------------------------------------------------
    // Get methods
    //------------------------------------------------------------
    
    public int getTimestamp () {
	return timestamp;
    }
    
    public int getSign () {
	return sign;
    }
    
    public Object getAttr (int pos) {
	return attrs.get(pos);
    } 
}
