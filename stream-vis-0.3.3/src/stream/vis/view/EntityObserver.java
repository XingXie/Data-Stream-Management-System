package stream.vis.view;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import stream.vis.data.EntityProperty;

public class EntityObserver extends JPanel {
    
    /// The table displaying the selected entity's properties
    private JTable propertyTable;
    
    /// The key column in the property table
    private static final int KEY_COLUMN = 0;
    
    /// The value column in the property table
    private static final int VALUE_COLUMN = 1;
    
    /// The width of the table as a fraction of the client view width
    private static final double WIDTH = 0.3;
    
    /// The height of the table as a fraction of the client view height
    private static final double HEIGHT = 0.38;
    
    public EntityObserver (ClientView clientView) {
	// Create the table for showing the properties
	propertyTable = getEmptyPropertyTable (clientView);
	
	// We place the table within a scroll pane since we want
	// since the table could be arbitrarily large
	JScrollPane scrollPane = new JScrollPane(propertyTable);
	
	setLayout (new BorderLayout(5,5));
	add (scrollPane, BorderLayout.CENTER);
	
	// Heading
	JLabel heading = new JLabel ("Plan Component Details");
	heading.setFont (heading.getFont().deriveFont(Font.BOLD));
	add (heading, BorderLayout.NORTH);
    }
    
    public void bindEntity (EntityProperty property) {
	propertyTable.setModel (new EntityTableModel (property));
	setColumnHeaders (propertyTable);
    }
    
    public void clear () {
	propertyTable.setModel (new EntityTableModel(null));
	setColumnHeaders (propertyTable);
    }
    
    private JTable getEmptyPropertyTable (ClientView clientView) {
	JTable propertyTable = new JTable (new EntityTableModel(null));
	setColumnHeaders (propertyTable);
	propertyTable.setColumnSelectionAllowed(false);
	
	int width = (int) (clientView.getScreenWidth() * WIDTH);
	int height = (int) (clientView.getScreenHeight() * HEIGHT);
	
	propertyTable.setPreferredScrollableViewportSize
	    (new Dimension(width, height));
	propertyTable.setBackground (Color.white);
	
	return propertyTable;
    }
    
    private void setColumnHeaders(JTable propertyTable) {
	TableColumn col;
	col = propertyTable.getColumnModel().getColumn(KEY_COLUMN);
	col.setHeaderValue("Property");
	col.setPreferredWidth(60);
	col = propertyTable.getColumnModel().getColumn(VALUE_COLUMN);
	col.setHeaderValue("Value");
	col.setPreferredWidth(190);
    }
    
    private class EntityTableModel extends AbstractTableModel {	
	EntityProperty property;
	
	EntityTableModel (EntityProperty property) {
	    this.property = property;
	}
	
	public int getRowCount() {
	    if (property == null)
		return 0;
	    else 
		return property.getNumProperties ();
	}
	
	public int getColumnCount() {
	    return 2;
	}
	
	public Object getValueAt(int row, int column) {
	    
	    // We don't have an entity to display
	    if (property == null)
		return "";
	    
	    // Asking for an invalid row
	    if (row > property.getNumProperties ())
		return "";
	    
	    // Should never do this
	    if (column >= 2)
		return "";
	    
	    if (column == 0)
		return property.getKey (row);
	    else
		return property.getValue (row);
	}
    }
}
