package stream.vis.view;

import java.awt.*;

public class MonitorEntityConnector extends Connector {
    

    /// Color for the connector
    private static final Color color = Color.black;
    
    public MonitorEntityConnector () {
	super ();
	setLineColor (color);
    }
    
    //------------------------------------------------------------
    // Set methods
    //------------------------------------------------------------
    
    // Src: monitor, port = middle left edge of the monitor
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
	
	if (dest instanceof QueueView) {
	    // Set the port = middle of the queue
	    int xOffset = dest.getPreferredSize().width/2;
	    int yOffset = dest.getPreferredSize().height/2;
	    setDestPort (new Port (xOffset, yOffset));	    
	}
	
	else {
	    // Set the port = right edge middlge
	    int xOffset = dest.getPreferredSize().width;
	    int yOffset = dest.getPreferredSize().height/2;
	    setDestPort (new Port (xOffset, yOffset));
	}
    }
    
    /**
     * React to movement of the components I am connecting
     */
    public void entityMoved (Component comp) {
	
	if (comp instanceof QueueView && comp == getDest()) {
	    // Set the port = middle of the queue
	    int xOffset = comp.getPreferredSize().width/2;
	    int yOffset = comp.getPreferredSize().height/2;
	    setDestPort (new Port (xOffset, yOffset));	    
	}
	
	super.entityMoved (comp);
    }
}
