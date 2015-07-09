package stream.vis.util;

public class Error {

    /// Generic parse error
    private static final int PARSE_ERR = 101;
    
    /// Duplicate table name
    private static final int DUPLICATE_TABLE_ERR = 102;
    
    /// Duplicate attr. in the table
    private static final int DUPLICATE_ATTR_ERR = 103;
    
    /// Unknown table (non-registered table) in a query
    private static final int UNKNOWN_TABLE_ERR = 104;
    
    /// Unknown variable in query
    private static final int UNKNOWN_VAR_ERR = 105;
    
    /// Ambiguous attribute
    private static final int AMBIGUOUS_ATTR_ERR = 106;
    
    /// Unknown attribute
    private static final int UNKNOWN_ATTR_ERR = 107;
    
    /// Window over relation err
    private static final int WINDOW_OVER_REL_ERR = 108;
    
    /// Operations over incompatible types
    private static final int TYPE_ERR = 109;
    
    /// Schema mismatch in union
    private static final int SCHEMA_MISMATCH_ERR = 110;
    
    /// Error code
    private static final int AMBIGUOUS_TABLE_ERR = 111;

    private static final int knownErrors[] = {
	PARSE_ERR,
	DUPLICATE_TABLE_ERR,
	DUPLICATE_ATTR_ERR,
	UNKNOWN_TABLE_ERR,
	UNKNOWN_VAR_ERR,
	AMBIGUOUS_ATTR_ERR,
	UNKNOWN_ATTR_ERR,
	WINDOW_OVER_REL_ERR,
	TYPE_ERR,
	SCHEMA_MISMATCH_ERR,
	AMBIGUOUS_TABLE_ERR
    };
    
    private static final String errorMsg[] = {
	"Parse Error",
	"Duplicate Table Name",
	"Duplicate Attribute in Table",
	"Unknown Table in Query",
	"Unknown Variable in Query",
	"Ambiguous Attribute in Query",
	"Unknown Attribute in Query",
	"Window over a relation",
	"Expression involving incompatible types",
	"Schema mismatch in Union/Except",
	"Ambiguous stream/relation name in query"
    };    
    
    public static boolean isKnownError (int errorCode) {
	for (int e = 0 ; e < knownErrors.length ; e++)
	    if (knownErrors[e] == errorCode)
		return true;
	return false;
    }
    
    public static String getErrorMesg (int errorCode) {
	for (int e = 0 ; e < knownErrors.length ; e++)
	    if (knownErrors[e] == errorCode)
		return errorMsg[e];
	return null;
    }    
}
