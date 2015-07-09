package stream.vis.view;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

/**
 * Legend pane displays the legend for the symbols used in a query plan.
 *
 * It diplays a vertical list of symbols and descriptions for the symbols.
 */ 

public class LegendPane extends JPanel {
    
    /// The vertical gap between the consecutive legend entries
    private static final int VGAP = 5;
    
    // All symbols are placed in equidimension panels for proper alignment    
    private static final int SYMBOL_PANEL_WIDTH = 40;    
    private static final int SYMBOL_PANEL_HEIGHT = 30;
    
    public LegendPane () {	
	
	setLayout (new BorderLayout (5,5));
	setTitle ();
	
	JPanel legendPane = new JPanel ();
	legendPane.setLayout(new BoxLayout(legendPane, BoxLayout.Y_AXIS));
	
	// Operator
	legendPane.add (getLegendEntry (new OperatorLegend(), "Operator"));
	legendPane.add (Box.createVerticalStrut(VGAP));	
	
	// Stream Queue
	legendPane.add (getLegendEntry (new QueueLegend (true), 
			     "Queue carrying stream"));
	legendPane.add (Box.createVerticalStrut(VGAP));	
	
	// Relation Queue
	legendPane.add (getLegendEntry (new QueueLegend (false), 
					"Queue carrying relation"));
	legendPane.add (Box.createVerticalStrut(VGAP));
	
	// Store
	legendPane.add (getLegendEntry (new StoreLegend (), "Store"));
	legendPane.add (Box.createVerticalStrut(VGAP));
	
	// Synopsis
	legendPane.add (getLegendEntry (new SynopsisLegend (), "Synopsis"));
	legendPane.add (Box.createVerticalGlue ());
	
	add (legendPane, BorderLayout.CENTER);
	setOpaque(true);
    }
    
    private void setTitle () {
	JLabel text = new JLabel("Plan Legend");
	text.setFont(text.getFont().deriveFont(Font.BOLD));
	add(text, BorderLayout.NORTH);
    }
    
    /// Get the panel for one entry in the legend which consists of an
    /// image and a description of the image
    private JPanel getLegendEntry (Component legendSymbol, String desc) {
	
	// We place the symbol in the center of a fixed size panel - to 
	// ensure a proper alignment ...
	JPanel legendSymbolPanel = getLegendSymbolPanel (legendSymbol);
	
	// Create a label for the description ...
	JLabel text = new JLabel (desc);
	text.setFont(text.getFont().deriveFont(Font.PLAIN));
	
	// ... and put it all together
	JPanel legendEntry = new JPanel ();
	legendEntry.setLayout(new FlowLayout(FlowLayout.LEADING, 10, 0));
	legendEntry.setOpaque (true);
	legendEntry.add (legendSymbolPanel);
	legendEntry.add (text);	
	return legendEntry;
    }
    
    /// Create a fixed size panel and place the symbol in the middle 
    /// of the panel.
    private JPanel getLegendSymbolPanel (Component legendSymbol) {
	// A fixed size panel ...
	JPanel legendSymbolPanel = new JPanel ();
	legendSymbolPanel.setPreferredSize 
	    (new Dimension (SYMBOL_PANEL_WIDTH, SYMBOL_PANEL_HEIGHT));	
	
	// We don't want to use a layout manager since they all screw up ...
	legendSymbolPanel.setLayout (null);
	
	// add the symbol
	legendSymbolPanel.add (legendSymbol);
	
	// ... we place the symbol in the center.
	int symbolWidth = legendSymbol.getPreferredSize().width;
	int symbolHeight = legendSymbol.getPreferredSize().height;
	int xpos = (SYMBOL_PANEL_WIDTH - symbolWidth)/2;
	int ypos = (SYMBOL_PANEL_HEIGHT - symbolHeight)/2;
	legendSymbol.setBounds (xpos, ypos, symbolWidth, symbolHeight);
	
	return legendSymbolPanel;
    }

    /**
     * The symbol used for operators in the legend.  The symbol is just a 
     * square of the same color as the operator.
     */ 
    
    private class OperatorLegend extends JComponent {
	
	/// The length of the edge of the square
	private final int EDGE_LEN = 20;
	
	/// The stroke for drawing the lines
	Stroke stroke;
	
	/// The fill color
	private final Color FILL_COLOR = Color.cyan;
	
	/// The square shape
	Shape square;
	
	/// Size of the component
	Dimension size;
	
	public OperatorLegend () {
	    super ();
	    
	    // Border
	    setBorder (BorderFactory.createRaisedBevelBorder());
	    
	    // The stroke for drawing the line
	    stroke = new BasicStroke (1);
	    
	    Insets insets = getInsets();
	    square = new Rectangle2D.Double (insets.left,
					     insets.top,
					     EDGE_LEN,
					     EDGE_LEN);
	    
	    size = new Dimension (EDGE_LEN + insets.left + insets.right,
				  EDGE_LEN + insets.top + insets.bottom);
	}
	
	public Dimension getPreferredSize () {
	    return size;
	}
	
	protected void paintComponent (Graphics g) {	
	    Graphics2D g2d = (Graphics2D)g.create();
	    g2d.setStroke (stroke);
	    g2d.setColor (FILL_COLOR);
	    g2d.fill (square);
	    g2d.dispose();
	}
    }
    
    /**
     * The symbol used for queues in the legend.  The symbol is just a 
     * straight line with the appropriate queue color
     */ 
    
    private class QueueLegend extends JComponent {
	
	/// The length of the line 
	private final int LINE_LEN = 30;
	
	/// The buffer space we leave in the borders
	private final int BORDER_BUFFER = 1;
	
	/// The stroke for drawing the lines
	Stroke stroke;
	
	/// Rendering hint to enable anti-aliasing
	RenderingHints hints;    
	
	/// The color for the line 
	private Color lineColor;
	
	/// the line
	Line2D.Double line;
	
	/// Size of the component
	Dimension size;
	
	/// Queue color if a stream flows through the queue
	private final Color STREAM_COLOR = Color.orange;
	
	/// Queue color if a relation flows through the queue
	private final Color RELATION_COLOR = Color.red;    
	
	public QueueLegend (boolean isStream) {	
	    super ();
	    
	    // Color of the line is diff for stream and reln queues
	    if (isStream)
		lineColor = STREAM_COLOR;
	    else
		lineColor = RELATION_COLOR;
	    
	    // hints that encodes that anti-aliasing has to be turned on
	    hints = new RenderingHints (RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
	    
	    hints.add (new RenderingHints
		       (RenderingHints.KEY_RENDERING,
			RenderingHints.VALUE_RENDER_QUALITY));	
	    
	    // The stroke for drawing the line
	    stroke = new BasicStroke (2);
	    
	    size = new Dimension (LINE_LEN + 2*BORDER_BUFFER, 2*BORDER_BUFFER);
	    
	    line = new Line2D.Double (BORDER_BUFFER,
				      BORDER_BUFFER,
				      BORDER_BUFFER + LINE_LEN,
				      BORDER_BUFFER);
	    
	    setOpaque (true);
	}
    
	public Dimension getPreferredSize () {
	    return size;
	}
    
	protected void paintComponent (Graphics g) {
	    Graphics2D g2d = (Graphics2D)g.create();
	
	    g2d.setRenderingHints (hints);
	    g2d.setStroke (stroke);
	    g2d.setColor (lineColor);
	    g2d.draw (line);
	    g2d.dispose();
	} 
    }    
    
    private class SynopsisLegend extends JComponent {
	//------------------------------------------------------------
	// Dimensions
	//------------------------------------------------------------    
    
	/// The height of the synopsis
	private final int HEIGHT = 20;
    
	/// The width
	private final int WIDTH = 20;
    
	/// Border buffer
	private final int BORDER_BUFFER = 2;
	
	/// Size of the component
	private final Dimension size 
	    = new Dimension (BORDER_BUFFER*2 + WIDTH,
			     BORDER_BUFFER*2 + HEIGHT);
	
	//------------------------------------------------------------
	// Painting related
	//------------------------------------------------------------
    
	/// The stroke for drawing the lines
	private final Stroke stroke = new BasicStroke (1);
    
	/// Rendering hint to enable anti-aliasing
	RenderingHints hints;
    
	/// The color of the line
	private final Color LINE_COLOR = Color.black;    
    
	/// The fill color
	private final Color FILL_COLOR = Color.pink;
	
	/// The circle representing the synopsis
	private final Ellipse2D circle = 
	    new Ellipse2D.Double (BORDER_BUFFER, BORDER_BUFFER,
				  WIDTH, HEIGHT);
	
	public SynopsisLegend () {
	    // hints that encodes that anti-aliasing has to be turned on
	    hints = new RenderingHints (RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
	    
	    hints.add (new RenderingHints
		       (RenderingHints.KEY_RENDERING,
			RenderingHints.VALUE_RENDER_QUALITY));
	    
	}
	
	public Dimension getPreferredSize () {
	    return size;
	}
	
	protected void paintComponent (Graphics g) {	
	    Graphics2D g2d = (Graphics2D)g.create();	
	    g2d.setRenderingHints (hints);
	    g2d.setStroke (stroke);	
	    
	    // Fill ...
	    g2d.setColor (FILL_COLOR);
	    g2d.fill (circle);
	    
	    // Draw the lines
	    g2d.setColor (LINE_COLOR);
	    g2d.draw (circle);
	    g2d.dispose();
	}
    }

    private class StoreLegend extends JComponent {
	/// The stroke for drawing the lines
	Stroke stroke;
    
	/// Rendering hint to enable anti-aliasing
	RenderingHints hints;
    
	/// The color of the line
	private final Color LINE_COLOR = Color.black;    
    
	/// The fill color
	private final Color FILL_COLOR = Color.yellow;
    
	/// The height of the synopsis
	private final int HEIGHT = 27;
    
	/// The width
	private final int WIDTH = 16;
    
	/// Border buffer
	private final int BORDER_BUFFER = 2;
    
	/// Size of the component
	Dimension size;
    
	/// Horizontal lines (4 in num)
	Line2D.Double[] hlines;
    
	/// Number of horizontal lines
	private final int NUM_HLINES = 4;
    
	/// Vertical lines (3 in num)
	Line2D.Double[] vlines;
    
	/// The full rectangle for fill purposes
	Rectangle2D.Double shape;
    
	/// Number of vertical lines
	private final int NUM_VLINES = 3;
	
	public StoreLegend () {

	    // hints that encodes that anti-aliasing has to be turned on
	    hints = new RenderingHints (RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
	    
	    hints.add (new RenderingHints
		       (RenderingHints.KEY_RENDERING,
			RenderingHints.VALUE_RENDER_QUALITY));	
	    
	    // The stroke for drawing the line
	    stroke = new BasicStroke (1);
	    
	    createLines ();
	    setSize ();	    
	}

	public Dimension getPreferredSize () {
	    return size;
	}

	protected void paintComponent (Graphics g) {
	    
	    Graphics2D g2d = (Graphics2D)g.create();
	    g2d.setRenderingHints (hints);
	    g2d.setStroke (stroke);	
	    
	    g2d.setColor (FILL_COLOR);
	    
	    g2d.fill (shape);
	    
	    g2d.setColor (LINE_COLOR);
	    for (int l = 0 ; l < NUM_HLINES ; l++)
		g2d.draw (hlines [l]);
	    for (int l = 0 ; l < NUM_VLINES ; l++)
		g2d.draw (vlines [l]);
	    g2d.dispose();
	}

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
    
	private void setSize () {
	    size = new Dimension (BORDER_BUFFER*2 + WIDTH,
				  BORDER_BUFFER*2 + HEIGHT);
	}	
    }
}
