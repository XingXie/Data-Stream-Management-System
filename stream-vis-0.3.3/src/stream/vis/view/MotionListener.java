package stream.vis.view;

import java.awt.Component;

/**
 * Interface for listening to motion of entities in a plan.  This is used
 * by connectors to ensure that the connections are consistent.
 */
public interface MotionListener {
    public void entityMoved (Component comp);
}
