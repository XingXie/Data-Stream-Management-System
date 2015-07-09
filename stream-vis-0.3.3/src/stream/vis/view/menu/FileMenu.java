package stream.vis.view.menu;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.util.List;

import stream.vis.data.*;
import stream.vis.util.*;
import stream.vis.view.*;

import java.io.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.xml.sax.*;

/**
 * File menubar contains load script, save script, and exit.
 */

public class FileMenu extends JMenu 
    implements ActionListener, ClientListener { 
    /// DSMS client
    Client client;
    
    /// the client visualizer of which we are the part
    ClientView clientView;
    
    JMenuItem loadRegistry;    
    JMenuItem saveRegistry;
    JMenuItem saveRegistryAs;
    JMenuItem exit;
    
    JFileChooser fc;
    
    // remember whether there is a currently opened file
    File curRegistryFile = null;
    
    // valid only if curRegistryFile is !null.
    boolean dirty = false;

    public FileMenu (Client client, ClientView clientView) {
	super ("File");
	
	this.client = client;
	this.clientView = clientView;
	
	loadRegistry   = new JMenuItem ("Load Registry");
	saveRegistry   = new JMenuItem ("Save Registry");
	saveRegistryAs = new JMenuItem ("Save Registry As");
	exit         = new JMenuItem ("Exit");
	
	add (loadRegistry);
	addSeparator ();	    
	add (saveRegistry);
	add (saveRegistryAs);
	addSeparator ();
	add (exit);
	
	loadRegistry.addActionListener (this);
	saveRegistry.addActionListener (this);
	saveRegistryAs.addActionListener (this);	    
	exit.addActionListener (this);
	
	setMnemonic (KeyEvent.VK_F);
	loadRegistry.setAccelerator 
	    (KeyStroke.getKeyStroke (KeyEvent.VK_O, ActionEvent.CTRL_MASK));
	saveRegistry.setAccelerator 
	    (KeyStroke.getKeyStroke (KeyEvent.VK_S, ActionEvent.CTRL_MASK));
	exit.setAccelerator 
	    (KeyStroke.getKeyStroke (KeyEvent.VK_Q, ActionEvent.CTRL_MASK));

	fc = new JFileChooser (InitManager.getScriptPath());

	client.addListener (this);
    }
    
    public void stateChanged (int oldState, int newState) {}
    public void baseTableAdded (NamedTable table) {
	dirty = true;
    }
    public void queryResultAvailable (QueryResult result) {}    
    public void planGenerated (QueryPlan plan) {}
    public void closeEvent () {}    
    public void resetEvent () {
	
    }
    public void queryAdded (Query query, UnnamedTable outSchema) {
	dirty = true;
    }
    public void viewAdded (Query query, NamedTable table) {}
    public void monitorAdded (Monitor mon, Query qry, 
			      QueryResult res, QueryPlan plan) {}
    
    public void actionPerformed(ActionEvent e) {
	if (!(e.getSource() instanceof JMenuItem))
	    return;
	
	JMenuItem source = (JMenuItem)e.getSource();
	
	if (source == loadRegistry) {
	    
	    if (dirty || curRegistryFile != null) {
		
		int option = 
		    JOptionPane.showConfirmDialog 
		    (clientView,
		     "Discard current registrations?",
		     "Warning",
		     JOptionPane.OK_CANCEL_OPTION);
		if (option != JOptionPane.OK_OPTION)
		    return;
		
		try {
		    client.reset ();
		}
		catch (FatalException f) {
		    f.printStackTrace ();
		}
	    }
	    
	    // prompt user to select the script file
	    int returnVal = fc.showOpenDialog (clientView);
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
		File file = fc.getSelectedFile();
		
		if(!file.canRead()) {
		    JOptionPane.showMessageDialog 
			(clientView, 
			 "Can't read from script file.", 
			 "Error", JOptionPane.ERROR_MESSAGE);
		    return;
		}
		
		//set the current opened script
		curRegistryFile = file;
		
		dirty = false;
		
		//read in script file and execute it
		loadRegistryFromFile(file);
	    
		JOptionPane.showMessageDialog (clientView, "Registry loaded", 
					       "Success",
					       JOptionPane.INFORMATION_MESSAGE);
	    }
	}
	
	else if (source == saveRegistry) {
	    //check if there exists a opened script
	    //if yes, save to that script
	    //if no,  prompt user enter a script name
	    if(curRegistryFile != null) 
		saveRegistryToFile(curRegistryFile);
	    else
		saveRegistryAs();
	}
	
	else if (source == saveRegistryAs) {
	    saveRegistryAs();
	}
	
	else if (source == exit) {	    
	    client.end();
	}
    }
    
    //-----------------------
    // save script
    //-----------------------
    private void saveRegistryAs() {
	int returnVal = fc.showSaveDialog (clientView);

	if (returnVal == JFileChooser.APPROVE_OPTION) {
	    File file = fc.getSelectedFile();
	    saveRegistryToFile(file);
	}
    }

    private void saveRegistryToFile(File file) {
	//check if the file exists
	if(file.exists()) {
	    int n = JOptionPane.showConfirmDialog
		(clientView,
		 "Overwrite " + file.getName() + "?",
		 "Please confirm overwrite",
		 JOptionPane.YES_NO_OPTION,
		 JOptionPane.QUESTION_MESSAGE);

	    //if user choose not to overwrite, just return
	    if(n!=0) return;
	}
	
	curRegistryFile = file;

	try {
	    FileWriter fw = new FileWriter(file);
	    fw.write(getRegistry());
	    fw.close();
	}
	catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private void saveRegistryToFileWithoutPrompt (File file) {
	try {
	    FileWriter fw = new FileWriter(file);
	    fw.write(getRegistry());
	    fw.close();
	}
	catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private String getRegistry() {
	StringBuffer strBuf = new StringBuffer();
	strBuf.append("<Script>\n");
	    
	List tables = client.getRegisteredTables();
	List queries= client.getRegisteredQueries();
	Iterator tableIter = tables.iterator();
	Iterator queryIter = queries.iterator();
	NamedTable table;
	Query query;

	while(tableIter.hasNext()) {
	    table = (NamedTable) tableIter.next();
	    
	    //if not base table, append query first
	    if(!table.isBase()) {
		boolean found = false;
		
		while(!found && queryIter.hasNext()) {
		    query = (Query) queryIter.next();
		    strBuf.append(query.toScript());
		    
		    if(query.isNamed() && (query.getNamedTable()==table))
			found = true;
		}
	    }
	    
	    //append table
	    strBuf.append(table.toScript());
	}
	
	//append all the remaining queries
	while(queryIter.hasNext()) {
	    query = (Query) queryIter.next();
	    strBuf.append(query.toScript());
	}

	//append all table bindings
	strBuf.append(clientView.encodeTableBindings());

	strBuf.append("</Script>\n");
	
	return strBuf.toString();
    }    
    
    //-----------------
    // load script
    //-----------------
    
    public void loadRegistryFromFile(File file) {
	try {
	    InputStream fileInput = new FileInputStream (file);
	    loadRegistryFromFile(fileInput);
	} 
	catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void loadRegistryFromFile(InputStream in) {
	try {
	    Document script = XmlHelper.getDocument(in);
	    
	    // Document Sanity check 
	    Element root = script.getDocumentElement ();
	    if (!root.getNodeName().equals ("Script")) {
		throw new FatalException ("Not a legal Registry file!");
	    }
	    
	    // Parse the document and generate the operator and store info
	    parseScript(root);
	    
	} catch(Exception e) {
	    e.printStackTrace();
	}
    }
    
    private void parseScript (Element root)
	throws FatalException, NonFatalException {
	
	// Iterate through the nodes and execute the commands
	NodeList nodeList = root.getChildNodes ();
	NamedTable table = null;
	Query query = null;
	    
	client.beginScriptSpec ();

	for (int n = 0 ; n < nodeList.getLength () ; n++) {	
	    Node curNode = nodeList.item (n);
	    
	    if (curNode.getNodeType() != Node.ELEMENT_NODE)
		continue;
	    
	    //if base table, just create and register
	    if (curNode.getNodeName().equals("Table")) {
		table = NamedTable.genTableFromScript(curNode);
		//register this table?
		//...
		if(table.isBase())
		    client.registerBaseTable(table);
		else {
		    assert(query!=null); //query should be set up already
		    client.registerView(query, table);
		}
		
		continue;
	    }
		
	    //if UnNamed query, just create and register
	    //if Named query, need to fetch the next
	    if (curNode.getNodeName().equals("Query")) {
		query = Query.genQueryFromScript(curNode);
		//register this query?
		//..
		client.registerQuery(query);		    
		
		continue;
	    }

	    //load table binding
	    if (curNode.getNodeName().equals("Binding")) {
		clientView.loadBinding(curNode);
	    }
	    
	    if (curNode.getNodeName().equals("DemoBinding")) {
		clientView.loadDemoBinding(curNode);
	    }


	}
	    
	client.endScriptSpec ();	    
    }
}
