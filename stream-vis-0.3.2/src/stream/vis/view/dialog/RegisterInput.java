package stream.vis.view.dialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import stream.vis.util.SpringUtilities;
import stream.vis.data.NamedTable;
import stream.vis.data.Types;

public class RegisterInput extends JDialog implements ActionListener {
    private static final int MAX_ATTRS = 20;
    
    /// The input table that the user is registering
    NamedTable table;
    
    /// Frame who owns us
    JFrame owner;
    
    /// Is the input a stream or a relation
    boolean isStream;
    
    /// The text field where the name of table is entered
    JTextField tableNameField;
    
    /// The text fields for the attribute names
    JTextField attrNameField [];
    
    /// The text fields for the attribute lengths
    JTextField attrLenField [];
    
    /// The combo box where the user picks the attribute type
    JComboBox  attrTypeBox [];
    
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
    
    private static final String zeroAttrMsg
	= "Atleast one attribute required";
    
    public RegisterInput (JFrame owner, boolean isStream) {
	super (owner);
	
	this.isStream = isStream;
	this.owner = owner;
	this.table = null;
	
	// Set the title
	if (isStream)
	    setTitle ("Register Stream");
	else
	    setTitle ("Register Relation");
	
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
     * Return the NamedTable constructed using the user entries. 
     * Return null if the user pressed "Cancel" button
     */
    public NamedTable getInputTable () {
	return table;
    }
    
    public void reset () {
	table = null;
	
	tableNameField.setText ("");
	tableNameField.grabFocus ();
	
	for (int a = 0 ; a < MAX_ATTRS ; a++) {
	    attrNameField [a].setText ("");
	    attrLenField [a].setText ("");
	    attrLenField [a].setEditable (false);
	    attrTypeBox [a].setSelectedIndex (0);
	}
    }
    
    private JPanel getMainPane () {
	JLabel tableNameTitle;
	JLabel attrTitle;
	
	if (isStream) 
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
	String[] types = {"int", "float", "char", "byte"};
	
	JPanel attrPane = new JPanel ();
        SpringLayout layout = new SpringLayout();
        attrPane.setLayout(layout);
	
	attrPane.add (new JLabel ("Name"));
	attrPane.add (new JLabel ("Type"));
	attrPane.add (new JLabel ("Length"));
	
	attrNameField = new JTextField [MAX_ATTRS];
	attrTypeBox = new JComboBox  [MAX_ATTRS];
	attrLenField  = new JTextField [MAX_ATTRS];
	
	for (int a = 0 ; a < MAX_ATTRS ; a++) {
	    attrNameField [a] = new JTextField (8); 
	    attrTypeBox [a] = new JComboBox (types);
	    attrLenField  [a] = new JTextField (4);
	    
	    attrPane.add (attrNameField [a]); 
	    attrPane.add (attrTypeBox [a]);
	    attrPane.add (attrLenField [a]);
	}
	
	SpringUtilities.makeCompactGrid(attrPane, //parent
					MAX_ATTRS+1, 3,
					5, 5,  //initX, initY
					5, 5); //xPad, yPad
	
	JScrollPane scrollPane = new JScrollPane (attrPane);
	Dimension size = attrPane.getPreferredSize();
	scrollPane.setPreferredSize 
	    (new Dimension(size.width + size.width/10,
			   (int)(size.height * 0.28)));
	
	// Make the attrLenField[] field editable only if type==char
	for (int a = 0 ; a < MAX_ATTRS ; a++) {
	    attrLenField [a].setEditable (false);	    
	    attrTypeBox[a].addActionListener 
		(new CharTypeListener (attrTypeBox[a], attrLenField[a]));
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
    
    private class CharTypeListener implements ActionListener {
	JComboBox typeBox;
	JTextField lenField;
	
	public CharTypeListener (JComboBox  typeBox, 
				 JTextField lenField) {
	    this.typeBox  = typeBox;
	    this.lenField = lenField;
	}
	
	public void actionPerformed (ActionEvent e) {
	    String selectedType = (String)typeBox.getSelectedItem();
	    if (selectedType.equals ("char"))
		lenField.setEditable (true);
	    else
		lenField.setEditable (false);
	}
    }
    
    public void actionPerformed (ActionEvent e) { 
	
	// "Register" Button pressed: we will try to construct the table
	// on success we return from the dialog, on failure we continue
	if (e.getSource() == registerButton) {
	    if (constructTable ())
		setVisible (false);
	}
	
	// Return from the dialog: 
	else if (e.getSource () == cancelButton) {
	    table = null;
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
	
	// true indicates this is a base named table ...
	table = new NamedTable (tableName, isStream, true);
	
	for (int a = 0 ; a < MAX_ATTRS ; a++) {
	    
	    attrName = getAttrName (a);	    
	    if (attrName == null) 
		break;
	    
	    attrType = getAttrType (a);
	    
	    if (attrType == Types.CHAR) {
		attrLen = getAttrLen (a);
		if (attrLen <= 0) {
		    table = null;
		    return false;
		}
		
		table.addAttr (attrName, attrType, attrLen);
	    }
	    
	    else {
		table.addAttr (attrName, attrType);
	    }
	}
	
	if (table.getNumAttrs () == 0) {
	    JOptionPane.showMessageDialog (owner, zeroAttrMsg, "Error", 
					   JOptionPane.ERROR_MESSAGE);
	    return false;
	}	
	
	return true;
    }
    
    private String getTableName () {
	String name = tableNameField.getText ();
	
	if (name == null) {
	    String msg = (isStream)? emptyStrNameMsg : emptyRelNameMsg;
	    JOptionPane.showMessageDialog (owner, msg, "Error", 
					   JOptionPane.ERROR_MESSAGE);
	    return null;
	}
	
	name = name.trim();
	if (name.length() == 0) {
	    String msg = (isStream)? emptyStrNameMsg : emptyRelNameMsg;
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
	String attrTypeStr = (String)attrTypeBox [index].getSelectedItem ();
	
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
