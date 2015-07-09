package stream.vis.view;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import stream.vis.data.Store;
import stream.vis.data.EntityProperty;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;
import stream.vis.data.Client;
import stream.vis.data.Monitor;

public class StoreView extends JComponent 
    implements MouseMotionListener, MouseListener, EntityView {

    /// The store for which we are the view
    Store store;
    
    /// The QueryPlanView that we belong to
    private QueryPlanView parentPlanView;

    //------------------------------------------------------------
    // Dimensions
    //------------------------------------------------------------
    
    /// The height of the synopsis
    private static final int HEIGHT = 27;
    
    /// The width
    private static final int WIDTH = 16;
    
    /// Border buffer
    private static final int BORDER_BUFFER = 2;
    
    /// Size of the component
    Dimension size;
    
    //------------------------------------------------------------
    // Painting related
    //------------------------------------------------------------
    
    /// The stroke for drawing the lines
    Stroke stroke;
    
    /// Rendering hint to enable anti-aliasing
    RenderingHints hints;
    
    /// The color of the line
    private static final Color LINE_COLOR = Color.black;    
    
    /// The fill color
    private static final Color FILL_COLOR = Color.yellow;
    
    /// Horizontal lines (4 in num)
    Line2D.Double[] hlines;
    
    /// Number of horizontal lines
    private static final int NUM_HLINES = 4;
    
    /// Vertical lines (3 in num)
    Line2D.Double[] vlines;
    
    /// The full rectangle for fill purposes
    Rectangle2D.Double shape;
    
    /// Number of vertical lines
    private static final int NUM_VLINES = 3;
    
    //------------------------------------------------------------
    // Motion
    //------------------------------------------------------------    
    
    /// X-pos at the time of mouse press (used in implementing drag)
    int mousePressX;
    
    /// Y-pos at the time of mouse press (used in implementing drag)
    int mousePressY;

    /// List of entities that are listening to our motion
    List motionListeners;
    
    /// is this component highlighted?
    boolean isHighlighted = false;
    
    /// The rectangle used ot hsow highlighting
    Rectangle2D.Double highlightRect;
    
    public StoreView (Store store, QueryPlanView qpv) {
	super ();
	
	this.store = store;
	this.parentPlanView = qpv;
	
	// Set the dimensions
	setSize ();
	
	// Do all the painting related initializations
	initPaint ();
	
	// Do all motion related initializations
	initMove ();
	
	// Do all highlighting related initializations
	initHighlight ();
	
	// Tool tip is the full name of the store
	setToolTipText (store.getName());
    }
    
    /// Get the store object that this view shows
    public Store getStore () {
	return store;
    }
    
    //------------------------------------------------------------
    // Dimension/Location
    //------------------------------------------------------------

    public Dimension getPreferredSize () {
	return size;
    }    
    
    /// Set the location of the synopsis view
    public void setLocation (int x, int y) {
	setBounds (x, y, 
		   WIDTH  + 2*BORDER_BUFFER, 
		   HEIGHT + 2*BORDER_BUFFER);
	highlightRect = new Rectangle2D.Double (0, 0, 
						getSize().width,
						getSize().height);
    }
    
    private void setSize () {
	size = new Dimension (BORDER_BUFFER*2 + WIDTH,
			      BORDER_BUFFER*2 + HEIGHT);
    }

    
    //------------------------------------------------------------
    // Painting related
    //------------------------------------------------------------
    
    /**
     * Initialize painting related state
     */
    private void initPaint () {	
	
	// hints that encodes that anti-aliasing has to be turned on
	hints = new RenderingHints (RenderingHints.KEY_ANTIALIASING,
				    RenderingHints.VALUE_ANTIALIAS_ON);	
	hints.add (new RenderingHints
		   (RenderingHints.KEY_RENDERING,
		    RenderingHints.VALUE_RENDER_QUALITY));	
	
	// The stroke for drawing the line
	stroke = new BasicStroke (1);	
	
	createLines ();
    }
    
    /// Called from within initPaint - create the lines used in 
    /// painting the component
    private void createLines () {
	hlines = new Line2D.Double[NUM_HLINES];
	for (int l = 0 ; l < NUM_HLINES ; l++) {
	    int yPos = BORDER_BUFFER + l * HEIGHT / (NUM_HLINES - 1);
	    hlines[l] = new Line2D.Double (BORDER_BUFFER,
					   yPos,
					   BORDER_BUFFER + WIDTH,
					   yPos);
	}
	
	vlines = new Line2D.Double[NUM_VLINES];
	for (int l = 0 ; l < NUM_VLINES ; l++) {
	    int xPos = BORDER_BUFFER + l * WIDTH / (NUM_VLINES - 1);
	    vlines[l] = new Line2D.Double (xPos,
					   BORDER_BUFFER,
					   xPos,
					   BORDER_BUFFER + HEIGHT);
	}
	
	shape = new Rectangle2D.Double (BORDER_BUFFER, BORDER_BUFFER,
					WIDTH, HEIGHT);
    }
    
    protected void paintComponent (Graphics g) {	
	Graphics2D g2d = (Graphics2D)g.create();
	
	paintRegular (g2d);
	if (isHighlighted)
	    paintHighlight(g2d);
	g2d.dispose();
    }
    
    /// The regular painting of the component.
    private void paintRegular (Graphics2D g2d) {
	// Anti aliasing
	g2d.setRenderingHints (hints);
	g2d.setStroke (stroke);	
	
	// Fill ...
        g2d.setColor (FILL_COLOR);
	g2d.fill (shape);
	
	// Draw the lines
	g2d.setColor (LINE_COLOR);
	for (int l = 0 ; l < NUM_HLINES ; l++)
	    g2d.draw (hlines [l]);
	for (int l = 0 ; l < NUM_VLINES ; l++)
	    g2d.draw (vlines [l]);
    }    
    
    //------------------------------------------------------------
    // Highlighting related
    //------------------------------------------------------------
    
    private void initHighlight () {
	isHighlighted = false;
    }
    
    /// Paint to reflect highlighting
    private void paintHighlight (Graphics2D g2d) {
	g2d.setComposite 
	    (AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 
					0.5f));
	g2d.setPaint (Color.gray);
	g2d.fill (highlightRect);
    }

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
	return store;
    }    
    
    public void handleMonitorRequest (Client client, 
				      ClientView clientView,
				      int x, int y) {
	String storeId = store.getServerId ();
	MonitorMenu menu = new MonitorMenu (client, clientView, this, 
					    storeId, parentPlanView);
	menu.addMonitor (Monitor.SS_STORE_SIZE);
	menu.show (this, x, y);
    }
    
    //------------------------------------------------------------
    // Motion related
    //------------------------------------------------------------
    
    private void initMove () {
	mousePressX = mousePressY = -1;
	addMouseMotionListener (this);	
	addMouseListener (this);	
	motionListeners = new ArrayList();
    }
    
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
    
    public void mouseMoved (MouseEvent e) {
	// do nothing
    }
    
    public void mouseClicked(MouseEvent e) {
	
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
    
    public Component getComponent () {
	return this;
    }
}
