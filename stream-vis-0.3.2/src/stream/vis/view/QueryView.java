package stream.vis.view;

import stream.vis.data.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class QueryView extends JPanel 
    implements ActionListener, ClientListener {
    
    Client client;

    /// Query which we are visualizing.
    Query query;
    
    /// Client view
    ClientView clientView;
    
    /// Button to show the plan for the query
    JButton planButton;
    
    /// 
    JButton outputButton;
    
    private static final String SHOW = "Show Output";
    private static final String HIDE = "Hide Output";
    
    public QueryView (Query query, ClientView clientView, Client client) {
	super ();
	
	this.query = query;
	this.clientView = clientView;
	this.client = client;
	
	// main components
	JPanel titlePane = getTitlePane ();
	JPanel textPane = getTextPane ();
	JPanel controlPane = getControlPane ();
	
	// pack all components
	setLayout (new BoxLayout (this, BoxLayout.PAGE_AXIS));
	add (titlePane);
	add (Box.createRigidArea (new Dimension (0, 15)));
	add (textPane);
	add (Box.createRigidArea (new Dimension (0, 15)));
	add (controlPane);	
	
	// a new query is added in plangen or run state only if the query
	// is a monitor query
	if (client.getState() == Client.PLANGEN || 
	    client.getState() == Client.RUN) {
	    planButton.setEnabled (true);	    
	}
	
	client.addListener (this);
    }
    
    private JPanel getTitlePane () {
	String titleString;
	
	titleString = "Query " + query.getQueryId() + ": ";
	if (query.isNamed()) {
	    titleString = titleString + query.getNamedTable();
	}
	JLabel title = new JLabel (titleString);
	title.setFont (title.getFont().deriveFont(Font.BOLD));
	
	JPanel titlePane = new JPanel ();
	titlePane.setLayout (new BoxLayout(titlePane, BoxLayout.LINE_AXIS));
	titlePane.add (Box.createRigidArea (new Dimension(15, 0)));
	titlePane.add (title);
	titlePane.add (Box.createHorizontalGlue());
	
	return titlePane;
    }  
    
    private JPanel getTextPane () {
	// Create a html representation of the query
	StringBuffer strBuf = new StringBuffer ();	
	String lines[] = query.getString().split("\n");
	
	strBuf.append ("<html>\n<kbd>\n");
	for (int l = 0 ; l < lines.length ; l++) {
	    strBuf.append (lines[l]);
	    if (l < lines.length - 1)
		strBuf.append ("<br>\n");
	}
	strBuf.append ("</kbd>\n</html>\n");
	
	String formattedQueryString = strBuf.toString ();
	
	JLabel queryText = new JLabel (formattedQueryString);
	
	JPanel textPane = new JPanel ();
	textPane.setLayout (new BoxLayout (textPane, BoxLayout.LINE_AXIS));
	textPane.add (Box.createRigidArea (new Dimension(15, 0)));
	textPane.add (Box.createRigidArea (new Dimension (15,0)));
	textPane.add (queryText);
	textPane.add (Box.createHorizontalGlue ());
	
	return textPane;
    }
    
    private JPanel getControlPane () {
	planButton = new JButton ("Plan");
	outputButton = new JButton (SHOW);	
	
	planButton.setEnabled (false);
	outputButton.setEnabled (false);
	
	JPanel controlPane = new JPanel ();
	controlPane.setLayout (new BoxLayout(controlPane,BoxLayout.LINE_AXIS));
	
	controlPane.add (Box.createRigidArea (new Dimension(15, 0)));
	controlPane.add (Box.createHorizontalGlue());
	controlPane.add (planButton);
	controlPane.add (Box.createRigidArea (new Dimension(15, 0)));
	controlPane.add (outputButton);	
	controlPane.add (Box.createRigidArea (new Dimension(15, 0)));
	
	planButton.addActionListener (this);
	outputButton.addActionListener (this);
	
	return controlPane;
    }
    
    public void actionPerformed (ActionEvent e) {
	Object src = e.getSource ();
	
	if (src == planButton) {
	    clientView.showPlan (query);
	}
	else if (src == outputButton) {
	    if (outputButton.getText().equals (SHOW)) {
		clientView.showDisplay (query);
		outputButton.setText(HIDE);
	    }
	    
	    else {
		clientView.hideDisplay (query);
		outputButton.setText(SHOW);
	    }
	}
    }
    
    public void stateChanged (int oldState, int newState) {
	
	if (newState == Client.PLANGEN) {
	    assert (!query.isMonitor());
	    
	    if (query.hasOutput ())
		planButton.setEnabled (true);
	    outputButton.setEnabled (false);
	}
	
	else if (newState == Client.RUN) {
	    if (query.hasOutput ()) {
		planButton.setEnabled (true); 
		
		if (!query.isMonitor ()) {
		    outputButton.setEnabled (true);
		    outputButton.setText (HIDE);
		}
	    }
	}
	
	else {
	    planButton.setEnabled (false);
	    outputButton.setEnabled (false);
	}
    } 
    
    public void baseTableAdded (NamedTable table) {}
    public void queryResultAvailable (QueryResult result) {}    
    public void planGenerated (QueryPlan plan) {}
    public void closeEvent () {}    
    public void resetEvent () {
	client.removeListener (this);
    }
    public void queryAdded (Query query, UnnamedTable outSchema) {}
    public void viewAdded (Query query, NamedTable table) {}
    public void monitorAdded (Monitor mon, Query qry, 
			      QueryResult res, QueryPlan plan) {}
}
