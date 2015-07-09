package stream.vis.view;

import java.awt.*;

public class StoreOpConnector extends Connector {
    
    /// Color for the connector
    private static final Color color = Color.black;
    
    public StoreOpConnector () {
	super ();
	setLineColor (color);
    }
    
    //------------------------------------------------------------
    // Set methods
    //------------------------------------------------------------
    
    // Src: store, port = bottom left corner of the store
    public void setSrc (Component src) {
	super.setSrc (src);
	
	// Set the port
	int xOffset = 0;
	int yOffset = src.getPreferredSize().height/2;
	setSrcPort (new Port (xOffset, yOffset));
    }
    
    // Dst: operator, port = top right corner of the op
    public void setDest (Component dest) {
	super.setDest (dest);
	
	// Set the port
	int xOffset = dest.getPreferredSize().width/2;
	int yOffset = 0;
	setDestPort (new Port (xOffset, yOffset));
    }
}
