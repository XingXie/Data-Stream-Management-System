package stream.vis.view;

/**
 * A port is the location to which a connector connects at the border
 * of a component.  For example, a queue connects to the top-middle 
 * of it source operator, and the bottom-middle of its destinatin
 * operator.  This class essentially stores the location of the port
 * with respect to the (x,y) position of the component being connected to.
 */ 

public class Port {
        
    public int xOffset;
    public int yOffset;
    
    public Port (int x, int y) {
	xOffset = x;
	yOffset = y;
    }
}

