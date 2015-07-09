package stream.vis.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import stream.vis.data.*;
import java.util.List;
import java.util.ArrayList;

/**
 * A visualization for queues.  A queue is just a special kind of connector
 * that connects two operators.
 */

public class QueueView extends Connector implements EntityView {
    
    /// The queue which we are visualizing
    private Queue queue;

    /// The QueryPlanView that we belong to
    private QueryPlanView parentPlanView;
    
    /// Queue color if a stream flows through the queue
    private static final Color STREAM_COLOR = Color.orange;
    
    /// Queue color if a relation flows through the queue
    private static final Color RELATION_COLOR = Color.red;
    
    /// Width of the queue
    private static final float DEFAULT_WIDTH = (float)2.0;
    
    private static final float SELECT_RADIUS = (float)7.0;
    
    /// Are we highlighted
    boolean isHighlighted;
    
    /// List of entities that are listening to our motion
    List motionListeners;
    
    public QueueView (Queue queue, QueryPlanView qpv) {
	super ();
	this.queue = queue;
	parentPlanView = qpv;
	
	setQueueColor ();
	setWidth (DEFAULT_WIDTH);
	
	setOpaque(false);
	
	isHighlighted = false;
	motionListeners = new ArrayList();	
    }
    
    //------------------------------------------------------------
    // Set methods
    //------------------------------------------------------------
    
    public void setSrc (Component src) {
	super.setSrc (src);
	
	// Set the port
	int xOffset = src.getPreferredSize().width/2;
	int yOffset = 0;
	setSrcPort (new Port (xOffset, yOffset));
    }
    
    public void setDest (Component dest) {
	super.setDest (dest);
	
	// Set the port
	Dimension size = dest.getPreferredSize();
	int xOffset = size.width/2;
	int yOffset = size.height;
	setDestPort (new Port (xOffset, yOffset));
    }
    
    private void setQueueColor () {
	int srcOpId = queue.getSrc();
	Operator srcOp = queue.getPlan().getOp (srcOpId);
	
	if (srcOp.outputsStream()) 
	    setLineColor (STREAM_COLOR);
	else
	    setLineColor (RELATION_COLOR);
    }
    
    //------------------------------------------------------------
    // Get methods
    //------------------------------------------------------------
    
    public Queue getQueue () {
	return queue;
    }
    
    /// paint self
    protected void paintComponent (Graphics g) {	
	super.paintComponent(g);

	if (isHighlighted) {
	    Graphics2D g2d = (Graphics2D)g.create();
	    
	    g2d.setComposite 
		(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 
					    0.5f));
	    
	    g2d.setPaint (Color.gray);	    
	    g2d.setColor (Color.gray);
	    g2d.setRenderingHints (hints);		
	    g2d.setStroke (stroke);	
	    g2d.draw(line);
	    g2d.fill(line);
	    
	    g2d.dispose ();
	}
    }
    
    public boolean contains (int x, int y) { 
	Component src, dest;
	Port srcPort, destPort;
	
	src = getSrc ();
	dest = getDest ();
	srcPort = getSrcPort ();
	destPort = getDestPort ();
	
	double x1 = src.getX() + srcPort.xOffset;
	double y1 = src.getY() + srcPort.yOffset;
	double x2 = dest.getX() + destPort.xOffset;
	double y2 = dest.getY() + destPort.yOffset;
	
	double x3, y3, x4, y4;
	double distance;
	
	if (Math.abs(y2-y1) > 1.0) {	
	    y3 = (double) y;
	    x3 = x1 + (x2-x1)*(y3-y1)/(y2-y1);	           
	    
	    distance = Point2D.distance(x3, y3, (double)x, (double)y);
	    
	    if (distance <= SELECT_RADIUS)
		return true;
	}
	
	if (Math.abs(x2-x1) > 1.0) {
	    x4 = (double) x;
	    y4 = y1 + (y2-y1)*(x4-x1)/(x2-x1);	    	    
	    
	    distance = Point2D.distance(x4, y4, (double)x, (double)y);
	    
	    if (distance <= SELECT_RADIUS)
		return true;
	}
	
	return false;
    }

    //------------------------------------------------------------
    // Highlighting related
    //------------------------------------------------------------
    
    /**
     * Highlight this component.  
     */
    public void highlight() {
	if (!isHighlighted) {
	    isHighlighted = true;
	    repaint ();
	}
    }
    
    /**
     * Unhighlight this component
     */ 
    public void unHighlight() {
	if (isHighlighted) {
	    isHighlighted = false;
	    repaint ();
	}
    }

    public EntityProperty getEntityProperty () {
	return queue;
    }

    public void handleMonitorRequest (Client client, 
				      ClientView clientView,
				      int x, int y) {
	String queueId = queue.getServerId ();
	MonitorMenu menu = new MonitorMenu (client, clientView, this, 
					    queueId, parentPlanView);
	menu.addMonitor (Monitor.SS_QUEUE_RATE);
	menu.show (this, x, y);
    }

    public Component getComponent () {
	return this;
    }

    /// Add a listener who is interested in the entity's motion
    public void addMotionListener (MotionListener listener) {
	motionListeners.add (listener);
    }

    private void alertListeners () {
	for (int l = 0 ; l < motionListeners.size() ; l++) {
	    
	    if (!(motionListeners.get(l) instanceof MotionListener))
		continue;
	    
	    MotionListener listener = (MotionListener)(motionListeners.get(l));
	    listener.entityMoved (this);
	}
    }        

    public void entityMoved (Component comp) {
	super.entityMoved (comp);
	alertListeners ();
    }
}
