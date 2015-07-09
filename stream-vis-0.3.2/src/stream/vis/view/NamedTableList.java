package stream.vis.view;

import stream.vis.data.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import java.util.List;

public class NamedTableList extends JTable {
    
    /// The table name column
    private static final int NAME_COLUMN = 0;
    
    /// The column that indicates if the table is a stream/relation
    private static final int TYPE_COLUMN = 1;
    
    /// The column that shows the attributes of the named table
    private static final int ATTR_COLUMN = 2;
    
    /// The width of the table as a fraction of the client view width
    private static final double WIDTH = 0.3;
    
    /// The height of the table as a fraction of the client view height
    private static final double HEIGHT = 0.38;
    
    /// List of tables we need to show
    List tables;
    
    /// parent 
    ClientView clientView;
    Client client;

    /// We will set the widths of the table columns manually since that
    /// auto setting does not use the horizontal scroll bar at all
    boolean colSizeSet = false;
    
    public NamedTableList (Client client, ClientView clientView) {
	super ();
	
	this.clientView = clientView;
	
	tables = client.getRegisteredTables ();
	
	// Create the table
	TableModel model = new TableModel (tables);	
	client.addListener (model);
	
	setModel (model);
	
	setColumnHeaders ();
	setColumnSelectionAllowed (false);
	setBackground (Color.white);
	
	// Turn off auto resize
	setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }

    protected void paintComponent(Graphics g) {
	// If this is the first invocation, set the column widths
	if (!colSizeSet) {
	    setColumnWidths (g);
	    colSizeSet = true;
	}
	
	super.paintComponent (g);
    }
    
    private void setColumnWidths (Graphics g) {
	TableColumnModel colModel;
	TableColumn col;
	int margin;
	int nameColWidth, typeColWidth, attrColWidth;
	
	// Used in getXXXColWidth methods to determine the width
	// of the strings
	FontMetrics metrics = g.getFontMetrics ();
	
	colModel = getColumnModel();
	margin = 2 * colModel.getColumnMargin ();
	
	// Determine the column widths
	nameColWidth = getNameColWidth (metrics, margin);
	typeColWidth = getTypeColWidth (metrics, margin);
	attrColWidth = getAttrColWidth (metrics, margin);
	
	// If the column widths use up less than the available width, scale
	// to use up all the width
	JScrollPane parent = clientView.getMainPane ();
	int scrollPaneWidth = parent.getViewportBorderBounds().width;	
	int totalWidth = nameColWidth + typeColWidth + attrColWidth;
	
	if (totalWidth < scrollPaneWidth) {
	    nameColWidth = 
		((nameColWidth * scrollPaneWidth) / totalWidth);
	    typeColWidth = 
		((typeColWidth * scrollPaneWidth) / totalWidth);
	    attrColWidth =
		((attrColWidth * scrollPaneWidth) / totalWidth);
	}
	
	col = colModel.getColumn (NAME_COLUMN);	
	col.setPreferredWidth (nameColWidth); 
	
	col = colModel.getColumn (TYPE_COLUMN);
	col.setPreferredWidth (typeColWidth);
	
	col = colModel.getColumn (ATTR_COLUMN);
	col.setPreferredWidth (attrColWidth);
    }
    
    private void setColumnHeaders() {
	TableColumn col;
	TableColumnModel colModel;
	
	colModel = getColumnModel();			
	col = colModel.getColumn (NAME_COLUMN);
	col.setHeaderValue ("Name");
	
	col = colModel.getColumn (TYPE_COLUMN);
	col.setHeaderValue ("Type");
	
	col = colModel.getColumn (ATTR_COLUMN);
	col.setHeaderValue ("Attributes");
    }    
    
    private int getNameColWidth (FontMetrics metrics, int margin) {
	// We want the minimum width to be twice as much needed to 
	// display name (since it looks nice :))
	int maxWidth = metrics.stringWidth ("Name") * 2 + margin;
	
	// Maximum over all the table names
	for (int t = 0 ; t < tables.size() ; t++) {
	    NamedTable table = (NamedTable)tables.get (t);
	    int width = metrics.stringWidth (table.getTableName ());
	    if (width > maxWidth)
		maxWidth = width;
	}
	
	// Without the extra nudge of 10 units, the display is not what
	// we want :(
	return maxWidth + margin + 10;
    }
    
    private int getTypeColWidth (FontMetrics metrics, int margin) {
	return metrics.stringWidth ("Relation") * 2 + margin;
    }
    
    private int getAttrColWidth (FontMetrics metrics, int margin) {
	int maxWidth = metrics.stringWidth ("Attributes")*2 + margin;
	
	for (int t = 0 ; t < tables.size() ; t++) {
	    NamedTable table = (NamedTable)tables.get (t);
	    int width = 
		metrics.stringWidth (table.getAttrEncoding());
	    if (width > maxWidth)
		maxWidth = width;
	}	
	
	return maxWidth + margin + 10;
    }
    
    private class TableModel extends AbstractTableModel 
	implements ClientListener {
	/// List of tables
	List tables;
	
	TableModel (List tables) {
	    this.tables = tables;
	}
	
	public int getRowCount () {
	    return tables.size();	    
	}
	
	public int getColumnCount () {
	    return 3;
	}
	
	public Object getValueAt (int row, int col) {
	    NamedTable table = (NamedTable)tables.get(row);
	    
	    // Stream name
	    if (col == 0)
		return table.getTableName();
		
	    // type
	    if (col == 1)
		if (table.isStream())
		    return "Stream";
		else
		    return "Relation";
	    
	    // Attr
	    if (col == 2) {
		return table.getAttrEncoding ();
	    }
	    
	    // should never come
	    return "";
	} 

	public void stateChanged (int oldState, int newState) {}	
	public void queryAdded (Query query, UnnamedTable outSchema) {}
	public void queryResultAvailable (QueryResult result) {}
	public void planGenerated (QueryPlan plan) {}
	public void monitorAdded (Monitor mon, Query qry, 
				  QueryResult res, QueryPlan plan) {}
	
	public void baseTableAdded (NamedTable table) {
	    colSizeSet = false;
	    fireTableRowsInserted (tables.size() - 1, tables.size() - 1);
	    clientView.showTableList();
	}
	public void viewAdded (Query query, NamedTable table) {
	    colSizeSet = false;
	    fireTableRowsInserted (tables.size() - 1, tables.size() - 1);
	}
	
	public void resetEvent () {
	    colSizeSet = false;
	    fireTableDataChanged ();
	} 
	
	public void closeEvent () {
	    
	}
    }
}
