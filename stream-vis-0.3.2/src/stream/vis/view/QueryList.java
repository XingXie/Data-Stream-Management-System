package stream.vis.view;

import stream.vis.data.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.List;

public class QueryList extends JPanel implements ClientListener {
    private Client client;
    private ClientView clientView;
    private JTextArea textArea;
    private static final int VGAP = 10;    
    private Component verticalGlue;
    
    public QueryList(Client client, ClientView clientView) {
	this.client = client;
	this.clientView = clientView;
	client.addListener (this);
	
	setLayout (new BoxLayout (this, BoxLayout.PAGE_AXIS));
	
	addGap ();
	addRule ();
	
	List queries = client.getRegisteredQueries ();	
	for (int q = 0 ; q < queries.size() ; q++) {
	    addGap ();
	    add (new QueryView ((Query)queries.get(q), clientView, client));
	    addGap ();
	    addRule ();
	}
	
	verticalGlue = Box.createVerticalGlue ();
	add (verticalGlue);
    }
    
    public void stateChanged (int oldState, int newState) {}    
    public void baseTableAdded (NamedTable table) {}
    public void queryResultAvailable (QueryResult result) {}    
    public void planGenerated (QueryPlan plan) {}
    public void closeEvent () {}    
    public void monitorAdded (Monitor mon, Query qry, 
			      QueryResult res, QueryPlan plan) {
	remove (verticalGlue);
	addGap ();
	add (new QueryView (qry, clientView, client));
	addGap ();
	addRule ();
	add (verticalGlue);	
    }
    
    public void resetEvent () {
	removeAll(); 
	addGap();
	addRule ();
	add (verticalGlue);
    }
    
    public void queryAdded (Query query, UnnamedTable outSchema) {
	if (!query.isNamed()) {
	    remove (verticalGlue);
	    addGap ();
	    add (new QueryView (query, clientView, client));
	    addGap ();
	    addRule ();
	    add (verticalGlue);
	    clientView.showQueryList();
	}
    }
    
    public void viewAdded (Query query, NamedTable table) {
	if (query.isNamed()) {
	    remove (verticalGlue);
	    addGap();
	    add (new QueryView (query, clientView, client));
	    addGap();
	    addRule ();	    
	    add (verticalGlue);
	    clientView.showQueryList();	   
	}
    }
    
    private void addGap () {
	add (Box.createRigidArea (new Dimension (0, VGAP)));
    }
    
    private void addRule () {	
	JPanel rule = new JPanel ();
	rule.setLayout (new BoxLayout (rule, BoxLayout.LINE_AXIS));
	rule.add (Box.createHorizontalGlue());
	rule.setBorder (BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
	
	add (rule);
    }
}
