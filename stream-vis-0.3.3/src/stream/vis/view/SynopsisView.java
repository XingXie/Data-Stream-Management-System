package stream.vis.view;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;
import stream.vis.data.Synopsis;
import stream.vis.data.EntityProperty;
import java.util.*;
import java.util.List;
import stream.vis.data.Client;
import stream.vis.data.Monitor;

public class SynopsisView extends JComponent 
    implements MouseMotionListener, MouseListener, EntityView {
    
    /// Synopsis for which we are the view
    Synopsis syn;    

    /// The QueryPlanView that we belong to
    private QueryPlanView parentPlanView;
    
    //------------------------------------------------------------
    // Dimensions
    //------------------------------------------------------------    
    
    /// The height of the synopsis
    private static final int HEIGHT = 20;
    
    /// The width
    private static final int WIDTH = 20;
    
    /// Border buffer
    private static final int BORDER_BUFFER = 2;
    
    /// Size of the component
    Dimension size;
    
    //------------------------------------------------------------
    // Painting related
    //------------------------------------------------------------
    
    /// The stroke for drawing the lines
    private static final Stroke stroke = new BasicStroke (1);
    
    /// Rendering hint to enable anti-aliasing
    RenderingHints hints;
    
    /// The color of the line
    private static final Color LINE_COLOR = Color.black;    
    
    /// The fill color
    private static final Color FILL_COLOR = Color.pink;
    
    /// The circle representing the synopsis
    private static final Ellipse2D circle = 
	new Ellipse2D.Double (BORDER_BUFFER, BORDER_BUFFER,
			      WIDTH, HEIGHT);
    
    //------------------------------------------------------------
    // Motion
    //------------------------------------------------------------    
    
    /// X-pos at the time of mouse press (used in implementing drag)
    int mousePressX;
    
    /// Y-pos at the time of mouse press (used in implementing drag)
    int mousePressY;
    
    /// List of entities that are listening to our motion
    List motionListeners;
    
    //------------------------------------------------------------
    // Highlighting/selection
    //------------------------------------------------------------
    
    boolean isHighlighted;    
    
    Ellipse2D highlightCircle;
    
    public SynopsisView (Synopsis syn, QueryPlanView qpv) {
	super ();
	
	this.syn = syn;
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
	setToolTipText (syn.getName());
    }
    
    public Dimension getPreferredSize () {
	return size;
    }
    
    public Synopsis getSyn () {
	return syn;
    }
    
    /// Set the location of the synopsis view
    public void setLocation (int x, int y) {
	setBounds (x, y, 
		   WIDTH  + 2*BORDER_BUFFER, 
		   HEIGHT + 2*BORDER_BUFFER);
	
	highlightCircle = new Ellipse2D.Double (0, 0, 
						getSize().width,
						getSize().height);
    }
    
    /**
     * Paint the component.  If the component is highlighted, follow 
     * the painting with the highlighting ..
     */
    
    protected void paintComponent (Graphics g) {	
	Graphics2D g2d = (Graphics2D)g.create();	
	paintRegular (g2d);
	
	if (isHighlighted)
	    paintHighlight (g2d);
	g2d.dispose();
    }
    
    //------------------------------------------------------------
    // Painting related
    //------------------------------------------------------------
    
    private void setSize () {
	size = new Dimension (BORDER_BUFFER*2 + WIDTH,
			      BORDER_BUFFER*2 + HEIGHT);
    }
    
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
    }
    
    /// The regular painting of the component.
    private void paintRegular (Graphics2D g2d) {
	// Anti aliasing
	g2d.setRenderingHints (hints);
	g2d.setStroke (stroke);	
	
	// Fill ...
        g2d.setColor (FILL_COLOR);
	g2d.fill (circle);
	
	// Draw the lines
	g2d.setColor (LINE_COLOR);
	g2d.draw (circle);
    }
    
    //------------------------------------------------------------
    // Highlightint related
    //------------------------------------------------------------
    
    /// Create the highlighting rectangle
    private void initHighlight () {
	isHighlighted = false;	
    }

    /// Paint to reflect highlighting
    private void paintHighlight (Graphics2D g2d) {
	g2d.setComposite 
	    (AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 
					0.5f));
	g2d.setPaint (Color.gray);
	g2d.fill (highlightCircle);
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
	return syn;
    }
    
    public void handleMonitorRequest (Client client, 
				      ClientView clientView,
				      int x, int y) {
	String synId = syn.getServerId ();
	MonitorMenu menu = new MonitorMenu (client, clientView, this, 
					    synId, parentPlanView);
	menu.addMonitor (Monitor.SS_SYN_CARD);
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
