package stream.vis.view.dialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import stream.vis.util.SpringUtilities;
import stream.vis.data.NamedTable;
import stream.vis.data.UnnamedTable;
import stream.vis.data.Types;

/**
 * AssignName takes an unnamed table, and gets the name for the table
 * from the user.
 */

public class AssignName extends JDialog implements ActionListener {
    
    /// The named table that we will return at the end of hte dialog
    NamedTable namedTable;
    
    /// The unnamed table that was input
    UnnamedTable unnamedTable;
    
    /// Frame who owns us
    JFrame owner;
    
    /// The text field where the name of table is entered
    JTextField tableNameField;
    
    /// The text fields for the attribute names
    JTextField attrNameField [];
    
    /// The text fields for the attribute lengths
    JTextField attrLenField [];
    
    /// The combo box where the user picks the attribute type
    JTextField attrTypeField [];
    
    /// Button to complete registering
    JButton registerButton;
    
    /// Button to cancel registering
    JButton cancelButton;
    
    private static final String emptyStrNameMsg 
	= "Stream name required";
    
    private static final String emptyRelNameMsg
	= "Relation name required";    
    
    private static final String emptyLenMsg 
	= "Length required for \"char\" fields";

    private static final String invalidLenMsg
	= "Invalid value for length";
    
    private static final String emptyAttrNameMsg
	= "Attribute name required for attribute position";    
    
    public AssignName (JFrame owner, UnnamedTable unnamedTable) {
	super (owner);
	
	this.owner = owner;
	this.namedTable = null;
	this.unnamedTable = unnamedTable;
	
	setTitle ("Assign Name");
	setModal (true);
	
	// The main panel displays the stream name text box and 
	// the heading for the attributes
	Component mainPane = getMainPane ();
	
	// Attr pane is the scroll pane for entering attr info
	Component attrPane = getAttrPane (); 
	
	// Button pane contains the register and cancel button
	// to complete the registration and to cancel it, resp.
	Component buttonPane = getButtonPane ();
	
	// Pack them all
	Container contentPane = getContentPane ();	
	contentPane.setLayout (new BorderLayout (5, 5));
	contentPane.add (mainPane, BorderLayout.NORTH);
	contentPane.add (attrPane, BorderLayout.CENTER);
	contentPane.add (buttonPane, BorderLayout.SOUTH);
	pack ();
	
	setLocationRelativeTo (owner);
    }
    
    /**
     * Return the NamedTable object constructed using the user entries. 
     * Return null if the user pressed "Cancel" button
     */
    public NamedTable getNamedTable () {
	return namedTable;
    }
    
    private JPanel getMainPane () {
	JLabel tableNameTitle;
	JLabel attrTitle;
	
	if (unnamedTable.isStream()) 
	    tableNameTitle = new JLabel ("Stream Name:");
	else
	    tableNameTitle = new JLabel ("Relation Name:");
	tableNameTitle.setFont(tableNameTitle.getFont().deriveFont(Font.BOLD));
	
	attrTitle = new JLabel ("Attributes");
	attrTitle.setFont (attrTitle.getFont().deriveFont (Font.BOLD));
	
	tableNameField = new JTextField (10);	
	
	JPanel mainPane = new JPanel (new GridLayout (2, 2));
	mainPane.add (tableNameTitle);
	mainPane.add (tableNameField);
	mainPane.add (attrTitle);
	
	return mainPane;
    }
    
    private JScrollPane getAttrPane () {
	String[] typeStrings = {"int", "float", "char", "byte"};
	
	JPanel attrPane = new JPanel ();
        SpringLayout layout = new SpringLayout();
        attrPane.setLayout(layout);
	
	attrPane.add (new JLabel ("Name"));
	attrPane.add (new JLabel ("Type"));
	attrPane.add (new JLabel ("Length"));
	
	int numAttrs = unnamedTable.getNumAttrs ();
	attrNameField = new JTextField [numAttrs];
	attrTypeField = new JTextField [numAttrs];
	attrLenField  = new JTextField [numAttrs];
	
	for (int a = 0 ; a < numAttrs ; a++) {
	    attrNameField [a] = new JTextField (8); 
	    attrTypeField [a] = new JTextField (5);
	    attrLenField  [a] = new JTextField (4);
	    
	    attrPane.add (attrNameField [a]); 
	    attrPane.add (attrTypeField [a]);
	    attrPane.add (attrLenField [a]);
	    
	    int type = unnamedTable.getAttrType (a);
	    attrTypeField [a].setText (typeStrings[type]);
	    
	    if (type == Types.CHAR) {
		int len = unnamedTable.getAttrLen (a);
		attrLenField[a].setText (Integer.toString(len));
	    }
	    
	    // Only the name field is editable
	    attrNameField[a].setEditable (true);
	    attrTypeField[a].setEditable (false);
	    attrLenField[a].setEditable (false);       
	}
	
	SpringUtilities.makeCompactGrid(attrPane, //parent
					numAttrs+1, 3,
					5, 5,  //initX, initY
					5, 5); //xPad, yPad
	
	JScrollPane scrollPane = new JScrollPane (attrPane);
	Dimension size = attrPane.getPreferredSize();
	
	// Roughly we want around 5 attributes to be visible
	if (numAttrs <= 5) {	    
	    scrollPane.setPreferredSize 
		(new Dimension(size.width + size.width/10,
			       (int)(size.height + size.height/10)));
	}
	else {
	    scrollPane.setPreferredSize 
		(new Dimension(size.width + size.width/10,
			       (int)(size.height * 5.5 / numAttrs)));
	}
	
	return scrollPane;
    }
    
    private JPanel getButtonPane () {
	registerButton = new JButton ("Register");
	cancelButton = new JButton ("Cancel");
	
	registerButton.addActionListener (this);
	cancelButton.addActionListener (this);
	
	// Register button is the "default" button - when the user enters
	// "Enter" this button is pressed
	if (getRootPane() != null)
	    getRootPane().setDefaultButton (registerButton);
	
	JPanel buttonPane = new JPanel();
	buttonPane.add (registerButton);
	buttonPane.add (cancelButton);
	
	return buttonPane;
    }
    
    public void actionPerformed (ActionEvent e) { 
	
	// "Register" Button pressed: we will try to construct the table
	// on success we return from the dialog, on failure we continue
	if (e.getSource() == registerButton) {
	    if (constructTable ()) {
		assert (namedTable != null);		
		setVisible (false);
	    }
	}
	
	// Return from the dialog: Assert: table == null
	else if (e.getSource () == cancelButton) {
	    namedTable = null;
	    setVisible(false);
	}
    }
    
    private boolean constructTable () {
	String tableName;
	String attrName;
	int attrType, attrLen;
	
	tableName = getTableName ();
	if (tableName == null)
	    return false;
	
	namedTable = new NamedTable 
	    (tableName, unnamedTable.isStream(), false);
	
	for (int a = 0 ; a < unnamedTable.getNumAttrs() ; a++) {
	    
	    attrName = getAttrName (a);
	    if (attrName == null) {
		JOptionPane.showMessageDialog (owner, 
					       emptyAttrNameMsg + a,
					       "Error",
					       JOptionPane.ERROR_MESSAGE);
		namedTable = null;
		return false;
	    }
	    
	    attrType = getAttrType (a);
	    
	    if (attrType == Types.CHAR) {
		attrLen = getAttrLen (a);
		if (attrLen <= 0) {
		    namedTable = null;
		    return false;
		}
		
		namedTable.addAttr (attrName, attrType, attrLen);
	    }
	    
	    else {
		namedTable.addAttr (attrName, attrType);
	    }
	}
	
	return true;
    }
    
    private String getTableName () {
	String name = tableNameField.getText ();
	
	if (name == null) {
	    String msg = (unnamedTable.isStream())? 
		emptyStrNameMsg : emptyRelNameMsg;
	    JOptionPane.showMessageDialog (owner, msg, "Error", 
					   JOptionPane.ERROR_MESSAGE);
	    return null;
	}
	
	name = name.trim();
	if (name.length() == 0) {
	    String msg = (unnamedTable.isStream())? 
		emptyStrNameMsg : emptyRelNameMsg;
	    JOptionPane.showMessageDialog (owner, msg, "Error", 
					   JOptionPane.ERROR_MESSAGE);
	    return null;
	}
	
	return name;
    }

    private String getAttrName (int index) {
	String attrName = attrNameField [index].getText ();
	
	if (attrName == null)
	    return null;
	
	attrName = attrName.trim ();
	if (attrName.length() == 0)
	    return null;
	return attrName;
    }
    
    private int getAttrType (int index) {
	String attrTypeStr = attrTypeField [index].getText();
	
	if (attrTypeStr.equals ("int"))
	    return Types.INTEGER;
	if (attrTypeStr.equals ("float"))
	    return Types.FLOAT;
	if (attrTypeStr.equals ("char"))
	    return Types.CHAR;
	if (attrTypeStr.equals ("byte"))
	    return Types.BYTE;
	
	// Should never come
	return Types.INTEGER;
    }

    private int getAttrLen (int index) {
	String attrLenStr = attrLenField [index].getText ();
	int attrLen;
	
	if (attrLenStr == null) {
	    JOptionPane.showMessageDialog (owner, emptyLenMsg, "Error", 
					   JOptionPane.ERROR_MESSAGE);	    
	    return -1;
	}
	
	try {
	    attrLen = Integer.parseInt (attrLenStr);
	}
	catch (NumberFormatException e) {
	    JOptionPane.showMessageDialog (owner, invalidLenMsg, "Error", 
					   JOptionPane.ERROR_MESSAGE);	    
	    return -1;
	}
	
	if (attrLen <= 0) {
	    JOptionPane.showMessageDialog (owner, invalidLenMsg, "Error", 
					   JOptionPane.ERROR_MESSAGE);
	}
	
	return attrLen;
    }
}
