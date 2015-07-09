package stream.vis.view;

import java.awt.*;

public class SynStoreConnector extends Connector {
    /// Color for the connector
    private static final Color color = Color.magenta;
    
    public SynStoreConnector () {
	super ();
	setLineColor (color);
	
	float dash1[] = {3.0f};
	setStroke (new BasicStroke (1.0f, 
				    BasicStroke.CAP_BUTT, 
				    BasicStroke.JOIN_MITER, 
				    10.0f, dash1, 0.0f));
    }
    
    //------------------------------------------------------------
    // Set methods
    //------------------------------------------------------------
    
    public void setSrc (Component src) {
	super.setSrc (src);
	
	// Set the port
	int xOffset = src.getPreferredSize().width/2;
	int yOffset = src.getPreferredSize().height;
	setSrcPort (new Port (xOffset, yOffset));
    }
    
    public void setDest (Component dest) {
	super.setDest (dest);
	
	// Set the port
	Dimension size = dest.getPreferredSize();
	int xOffset = size.width/2;
	int yOffset = 0;
	setDestPort (new Port (xOffset, yOffset));
    }
}
