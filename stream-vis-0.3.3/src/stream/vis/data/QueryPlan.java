package stream.vis.data;

import java.util.*;
import java.io.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import stream.vis.util.FatalException;

public class QueryPlan {
    /// Root element name in an XML plan
    private static final String PLAN = "plan";
    
    /// Name for an operator node in an XML plan
    private static final String OPERATOR = "operator";
    
    /// Name for an identifier attribute
    private static final String ID = "id";
    
    /// 
    private static final String STREAM = "stream";
    
    /// Name for an input element
    private static final String INPUT = "input";
    
    /// Name for an "name" element
    private static final String NAME = "name";
    
    /// Name for the queue attribute 
    private static final String QUEUE = "queue";
    
    /// Name for the store attribute
    private static final String STORE = "store";
    
    /// Owning operator of a synopsis/store element
    private static final String OWNER = "owner";
    
    /// name for a synopsis element
    private static final String SYNOPSIS = "synopsis";
    
    /// Source store of a synopsis
    private static final String SOURCE = "source";
    
    /// Pos of a synopsis
    private static final String POS = "pos";
    
    /// Long name of an operator
    private static final String LONGNAME = "lname";
    
    /// Query Id for an output operator
    private static final String QUERY = "query";
    
    /// Property of a component
    private static final String PROPERTY = "property";
    
    private static final String PROP_NAME = "name";
    
    /// Value 
    private static final String VALUE = "value";
    
    /// Operators in the query plan
    List ops;
    
    /// Queues in the query plan
    List queues;
    
    /// Stores in the query plan
    List stores;
    
    /// Synopses in the query plan
    List syns;
    
    /// Mapping from Query Id to operator that produces it
    Map queryOutputMap;
    
    /// Build a query plan given its XML representation
    public QueryPlan (Document xmlPlan) throws FatalException {
	
	// Lists containing temporary representation of operators, stores
	// and synopses
	ArrayList oplist = new ArrayList ();
	ArrayList storeList = new ArrayList ();
	ArrayList synList = new ArrayList ();
	
	// Document Sanity check 
	Element root = xmlPlan.getDocumentElement ();
	if (!root.getNodeName().equals (PLAN)) {
	    throw new FatalException ("Corrupted plan?");
	}
	
	// Parse the document and generate the operator and store info
	parseDocument (root, oplist, storeList, synList);
	
	// Mapping from (server) id of operators to their posn in oplist
	// which is used as a client side id
	Map opIdMap = generateOpIdMap (oplist);
	
	// Mapping from (server) id of stores to client side id
	Map storeIdMap = generateStoreIdMap (storeList);
	
	// Generate the Operator objects
	generateOps (oplist, opIdMap);
	
	// Generate the queues
	generateQueues (oplist, opIdMap);
	
	// Generate stores
	generateStores (storeList, storeIdMap, opIdMap);
	
	// Generate the synopses
	generateSynopses (synList, storeIdMap, opIdMap);
	
	// Generate the mapping from queries to output ops
	queryOutputMap = new HashMap ();
	generateQueryOutputMap (oplist, queryOutputMap);
    }
    
    public QueryPlan (String planString) throws FatalException {
	this (QueryPlan.getDocument(planString));  		
    }
    
    
    public void updatePlan (String newPlanStr) throws FatalException {
	
	Document newXMLPlan;
	
	// get the xml document
	newXMLPlan = QueryPlan.getDocument (newPlanStr);
	
	// Lists containing temporary representation of operators, stores
	// and synopses
	ArrayList oplist = new ArrayList ();
	ArrayList storeList = new ArrayList ();
	ArrayList synList = new ArrayList ();
	
	// Document Sanity check 
	Element root = newXMLPlan.getDocumentElement ();
	if (!root.getNodeName().equals (PLAN)) {
	    throw new FatalException ("Corrupted plan?");
	}
	
	// Parse the document and generate the operator and store info
	parseDocument (root, oplist, storeList, synList);
	
	// min id of a new operator
	int minNewOpId = ops.size();
	
	// Mapping from server id to client id of operators
	Map opIdMap = updateOpIdMap (oplist);
	
	// Mapping from server id to client id of stores
	Map storeIdMap = updateStoreIdMap (storeList);
	
	// create new entities
	addNewOps (oplist, opIdMap);
	addNewQueues (oplist, opIdMap, minNewOpId);
	addNewStores (storeList, storeIdMap, opIdMap);
	addNewSynopses (synList, storeIdMap, opIdMap, minNewOpId);
	
	// update the query -> output id mapping
	updateQueryOutputMap (oplist, queryOutputMap, opIdMap);
    }
    
    public int getNumOps() {
	return ops.size();
    }
    
    public Operator getOp (int id) {
	return (Operator)ops.get(id);
    }
    
    public int getNumQueues () {
	return queues.size();
    }
    
    public Queue getQueue (int id) {
	return (Queue)queues.get(id);
    }
    
    public int getNumStores () {
	return stores.size();
    }
    
    public Store getStore (int id) {
	return (Store)stores.get(id);
    }
    
    public int getNumSyns () {
	return syns.size();
    }
    
    public Synopsis getSyn (int id) {
	return (Synopsis)syns.get(id);
    }
    
    public int getOutputOp (int queryId) {	
	Integer opId = (Integer)queryOutputMap.get(new Integer(queryId));
	return opId.intValue();
    }
    
    private void parseDocument (Element root, 
				List oplist,
				List storeList,
				List synList) throws FatalException {
	
	// Iterate through the nodes and retrieve operator/store/synopsis info
	NodeList nodeList = root.getChildNodes ();
	
	for (int n = 0 ; n < nodeList.getLength () ; n++) {	
	    Node curNode = nodeList.item (n);
	    
	    if (curNode.getNodeType() != Node.ELEMENT_NODE)
		continue;
	    
	    if (curNode.getNodeName().equals(OPERATOR)) {
		OpInfo opInfo = getOpInfo(nodeList.item(n));
		oplist.add (opInfo);
		continue;
	    }
	    
	    if (curNode.getNodeName().equals(STORE)) {		
		StoreInfo storeInfo = getStoreInfo (nodeList.item(n));
		storeList.add (storeInfo);
		continue;
	    }
	    
	    if (curNode.getNodeName().equals(SYNOPSIS)) {
		SynInfo synInfo = getSynInfo (nodeList.item(n));
		synList.add (synInfo);
		continue;
	    }
	}
    }
    
    // Generate the mapping from server id of the operators to 
    // the client id, which is just the index into oplist
    private Map generateOpIdMap (List oplist) {
	Map opIdMap = new HashMap ();
	for (int o = 0 ; o < oplist.size() ; o++) {
	    OpInfo curOp = (OpInfo)oplist.get(o);
	    opIdMap.put (curOp.serverId, new Integer (o));
	}
	return opIdMap;
    }   
    
    // Generate the mapping from server id to client id.  We want to client 
    // id to be the index in the ops list.  This method is called to update
    // a plan, so there might be previously created operators.
    private Map updateOpIdMap (List oplist) {
	Map opIdMap = new HashMap();

	// create the map for existing operators
	for (int o = 0 ; o < ops.size() ; o++) {
	    Operator op = (Operator)ops.get(o);
	    opIdMap.put (op.getServerId(), new Integer(o));
	}
	
	int opId = ops.size();
	
	// create the map for the new operators
	for (int o = 0 ; o < oplist.size (); o++) {
	    OpInfo opInfo = (OpInfo)oplist.get(o);
	    
	    // new operator
	    if (opIdMap.get(opInfo.serverId) == null) {
		opIdMap.put (opInfo.serverId, new Integer (opId));
		opId++;
	    }
	}
	
	return opIdMap;
    }    
    
    // Generate the mapping from server id of the stores to the 
    // client id of the stores which is the index into storeList
    private Map generateStoreIdMap (List storeList) {
	Map storeIdMap = new HashMap();
	for (int s = 0 ; s < storeList.size() ; s++) {
	    StoreInfo curStore = (StoreInfo)storeList.get(s);
	    storeIdMap.put (curStore.serverId, new Integer(s));
	}
	return storeIdMap;
    }
    
    // Generate the mapping from server id to client id.  We want to client 
    // id to be the index in the stores list.  This method is called to 
    // update a plan, so there might be previously created stores
    private Map updateStoreIdMap (List storeList) {
	Map storeIdMap = new HashMap ();
	
	// create the mapping for previously existant stores
	for (int s = 0 ; s < stores.size() ; s++) {
	    Store store = (Store)stores.get (s);
	    storeIdMap.put (store.getServerId(), new Integer (s));
	}
	
	int storeId = stores.size();
	
	// create the mappin gfor the new stores
	for (int s = 0 ; s < storeList.size() ; s++) {
	    StoreInfo storeInfo = (StoreInfo)storeList.get(s);
	    
	    if (storeIdMap.get (storeInfo.serverId) == null) {
		storeIdMap.put (storeInfo.serverId, new Integer (storeId));
		storeId ++;
	    }
	}
	
	return storeIdMap;
    }
    
    // Generate the operators
    private void generateOps (List oplist, Map opIdMap) {
	ops = new ArrayList ();	
	
	for (int o = 0 ; o < oplist.size() ; o++) {
	    OpInfo opInfo = (OpInfo)oplist.get(o);	    
	    
	    // Array of inputs
	    int[] opInputs = new int [opInfo.inputs.size()];	    
	    for (int i = 0 ; i < opInputs.length ; i++) {
		String childServerId = (String)opInfo.inputs.get(i);
		int childId = ((Integer)opIdMap.get(childServerId)).intValue();
		
		opInputs [i] = childId;
	    }
	    
	    assert (ops.size() == o);
	    
	    Operator op = new Operator (o, opInfo.serverId, opInfo.name, 
					opInfo.longName, opInputs, this,
					opInfo.outputsStream);
	    for (int p = 0 ; p < opInfo.properties.size() ; p++) {
		Property property = (Property)opInfo.properties.get(p);
		op.addProperty (property.name, property.value);
	    }
	    
	    ops.add(op);
	}
    }

    private void addNewOps (ArrayList oplist, Map opIdMap) {
	
	for (int o = 0 ; o < oplist.size (); o++) {
	    OpInfo opInfo = (OpInfo) oplist.get (o);
	    
	    // Client side of the operator
	    int opId = ((Integer)opIdMap.get(opInfo.serverId)).intValue();
	    
	    // Is this a previously created operator
	    if (opId < ops.size())
		continue;
	    
	    // The way we generated opids this is guaranteed to hold
	    assert (opId == ops.size());
	    
	    // Array of inputs
	    int[] opInputs = new int [opInfo.inputs.size()];	    
	    for (int i = 0 ; i < opInputs.length ; i++) {
		String childServerId = (String)opInfo.inputs.get(i);
		int childId = ((Integer)opIdMap.get(childServerId)).intValue();
		opInputs [i] = childId;
	    }
	    
	    // create the operator
	    Operator op = new Operator (opId, opInfo.serverId, opInfo.name, 
					opInfo.longName, opInputs, this,
					opInfo.outputsStream);
	    
	    for (int p = 0 ; p < opInfo.properties.size() ; p++) {
		Property property = (Property)opInfo.properties.get(p);
		op.addProperty (property.name, property.value);
	    }
	    
	    ops.add(op);
	}
    }
    
    private void generateQueues (ArrayList oplist, Map opIdMap) {
	
	queues = new ArrayList ();
	
	for (int o = 0 ; o < oplist.size(); o++) {
	    OpInfo opInfo = (OpInfo)oplist.get(o);
	    
	    for (int c = 0 ; c < opInfo.inQueues.size() ; c++) {
		String srcServerId = (String)opInfo.inputs.get(c);
		int srcId = ((Integer)opIdMap.get(srcServerId)).intValue();
		
		int qId = queues.size();
		Queue queue = new Queue (qId, (String)opInfo.inQueues.get(c),
					 srcId, o, this);
		
		queues.add(queue); 
	    }
	}
    }
    
    private void addNewQueues (ArrayList oplist, Map opIdMap, int minNewOpId) {
	
	for (int o = 0 ; o < oplist.size () ; o++) {
	    OpInfo opInfo = (OpInfo)oplist.get(o);
	    
	    int destOpId = ((Integer)opIdMap.get(opInfo.serverId)).intValue();
	    if (destOpId < minNewOpId)
		continue;
	    
	    for (int c = 0 ; c < opInfo.inQueues.size() ; c++) {
		String srcServerId = (String)opInfo.inputs.get(c);
		int srcId = ((Integer)opIdMap.get(srcServerId)).intValue();
		int qId = queues.size();
		Queue queue = new Queue (qId, 
					 (String)opInfo.inQueues.get(c),
					 srcId, 
					 destOpId, 
					 this);		
		queues.add(queue);
	    }
	}
    }
    
    void generateStores (List storeList, Map storeIdMap, Map opIdMap) {
	stores = new ArrayList ();
	
	for (int s = 0 ; s < storeList.size() ; s++) {
	    StoreInfo storeInfo = (StoreInfo) storeList.get(s);
	    
	    int ownOpId = ((Integer)opIdMap.get(storeInfo.ownOp)).intValue();
	    stores.add(new Store (s, storeInfo.serverId,
				  storeInfo.name, ownOpId, this));
	}
    }

    private void addNewStores (List storeList, Map storeIdMap, Map opIdMap) {
	
	for (int s = 0 ; s < storeList.size() ; s++) {
	    StoreInfo storeInfo = (StoreInfo) storeList.get(s);
	    
	    // id that we assigned to this store
	    int storeId = 
		((Integer)storeIdMap.get(storeInfo.serverId)).intValue();
	    
	    // old store
	    if (storeId < stores.size())
		continue;

	    // the way we generate storeId ensures this
	    assert(storeId == stores.size());
	    
	    // the operator who owns this store
	    int ownOpId = ((Integer)opIdMap.get(storeInfo.ownOp)).intValue();

	    Store store = new Store (storeId, storeInfo.serverId, 
				     storeInfo.name, ownOpId, this);
	    stores.add (store);
	}
    }    
    
    private void generateSynopses (List synList, Map storeIdMap, Map opIdMap) 
	throws FatalException {
	
	syns = new ArrayList ();
	
	for (int s = 0 ; s < synList.size() ; s++) {
	    
	    SynInfo synInfo = (SynInfo) synList.get(s);

	    int ownOpId = ((Integer)opIdMap.get(synInfo.ownOp)).intValue();
	    int srcStoreId =
		((Integer)storeIdMap.get(synInfo.source)).intValue();
	    int type = decodePos (synInfo.pos);
	    
	    syns.add(new Synopsis (s, synInfo.serverId, synInfo.name, 
				   ownOpId, type, srcStoreId, this));
	}
    }
    
    private void addNewSynopses (List synList, Map storeIdMap, 
				 Map opIdMap, int minNewOpId) 
	throws FatalException {
	
	for (int s = 0 ; s < synList.size () ; s++) {
	    
	    SynInfo synInfo = (SynInfo) synList.get(s);
	    
	    // owner op
	    int ownOpId = ((Integer)opIdMap.get(synInfo.ownOp)).intValue();
	    
	    // if the owner op is old, the synopsis is old
	    if (ownOpId < minNewOpId)
		continue;
	    
	    // source store
	    int srcStoreId =
		((Integer)storeIdMap.get(synInfo.source)).intValue();
	    int type = decodePos (synInfo.pos);
	    
	    int synId = syns.size();
	    
	    syns.add(new Synopsis (synId, synInfo.serverId, synInfo.name, 
				   ownOpId, type, srcStoreId, this));
	}
    }
    
    private void generateQueryOutputMap (List oplist, Map queryOutputMap) 
	throws FatalException {
	
	try {
	    for (int o = 0 ; o < oplist.size() ; o++) {
		OpInfo opInfo = (OpInfo)oplist.get(o);
		if (opInfo.queryId != null) {
		    queryOutputMap.put (new Integer (opInfo.queryId),
					new Integer (o));
		}
	    }
	}
	catch (NumberFormatException e) {
	    throw new FatalException ("Invalid query plan");
	}
    }
    
    private void updateQueryOutputMap (List oplist, Map queryOutputMap, 
				       Map opIdMap) 
	throws FatalException {
	
	try {
	    for (int o = 0 ; o < oplist.size () ; o++) {
		OpInfo opInfo = (OpInfo) oplist.get (o);

		if (opInfo.queryId != null) {
		    Integer queryId = new Integer (opInfo.queryId);
		
		    if (queryOutputMap.get (queryId) == null) {		    
			queryOutputMap.put (queryId, 
					    opIdMap.get(opInfo.serverId));
		    }		    
		}
	    }
	}
	catch (NumberFormatException e) {
	    throw new FatalException ("Invalid query plan");
	}
    }
    
    private int decodePos (String pos) throws FatalException {
	if (pos.equals ("left"))
	    return Synopsis.LEFT;
	if (pos.equals ("right"))
	    return Synopsis.RIGHT;
	if (pos.equals ("center"))
	    return Synopsis.CENTER;
	if (pos.equals ("output"))
	    return Synopsis.OUTPUT;
	throw new FatalException ("Invalid query plan");
    }
    
    // "Struct" with all the operator info
    private class OpInfo {
	
	/// Operator identifier
	public String serverId;
	
	/// Operator type
	public String name;
	
	/// Long name for the operator (for vis. purposes, we 
	/// use "name" for displaying operators, and longName
	/// as a tooltip.
	public String longName;
	
	/// If the operator in output operator (name == "output"), then
	/// queryId is the id of the query for which it is the output.  
	/// For non-output operators, queryId is null
	public String queryId;
	
	/// Id's of the input operators
	public ArrayList inputs;
	
	/// Queue ids from which each input is read
	public ArrayList inQueues;	

	/// List of "static" properties
	public List properties;

	/// does the operator produce a stream
	public boolean outputsStream;
    }
    
    // Parse an operator node into an opInfo struct
    private OpInfo getOpInfo (Node opNode) throws FatalException {
	OpInfo opInfo = new OpInfo();
	opInfo.inputs = new ArrayList ();
	opInfo.inQueues = new ArrayList ();
	opInfo.properties = new ArrayList ();
	// Id
	opInfo.serverId = 
	    opNode.getAttributes().getNamedItem(ID).getNodeValue();
	
	if (opInfo.serverId == null)
	    throw new FatalException ("Invalid query plan");

	// stream?
	String bstream = 
	    opNode.getAttributes().getNamedItem(STREAM).getNodeValue();
	if (bstream == null)
	    throw new FatalException ("Invalid query plan");
	if (bstream.equals("0")) {
	    opInfo.outputsStream = false;
	}
	else if (bstream.equals ("1")) {
	    opInfo.outputsStream = true;
	}
	else {
	    throw new FatalException ("Invalid query plan");
	}
	
	NodeList childs = opNode.getChildNodes ();
	for (int n = 0 ; n < childs.getLength () ; n++) {
	    Node child = childs.item (n);
	    
	    if (child.getNodeType () != Node.ELEMENT_NODE)
		continue;
	    
	    if (child.getNodeName().equals (NAME)) {
		opInfo.name = getText (child);
	    }
	    
	    else if (child.getNodeName().equals (INPUT)) {		
		opInfo.inputs.add(getText (child));
		
		String queueId = 
		    child.getAttributes().getNamedItem(QUEUE).getNodeValue();
		if (queueId == null)
		    throw new FatalException ("Invalid query plan");
		opInfo.inQueues.add (queueId);
	    }
	    else if (child.getNodeName().equals (LONGNAME)) {
		opInfo.longName = getText (child);
	    }
	    else if (child.getNodeName().equals (QUERY)) {
		opInfo.queryId = getText (child);
	    }    
	    else if (child.getNodeName().equals (PROPERTY)) {
		Property property = getProperty (child);
		opInfo.properties.add (property);
	    }
	}
	
	return opInfo;
    }
    
    // "Struct" with the store information
    private class StoreInfo {	
	/// Store identifier
	public String serverId;
	
	/// Store name
	public String name;
	
	/// Id of the owning op
	public String ownOp;
    }
    
    private StoreInfo getStoreInfo (Node node) throws FatalException {
	StoreInfo storeInfo = new StoreInfo ();
	
	// Id
	storeInfo.serverId = 
	    node.getAttributes().getNamedItem(ID).getNodeValue();
	
	if (storeInfo.serverId == null) {
	    throw new FatalException ("Invalid query plan");
	}
	
	NodeList childs = node.getChildNodes ();
	for (int n = 0 ; n < childs.getLength () ; n++) {
	    Node child = childs.item (n);
	    
	    if (child.getNodeType () != Node.ELEMENT_NODE)
		continue;
	    
	    if (child.getNodeName().equals (NAME)) {
		storeInfo.name = getText (child);
	    }
	    
	    if (child.getNodeName().equals (OWNER)) {
		storeInfo.ownOp = getText (child);
	    }
	}
	
	return storeInfo;
    }
    
    private class SynInfo {
	
	/// Synopsis identifier
	public String serverId;
	
	/// Synopsis name
	public String name;
	
	/// Id of the owning op
	public String ownOp;
	
	/// Id of the source store
	public String source;
	
	/// Position of the synopsis ("left", "right", "center", "output")
	public String pos;
    }    
    
    private SynInfo getSynInfo (Node node) throws FatalException {
	SynInfo synInfo = new SynInfo ();
	
	// Id
	synInfo.serverId =
	    node.getAttributes().getNamedItem(ID).getNodeValue();
	
	NodeList childs = node.getChildNodes ();
	for (int n = 0 ; n < childs.getLength () ; n++) {
	    Node child = childs.item (n);
	    
	    if (child.getNodeType () != Node.ELEMENT_NODE)
		continue;
	    
	    if (child.getNodeName().equals (NAME)) {
		synInfo.name = getText (child);
	    }
	    
	    if (child.getNodeName().equals (OWNER)) {
		synInfo.ownOp = getText (child);
	    }
	    
	    if (child.getNodeName().equals (SOURCE)) {
		synInfo.source = getText (child);
	    }
	    
	    if (child.getNodeName().equals (POS)) {
		synInfo.pos = getText (child);
	    }
	}
	
	return synInfo;
    }
    
    private class Property {
	String name;
	String value;
    }
    
    private Property getProperty (Node n) {
	Property property = new Property ();
	
	property.name = 
	    n.getAttributes().getNamedItem (PROP_NAME).getNodeValue();
	property.value =
	    n.getAttributes().getNamedItem (VALUE).getNodeValue ();
	
	return property;
    }
    
    private String getText (Node n) throws FatalException {
	Node child = n.getFirstChild ();
	
	// Has to be a text node
	if (!child.getNodeName().equals ("#text")) 
	    throw new FatalException ("Invalid query plan");
	
	return child.getNodeValue().trim();
    }
    
    private static Document getDocument (String xml) throws FatalException {
	Document document;
	
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
  	factory.setIgnoringComments(true);
  	factory.setIgnoringElementContentWhitespace(true);
	
	try {
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    document = builder.parse 
		(new ByteArrayInputStream (xml.getBytes()));
	}
	catch (ParserConfigurationException e) {
	    throw new FatalException ("Unknown exception");
	}
	catch (SAXException e) {
	    throw new FatalException ("Unknown exception");
	}
	catch (IOException e) {
	    throw new FatalException ("Unknown exception");
	}
	
	return document;
    }


    private static Document getDocumentOld (String filename) 
	throws FatalException {
	
	Document document;
	
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
  	factory.setIgnoringComments(true);
  	factory.setIgnoringElementContentWhitespace(true);
	
	try {
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    InputStream fileInput = new FileInputStream (filename);
	    document = builder.parse (new InputSource (fileInput));
	    return document;
	}
	
	catch (ParserConfigurationException e) {
	    throw new FatalException ("Unknown exception");
	}
	catch (FileNotFoundException e) {
	    throw new FatalException ("Plan file not found");
	}
	catch (SAXException e) {
	    throw new FatalException ("Unknown exception");
	}
	catch (IOException e) {
	    throw new FatalException ("Plan file i/o exception");
	}
    }
        
    public static void mainTest (String[] args) {
	
	try {	    
	    String filename = args[0];	    
	    Document document = getDocumentOld (filename);
	    QueryPlan plan = new QueryPlan (document);
	}
	
	catch (ArrayIndexOutOfBoundsException e) {
	    System.out.println ("Invalid usage");
	}
	catch (FatalException e) {
	    System.out.println ("Fatal exception:" + e);
	}
    }
}
