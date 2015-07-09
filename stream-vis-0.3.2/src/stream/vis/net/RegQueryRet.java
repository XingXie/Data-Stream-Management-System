package stream.vis.net;

/**
 * Struct containing the data that the server returns in response to 
 * a register query command.  The query might be a view or a query that
 * needs an output.
 */

public class RegQueryRet {
    /// The server assigned id for the query
    public int queryId;
    
    /// The output id (used to establish an output connection and get the
    /// query output)
    public int outputId;
    
    /// The error code
    public int errorCode;
    
    /// The schema of the output encoded in XML
    public String schema;
}
