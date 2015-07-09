package stream.vis.view.menu;

import java.awt.event.*;
import javax.swing.*;

import stream.vis.data.*;
import stream.vis.view.ClientView;
import stream.vis.util.FatalException;

/**
 * Command menu contains items to send commands to the server:
 * register query, register inputs, etc.
 */ 
    
public class CommandMenu extends JMenu 
    implements ActionListener, ClientListener {
    
    Client client;
    ClientView clientView;
    
    JMenuItem regStream;
    JMenuItem regReln;
    JMenuItem regQuery;
    JMenuItem genPlan;
    JMenuItem execute;
    JMenuItem reset;
    JMenuItem hardReset;

    public CommandMenu(Client client, ClientView clientView) {
	super("Command");
	
	this.client = client;
	this.clientView = clientView;
	
	regStream = new JMenuItem ("Register Stream");
	regReln   = new JMenuItem ("Register Relation");
	regQuery  = new JMenuItem ("Register Query");
	genPlan   = new JMenuItem ("Generate Plan");
	execute   = new JMenuItem ("Run");
	reset     = new JMenuItem ("Reset");
	hardReset = new JMenuItem ("Complete Reset");

	add (regStream);
	add (regReln);
	add (regQuery);
	add (genPlan);
	add (execute);
	add (reset);	    
	add (hardReset);

	regStream.addActionListener (this);
	regReln.addActionListener (this);
	regQuery.addActionListener (this);
	genPlan.addActionListener (this);
	execute.addActionListener (this);
	reset.addActionListener (this);	
	hardReset.addActionListener (this);

	client.addListener (this);
	
	createShortcuts ();
    }
    
    public void actionPerformed(ActionEvent e) {
	Object source = e.getSource ();
	
	try {
	    if (source == regStream) {
		clientView.regStream();
	    }
	    else if (source == regReln) {
		clientView.regRelation();
	    }
	    else if (source == regQuery) {
		clientView.regQuery();
	    }
	    else if (source == genPlan) {
		client.genPlan ();
	    }
	    else if (source == execute) {
		client.run ();
	    }
	    else if (source == reset) {
		client.softReset ();
	    }
	    else if (source == hardReset) {
		client.reset ();
	    }
	}
	
	catch (Exception f) {
	    System.out.println (f);
	    System.exit(1);
	}
    }
    
    public void stateChanged (int oldState, int newState) {
	updateEnabling (newState);
    }
    
    public void baseTableAdded (NamedTable table) {
	
    }
    
    public void queryAdded (Query query, UnnamedTable outSchema) {

    }
    
    public void viewAdded (Query query, NamedTable table) {

    }
    
    public void queryResultAvailable (QueryResult result) {
	
    }
    
    public void planGenerated (QueryPlan plan) {
	
    }
    public void monitorAdded (Monitor mon, Query qry, 
			      QueryResult res, QueryPlan plan) {}

    
    public void resetEvent () {}    
    public void closeEvent () {}

    //------------------------------------------------------------
    // Commands Enabling/disabling logic
    //------------------------------------------------------------
    
    private void disableAllMenuItems () {
	regStream.setEnabled (false);
	regReln.setEnabled (false);
	regQuery.setEnabled (false);
	genPlan.setEnabled (false);
	execute.setEnabled (false);
	reset.setEnabled (false);	
    }
    
    private void updateEnabling (int state) {
	disableAllMenuItems ();
	
	switch (state) {
	case Client.USERAPPSPEC:
	    regStream.setEnabled (true);
	    regReln.setEnabled (true);
	    regQuery.setEnabled (true);
	    genPlan.setEnabled (true);
	    reset.setEnabled (true);
	    break;
	    
	case Client.PLANGEN:
	    execute.setEnabled (true);
	    reset.setEnabled (true);
	    break;
	    
	case Client.RUN:
	    reset.setEnabled (true);
	    break;
	    
	default:
	    break;
	}
    } 

    //------------------------------------------------------------
    // Keyboard shortcuts
    //------------------------------------------------------------
    private void createShortcuts () {
	setMnemonic (KeyEvent.VK_C);
	
	regStream.setAccelerator 
	    (KeyStroke.getKeyStroke (KeyEvent.VK_S, ActionEvent.ALT_MASK));
	regReln.setAccelerator
	    (KeyStroke.getKeyStroke (KeyEvent.VK_R, ActionEvent.ALT_MASK));
	regQuery.setAccelerator 
	    (KeyStroke.getKeyStroke (KeyEvent.VK_Q, ActionEvent.ALT_MASK));
	genPlan.setAccelerator 
	    (KeyStroke.getKeyStroke (KeyEvent.VK_P, ActionEvent.ALT_MASK));
	execute.setAccelerator
	    (KeyStroke.getKeyStroke (KeyEvent.VK_X, ActionEvent.ALT_MASK));
    }
}
