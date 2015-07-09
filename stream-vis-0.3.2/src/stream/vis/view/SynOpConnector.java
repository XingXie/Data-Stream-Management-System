package stream.vis.view;

import java.awt.*;
import stream.vis.data.Synopsis;

public class SynOpConnector extends Connector {
    /// The type of the synopsis
    int synType;
    
    /// Color for the connector
    private static final Color color = Color.black;
    
    public SynOpConnector (int synType) {
	super ();
	this.synType = synType;
	setLineColor (color);
    }
    
    //------------------------------------------------------------
    // Set methods
    //------------------------------------------------------------
    
    // Src: store, port = bottom left corner of the store
    public void setSrc (Component src) {
	super.setSrc (src);
	
	// Set the port	: this depends on whether the synopsis is to the
	// left of the operator or to the right
	int xOffset, yOffset;
	if (synType == Synopsis.LEFT || synType == Synopsis.OUTPUT) {
	    xOffset = src.getPreferredSize().width;
	}
	else {
	    xOffset = 0;
	}	
	yOffset = src.getPreferredSize().height/2;
	setSrcPort (new Port (xOffset, yOffset));
    }
    
    // Dst: operator, port = top right corner of the op
    public void setDest (Component dest) {
	super.setDest (dest);
	
	// Set the port
	int xOffset, yOffset;
	if (synType == Synopsis.LEFT) {
	    xOffset = 0;
	    yOffset = dest.getPreferredSize().height/2;
	}
	else if (synType == Synopsis.RIGHT || synType == Synopsis.CENTER) {
	    xOffset = dest.getPreferredSize().width;
	    yOffset = dest.getPreferredSize().height/2;
	}
	else {
	    xOffset = dest.getPreferredSize().width/2;
	    yOffset = 0;
	}
	
	setDestPort (new Port (xOffset, yOffset));
    }
}
