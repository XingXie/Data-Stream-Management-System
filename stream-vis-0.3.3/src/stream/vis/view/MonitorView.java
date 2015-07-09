package stream.vis.view;

import stream.vis.data.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Visualization for the results of a monitor.
 */

public class MonitorView extends JComponent 
    implements MouseMotionListener, MouseListener {
    
    /// The query for the monitor
    private Query query;
    
    /// The result of the monitor query (updated asynchronously)
    private QueryResult data;
    
    /// The entity being monitored
    private EntityView entityView;
    
    /// refresher to refresh monitor display
    private MonitorRefresher refresher;
    
    /// The client view of which we are a part 
    private ClientView clientView;
    
    //------------------------------------------------------------
    // Dimensions
    //------------------------------------------------------------
    
    /// Border buffer
    private static final int BORDER_BUFFER = 2;
    
    /// vertical distance between border and max line
    private static final int MAX_LINE_DEPTH = 15;
    
    /// vertical distance between top border and min line
    private static final int MIN_LINE_DEPTH = 50;
    
    /// vertical distance between border and max "text"
    private static final int MAX_TEXT_DEPTH = 10;
    
    /// The height of the synopsis
    private static final int HEIGHT = 55;
    
    /// The width
    private static final int WIDTH = 50;
    
    /// The vertical distance between min line and max line
    private static final int MAX_HEIGHT = MIN_LINE_DEPTH - MAX_LINE_DEPTH;
    
    /// Size of the component
    Dimension size;
    
    //------------------------------------------------------------
    // Painting related
    //------------------------------------------------------------
    
    /// The stroke for drawing the borders
    private final static Stroke borderStroke = new BasicStroke (1.2f);
    
    /// The stroke for drawing the plot lines
    private final static Stroke graphStroke = new BasicStroke (1.0f);
    
    /// The stroke for drawing min/max lines
    final static float dash1[] = {2.0f};
    final static BasicStroke extremaStroke = 
	new BasicStroke(1.0f, 
			BasicStroke.CAP_BUTT, 
			BasicStroke.JOIN_MITER, 
			10.0f, dash1, 0.0f);
    
    /// Rendering hint to enable anti-aliasing
    RenderingHints hints;
    
    /// The border rectangle
    Rectangle2D border;
    
    /// The borders of the monitor
    private static final Color BORDER_COLOR = Color.black;
    
    /// The color of the max/min lines
    private static final Color EXTREMA_COLOR = Color.red;    
    
    /// The color of the max/min lines
    private static final Color GRAPH_COLOR = Color.green;    
    
    /// The Max line
    Line2D maxLine;
    
    /// The min line
    Line2D minLine;
    
    private static final int NUM_STEPS = WIDTH - 2;
    
    // x offset of the first step (newest step)
    private static final int FIRST_STEP_X = BORDER_BUFFER + WIDTH - 2;    
    
    //------------------------------------------------------------
    // Motion
    //------------------------------------------------------------    
    
    /// X-pos at the time of mouse press (used in implementing drag)
    int mousePressX;
    
    /// Y-pos at the time of mouse press (used in implementing drag)
    int mousePressY;
    
    /// List of entities that are listening to our motion
    List motionListeners;
    
    public MonitorView (EntityView entityView, QueryResult data, 
			ClientView clientView) {
	this.data = data;
	this.query = data.getQuery ();
	this.entityView = entityView;
	this.clientView = clientView;
	
	// Set the dimensions
	setSize ();
	
	// Do all the painting related initializations
	initPaint ();
	
	// Do all motion related initializations
	initMove ();	
	
	refresher = new MonitorRefresher ();
    }
    
    public EntityView getMonitoredEntity () {
	return entityView;
    }

    public Dimension getPreferredSize () {
	return size;
    }
    
    public void stop () {
	refresher.stop ();
    }
    
    /// Set the location of the synopsis view
    public void setLocation (int x, int y) {
	setBounds (x, y, 
		   WIDTH  + 2*BORDER_BUFFER, 
		   HEIGHT + 2*BORDER_BUFFER);
    }
    
    /**
     * Paint the component.
     */
    
    protected void paintComponent (Graphics g) {	
	Graphics2D g2d = (Graphics2D)g.create();	
	
	// Anti aliasing
	g2d.setRenderingHints (hints);
	
	// draw the border
	g2d.setStroke (borderStroke);	
	g2d.setColor (BORDER_COLOR);
	g2d.draw (border);
	
	drawGraph (g2d);
	
	g2d.dispose();
    }
    
    private void drawGraph (Graphics2D g2d) { 	
	// draw the min/max lines
	g2d.setStroke (extremaStroke);
	g2d.setColor (EXTREMA_COLOR);	
	g2d.draw (minLine);
	g2d.draw (maxLine);
	
	g2d.setStroke (graphStroke);	
	g2d.setColor (GRAPH_COLOR);
	synchronized (data) {
	    if (data.getNumAvailableTuples () == 0)
		drawIntGraph (g2d);
	    else if (data.get(0).getAttr(0) instanceof Integer)
		drawIntGraph (g2d);
	    else 
		drawFloatGraph (g2d);	    
	}	
    }    
    
    private void drawIntGraph (Graphics2D g2d) {
	int max = 0;
	int numTuples = data.getNumAvailableTuples ();
	
	if (numTuples > NUM_STEPS)
	    numTuples = NUM_STEPS;

	// determine the max value in the last NUM_STEPS
	for (int s = 0 ; s < numTuples ; s++) {
	    Tuple t = data.get (s);	    
	    
	    assert (t.getAttr(0) instanceof Integer);	    
	    int val = ((Integer)t.getAttr(0)).intValue();
	    
	    if (max < val)
		max = val;	    
	}
	
	if (max == 0) 
	    max = 1;
	
	// plot the graph
	int step;
	int xCur, yCur, xPrev, yPrev;	
	xPrev = yPrev = 0;
	for (step = 0 ; step < numTuples ; step++) {
	    Tuple t = data.get (step);	    
	    int val = ((Integer)t.getAttr(0)).intValue();
	    
	    // point in the graph
	    xCur = FIRST_STEP_X - step;
	    yCur = MIN_LINE_DEPTH + BORDER_BUFFER - 
		(val * MAX_HEIGHT)/max;
	    
	    if (step != 0) {
		g2d.drawLine (xCur, yCur, xPrev, yPrev);
	    }
	    
	    xPrev = xCur; 
	    yPrev = yCur;
	}	
	
	for (; step < NUM_STEPS ; step++) {
	    xCur = FIRST_STEP_X - step;
	    yCur = MIN_LINE_DEPTH + BORDER_BUFFER;
	    
	    if (step != 0) {
		g2d.drawLine (xCur, yCur, xPrev, yPrev);
	    }
	    
	    xPrev = xCur; 
	    yPrev = yCur;
	}	
    }
    
    private float findFloatMax (int numTuples) {
	float max = 0.0f;
	
	// determine the max value in the last NUM_STEPS
	for (int s = 0 ; s < numTuples ; s++) {
	    Tuple t = data.get (s);
	    
	    // no more tuples
	    if (t == null) break;
	    
	    assert (t.getAttr(0) instanceof Float);	    
	    float val = ((Float)t.getAttr(0)).floatValue();
	    
	    if (max < val)
		max = val;	    
	}
	
	return max;
    }
    
    private float normalizeFloatMax (float max) {
	float maxNorm;
	
	if (max > 10.0f) {
	    int intMax;
	    int power;
	    
	    intMax = (int)max;
	    for (power = 1; power < intMax / 4 ; power *= 10);
	    
	    assert (power >= 10);
	    power /= 10;
	    
	    // round off to power
	    intMax += (power - intMax % power);
	    maxNorm = (float)intMax;
	}
	
	else if (max > 1.0f) {
	    // round off to the next integer
	    maxNorm = (float)Math.ceil (max);
	}
	
	else {
	    // We don't want max to be too small
	    if (max < 0.1f)
		max = 0.1f;	
	    
	    maxNorm = ((float)Math.ceil(max * 10))/10;
	}	
	
	return maxNorm;
    }
    
    private void drawFloatGraph (Graphics2D g2d) {
	float max, maxNorm;
	
	int numTuples = data.getNumAvailableTuples ();
	if (numTuples > NUM_STEPS)
	    numTuples = NUM_STEPS;
	
	// find the maximum
	max = findFloatMax (numTuples);
	
	// normalization rounds off the maximum value - this prevents
	// frequently changing maxes
	maxNorm = normalizeFloatMax (max);
	
	// plot the graph
	int step;
	int xCur, yCur, xPrev, yPrev;	
	
	xPrev = yPrev = 0;
	for (step = 0 ; step < numTuples ; step++) {
	    Tuple t = data.get (step);
	    if (t == null) break;
	    
	    float val = ((Float)t.getAttr(0)).floatValue();
	    
	    // point in the graph
	    xCur = FIRST_STEP_X - step;
	    yCur = MIN_LINE_DEPTH + BORDER_BUFFER - 
		((int)((val * MAX_HEIGHT)/maxNorm));
	    
	    if (step != 0) {
		g2d.drawLine (xCur, yCur, xPrev, yPrev);
	    }
	    
	    xPrev = xCur; 
	    yPrev = yCur;
	}
	
	for (; step < NUM_STEPS ; step++) {
	    xCur = FIRST_STEP_X - step;
	    yCur = MIN_LINE_DEPTH + BORDER_BUFFER;
	    
	    if (step != 0) {
		g2d.drawLine (xCur, yCur, xPrev, yPrev);
	    }
	    
	    xPrev = xCur; 
	    yPrev = yCur;
	}
	
	g2d.setColor (EXTREMA_COLOR);
	String maxString = "" + maxNorm;
	g2d.drawString (maxString, 3, 13);	
    }
    
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
	
	// The border of the monitor
	border = new Rectangle2D.Double (BORDER_BUFFER, BORDER_BUFFER,
					 WIDTH, HEIGHT);
	
	// the max line
	maxLine = new Line2D.Double (BORDER_BUFFER, 
				     BORDER_BUFFER + MAX_LINE_DEPTH,
				     BORDER_BUFFER + WIDTH,
				     BORDER_BUFFER + MAX_LINE_DEPTH);
	
	minLine = new Line2D.Double (BORDER_BUFFER,
				     BORDER_BUFFER + MIN_LINE_DEPTH,
				     BORDER_BUFFER + WIDTH,
				     BORDER_BUFFER + MIN_LINE_DEPTH);
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
	if (e.isPopupTrigger ()) {
	    MonitorPopupMenu popupMenu = new MonitorPopupMenu ();
	    popupMenu.show (this, e.getX(), e.getY());
	}
	
	if ((e.getModifiers() & e.BUTTON1_MASK) != 0) {
	    mousePressX = e.getX();
	    mousePressY = e.getY();
        }
    }
    
    public void mouseReleased(MouseEvent e) {
	if (e.isPopupTrigger ()) {
	    MonitorPopupMenu popupMenu = new MonitorPopupMenu ();
	    popupMenu.show (this, e.getX(), e.getY());
	}
	
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
    
    //------------------------------------------------------------
    // Monitor refresher - wake up every 30 seconds and refresh
    //------------------------------------------------------------
    /// The frequency at which we want to refresh: 150 millisecs
    private static final int PERIOD = 150;
    
    private class MonitorRefresher extends TimerTask {
	/// The timer to keep scheduling us periodically
	private Timer timer;
	
	public MonitorRefresher () {
	    super ();
	    timer = new Timer ();
	    timer.schedule (this, 0, PERIOD);	    
	}
	
	public void run () {
	    repaint ();
	}
	
	public void stop () {
	    timer.cancel ();
	}
    }

    private class MonitorPopupMenu extends JPopupMenu 
	implements ActionListener {
	
	JMenuItem showPlanItem;
	
	MonitorPopupMenu () {
	    showPlanItem = new JMenuItem ("Show Plan");
	    showPlanItem.addActionListener (this);
	    add (showPlanItem);
	}
	
	public void actionPerformed (ActionEvent e) {
	    if (e.getSource () == showPlanItem) {
		clientView.showPlan (query);
	    }
	}
    }
}
