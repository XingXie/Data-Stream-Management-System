package stream.vis.net;

public class RegMonRet {
    /// server assigned id for the monitor
    public int monId;
    
    /// The output id (used to establish connection and get the output)
    public int outputId;
    
    /// The error code
    public int errorCode;
    
    /// The schema of the output encoded in XML
    public String schema;

    /// The string containing the modified plan
    public String planString;
}
