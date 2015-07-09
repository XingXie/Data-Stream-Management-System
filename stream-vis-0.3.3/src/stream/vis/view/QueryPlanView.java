package stream.vis.view;

import java.util.*;
import java.io.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import stream.vis.data.*;
import stream.vis.util.FatalException;
import stream.vis.data.Queue;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.List;
import java.util.ArrayList;

/**
 * Visualization for a query plan
 */ 

public class QueryPlanView extends JPanel 
    implements MouseListener {
    
    public static String filename;
    
    /// The client of which we are part
    Client client;
    
    /// The client view of which we are part
    ClientView clientView;
    
    /// The plan for which we are showing.
    QueryPlan plan;
    
    /// The query ... [[ Explanation ]]
    Query query;
    
    /// The list of operator views
    List opViews;
    
    /// The list of queue views
    List queueViews;
    
    /// The list of store views
    List storeViews;
    
    /// The list of synopsis views
    List synViews;
    
    /// The list of monitor views
    List monitorViews;
    
    /// Mapping from op -> opView
    Map opToView;
    
    /// Mapping from queue -> queueView
    Map queueToView;
    
    /// Mapping from syn -> synView
    Map synToView;
    
    /// Mapping from store -> storeView
    Map storeToView;
    
    /// current highlighted object
    EntityView curHighlighted = null;  
    
    /// The set of visible operators
    boolean[] visible;
    
    /// 
    EntityObserver observer;
    
    public QueryPlanView (Query query, 
			  QueryPlan plan, 
			  EntityObserver observer, 
			  Client client,
			  ClientView clientView) {
	
	this.plan = plan;
	this.query = query;
	this.observer = observer;
	this.client = client;
	this.clientView = clientView;
	
	setBackground (Color.white);
	setDoubleBuffered (true);
	
	// The layout manager lays out the plan components - operators,
	// queues, etc on the panel
	if (query != null)
	    setLayout (new QueryPlanLayoutManager2());
	else
	    setLayout (new QueryPlanLayoutManager ());
	
	opViews      = new ArrayList ();
	queueViews   = new ArrayList ();
	storeViews   = new ArrayList ();
	synViews     = new ArrayList ();
	monitorViews = new ArrayList ();
	
	opToView    = new HashMap ();
	synToView   = new HashMap ();
	queueToView = new HashMap ();
	storeToView = new HashMap ();
	
	// Compute visible operators
	computeVisibleOps (plan, query);
	
	// Create the subcomponents of the plan
	addPlanComponents();	
	
	addMouseListener (this);
    }
    
    public QueryPlan getQueryPlan () {
	return plan;
    }
    
    public OperatorView getOpView (int id) {
	return (OperatorView)opToView.get(new Integer(id));
    }
    
    public QueueView getQueueView (int id) {
	return (QueueView)queueToView.get(new Integer(id));
    }
    
    public StoreView getStoreView (int id) {
	return (StoreView)storeToView.get(new Integer(id));
    }
    
    public SynopsisView getSynView (int id) {
	return (SynopsisView)synToView.get(new Integer(id));
    }
    
    public int getRootOp () {
	return plan.getOutputOp (query.getQueryId());
    }
    
    public boolean isVisible (int opId) {
	if (opId >= visible.length) {
	    return false;
	}
	return visible[opId];
    }
    
    // Stop all the monitors (the refresh thread ...)
    public void reset () {
	for (int m = 0 ; m < monitorViews.size() ; m++) {
	    MonitorView monView = (MonitorView) monitorViews.get (m);
	    monView.stop ();
	}
    }
    
    private void addPlanComponents () {
	
	// Add the views for each operator
	for (int o = 0 ; o < plan.getNumOps () ; o++) {
	    if (!visible [o])
		continue;
	    
	    Operator op = plan.getOp (o);
	    OperatorView opView = new OperatorView (op, this);
	    add (opView);
	    
	    opViews.add (opView);
	    opToView.put (new Integer(o), opView);
	}
	
	// Add view for each queue
	for (int q = 0 ; q < plan.getNumQueues () ; q++) {
	    Queue queue = plan.getQueue (q);
	    int srcOpId = queue.getSrc ();
	    int dstOpId = queue.getDest ();
	    
	    assert (!visible[dstOpId] || visible[srcOpId]);
	    
	    if (!visible[dstOpId])
		continue;
	    
	    QueueView qview = new QueueView (queue, this);
	    add (qview);
	    
	    queueViews.add (qview);
	    queueToView.put (new Integer(q), qview);
	    
	    OperatorView srcView = (OperatorView) 
		opToView.get(new Integer(srcOpId));
	    
	    assert (srcView != null);

	    OperatorView dstView = (OperatorView) 
		opToView.get(new Integer(dstOpId));
	    
	    assert (dstView != null);
	    
	    qview.setSrc (srcView);
	    qview.setDest (dstView);
	    
	    srcView.addMotionListener (qview);
	    dstView.addMotionListener (qview);
	}	
	
	// add a view for each store
	for (int s = 0 ; s < plan.getNumStores() ; s++) {	    
	    Store store = plan.getStore (s);
	    int ownOpId = store.getOwnOp();
	    
	    if (!visible[ownOpId])
		continue;
	    
	    StoreView sview = new StoreView (store, this);
	    add (sview);
	    
	    storeViews.add (sview);
	    storeToView.put (new Integer(s), sview);
	    
	    // add a connector linking the store and the owning op
	    OperatorView opview = (OperatorView)
		opToView.get(new Integer(ownOpId));
	    assert (opview != null);
	    
	    StoreOpConnector conn = new StoreOpConnector ();
	    conn.setSrc (sview);
	    conn.setDest (opview);
	    add (conn);
	    
	    opview.addMotionListener (conn);
	    sview.addMotionListener (conn);
	}	
	
	// add a view for each synopsis
	for (int s = 0 ; s < plan.getNumSyns() ; s++) {
	    Synopsis syn = plan.getSyn (s);
	    int ownOpId = syn.getOwnOp ();
	    
	    if (!visible[ownOpId])
		continue;
	    
	    SynopsisView sview = new SynopsisView (syn, this);
	    add (sview);	    
	    synViews.add (sview);
	    
	    OperatorView opview = (OperatorView)
		opToView.get(new Integer(ownOpId));
	    assert (opview != null);
	    
	    SynOpConnector conn = new SynOpConnector (syn.getType());
	    conn.setSrc (sview);
	    conn.setDest (opview);
	    add (conn);
	    
	    sview.addMotionListener (conn);
	    opview.addMotionListener (conn);
	    
	    // Connector for synopsis and stores
	    int storeId = syn.getStore ();
	    StoreView storeView = (StoreView)
		storeToView.get(new Integer(storeId));
	    assert (storeView != null);
	    
	    SynStoreConnector storeConn = new SynStoreConnector ();
	    storeConn.setSrc (sview);
	    storeConn.setDest (storeView);
	    
	    storeView.addMotionListener (storeConn);
	    sview.addMotionListener (storeConn);
	    add (storeConn);
	}
    }

    public void addMonitor (Monitor monitor, 
			    QueryResult monitorData,
			    EntityView entityView) {
	
	// create a monitor view
	MonitorView monView = new MonitorView (entityView, monitorData, 
					       clientView);
	add (monView);
	monitorViews.add (monView);
	
	MonitorEntityConnector conn = new MonitorEntityConnector ();
	conn.setSrc (monView);
	conn.setDest (entityView.getComponent());
	add (conn);	
	
	monView.addMotionListener (conn);
	entityView.addMotionListener (conn);
    }
    
    public void mouseClicked(MouseEvent e) {
	// if the click occurs within a queue, we should not unhighlight
	// it
	int x = e.getX();
	int y = e.getY();
	for (int q = 0 ; q < queueViews.size() ; q++) {
	    QueueView qview = (QueueView)queueViews.get(q);
	    if (qview.getBounds().contains (x,y) &&
		qview.contains (x,y)) {
		return;
	    }
	}
	
	// Otherwise, we unhighlight the currently highlighted entity
	if (curHighlighted != null) {
	    curHighlighted.unHighlight (); 
	    observer.clear ();
	}	
    }
    
    public void mouseEntered(MouseEvent e) {
	
    }
    
    public void mouseExited(MouseEvent e) {
	
    }
    
    public void mousePressed(MouseEvent e) {	
	int x = e.getX();
	int y = e.getY();
	
	for (int q = 0 ; q < queueViews.size() ; q++) {
	    QueueView qview = (QueueView)queueViews.get(q);
	    if (qview.getBounds().contains (x,y) &&
		qview.contains (x,y)) {
		
		reportMousePress (qview, e);
		break;
	    }
	}
    }
    
    public void mouseReleased(MouseEvent e) {
	int x = e.getX();
	int y = e.getY();
	
	for (int q = 0 ; q < queueViews.size() ; q++) {
	    QueueView qview = (QueueView)queueViews.get(q);
	    if (qview.getBounds().contains (x,y) &&
		qview.contains (x,y)) {
		
		reportMouseRelease (qview, e);
		break;
	    }
	}
    }
    
    public void reportMousePress (EntityView entity, 
				  MouseEvent e) {

	if (e.isPopupTrigger ()) {
	    int x = e.getX();
	    int y = e.getY();
	    
	    // Ugly hack: since we are manually handling mouse events 
	    // for queues
	    if (entity instanceof QueueView) {		
		QueueView qview = (QueueView)entity;
		x -= qview.getLocation().getX();
		y -= qview.getLocation().getY();
	    }
	    
	    entity.handleMonitorRequest (client, clientView, x, y);
	    return;
	}
	
	if (curHighlighted != null)
	    curHighlighted.unHighlight ();
	entity.highlight ();
	observer.bindEntity (entity.getEntityProperty());
	curHighlighted = entity;
    }
    
    public void reportMouseRelease (EntityView entity, MouseEvent e) {
	if (e.isPopupTrigger ()) {
	    int x = e.getX();
	    int y = e.getY();
	    
	    // Ugly hack: since we are manually handling mouse events 
	    // for queues
	    if (entity instanceof QueueView) {		
		QueueView qview = (QueueView)entity;
		x -= qview.getLocation().getX();
		y -= qview.getLocation().getY();
	    }

	    entity.handleMonitorRequest (client, clientView, x, y);
	    return;
	}
    }
    
    private void computeVisibleOps (QueryPlan plan, 
				    Query query) {
	
	visible = new boolean[plan.getNumOps()];
	
	// all operators are visible is query == null
	if (query == null) {
	    for (int o = 0 ; o < visible.length ; o++)
		visible [o] = true;
	    return;
	}
	
	int opId = plan.getOutputOp (query.getQueryId());
	computeVisibleOps (opId, visible, plan);	
    }
    
    private void computeVisibleOps (int opId, boolean[] visible,
				    QueryPlan plan) {
	visible [opId] = true;
	Operator op = plan.getOp (opId);
	
	for (int i = 0 ; i < op.getNumInputs() ; i++) 
	    if (!visible[op.getInput(i)])
		computeVisibleOps (op.getInput(i), visible, plan);
    }
}
