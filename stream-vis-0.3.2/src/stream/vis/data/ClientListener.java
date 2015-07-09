package stream.vis.data;

import stream.vis.net.TableFeeder;

public interface ClientListener {    
    /**
     * The state of the client changed from 'oldState' to 'newState'
     */
    public void stateChanged (int oldState, int newState);
    
    /**
     * A new base table has been added.
     */
    public void baseTableAdded (NamedTable table);
    
    /**
     * A new query has been added
     */
    public void queryAdded (Query query, UnnamedTable outSchema);
    
    /**
     * A new view has been specified
     */
    public void viewAdded (Query query, NamedTable table);
    
    /**
     * A query result is available
     */
    public void queryResultAvailable (QueryResult result);
    
    /**
     * Output of a monitor query available
     */
    public void monitorAdded (Monitor mon, Query qry, 
			      QueryResult res, QueryPlan plan);
    
    /**
     * Query plan has been generated
     */
    public void planGenerated (QueryPlan plan);
    
    /**
     * Reset event - clear everything on slate now and start
     * with a clean slate
     */
    public void resetEvent ();
    
    /**
     * Terminate event - close the visualizer
     */
    public void closeEvent ();    
}
