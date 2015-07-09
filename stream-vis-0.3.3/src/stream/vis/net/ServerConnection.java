/**
 * 
 * Xing 2010.10.15: User and Pwd are added.
 */

package stream.vis.net;

import stream.vis.util.FatalException;
import java.io.*;
import java.net.*;

public class ServerConnection {
    
    /// Host to which we are connecting
    private String host;
    
    /// The port at which the STREAM server is listening
    private int port;    
   
    /// User
    private String user;
    
    /// Pwd
    private String pwd;
    
    
    //------------------------------------------------------------
    // Command Connection Related
    //------------------------------------------------------------
    
    /// Socket for the command connection
    Socket commandSocket;
    
    /// Input stream for command connection
    private DataInputStream  commandInStream;
    
    
    /// Output stream for command connection
    private DataOutputStream commandOutStream;        
    
    //------------------------------------------------------------
    // Protocol strings
    //------------------------------------------------------------
    private static final String COMMAND_CONN_ID       = "COMMAND_CONN"; 
    private static final String OUTPUT_CONN_ID        = "OUTPUT_CONN";
    private static final String INPUT_CONN_ID         = "INPUT_CONN";
    
    // Add anthentication Message 
    private static final String AUTH_USER_COMMAND     = "AUTH USER";
    private static final String AUTH_PWD_COMMAND     = "AUTH PWD";
    
    private static final String BEGIN_APP_COMMAND     = "BEGIN APP";
    private static final String REG_INPUT_COMMAND     = "REGISTER INPUT";
    private static final String REG_OUT_QUERY_COMMAND ="REGISTER OUTPUT QUERY";
    private static final String REG_QUERY_COMMAND     = "REGISTER QUERY";
    private static final String REG_VIEW_COMMAND      = "REGISTER NAME";
    private static final String END_APP_COMMAND       = "END APP";    
    private static final String GEN_PLAN_COMMAND      = "GENERATE PLAN";
    private static final String EXECUTE_COMMAND       = "EXECUTE";
    private static final String RESET_COMMAND         = "RESET";
    private static final String TERMINATE_COMMAND     = "TERMINATE";
    private static final String REG_MON_COMMAND       = "REGISTER MONITOR";
    
    public ServerConnection (String host, int port, String user, String pwd) throws FatalException {
	this.host = host;
	this.port = port;
	this.user = user;
	this.pwd = pwd;
	
	// Establish a command connection to send commands to the server
	commandSocket = establishCommandConnection ();
	
	// Set commandInStream and commandOutStream
	setupCommandStreams ();
	
	// Authentication for username
	AuthUserRet ret = new AuthUserRet ();
	try{
	sendMesg(AUTH_USER_COMMAND);
	sendMesg(user);
	ret.errorCode = getErrorCode ();
	}
	catch (IOException e) {
	    throw new FatalException ("IO Exception for AUTH");
	}
	//XING DIS
	if (ret.errorCode == 2)
		 throw new FatalException 
		    ("Connection Denied: The user level is not matching the server level");
	if (ret.errorCode != 0)
		 throw new FatalException 
		    ("The user is not existed");
    
	//Authentication for password
	AuthPwdRet ret2 = new AuthPwdRet ();
	try{
	sendMesg(AUTH_PWD_COMMAND);
	sendMesg(pwd);
	ret2.errorCode = getErrorCode ();
	 
	}
	catch (IOException e) {
	    throw new FatalException ("IO Exception for AUTH");
	}
	if (ret2.errorCode != 0)
		 throw new FatalException 
		    ("The password is not matched for the user");
	
    }
    
    public BeginAppRet beginApp () throws FatalException {
	BeginAppRet ret = new BeginAppRet ();
	
	try {
	    sendMesg (BEGIN_APP_COMMAND);
	    ret.errorCode = getErrorCode ();
	}
	catch (IOException e) {
	    throw new FatalException ("IO Exception");
	}
	
	return ret;
    }
    
    
    
    
    public RegInputRet registerInput (String regInputStr) 
	throws FatalException {	
	
	RegInputRet ret = new RegInputRet ();
	
	try {
	    sendMesg (REG_INPUT_COMMAND);
	    sendMesg (regInputStr);
	    ret.errorCode = getErrorCode ();

	    if (ret.errorCode == 0)
		ret.inputId = getIntMesg ();
	    
	    return ret;
	}
	catch (IOException e) {
	    throw new FatalException ("IO Exception");
	}
    }
    
    /** 
     * Register a query for which we want an output.
     */ 
    
    public RegQueryRet registerOutQuery (String query) 
	throws FatalException {
	
	RegQueryRet ret = new RegQueryRet ();
	
	try {
	    sendMesg (REG_OUT_QUERY_COMMAND);
	    sendMesg (query);
	    ret.errorCode = getErrorCode ();
	    
	    if (ret.errorCode == 0) {
		ret.queryId = getIntMesg ();
		ret.outputId = getIntMesg ();
		ret.schema = receiveMesg ();
	    }
	    
	    return ret;
	}
	catch (IOException e) {
	    throw new FatalException ("IO Exception");
	}
    }
    
    /**
     * Register a query for which we do not need an output (pure view)
     */
    
    public RegQueryRet registerQuery (String query) 
	throws FatalException {
	
	RegQueryRet ret = new RegQueryRet ();
	
	try {
	    sendMesg (REG_QUERY_COMMAND);
	    sendMesg (query);
	    ret.errorCode = getErrorCode ();
	    
	    if (ret.errorCode == 0) {
		ret.queryId = getIntMesg ();
		ret.schema = receiveMesg ();
	    }
	    
	    return ret;
	}
	
	catch (IOException e) {
	    throw new FatalException ("IO Exception");
	}
    }
    
    public RegMonRet registerMonitor (String monQuery) 
	throws FatalException {
	RegMonRet ret = new RegMonRet ();
	
	try {
	    sendMesg (REG_MON_COMMAND);
	    sendMesg (monQuery);
	    ret.errorCode = getErrorCode ();
	    
	    if (ret.errorCode == 0) {
		ret.monId = getIntMesg ();
		ret.outputId = getIntMesg ();
		ret.schema = receiveMesg ();
		ret.planString = receiveMesg();	  
	    }
	    
	    return ret;
	}
	catch (IOException e) {
	    throw new FatalException ("IO Exception");
	}
    }    
    
    public RegViewRet registerView (String regViewStr, int queryId) 
	throws FatalException {
	RegViewRet ret = new RegViewRet ();
	
	try {
	    sendMesg (REG_VIEW_COMMAND);
	    sendMesg (Integer.toString(queryId));
	    sendMesg (regViewStr);
	    
	    ret.errorCode = getErrorCode ();
	    return ret;
	}
	catch (IOException e) {
	    throw new FatalException ("IO Exception");
	}
    }
    
    public EndAppRet endApp () throws FatalException {
	EndAppRet ret = new EndAppRet ();
	
	try {
	    sendMesg (END_APP_COMMAND);
	    ret.errorCode = getErrorCode ();
	    return ret;
	}
	catch (IOException e) {
	    throw new FatalException ("IO Exception");
	}
    }
    
    public GenPlanRet genPlan () throws FatalException {
	GenPlanRet ret = new GenPlanRet ();
	
	try {
	    sendMesg (GEN_PLAN_COMMAND);
	    ret.errorCode = getErrorCode ();
	    ret.planString = receiveMesg();
	    return ret;
	}
	catch (IOException e) {
	    throw new FatalException ("IO Exception");
	}
    }
    
    public ExecRet execute () throws FatalException {
	ExecRet ret = new ExecRet ();
	
	try {
	    sendMesg (EXECUTE_COMMAND);
	    ret.errorCode = getErrorCode ();
	    return ret;
	}
	catch (IOException e) {
	    throw new FatalException ("IO Exception");
	}
    }
    
    public ResetRet reset () throws FatalException {
	ResetRet ret = new ResetRet ();
	
	try {
	    sendMesg (RESET_COMMAND);
	    ret.errorCode = getErrorCode ();
	    return ret;
	}
	catch (IOException e) {
	    throw new FatalException ("IO Exception");
	}
    }
    
    public TerminateRet terminate () throws FatalException {
	TerminateRet ret = new TerminateRet ();
	
	try {
	    sendMesg (TERMINATE_COMMAND);
	    ret.errorCode = getErrorCode ();
	    return ret;
	}
	catch (IOException e) {
	    throw new FatalException ("IO Exception");
	}
    }
    
    private void setupCommandStreams () throws FatalException {
	try {
	    commandInStream = new DataInputStream
		(new BufferedInputStream (commandSocket.getInputStream()));
	    commandOutStream = new DataOutputStream
		(new BufferedOutputStream (commandSocket.getOutputStream()));
	}
	
	catch (IOException e) {
	    throw new FatalException ("IO Exception");
	}
    }
    
    /** 
     * Establish a command connection (used to send commands to the server)
     */ 
    
    private Socket establishCommandConnection () throws FatalException {
	return establishConnection (COMMAND_CONN_ID);
    }
    
    /**
     * Establish an input connection (used to input streams/rels to the server)
     */
    public Socket establishInputConnection (int inputId) 
	throws FatalException {
	return establishConnection (INPUT_CONN_ID + inputId);
    }
    
    /**
     * Establish an output connection (used to get output from the server)
     */
    public Socket establishOutputConnection (int outputId) 
	throws FatalException {
	return establishConnection (OUTPUT_CONN_ID + outputId);
    }
    
    /**
     * Establish a new connection with the server.  The connection
     * could be a command connection, input connection, or an output
     * connection.  The user of this method encodes the type of the 
     * connection in the connection identifier "connId".  This method
     * just sends the connId to the server and checks if the error code
     * is zero.
     */
    
    private Socket establishConnection (String connId) throws FatalException {
	try {
	    Socket newSock = new Socket(host, port); 
	    //	    newSock.setSoTimeout (1000);

	    DataOutputStream out= new DataOutputStream
		(new BufferedOutputStream(newSock.getOutputStream()));
	    
	    DataInputStream in  = new DataInputStream
		(new BufferedInputStream(newSock.getInputStream()));
	    
	    sendMesg(out, connId);	
	    
	    int errorCode = getErrorCode (in);
	    if (errorCode != 0) {
		throw new FatalException 
		    ("Server returned error while connecting");
	    }
	    
	    return newSock;	    
	} 
	
	catch (UnknownHostException e) {
	    throw new FatalException 
		("IP address of server could not be determined");
	}
	
	catch (IOException e) {
	    throw new FatalException
		("IO Exception while connecting to the server");
	}
	
	catch (SecurityException e) {
	    throw new FatalException
		("Security Exception while connecting to the server");
	}
    }
    
    /**
     * Send a (text) message to the server - this is one of the communication
     * primitives.  The text message is encoded as (length of the msg) 
     * followed by the actual content.
     */ 
    
    private void sendMesg (DataOutputStream out, String content) 
	throws IOException{		
	
	byte[] contentBytes = content.getBytes("US-ASCII");	
	int msgLength = contentBytes.length + 1; 
	
	out.writeInt(msgLength); 	
	out.write(contentBytes); 
	out.write(0);
	out.flush();
    }
    
    /**
     * Send message on the command stream.
     */ 
    
    private void sendMesg (String content) 
	throws IOException{		
	
	byte[] contentBytes = content.getBytes("US-ASCII");	
	int msgLength = contentBytes.length + 1; 
	
	commandOutStream.writeInt(msgLength); 	
	commandOutStream.write(contentBytes); 
	commandOutStream.write(0);
	commandOutStream.flush();
    }
    
    /**
     * Receive a text message from the server - the dual of sendMesg()
     * The text message is encoded as length followed by the actual
     * content in bytes.
     */
    private String receiveMesg(DataInputStream in) throws IOException {
	// Length of the message
	int msgLen = in.readInt();
	
	// content
	byte[] buf = new byte[msgLen];
	in.readFully(buf, 0, msgLen);
	String data = new String(buf, 0, msgLen - 1, "US-ASCII");
	
	return data;
    }
    
    /**
     * Receive message from the command connection
     */    
    private String receiveMesg() throws IOException {
	// Length of the message
	int msgLen = commandInStream.readInt();
	
	// content
	byte[] buf = new byte[msgLen];
	commandInStream.readFully(buf, 0, msgLen);
	String data = new String(buf, 0, msgLen - 1, "US-ASCII");
	
	return data;
    }


    /**
     * An error code sent by the server is just a message whose 
     * content is an integer containing the error code.
     */
    private int getErrorCode (DataInputStream in) throws FatalException {
	try {
	    String errorCode = receiveMesg (in);	
	    return Integer.parseInt (errorCode);
	}
	catch (IOException e) {
	    throw new FatalException ("IO Exception");
	}
	catch (NumberFormatException e) {
	    throw new FatalException ("Protocol Violation");
	}
    }
    
    /**
     * Get error code on the command connection
     */    
    private int getErrorCode () throws FatalException {
	try {
	    String errorCode = receiveMesg ();	
	    return Integer.parseInt (errorCode);
	}
	catch (IOException e) {
	    throw new FatalException ("IO Exception");
	}
	catch (NumberFormatException e) {
	    throw new FatalException ("Protocol Violation");
	}
    }
    
    /**
     * Get an integer value from the command connection
     */ 
    private int getIntMesg () throws FatalException {
	try {
	    String intStr = receiveMesg ();	
	    return Integer.parseInt (intStr);
	}
	catch (IOException e) {
	    throw new FatalException ("IO Exception");
	}
	catch (NumberFormatException e) {
	    throw new FatalException ("Protocol Violation");
	}
    }
    
}
