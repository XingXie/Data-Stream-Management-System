package stream.vis.view.result;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import java.util.Timer;
import java.util.TimerTask;
import stream.vis.data.*;


public class RelationDisplay extends JFrame {
    
    private QueryResult result;
    
    private JTable resultTable;
    
    private RelationDisplayModel model;
    
    private TableRefresher refresher;
    
    public RelationDisplay (QueryResult res) {
	super ();
	
	result = res;
	
	model = new RelationDisplayModel (result);
	resultTable = new JTable (model);
	setColumnHeaders (resultTable);
	
	JScrollPane scrollPane = new JScrollPane (resultTable);
	scrollPane.setVerticalScrollBarPolicy
	    (JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	scrollPane.setAutoscrolls(true);
	
	setDisplayTitle ();	
	getContentPane().add (scrollPane);
	pack ();	
	
	refresher = new TableRefresher (model);
    }
    
    private void setDisplayTitle () {
	setTitle ("Query " + result.getQuery().getQueryId());
    }
    
    private void setColumnHeaders (JTable table) {
	// Set the timestamp column
	table.getColumnModel().getColumn(0).setHeaderValue ("Timestamp");
	
	// Set the sign column
	table.getColumnModel().getColumn(1).setHeaderValue ("Ins/Del");
	
	// If the query output has been named by the user, we use it
	// to display the column headings, otherwise we use defaults
	if (result.getQuery().isNamed())
	    setNamedCols (table);
	else
	    setDefaultColNames (table);
    }
    
    private void setNamedCols (JTable table) {
	TableColumnModel colModel = table.getColumnModel ();
	NamedTable schema = result.getQuery().getNamedTable ();
	
	for (int c = 2 ; c < colModel.getColumnCount() ; c++) {
	    TableColumn col = colModel.getColumn(c);
	    col.setHeaderValue (schema.getAttrName (c-2));
	}
    }
    
    private void setDefaultColNames (JTable table) {
	TableColumnModel colModel = table.getColumnModel ();
	
	for (int c = 2 ; c < colModel.getColumnCount() ; c++) {
	    TableColumn col = colModel.getColumn(c);
	    col.setHeaderValue ("Attr " + (c-1));
	}
    }
    
    private class TableRefresher extends TimerTask {
	/// The model for the table we are refreshing
	private RelationDisplayModel model;
	
	/// The timer to keep scheduling us periodically
	private Timer timer;
	
	/// The frequency at which we want to refresh: 150 millisecs
	private static final int PERIOD = 150;
	
	public TableRefresher (RelationDisplayModel model) {
	    super ();
	    this.model = model;
	    
	    timer = new Timer ();
	    timer.schedule (this, 0, PERIOD);
	}
	
	public void run () {
	    model.refresh ();
	}
	
	public void stop () {
	    timer.cancel ();
	}
    }
    
    private class RelationDisplayModel extends AbstractTableModel {
	/// The query result which we are showing
	QueryResult result;
	
	/// The number of tuples which the table has shown
	int numTuplesLast;
	
	/// The table only shows a window of last few tupls ...
	int windowSizeLast;
	
	public RelationDisplayModel (QueryResult result) {	    
	    this.result = result;
	}
	
	public int getRowCount () {
	    return result.getNumAvailableTuples ();
	}
	
	// one additional column for timestamps
	public int getColumnCount () {
	    return result.getSchema().getNumAttrs() + 2;
	}
	
	public Object getValueAt (int row, int column) {
	    
	    Tuple tuple = result.get (row);
	    
	    if (tuple == null) {
		return "";
	    }
	    
	    assert (column < result.getSchema().getNumAttrs() + 2);
	    
	    // Timestamp
	    if (column == 0)
		return new Integer (tuple.getTimestamp());
	    
	    if (column == 1) {
		int sign = tuple.getSign ();
		if (sign == Tuple.PLUS)
		    return new Character ('+');
		else
		    return new Character ('-');
	    }
	    
	    // regular data column
	    return tuple.getAttr (column - 2);
	}
	
	public Class getColumnClass (int column) {
	    return getValueAt (0, column).getClass();
	}
	
	public void refresh () {
	    int numTuples, windowSize;
	    
	    // We don't want result to get updated while we are refreshing
	    synchronized (result) {
		numTuples = result.getNumTuples ();
		windowSize = result.getNumAvailableTuples ();
		int numIns = numTuples - numTuplesLast;
		
		if (numIns > 0) {
		    fireTableRowsInserted (0, numIns - 1);
		    fireTableRowsDeleted (windowSize, 
					  windowSizeLast + numIns - 1);
		}
	    }
	    
	    numTuplesLast = numTuples;
	    windowSizeLast = windowSize;
	}
    }
}
