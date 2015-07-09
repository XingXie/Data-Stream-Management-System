package stream.vis.view.menu;

import java.awt.event.*;
import javax.swing.*;
import stream.vis.data.*;
import stream.vis.view.ClientView;
import java.util.List;
import java.util.ArrayList;

public class ViewMenu extends JMenu 
    implements ActionListener, ClientListener {
    
    ClientView clientView;    
    JMenuItem viewRegTables;
    JMenuItem viewRegQueries;
    List viewPlans;
    boolean showPlan;
    
    public ViewMenu (Client client, ClientView clientView) {
	super ("View");	
	this.clientView = clientView;
	
	setMnemonic (KeyEvent.VK_V);
	
	viewRegTables = new JMenuItem("Registered Streams and Relations"); 
	viewRegQueries= new JMenuItem("Registered Queries");
	
	add (viewRegTables);
	add (viewRegQueries);
	addSeparator ();
	
	viewPlans = new ArrayList ();
	List queries = client.getRegisteredQueries ();
	for (int q = 0 ; q < queries.size() ; q++) {
	    Query qry = (Query)queries.get(q);
	    
	    ViewPlanItem viewPlanItem = new ViewPlanItem (qry);
	    add (viewPlanItem);
	    viewPlanItem.setEnabled (false);
	    viewPlans.add (viewPlanItem);    
	}
	
	viewRegTables.addActionListener (this);
	viewRegQueries.addActionListener(this);	
	client.addListener (this);

	showPlan = false;
    }
    
    public void actionPerformed (ActionEvent e) {
	Object source = e.getSource ();
	
	if (source == viewRegTables) {
	    clientView.showTableList();
	}
	
	else if (source == viewRegQueries) {
	    clientView.showQueryList();
	}	
    }
    
    public void stateChanged (int oldState, int newState) {
	if (newState == Client.PLANGEN || newState == Client.RUN) {
	    showPlan = true;
	    for (int p = 0 ; p < viewPlans.size() ; p++) {
		ViewPlanItem viewPlan = (ViewPlanItem) viewPlans.get(p);
		viewPlan.setEnabled (true);
	    }
	}
	else {
	    showPlan = false;
	    for (int p = 0 ; p < viewPlans.size() ; p++) {
		ViewPlanItem viewPlan = (ViewPlanItem) viewPlans.get(p);
		viewPlan.setEnabled (false);
	    }
	}
    }
    
    public void queryAdded (Query query, UnnamedTable outSchema) {
	if (query.hasOutput ()) {
	    ViewPlanItem viewPlanItem = new ViewPlanItem (query);
	    add (viewPlanItem);
	    // if qry is added, then plan should not have been generated
	    viewPlanItem.setEnabled (false);
	    viewPlans.add (viewPlanItem);	    
	}
    }
    
    public void queryResultAvailable (QueryResult result) {}
    public void planGenerated (QueryPlan plan) {}    
    public void baseTableAdded (NamedTable table) {}
    public void viewAdded (Query query, NamedTable table) {}    
    public void monitorAdded (Monitor mon, Query qry, 
			      QueryResult res, QueryPlan plan) {
	ViewPlanItem viewPlanItem = new ViewPlanItem (qry);
	add (viewPlanItem);
	viewPlanItem.setEnabled (showPlan);
	viewPlans.add (viewPlanItem);
    }
    
    public void resetEvent () {
	for (int p = 0 ; p < viewPlans.size() ; p++)
	    remove ((ViewPlanItem)viewPlans.get(p));
    }
    
    public void closeEvent () {}  
    
    private class ViewPlanItem extends JMenuItem implements ActionListener {
	Query query;
	
	ViewPlanItem (Query qry) {
	    super ();
	    
	    assert (qry.hasOutput ());
	    query = qry;
	    setText ("Query " + qry.getQueryId());
	    addActionListener(this);
	}
	
	public void actionPerformed (ActionEvent e) {
	    assert (e.getSource () == this);
	    clientView.showPlan (query);
	}
    }
}
