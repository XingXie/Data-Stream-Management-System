package stream.vis.view;

import stream.vis.data.EntityProperty;
import stream.vis.data.Client;
import java.awt.*;

/**
 * Set of routines implemented by all entities views - queues, operators
 * synopses and stores.  This is a generalization of highlightable
 * that was present earlier
 */

public interface EntityView {
    
    /// highlight the entity
    public void highlight ();
    
    /// remove the highlight
    public void unHighlight ();
    
    /// Get the list of properties for the entity being viewed
    public EntityProperty getEntityProperty ();
    
    /// Handle a request for a monitor - we need the client to complete
    /// the monitor creation
    public void handleMonitorRequest (Client client, 
				      ClientView clientView,
				      int x, int y);
    
    /// Glorified cast ...
    public Component getComponent ();
    
    /// Add a listener who is interested in the entity's motion
    public void addMotionListener (MotionListener listener);
}
