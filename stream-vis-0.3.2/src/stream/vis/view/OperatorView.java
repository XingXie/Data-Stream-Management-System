package stream.vis.view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import stream.vis.data.Operator;
import stream.vis.data.EntityProperty;
import stream.vis.data.Client;
import stream.vis.data.Monitor;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Visualization for an operator - just a label that shows the text of the
 * operator's name.
 */ 

public class OperatorView extends JPanel 
    implements MouseMotionListener, MouseListener, EntityView {
    
    /// The operator that we are the view for.
    private Operator op;
    
    /// The QueryPlanView that we belong to
    private QueryPlanView parentPlanView;
        
    /// X-pos at the time of mouse press (used in implementing drag)
    int mousePressX;
    
    /// Y-pos at the time of mouse press (used in implementing drag)
    int mousePressY;
    
    /// List of entities that are listening to our motion
    List motionListeners;
    
    /// Are we highlighted
    boolean isHighlighted;
    
    /// The rectangle used to show highlighting
    Rectangle2D.Double highlightRectangle;
    
    public OperatorView (Operator op, QueryPlanView qpv) {
	this.op = op;
        this.parentPlanView = qpv;
	
	setBackground (Color.cyan);
	setLayout (new BorderLayout ());
	setOpaque (true);
	
	JLabel label = new JLabel ();
	label.setBorder (BorderFactory.createRaisedBevelBorder());
	label.setText (op.getName());	
	add (label, BorderLayout.CENTER);
	
	mousePressX = mousePressY = -1;
	addMouseMotionListener (this);	
	addMouseListener (this);	
	motionListeners = new ArrayList();	
	
	isHighlighted = false;
	
	setToolTipText (op.getLongName ());
    }
    
    /**
     * If we are not highlighted, just call the parent paint method
     * Otherwise, call the parent paint method and then draw the highligh.
     *
     */
    protected void paintComponent(Graphics g) {
	super.paintComponent (g);
	
	if (isHighlighted) {
	    paintHighlight (g);
	}
    }
    
    /**
     * We intercept the setBounds() method to calculate the rectangle
     * that we use to show shading ...
     */
    public void setBounds(int x, int y, int width, int height) {
	super.setBounds (x, y, width, height);
	setHighlightRectangle (width, height);
    }    
    
    /**
     * Get the operator that we are the view of.
     */
    
    public Operator getOp () {
	return op;
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
    
    /**
     * Set the rectangle used to highlight the operator: this is 
     * called from withing setBounds() method with the width and
     * height of this component.
     */
    private void setHighlightRectangle (int width, int height) {	
	highlightRectangle = new Rectangle2D.Double (0, 0, width, height);
    }
    
    /**
     * Paint the highlighting: called by the paintComponent method
     * if this component is currently highlighted.
     */
    private void paintHighlight (Graphics g) {	
	Graphics2D g2d = (Graphics2D)g.create();
	
	g2d.setComposite 
	    (AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 
					0.5f));
	g2d.setPaint (Color.gray);
	g2d.fill (highlightRectangle);
	g2d.dispose ();
    }
    
    //------------------------------------------------------------
    // Motion related
    //------------------------------------------------------------
    
    /**
     * Move the object in response to a user drag
     */
    public void mouseDragged(MouseEvent e) {
	if (mousePressX >= 0 && mousePressY >= 0) {
	    int dx = e.getX() - mousePressX;
	    int dy = e.getY() - mousePressY;
	    if (dx != 0 || dy != 0) {
		setLocation (getX() + dx, getY() + dy);
		alertListeners ();
	    }
	}
    }
    
    /**
     * When the mouse clicks over this component - request the parent
     * component to highlight me ...
     */
    
    public void mouseClicked(MouseEvent e) {
	
    }
    
    public void mouseMoved (MouseEvent e) {
	// do nothing
    }
    
    public void mouseEntered(MouseEvent e) {
	
    }
    
    public void mouseExited(MouseEvent e) {
	
    }
    
    public void mousePressed(MouseEvent e) {	
	parentPlanView.reportMousePress (this, e);
	if ((e.getModifiers() & e.BUTTON1_MASK) != 0) {
	    mousePressX = e.getX();
	    mousePressY = e.getY();
        }
    }
    
    public void mouseReleased(MouseEvent e) {
	parentPlanView.reportMouseRelease (this, e);
	mousePressX = -1;
	mousePressY = -1;
    }
    
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
    
    public EntityProperty getEntityProperty () {
	return op;
    }
    
    /// Handle a request for a monitor
    public void handleMonitorRequest (Client client, 
				      ClientView clientView,
				      int x, int y) {	
	String opId = op.getServerId ();
	MonitorMenu menu = new MonitorMenu (client, clientView, this, 
					    opId, parentPlanView);
	menu.addMonitor (Monitor.SS_OP_TIME);

	// for join operators add a join selectivity button
	if (op.getName().equals("Join") || op.getName().equals("StrJoin")) {   
	    menu.addMonitor (Monitor.SS_JOIN_SEL);
	}
	menu.show (this, x, y);
    }
    
    public Component getComponent () {
	return this;
    }
}
