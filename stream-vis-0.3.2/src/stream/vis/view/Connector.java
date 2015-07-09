package stream.vis.view;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

/**
 * A connector is a line that connects two components.  
 * Subclasses are: Queues 
 */ 

public class Connector extends JComponent implements MotionListener {
    
    /// The source component
    private Component src;
    
    /// The destination component
    private Component dst;
    
    /// Port within the src where this component connects
    private Port srcPort;
    
    /// Port within the dest where this component connects
    private Port dstPort;
    
    /// The line that represents the componet
    Line2D.Double line;
    
    /// The color of the line
    private Color lineColor;
    
    /// The width of the line
    private float width;
    
    /// Buffer we leave in the border of the component
    private static final int BORDER_BUFFER = 3;

    /// The stroke for the line
    Stroke stroke;
    
    /// Rendering hint to enable anti-aliasing
    RenderingHints hints;
       
    // Default values
    private static final float DEFAULT_WIDTH = (float)1.0;
    
    private static final Color DEFAULT_COLOR = Color.black;
    
    public Connector () {
	super ();
	
	// hints that encodes that anti-aliasing has to be turned on
	hints = new RenderingHints (RenderingHints.KEY_ANTIALIASING,
				    RenderingHints.VALUE_ANTIALIAS_ON);
	
	hints.add (new RenderingHints
		   (RenderingHints.KEY_RENDERING,
		    RenderingHints.VALUE_RENDER_QUALITY));
	
	// Default values
	lineColor = DEFAULT_COLOR;	
	width = DEFAULT_WIDTH;
	
	// The stroke for drawing the line
	stroke = new BasicStroke (width);	
    }
    
    //------------------------------------------------------------
    // Set methods
    //------------------------------------------------------------
    
    public void setSrc (Component src) {
	this.src = src;
    }
    
    public void setDest (Component dst) {
	this.dst = dst;	
    }
    
    public void setSrcPort (Port p) {
	srcPort = p;
    }
    
    public void setDestPort (Port p) {
	dstPort = p;
    }
    
    public void setWidth (float width) {
	this.width = width;
	stroke = new BasicStroke (width);	
    }
    
    /**
     * Set the line specification for the connector.  The line should
     * extend between the points (x1,y1) and (x2,y2), and the coordinates
     * are specified wrt the parent component
     */
    public void setLine (int x1, int y1, int x2, int y2) {	
	
	int minX, maxX;		
	if (x1 < x2) {
	    minX = x1;
	    maxX = x2;
	}
	else {
	    minX = x2;
	    maxX = x1;
	}
	
	int minY, maxY;
	if (y1 < y2) {
	    minY = y1;
	    maxY = y2;
	}
	else {
	    minY = y2;
	    maxY = y1;
	}
	
	// The bounds for this component within the parent: 
	// note y2 = min(y1,y2).  We leave an extra buffer 
	// on either side to accomodate for the width of the lines
	int startX, startY, lenX, lenY;
	startX = minX - BORDER_BUFFER;
	startY = minY - BORDER_BUFFER; 
	lenX =   maxX - minX + 2 * BORDER_BUFFER;
	lenY =   maxY - minY + 2 * BORDER_BUFFER;

	if((maxX-minX)<width) lenX += width;
	if((maxY-minY)<width) lenY += width;

	setBounds (startX, startY, lenX, lenY);

	// Line for the queue
	Point2D.Double p1 = new Point2D.Double(x1 - minX + BORDER_BUFFER, 
					       y1 - minY + BORDER_BUFFER);
	Point2D.Double p2 = new Point2D.Double(x2 - minX + BORDER_BUFFER, 
					       y2 - minY + BORDER_BUFFER);
	line = new Line2D.Double (p1, p2);	
    }
    
    public void setLineColor (Color c) {
	lineColor = c;
    }

    public void setStroke (Stroke stroke) {
	this.stroke = stroke;
    }
    
    //------------------------------------------------------------
    // Get methods
    //------------------------------------------------------------
    
    public Component getSrc () {
	return src;
    }
    
    public Component getDest () {
	return dst;
    }
    
    public Port getSrcPort () {
	return srcPort;
    }
    
    public Port getDestPort () {
	return dstPort;
    }
    
    public Dimension getPreferredSize () {
	return getSize ();
    }
    
    //------------------------------------------------------------
    
    protected void paintComponent (Graphics g) {	
	if (line == null) return;
	
	Graphics2D g2d = (Graphics2D)g.create();
	g2d.setColor (lineColor);
	g2d.setRenderingHints (hints);		
	g2d.setStroke (stroke);	
	g2d.draw(line);
	g2d.fill(line);
	g2d.dispose();
    }
    
    /**
     * React to movement of the components I am connecting
     */
    public void entityMoved (Component comp) {
	// Should never happen
	if (comp != src && comp != dst) {
	    return;
	}
	
	// new line
	setLine (src.getX() + srcPort.xOffset,
		 src.getY() + srcPort.yOffset,
		 dst.getX() + dstPort.xOffset,
		 dst.getY() + dstPort.yOffset);
    }

    public void setLineFromEnds () {
	setLine (src.getX() + srcPort.xOffset,
		 src.getY() + srcPort.yOffset,
		 dst.getX() + dstPort.xOffset,
		 dst.getY() + dstPort.yOffset);	
    }
}
