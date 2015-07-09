package stream.vis.util;

public class NonFatalException extends Exception {
    private String mesg;
    public NonFatalException (String e) {
	super (e);
	this.mesg = e;
    }
    
    public String getMesg() {
	return mesg;
    }
}
