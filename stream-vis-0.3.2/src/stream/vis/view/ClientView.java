package stream.vis.view;

import stream.vis.data.*;
import stream.vis.view.dialog.*;
import stream.vis.view.input.*;
import stream.vis.view.result.StreamDisplay;
import stream.vis.view.result.RelationDisplay;
import stream.vis.view.menu.MenuBar;
import stream.vis.util.*;
import stream.vis.net.*;

import java.util.*;
import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.xml.sax.*;

public class ClientView extends JFrame 
    implements ActionListener, ClientListener {
    
    /// The client who we are visualizing
    private Client client;
    
    // Dimensions
    private int width;
    private int height;

    /// display positions
    private int feederViewStartX;
    private int feederViewStartY;
    private int queryResultStartX;
    private int queryResultStartY;
    
    private static final int FEEDERVIEW_XINC    = 220;
    private static final int FEEDERVIEW_YINC    = 0;
    private static final int QUERYRESULT_XINC   = 60;
    private static final int QUERYRESULT_YINC   = 30;    
    private static final int FEEDERVIEW_XINIT   = 0;
    private static final int FEEDERVIEW_YINIT   = 0;
    private static final int QUERYRESULT_XINIT  = 0;
    private static final int QUERYRESULT_YINIT  = 120;    
    
    private static final float LEGEND_WIDTH     = 0.3f;    
    private static final float LEGEND_HEIGHT    = 0.15f;    
    private static final float PLAN_PANE_WIDTH  = 0.6661f;    
    private static final float PLAN_PANE_HEIGHT = 1.0f;

    /// init manager
    public InitManager initMgr = new InitManager();

    //------------------------------------------------------------
    // (Main) Subcomponents of the visualizer
    //------------------------------------------------------------
    
    /// The pane in which the query plans are shown
    private JScrollPane mainPane;
    
    /// Pane showing the list of registered tables (displayed in mainPane)
    private NamedTableList tableList;
    
    /// Pane showing the list of queries (displayed in mainPane)
    private QueryList queryList;
    
    /// The list of plan views
    private List planViews;
    
    /// Mapping from query -> plans
    private Map qryToPlanView;
    
    /// menu bar
    private MenuBar menuBar;
    
    /// The entity observer pane where information about the selected
    /// components of a plan are shown
    EntityObserver entityObs;    
    
    //------------------------------------------------------------
    // The buttons for various commands
    //------------------------------------------------------------
    
    /// Register a stream
    JButton regStreamButton;
    
    /// Register a relation
    JButton regRelationButton;
    
    /// Register a query
    JButton regQueryButton;
    
    /// Generate plan
    JButton genPlanButton;
    
    /// Execute
    JButton runButton;
    
    /// Reset
    JButton resetButton;
    
    //------------------------------------------------------------
    // The dialogs 
    //------------------------------------------------------------
    
    RegisterInput regStreamDialog;
    
    RegisterInput regRelationDialog;
    
    RegisterQuery regQueryDialog;
    
    //------------------------------------------------------------
    // Stream/Relation output & input displays
    //------------------------------------------------------------
    
    /// List of displays showing an output stream
    List streamDisplays;
    
    /// List of displays showing an output relation
    List relnDisplays;
    
    /// List of tables
    List tables;

    /// mapping from tables to TableFeederViews
    Map  tableFeederViews;

    /// mapping from tables to TableBindingInfo
    Hashtable tableBindings;
    
    /// Mapping from query -> output display 
    Map queryToDisplay;
    
    //------------------------------------------------------------
    
    private static final String TITLE = "STREAM Visualizer";
    
    public ClientView (Client client) {
	super (TITLE);
	
	this.client = client;
	
	// determine the screen dimensions
	setSize ();
        feederViewStartX = FEEDERVIEW_XINIT;
        feederViewStartY = FEEDERVIEW_YINIT;
        queryResultStartX= QUERYRESULT_XINIT;
        queryResultStartY= QUERYRESULT_YINIT;
    
	// Action buttons lead the user through the sequence of 
	// actions -- registering streams/relations etc
	Component actionButtonPane = createActionButtons (client);
	
	// Entity observer provides information about an entity
	// (operator, queue, etc) when the entity is selected
	entityObs = new EntityObserver (this);
	
	// Legend pane is the legend of the figures used in the
	// query plan
	Component legendPane = createLegendPane();
	
	// Pack the above 3 panes into a single pane that occupies
	// the right portion of the visualizer
	Component rightPane = packRightPane (actionButtonPane, 
					     entityObs,
					     legendPane);
	
	// The main plan visualizer pane
	mainPane = createMainPane ();
	
	// The menu
	menuBar = new MenuBar (client, this);
	setJMenuBar (menuBar);
	
	// Putting it all together
	JSplitPane wholePane = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT,
					       true, mainPane, rightPane);
	
	// add quit on close listener
	addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    exit ();
		}
	    });
	
	// create the dialogs 
	createDialogs ();
	
	streamDisplays = new ArrayList ();
	relnDisplays = new ArrayList ();

	tables = new ArrayList();
	tableFeederViews = new HashMap ();
	tableBindings = new Hashtable ();

	queryToDisplay = new HashMap ();
	
	getContentPane().add (wholePane);
	pack ();
    }
    
    private void exit () {
	client.end ();
    }
    
    public void connect () {
	Connect connect = new Connect (this);
	boolean success = false;

	while(!success) {	
	    connect.setVisible (true);
	
	    String host = connect.getHost ();
	
	    // host == null is the flag that the user canceled he connection
	    if (host == null) {
		System.out.println("host is null");
		System.exit (0);
	    }
	    int port = connect.getPort ();
	    
	    try {
		client.setServerInfo (host, port);
		success = true;
	    }
	    catch (FatalException e) {
		JOptionPane.showMessageDialog (this, 
					       e.toString(), 
					       "Fail to connect to Server",
					       JOptionPane.ERROR_MESSAGE);
	    }
	}
    }
    
    public int getScreenWidth () {
	return width;
    }
    
    public int getScreenHeight () {
	return height;
    }
    
    public JScrollPane getMainPane () {
	return mainPane;
    }    
    
    public void regStream() throws FatalException {
	
	regStreamDialog.reset ();
	while (true) {
	    try {
		regStreamDialog.setVisible (true);
		
		NamedTable table = regStreamDialog.getInputTable ();
		if (table == null)
		    return;
		
		client.registerBaseTable (table);
		
		// break not reached if exception occurs
		break;
	    }
	    catch (NonFatalException e) {
		JOptionPane.showMessageDialog (this, e.getMesg(), "Error",
					       JOptionPane.ERROR_MESSAGE);
		// goto while (true)
	    }
	}
    }
    
    public void regRelation() throws FatalException {
	regRelationDialog.reset ();
	
	while (true) {
	    try {
		regRelationDialog.setVisible (true);
	
		NamedTable table = regRelationDialog.getInputTable ();
		if (table == null)
		    return;
		
		client.registerBaseTable (table);
		
		break;
	    }
	    catch (NonFatalException e) {
		JOptionPane.showMessageDialog (this, e.getMesg(), "Error",
					       JOptionPane.ERROR_MESSAGE);
		// goto while (true)
	    }	    
	}
    }
    
    public void regQuery() throws FatalException {
	regQueryDialog.reset ();

	while (true) {
	    try {
		regQueryDialog.setVisible (true);
		
		String queryText = regQueryDialog.getQueryText ();	
		// queryText == null => user pressed cancel
		if (queryText == null)
		    return;	
	
		boolean isNamed = regQueryDialog.isNamed ();	
		boolean hasOutput = regQueryDialog.outputRequired ();       
		Query query = new Query (queryText, hasOutput, isNamed);
		
		client.registerQuery (query);
		break;
	    }
	    catch (NonFatalException e) {
		JOptionPane.showMessageDialog (this, e.getMesg(), "Error",
					       JOptionPane.ERROR_MESSAGE);
		// goto while (true)
	    }
	}
    }
    
    public void showTableList() {
	assert (tableList != null);
	
	mainPane.getViewport().setView(tableList);
	
	setTitle (TITLE + ": Registered streams/relations");
    }
    
    public void showQueryList() {
	assert (queryList != null);
	
	mainPane.getViewport().setView(queryList);
	
	setTitle (TITLE + ": Registered queries");
    }
    
    public void showPlan (Query qry) {
	assert (qry.hasOutput ());
	
	QueryPlanView planView = (QueryPlanView)qryToPlanView.get (qry);
	mainPane.getViewport().setView (planView);
	
	setTitle (TITLE + ": Query " + qry.getQueryId());
    }
    
    public void showDisplay (Query qry) {
	assert (qry.hasOutput ());
	
	Component display = (Component)queryToDisplay.get (qry);
	display.setVisible (true);
    }
    
    public void hideDisplay (Query qry) {	
	assert (qry.hasOutput ());
	
	Component display = (Component)queryToDisplay.get (qry);
	display.setVisible (false);	
    }
    
    //------------------------------------------------------------
    // GUI related
    //------------------------------------------------------------
    
    private void setSize () {
    	Toolkit t = Toolkit.getDefaultToolkit();
    	width = t.getScreenSize().width - 100;
    	height = t.getScreenSize().height - 150; 
    }
    
    /**
     * Buttons 
     */
    private JPanel createActionButtons (Client client) {	
	// The text indicating the role of the buttons	
	JLabel heading = new JLabel ("Commands");
	heading.setFont(heading.getFont().deriveFont(Font.BOLD));
	
	// Action buttons
	regStreamButton   = new JButton ("Register Stream");
	regRelationButton = new JButton ("Register Relation");
	regQueryButton    = new JButton ("Register Query");
	genPlanButton     = new JButton ("Generate Plan");
	runButton         = new JButton ("Run");
	resetButton       = new JButton ("Reset");
	
	// Set the action listener
	regStreamButton.addActionListener (this);
	regRelationButton.addActionListener (this);
	regQueryButton.addActionListener (this);
	genPlanButton.addActionListener (this);
	runButton.addActionListener (this);
	resetButton.addActionListener (this);
	
	JPanel buttonPanel = new JPanel (new GridLayout (3, 2, 5, 5));
	buttonPanel.add (regStreamButton);
	buttonPanel.add (genPlanButton);
	buttonPanel.add (regRelationButton);
	buttonPanel.add (runButton);
	buttonPanel.add (regQueryButton);
	buttonPanel.add (resetButton);	
	
	JPanel fullPanel = new JPanel (new BorderLayout (10,10));
	fullPanel.add (heading, BorderLayout.NORTH);
	fullPanel.add (buttonPanel, BorderLayout.CENTER);
	
	return fullPanel;
    }
    
    private Component createLegendPane() {
	JScrollPane legendPane = 
	    new JScrollPane (new LegendPane());
	legendPane.setPreferredSize 
	    (new Dimension ((int)(width * LEGEND_WIDTH),
			    (int)(height * LEGEND_HEIGHT)));
	return legendPane;
    }
    
    private JPanel packRightPane (Component actionButtonPane, 
				  Component entityObs, 
				  Component legendPane) {
	
	// Combine entityObs and legendPane using a split pane
	JSplitPane splitPane = new JSplitPane (JSplitPane.VERTICAL_SPLIT,
					       true, 
					       entityObs, 
					       legendPane);
	
	splitPane.setBorder(null);
	
	JPanel rightPane = new JPanel (new BorderLayout (10,10));
	rightPane.add (actionButtonPane, BorderLayout.NORTH);
	rightPane.add (splitPane, BorderLayout.CENTER);
	
	return rightPane;
    }    
    
    private JScrollPane createMainPane () {
	// An empty panel until we have a plan
	JPanel filler = new JPanel();	
	filler.setBackground(Color.white);
	
	JScrollPane scrollPane = new JScrollPane(filler);	
	scrollPane.setPreferredSize
	    (new Dimension((int)(width * PLAN_PANE_WIDTH),
			   (int)(height * PLAN_PANE_HEIGHT)));
	scrollPane.setVerticalScrollBarPolicy
	    (JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	scrollPane.setHorizontalScrollBarPolicy
	    (JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
	
	tableList = new NamedTableList(client, this);	
	queryList = new QueryList(client, this);
	planViews = new ArrayList ();
	qryToPlanView = new HashMap ();
	
	//queryList.setLayout(new BoxLayout(queryList, BoxLayout.PAGE_AXIS));
	
	return scrollPane;
    }    
    
    private void createDialogs () {
	regStreamDialog = new RegisterInput (this, true);
	regRelationDialog = new RegisterInput (this, false);
	regQueryDialog = new RegisterQuery (this);
    }
    
    //------------------------------------------------------------
    // Respond to user Action
    //------------------------------------------------------------
    
    public void actionPerformed (ActionEvent e) {
	Object source = e.getSource ();
	
	try {
	    if (source == regStreamButton) {
		regStream();
	    }
	    
	    else if (source == regRelationButton) {
		regRelation();
	    }
	    
	    else if (source == regQueryButton) {	    
		regQuery();
	    }
	    
	    else if (source == genPlanButton) {
		client.genPlan ();
	    }
	    
	    else if (source == runButton) {	    
		client.run ();
	    }
	    
	    else if (source == resetButton) {
		client.softReset ();
	    }
	}
	catch (FatalException f) {
	    JOptionPane.showMessageDialog (this, f.toString(), "Error",
					   JOptionPane.ERROR_MESSAGE);
	    System.exit (0);
	}
    }    
    
    
    //------------------------------------------------------------
    // ClientListener
    //------------------------------------------------------------

    /**
     * The state of the client changed from 'oldState' to 'newState'
     */
    public void stateChanged (int oldState, int newState) {
	updateButtonEnabling (newState);
    }
    
    /**
     * A new base table has been added.
     */
    public void baseTableAdded (NamedTable table) {
	//record the base table here
	tables.add (table);	
    }
    
    /**
     * A new query has been added
     */
    public void queryAdded (Query query, UnnamedTable unnamedTable) {	
	
	try {
	    // If the query is a named query, we want to get the 
	    // attirubute & output names of the query
	    if (query.isNamed() && client.getState() == Client.USERAPPSPEC) {
		AssignName assignName = 
		    new AssignName (this, unnamedTable);
		
		while (true) {
		    try {
			assignName.setVisible (true);
			
			NamedTable table = assignName.getNamedTable ();
			if (table == null) 
			    return; 
			
			client.registerView (query, table);
			break;
		    } 
		    catch (NonFatalException e) {
			JOptionPane.showMessageDialog 
			    (this, e.getMesg(), "Error",
			     JOptionPane.ERROR_MESSAGE);
			// goto while (true)			
		    }
		}
	    }
	}
	catch(FatalException e) {
	    JOptionPane.showMessageDialog (this, e.toString(), "Error",
					   JOptionPane.ERROR_MESSAGE);
	    System.exit(0);
	}
    }
    
    /**
     * A new view has been specified
     */
    public void viewAdded (Query query, NamedTable table) {
    }
    
    /**
     * A query result is available
     */
    public void queryResultAvailable (QueryResult result) {
	if (result.getSchema().isStream()) {
	    StreamDisplay sd = new StreamDisplay (result);
	    sd.setLocation(new Point(queryResultStartX,
				     queryResultStartY));
	    queryResultStartX += QUERYRESULT_XINC;
	    queryResultStartY += QUERYRESULT_YINC;
	    streamDisplays.add (sd);
	    sd.setVisible (true);
	    queryToDisplay.put (result.getQuery(), sd);
	}
	
	else {
	    RelationDisplay rd = new RelationDisplay (result);
	    rd.setLocation(new Point(queryResultStartX,
				     queryResultStartY));
	    queryResultStartX += QUERYRESULT_XINC;
	    queryResultStartY += QUERYRESULT_YINC;
	    relnDisplays.add (rd);
	    rd.setVisible (true);
	    queryToDisplay.put (result.getQuery(), rd);
	}
    }

    public void monitorAdded (Monitor mon, Query qry, QueryResult res, 
			      QueryPlan plan) {
	QueryPlanView planView = new QueryPlanView (qry, plan, 
						    entityObs, client, this);
	planViews.add (planView);
	qryToPlanView.put (qry, planView);	
    }
    
    /**
     * Need to create a new TableFeederView
     */
    public TableFeederView createAndShowTableFeederView (NamedTable table) {
	TableFeederView view = (TableFeederView)tableFeederViews.get(table);
	if ( view == null ) {
	    view = new TableFeederView (table, this);
	    view.setLocation(new Point(feederViewStartX,
				       feederViewStartY));
	    
	    tableFeederViews.put(table, view);
	    feederViewStartX += FEEDERVIEW_XINC;
	    feederViewStartY += FEEDERVIEW_YINC;
	}
	
	view.setVisible(true);
	return view;
    }
    
    /**
     * bind TableFeeder to a table
     */
    public TableFeeder bindTableFeeder (NamedTable table, String fileName,
					boolean bLoop, boolean bAppTs) {
	TableFeeder feeder = null;
	try {
	    feeder = client.createTableFeeder 
		(table, fileName, bLoop, bAppTs);
	}
	catch (FatalException e) {
	    JOptionPane.showMessageDialog (this, e.toString(), "Error",
					   JOptionPane.ERROR_MESSAGE);
	    System.exit(0);	    
	}
	
	TableBindingInfo tbi = new TableBindingInfo(table.getTableName(),
						    fileName, bLoop,
						    bAppTs);
	tableBindings.put(table, tbi);
	
	return feeder;
    }
    
    /**
     * unbind TableFeeder from table
     */
    public void unbindTableFeeder (NamedTable table) {
	try {
	    client.destroyTableFeeder (table);
	}
	catch (FatalException e) {
	    JOptionPane.showMessageDialog (this, e.toString(), "Error",
					   JOptionPane.ERROR_MESSAGE);
	    System.exit(0);	    
	}
    }

    /**
     * Encode table binding 
     */
    public String encodeTableBindings () {
	StringBuffer strBuf = new StringBuffer();

	Enumeration enumer = tableBindings.keys();
	while (enumer.hasMoreElements()) {
	    Object key = enumer.nextElement();
	    TableBindingInfo tbi = (TableBindingInfo) tableBindings.get(key);
	    strBuf.append("<Binding>\n");

	    strBuf.append("<TableName>");
	    strBuf.append(tbi.getTableName());
	    strBuf.append("</TableName>\n");
	    strBuf.append("<FileName>");
	    strBuf.append(tbi.getFileName());
	    strBuf.append("</FileName>\n");
	    strBuf.append("<bLoop>");
	    strBuf.append(tbi.getbLoop());
	    strBuf.append("</bLoop>\n");
	    strBuf.append("<bAppTs>");
	    strBuf.append(tbi.getbAppTs());
	    strBuf.append("</bAppTs>\n");

	    strBuf.append("</Binding>\n");
	}

	return strBuf.toString();
    }

    /* load table binding */
    public void loadBinding (Node node) {
	TableBindingInfo tbi = new TableBindingInfo();

	try {
	    tbi.genTableBindingInfo(node);
	} catch (Exception e) {
	    e.printStackTrace();	    
	}

	for (int t = 0 ; t < tables.size(); t++) { 
	    NamedTable table = (NamedTable)tables.get (t);
	    
	    if (table.getTableName().equals(tbi.getTableName())) {
	       
		TableFeederView tbv = createAndShowTableFeederView(table);
		tbv.bindTableFeeder(tbi.getFileName(),
				    tbi.getbLoop(),
				    tbi.getbAppTs());
		break;
	    }
	}	
    }

    /* load table binding */
    public void loadDemoBinding (Node node) {
	TableBindingInfo tbi = new TableBindingInfo();

	try {
	    tbi.genTableBindingInfo(node);
	} catch (Exception e) {
	    e.printStackTrace();	    
	}

	for (int t = 0 ; t < tables.size(); t++) { 
	    NamedTable table = (NamedTable)tables.get (t);
	    
	    if (table.getTableName().equals(tbi.getTableName())) {
	       
		TableFeederView tbv = createAndShowTableFeederView(table);
		String fileName = tbi.getFileName();
		fileName =InitManager.getDemoPath() + fileName;
		tbv.bindTableFeeder(fileName,
				    tbi.getbLoop(),
				    tbi.getbAppTs());
		break;
	    }
	}	
    }

    /* a convenient method to make demoMenu easier */
    public void loadRegistryFromFile(File file) {
	menuBar.getFileMenu().loadRegistryFromFile(file);
    }

    public void loadRegistryFromFile(InputStream in) {
	menuBar.getFileMenu().loadRegistryFromFile(in);
    }
    

    /**
     * Query plan has been generated. Generate a planView for each
     * output query in the plan
     */
    public void planGenerated (QueryPlan plan) {
	List queries = client.getRegisteredQueries ();
	
	for (int q = 0 ; q < queries.size() ; q++) {
	    Query qry = (Query)queries.get(q);
	    
	    if (qry.hasOutput ()) {
		QueryPlanView planView = new QueryPlanView 
		    (qry, plan, entityObs, client, this);
		planViews.add (planView);
		qryToPlanView.put (qry, planView);
	    }
	}	
    }
    
    public void resetEvent () {
	
	// reset the plan views - 
	for (int v = 0 ; v < planViews.size () ; v++) {
	    QueryPlanView planView = (QueryPlanView)planViews.get (v);
	    planView.reset ();
	}
	
	entityObs.clear();

	// reset the plan
	planViews.clear ();
	qryToPlanView.clear ();
	JPanel filler = new JPanel();	
	filler.setBackground(Color.white);	
	mainPane.getViewport().setView(filler);
	setTitle (TITLE);	
	
	// Close all the stream displays
	for (int s = 0 ; s < streamDisplays.size() ; s++) {
	    StreamDisplay sd = (StreamDisplay)streamDisplays.get(s);
	    sd.setVisible(false);
	}
	streamDisplays.clear ();
	
	// Close all relation displays
	for (int r = 0 ; r < relnDisplays.size() ; r++) {
	    RelationDisplay rd = (RelationDisplay)relnDisplays.get(r);
	    rd.setVisible (false);
	}
	relnDisplays.clear ();
	
	// Close all the table feeder views
	for (int t = 0 ; t < tables.size(); t++) { 
	    NamedTable table = (NamedTable)tables.get (t);
	    
	    if (!table.isBase()) 
		continue;
	    
	    TableFeederView tfv = (TableFeederView)tableFeederViews.get (table);
	    if(tfv != null) tfv.setVisible(false);
	}

	// clear current info about tables
	tables.clear ();
	tableFeederViews.clear ();
	tableBindings.clear ();

	// reset position info
        feederViewStartX = FEEDERVIEW_XINIT;
        feederViewStartY = FEEDERVIEW_YINIT;
        queryResultStartX= QUERYRESULT_XINIT;
        queryResultStartY= QUERYRESULT_YINIT;
     }
    
    public void closeEvent () {} 
    
    //------------------------------------------------------------
    // Action Buttons Enabling/disabling logic
    //------------------------------------------------------------
    
    private void disableAllButtons () {
	regStreamButton.setEnabled (false);
	regRelationButton.setEnabled (false);
	regQueryButton.setEnabled (false);
	genPlanButton.setEnabled (false);
	runButton.setEnabled (false);
	resetButton.setEnabled (false);	
    }
    
    private void updateButtonEnabling (int state) {
	disableAllButtons ();
	
	switch (state) {
	case Client.USERAPPSPEC:
	    regStreamButton.setEnabled (true);
	    regRelationButton.setEnabled (true);
	    regQueryButton.setEnabled (true);
	    genPlanButton.setEnabled (true);
	    resetButton.setEnabled (true);
	    break;
	    
	case Client.PLANGEN:
	    runButton.setEnabled (true);
	    resetButton.setEnabled (true);
	    break;
	    
	case Client.RUN:
	    resetButton.setEnabled (true);
	    break;
	    
	default:
	    break;
	}
    }    
    
    //Main testing
    private static void createAndShowGUI() {
	try {
	    UIManager.setLookAndFeel
		(UIManager.getSystemLookAndFeelClassName());
	    Client client = new Client ();
	    ClientView clientView = new ClientView (client);
	    client.addListener (clientView);
	    clientView.setVisible(true);
	    clientView.connect ();
	}
	catch (Exception e) {
	    System.out.println (e);
	}
    }
    
    public static void main(String[] args) {

	try {
	    InitManager.setupPaths();
	} 
	catch (IOException e) {
	    e.printStackTrace();
	    System.exit(0);
	}

	javax.swing.SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    createAndShowGUI();
		}
	    });
    }

    //private class to record table binding info
    private class TableBindingInfo {
	private String tableName;
	private String fileName;
	private boolean bLoop;
	private boolean bAppTs;

	public TableBindingInfo () { super(); }

	public TableBindingInfo (String tableName,
				 String fileName,
				 boolean bLoop,
				 boolean bAppTs) {
	    this.tableName = tableName;
	    this.fileName = fileName;
	    this.bLoop = bLoop;
	    this.bAppTs = bAppTs;
	}

	public String getTableName () {return tableName;}
	public String getFileName () {return fileName;}
	public boolean getbLoop () {return bLoop;}
	public boolean getbAppTs () {return bAppTs;}
	
	public void genTableBindingInfo (Node node) 
	throws FatalException {

	    NodeList childs = node.getChildNodes ();
	    String tableName="", fileName="", bVal;
	    boolean bLoop=false, bAppTs=false;

	    for (int n = 0 ; n < childs.getLength () ; n++) {
		Node child = childs.item (n);
	    
		if (child.getNodeType () != Node.ELEMENT_NODE)
		    continue;
	    
		if (child.getNodeName().equals ("TableName")) {
		    tableName = XmlHelper.getText (child);
		    continue;
		}

		if (child.getNodeName().equals ("FileName")) {
		    fileName = XmlHelper.getText (child);
		    continue;
		}
	    
		if (child.getNodeName().equals ("bLoop")) {
		    bVal = XmlHelper.getText (child);
		    if(bVal.equals("true")) bLoop=true;
		    else bLoop = false;
		    continue;
		}
	    
		if (child.getNodeName().equals ("bAppTs")) {
		    bVal = XmlHelper.getText (child);
		    if(bVal.equals("true")) bAppTs=true;
		    else bAppTs = false;
		    continue;
		}	    
	    }

	    this.tableName = tableName;
	    this.fileName = fileName;
	    this.bLoop = bLoop;
	    this.bAppTs = bAppTs;
	}
    }
}
