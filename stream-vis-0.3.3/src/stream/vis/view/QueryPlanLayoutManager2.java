package stream.vis.view;

import java.awt.*;
import java.util.*;
import stream.vis.data.*;
import stream.vis.data.Queue;
import java.util.List;

/**
 *  The Layout Manager for laying out query plans.  Broadly, the strategy
 * is as follows: we consider only the operator tree and assign operators
 * to "grids", where grids are formed by dividing the screen area into a equi
 * sized cells.  From the assignment of operators to grids their absolute
 * position can easily be determined.  
 *
 * Synopses and stores are "satellites" of operators and their posn follows
 * from the position of the owning operator.  Grids are large enough to
 * ensure that all the satellites fit along with the operator within one 
 * grid.
 */

public class QueryPlanLayoutManager2 implements LayoutManager2 {
    /// Used to keep track of changes in the query plan
    private boolean dirty;
    
    /// The required size for the QueryPlanView panel.
    private Dimension size;
    
    /// The width of each grid cell
    private static final int GRID_WIDTH = 125;
    
    /// Height of each grid cell
    private static final int GRID_HEIGHT = 100;
    
    /// We leave some buffer at the edge of the QueryPlanView panel
    private static final int BORDER_BUFFER = 40;
    
    /** 
     * Constants determining the position of the satellites around
     * an operator: [[ more comments ]]
     */
    
    /// Bottom satellite X offset from the operator edges
    private static final int BSAT_X_OFFSET = 20;
    
    /// Top satellite X offset from the operator axis of symmetry
    private static final int TSAT_X_OFFSET = 15;
    
    /// Top satellite Y offset from the operator Y offset
    private static final int TSAT_Y_OFFSET = -40;
    
    public QueryPlanLayoutManager2 () {
	dirty = true;
    }
    
    public void layoutContainer (Container parent) {
	if (!dirty || !(parent instanceof QueryPlanView))
	    return;
	
	QueryPlanView planView  = (QueryPlanView) parent;
	QueryPlan plan = planView.getQueryPlan ();
	
	// Layout the operators in a logical grid.  On return opToGrid 
	// contains the mapping of each operator to its grid position
	GridPos[] gridPos = computeGridLayout (plan, planView);
	
	// Compute the absolute position and bounds for an operator
	// based on its grid position
	Bounds [] opBounds = computeOpBounds (gridPos, planView);
	
	// Generate the positions of each of the plan component using
	// the grid layout
	generateComponentPos (opBounds, parent);
	
	// Set height and width of this component
	computeSize(gridPos);
	
	dirty = false;
    }
    
    public void addLayoutComponent(String name, Component comp) {
	if (comp instanceof MonitorView) {
	    MonitorView monView = (MonitorView)comp;
	    Component monEntity = monView.getMonitoredEntity().getComponent();
	    placeRelativeTo (monView, monEntity);
	}
	else if (comp instanceof MonitorEntityConnector) {
	    Connector conn = (Connector)comp;
	    conn.setLineFromEnds ();
	}
	//dirty = true;
    }
    
    public void addLayoutComponent(Component comp, Object constraints) {
	if (comp instanceof MonitorView) {
	    MonitorView monView = (MonitorView)comp;
	    Component monEntity = monView.getMonitoredEntity().getComponent();
	    placeRelativeTo (monView, monEntity);
	}
	
	else if (comp instanceof MonitorEntityConnector) {
	    Connector conn = (Connector)comp;
	    conn.setLineFromEnds ();
	}
	//dirty = true;
    }
    
    public void removeLayoutComponent(Component comp) {
	//dirty = true;
    }
    
    public void invalidateLayout(Container target) {
	//dirty = true;
    }
    
    public float getLayoutAlignmentX(Container target) {
	return 0.5f;   //centered
    }
    
    public float getLayoutAlignmentY(Container target) {
	return 0.5f;   //centered
    }
    
    public Dimension minimumLayoutSize(Container parent) {
	return getSize(parent);
    }
    
    public Dimension preferredLayoutSize(Container parent) {	
	return getSize(parent);
    }
    
    public Dimension maximumLayoutSize(Container parent) {
	return getSize(parent);
    }
    
    private Dimension getSize(Container parent) {
	if (dirty) {
	    layoutContainer(parent);
	}
	return size;
    }
    
    private GridPos[] computeGridLayout (QueryPlan plan, 
					 QueryPlanView planView) {
	
	// Compute the leaf operators 
	boolean[] isLeaf = computeLeafs (plan, planView);
	
	// Assign "levels" to each operator.  A level indicates how high
	// an operator appears in the plan. On return levels[i] is the level
	// for operator 'i'
	int[] levels = computeLevels (plan, planView);
	
	// Assign hval (for horizontal value) to each operator.  A smaller 
	// hval indicates that the operator should be more to the left and 
	// a larger hval indicates that the operator should be more the right
	int[] hvals = computeHvals (plan, planView);
	
	// Order the operators by hvals
	int[] opsInHvalOrder = orderByHval (hvals);
	
	// Maximum level of an operator
	int maxLevel = getMaxLevel (levels);
	
	// We determine the positions in the decreasing order
	// of levels
	GridPos[] posns = new GridPos[plan.getNumOps()];
	for (int l = maxLevel ; l >= 0 ; l--) {
	    computeGridPosn (l, levels, opsInHvalOrder, 
			     plan, planView, isLeaf, posns);
	}
	
	return posns;
    }
    
    // We compute the actual positions of the operators based on their grid
    // positions.  This is straightforward, except for one special case
    private Bounds[] computeOpBounds (GridPos[] gridPos, 
				      QueryPlanView planView) {
	int numOps = gridPos.length;
	
	Bounds[] bounds = new Bounds [numOps];
	
	for (int o = 0 ; o < numOps ; o++) {
	    
	    if (!planView.isVisible(o))
		continue;
	    
	    // normal case
	    Dimension preferredSize = 
		planView.getOpView(o).getPreferredSize();
	    
	    bounds[o] = new Bounds();
	    bounds[o].width = preferredSize.width;
	    bounds[o].height = preferredSize.height;
	    
	    // We want to place the operator in the center of the cell -
	    // so the adjustment (GRID_WIDTH - width)/2
	    bounds[o].x = gridPos[o].hpos*GRID_WIDTH + BORDER_BUFFER + 
		(GRID_WIDTH - preferredSize.width)/2;
	    bounds[o].y = gridPos[o].level*GRID_HEIGHT + BORDER_BUFFER;
	}
	
	return bounds;
    }
    
    private void generateComponentPos (Bounds[] opBounds, Container parent) {
	Component[] components = parent.getComponents ();
	List connectors = new ArrayList ();

	for (int c = 0 ; c < components.length ; c++) {
	    
	    if (components[c] instanceof OperatorView) {
		generateOpPos (opBounds, (OperatorView)components[c]);
	    }
	    
	    else if (components[c] instanceof StoreView) {
		generateStorePos (opBounds, (StoreView)components[c]);
	    }
	    
	    else if (components[c] instanceof SynopsisView) {
		generateSynPos (opBounds, (SynopsisView)components[c]);
	    }
	    else if (components[c] instanceof Connector) {
		connectors.add (components [c]);
	    }
	}
	
	// Layout all the connectors
	for (int c = 0 ; c < connectors.size () ; c++)
	    ((Connector)connectors.get(c)).setLineFromEnds ();
	
    }
    
    private void generateOpPos (Bounds[] opBounds, OperatorView opView) {
	int opid = opView.getOp().getId();
	opView.setBounds (opBounds[opid].x, 
			  opBounds[opid].y,
			  opBounds[opid].width,
			  opBounds[opid].height);
    }
    
    private void generateStorePos (Bounds[] opBounds, StoreView sview) {
	int ownOp = sview.getStore().getOwnOp ();
	
	int x = opBounds[ownOp].x + opBounds[ownOp].width/2 + 
	    TSAT_X_OFFSET;
	int y = opBounds[ownOp].y + TSAT_Y_OFFSET;
	
	sview.setLocation (x, y);
    }    
    
    private void generateSynPos (Bounds[] opBounds, 
				 SynopsisView sview) {	
	int ownOp = sview.getSyn().getOwnOp();
	int synType = sview.getSyn().getType();
	int synWidth = sview.getPreferredSize().width;
	int synHeight = sview.getPreferredSize().height;
	int type = sview.getSyn().getType();
	
	int x, y;
	
	if (type == Synopsis.LEFT) {
	    x = opBounds[ownOp].x - BSAT_X_OFFSET - synWidth;
	    y = opBounds[ownOp].y + opBounds[ownOp].height/2 - synHeight/2;
	}
	else if (type == Synopsis.RIGHT || type == Synopsis.CENTER) {
	    x = opBounds[ownOp].x + opBounds[ownOp].width + BSAT_X_OFFSET;
	    y = opBounds[ownOp].y + opBounds[ownOp].height/2 - synHeight/2;
	}
	else {
	    x = opBounds[ownOp].x + opBounds[ownOp].width/2 - TSAT_X_OFFSET - synWidth;
	    y = opBounds[ownOp].y + TSAT_Y_OFFSET;
	}
	
	sview.setLocation (x,y);
    }
    
    private void computeSize (GridPos[] gridPosns) {
	int maxXGrid = 0;
	int maxYGrid = 0;
	
	for (int o = 0 ; o < gridPosns.length ; o++) { 
	    if (gridPosns[o] == null)
		continue;

	    if (gridPosns[o].hpos > maxXGrid)
		maxXGrid = gridPosns[o].hpos;
	    if (gridPosns[o].level > maxYGrid)
		maxYGrid = gridPosns[o].level;
	}
	
	int maxX = (maxXGrid+1) * GRID_WIDTH + 2*BORDER_BUFFER;
	int maxY = (maxYGrid+1) * GRID_HEIGHT + 2*BORDER_BUFFER;
	
	size = new Dimension (maxX, maxY);
    }
    
    // Compute the root operators of a query plan.  These are operators
    // that are not input to any other operators
    private boolean[] computeRoots (QueryPlan plan) {
	boolean[] isRoot = new boolean [plan.getNumOps()];
	
	// Optimistically initialize
	for (int o = 0 ; o < isRoot.length ; o++)
	    isRoot [o] = true;
	
	for (int o = 0 ; o < isRoot.length ; o++) {	    
	    Operator op = plan.getOp (o);
	    for (int i = 0 ; i < op.getNumInputs() ; i++)
		isRoot[op.getInput(i)] = false;
	}
	
	return isRoot;
    }
    
    // Compute the leaf operators of a query plan.
    private boolean[] computeLeafs (QueryPlan plan, QueryPlanView planView) {
	
	boolean[] isLeaf = new boolean [plan.getNumOps()];
	
	for (int o = 0 ; o < isLeaf.length ; o++) {
	    if ((plan.getOp(o).getNumInputs() == 0) &&
		planView.isVisible (o))
		isLeaf[o] = true;
	    else
		isLeaf[o] = false;
	}
	
	return isLeaf;
    }
    
    // Compute the levels for operators.  We want to ensure that each 
    // operator is at a lower level than any operator that reads off it.  
    // (Levels are nonnegative and 0 is the highest level)
    private int[] computeLevels (QueryPlan plan, QueryPlanView planView) {
	
	int[] levels = new int[plan.getNumOps()];
	for (int o = 0 ; o < levels.length ; o++)
	    levels [o] = -1;
	
	int rootOp = planView.getRootOp();
	levels [rootOp] = 0;
	computeLevelsRec (rootOp, plan, levels);
	
	return levels;
    }
    
    // Compute levels recursively - called by computeLevels()
    private void computeLevelsRec (int opId, QueryPlan plan, int[] levels) {
	Operator op = plan.getOp (opId);
	
	for (int c = 0 ; c < op.getNumInputs() ; c++) {
	    int childId = op.getInput (c);
	    
	    if (levels [childId] < levels[opId] + 1)
		levels [childId] = levels[opId] + 1;
	    
	    computeLevelsRec (childId, plan, levels);
	}
    }
    
    private int getMaxLevel (int[] levels) {
	int maxLevel = 0;
	
	for (int o = 0 ; o < levels.length ; o++)
	    if (levels[o] > maxLevel)
		maxLevel = levels[o];
	return maxLevel;
    }
    
    // Basically we do a inorder traversal of the query plan, ensuring 
    // that the left children of a node are visited before the right
    // children.  The hval for a node is its "visit" number.
    private int[] computeHvals (QueryPlan plan, QueryPlanView planView) {
	int numOps = plan.getNumOps();
	int[] hvals = new int[numOps];
	
	// initialize
	for (int o = 0 ; o < numOps ; o++)
	    hvals [o] = -1; // not set
	
	int curHval = 0;
	curHval = computeHvalsRec (planView.getRootOp(), plan, curHval, hvals);
	
	return hvals;
    }
    
    private int computeHvalsRec (int opId, QueryPlan plan, 
				 int curHval, int[] hvals) {
	
	// We have visited this node before
	if (hvals [opId] != -1) 
	    return curHval;
	
	// We only understand binary operators as of now
	Operator op = plan.getOp (opId);
	int numInputs = op.getNumInputs ();
	
	if (numInputs == 0) {
	    // Visit myself
	    hvals [opId] = curHval;
	    curHval++;
	    return curHval;
	}
	
	if (numInputs == 1) {
	    // Visit myself 
	    hvals [opId] = curHval;
	    curHval ++;
	    // & then my child
	    return computeHvalsRec (op.getInput(0), plan, curHval, hvals);
	}
	
	// Visit the left child first
	curHval = computeHvalsRec (op.getInput(0), plan, curHval, hvals);
	
	// Visit mysefl
	hvals [opId] = curHval;
	curHval++;
	
	// visit the rest of the children
	for (int c = 1; c < numInputs ; c++) {
	    int childId = op.getInput (c);
	    curHval = computeHvalsRec (childId, plan, curHval, hvals);
	}
	
	return curHval;
    }
    
    private int[] orderByHval (int[] hvals) {
	int[] opsByHval = new int[hvals.length];
	for (int o = 0 ; o < hvals.length ; o++)
	    opsByHval [o] = -1;

	for (int o = 0 ; o < hvals.length ; o++) {
	    if (hvals[o] != -1)
		opsByHval [hvals[o]] = o;
	}
	return opsByHval;
    }
    
    private void computeGridPosn (int curLevel, 
				  int[] levels,
				  int[] opsInHvalOrder,
				  QueryPlan plan,
				  QueryPlanView planView,
				  boolean[] isLeaf,
				  GridPos[] posns) {
	boolean firstOp = true;
	int prevPos = 0;
	
	// We layout the ops within the curLevel by their hval order
	// but we try greedily to place them just above their children
	for (int p = 0 ; p < opsInHvalOrder.length ; p++) {
	    
	    int opId = opsInHvalOrder [p];
	    if (opId == -1)
		continue;
	    
	    // Ignore ops at other levels
	    if (levels [opId] != curLevel)
		continue;
	    
	    assert (planView.isVisible (opId));
	    
	    // Get the best position for the operator based on its 
	    // children's positions
	    int bestPos = getBestPosn (opId, plan, isLeaf, posns, 
				       opsInHvalOrder);
	    
	    // if this is not the first operator in this level,
	    // we have to ensure the ordering ...
	    if (!firstOp) {
		if (bestPos <= prevPos)
		    bestPos = prevPos + 1;		
	    }
	    
	    posns [opId] = new GridPos (curLevel, bestPos);
	    
	    prevPos = bestPos;
	    firstOp = false;	    
	}
    }
    
    int getBestPosn (int opId, QueryPlan plan, 
		     boolean[] isLeaf, GridPos[] posns,
		     int[] opsInHvalOrder) {
	
	Operator op = plan.getOp (opId);	
	int numInputs = op.getNumInputs ();
	
	if (numInputs == 0) 
	    return getBestLeafPosn (opId, isLeaf, opsInHvalOrder);
	
	// The best position for single input operator is 
	// directly above its first input
	if (numInputs == 1)
	    return posns[op.getInput(0)].hpos;	
	
	// The best position for a double input operator 
	// is the median of its children's positions
	return (posns [op.getInput(0)].hpos + posns [op.getInput(1)].hpos) / 2;
    }
    
    int getBestLeafPosn (int opId, boolean[] isLeaf, int[] opsInHvalOrder) {
	int pos = 0;
	
	for (int o = 0 ; o < opsInHvalOrder.length ; o++) {
	    if (opsInHvalOrder[o] == opId)
		break;
	    
	    if (isLeaf[opsInHvalOrder[o]])
		pos += 2;
	}
	
	return pos;
    }
    
    // Position within an integer grid
    private class GridPos {
	public int level;
	public int hpos;
	GridPos (int l, int h) {
	    level = l;
	    hpos = h;
	}
    }    
    
    // Bounds for an operator (conforming to setBounds() method)
    private class Bounds {
	public int x;
	public int y;
	public int width;
	public int height;
    }
    
    private static final int MON_OFFSET = 80;
    private void placeRelativeTo (Component monitor, Component monEntity) {
	// parent = monitored entity
	Rectangle parentBounds = monEntity.getBounds();
	Dimension monSize = monitor.getPreferredSize ();
	
	int x, y;
	
	x = parentBounds.x + parentBounds.width/2 
	    + MON_OFFSET - monSize.width/2;
	
	y = parentBounds.y + parentBounds.height/2 - monSize.height/2;
	
	monitor.setLocation (x, y);
    }
}
