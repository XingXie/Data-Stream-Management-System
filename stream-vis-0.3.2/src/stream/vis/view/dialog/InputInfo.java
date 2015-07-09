package stream.vis.view.dialog;

import stream.vis.util.FatalException;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

import stream.vis.util.SpringUtilities;
import stream.vis.util.InitManager;
import stream.vis.data.*;
import stream.vis.view.*;
import stream.vis.view.input.*;

/**
 * Get the input information from the user
 */

public class InputInfo extends JDialog 
    implements ActionListener, ItemListener {    
    
    /// The filename where the input is present
    private String fileName;
    
    /// Whether the timestamp is application specified or system generated
    private boolean bAppTimestamp;
    
    private boolean bLoop;
    
    /// The text field in which user enters the input filename
    JTextField fileNameField;
    
    /// The check box in which user specifies whether app time is required
    JCheckBox appTimeBox;
    
    /// The check box in which user specified whether the file should be looped
    JCheckBox loopBox;
    
    /// ok button
    JButton okButton;
    
    /// Cancecl button
    JButton cancelButton;
    
    /// Browse button for picking filenames
    JButton browseButton;
    
    /// fileChooser
    JFileChooser fc;

    /// the owner
    TableFeederView tableFeederView;

    /// the table
    NamedTable table;
    
    public InputInfo (TableFeederView tv, NamedTable table){
	super (tv);
	setModal (true);
	setResizable (false);
	
	this.tableFeederView = tv;
	this.table = table;       
	
	// Main pane contains the text fields where the user enters 
	// the input information, and the descriptions of these fields
	Component mainPane = createMainPane ();	
	
	// The ok and cancel buttons
	Component buttonPane = createButtonPane ();
	
	pack (mainPane, buttonPane);
	pack ();
	
	// initialization
	fc = new JFileChooser(InitManager.getScriptPath());
	bAppTimestamp = false;
	bLoop = true;
	
	setLocationRelativeTo (tv);
    }
    
    private Component createMainPane () {
	
	// Create all the components
	JLabel fileNameLabel = new JLabel 
	    ("Source for " + table.getTableName()); 
	
	fileNameField = new JTextField (10);
	browseButton = new JButton ("Browse");
	
	JPanel fileNamePane = new JPanel ();
	fileNamePane.setLayout (new FlowLayout ());
	fileNamePane.add (fileNameLabel);
	fileNamePane.add (fileNameField);
	fileNamePane.add (browseButton);
	
	appTimeBox = new JCheckBox ("File contains timestamps");
	loopBox = new JCheckBox ("Loop over file contents");
	loopBox.setSelected (true);
	
	appTimeBox.addItemListener(this);
	loopBox.addItemListener (this);
	browseButton.addActionListener(this);
	
	JPanel mainPane = new JPanel ();
	mainPane.setLayout (new BoxLayout (mainPane, BoxLayout.PAGE_AXIS));
	
	mainPane.add (fileNamePane);
	mainPane.add (appTimeBox);
	mainPane.add (loopBox);
	
	return mainPane;
    }
    
    private JPanel createButtonPane () {
	okButton = new JButton ("Ok");
	cancelButton = new JButton ("Cancel");
	
	okButton.addActionListener(this);
	cancelButton.addActionListener(this);
	
	getRootPane().setDefaultButton (okButton);	
	
	JPanel pane = new JPanel ();
	pane.add (okButton);
	pane.add (cancelButton);
	
	return pane;
    }
    
    private void pack (Component mainPane, Component buttonPane) {
	Container contentPane = getContentPane ();
	
	contentPane.setLayout (new BorderLayout ());
	contentPane.add (mainPane, BorderLayout.CENTER);
	contentPane.add (buttonPane, BorderLayout.SOUTH);	
    }

    public void actionPerformed (ActionEvent e) {
	Object source = e.getSource ();
	
	if (source == okButton) {
	    fileName = fileNameField.getText();
	    
	    // Perform basic sanity checks
	    if (!sanityCheck (fileName)) {
		JOptionPane.showMessageDialog 
		    (this, 
		     "Unable to read file or invalid file format",
		     "Error", 
		     JOptionPane.ERROR_MESSAGE);
		return;
	    }
	    
	    
	    tableFeederView.bindTableFeeder 
		(fileName, bLoop, bAppTimestamp);
	    
	    setVisible (false);	    	    
	}
	
	else if (source == cancelButton) {
	    setVisible (false);
	}
	
	else if (source == browseButton) {
	    // prompt user to select file
	    int returnVal = fc.showOpenDialog (this);
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
		File file = fc.getSelectedFile();
		// updates textbox display
		fileNameField.setText(file.getPath());
	    }
	}
    }
    
    private boolean sanityCheck (String filename) {
	File file = new File(fileName);
	
	// check sanity of file (later.. do a more thorough check)
	if(!file.canRead()) {
	    return false;
	}
	
	try {
	    BufferedReader reader = new BufferedReader 
		(new InputStreamReader (new FileInputStream(file)));
	

	    String firstTuple = reader.readLine ();

	    String attrs[] = firstTuple.split (",");
	    
	    // remove white spaces to make Integer.parseInt() work
	    for(int i=0; i<attrs.length; i++) attrs[i]=attrs[i].trim();

	    if (table.isStream()) {
		if (bAppTimestamp) {
		    if (attrs.length != table.getNumAttrs() + 1) {
			return false;
		    }
		    
		    if (!checkTimestamp (attrs[0]))
			return false;
		    
		    // check other attrs
		    for (int a = 1 ; a < attrs.length ; a++)
			if (!checkType (attrs[a], table.getAttrType (a-1)))
			    return false;
		}
		else {
		    if (attrs.length != table.getNumAttrs()) {
			return false;
		    }
		    
		    for (int a = 0 ; a < attrs.length ; a++)
			if (!checkType (attrs[a], table.getAttrType (a))) {
			    return false;
			}
		}
	    }
	    else {
		if (bAppTimestamp) {
		    if (attrs.length != table.getNumAttrs() + 2) {
			return false;
		    }
		    
		    if (!checkTimestamp (attrs[0]))
			return false;
		    
		    if (!checkSign (attrs[1]))
			return false;
		    
		    for (int a = 2 ; a < attrs.length ; a++)
			if (!checkType (attrs[a], table.getAttrType (a-2)))
			    return false;
		}
		else {
		    if (attrs.length != table.getNumAttrs() + 1) {
			return false;
		    }
		    
		    if (!checkSign (attrs[0]))
			return false;
		    
		    for (int a = 1 ; a < attrs.length ; a++)
			if (!checkType (attrs[a], table.getAttrType (a-1)))
			    return false;		
		}
	    }
	}
	catch (FileNotFoundException f) {
	    return false;
	}
	catch (IOException e) {
	    return false;
	}
	
	return true;
    }
    
    private boolean checkTimestamp (String val) {
	try {
	    if (Integer.parseInt (val) < 0)
		return false;
	    return true;
	}
	catch (NumberFormatException e) {
	    return false;
	}
    }
    
    private boolean checkSign (String val) {
	if (val.length() != 1)
	    return false;
	if (val.charAt(0) != '+' &&
	    val.charAt(0) != '-')
	    return false;
	return true;
    }
    
    private boolean checkType (String val, int type) {
	switch (type) {
	case Types.INTEGER:
	    try {
		Integer.parseInt (val);
		return true;
	    }
	    catch (NumberFormatException e) {
		return false;
	    }
	    
	case Types.FLOAT:
	    try {
		Float.parseFloat(val);
		return true;
	    }
	    catch (NumberFormatException e) {
		return false;
	    }
	    
	case Types.CHAR:
	    return true;
	    
	case Types.BYTE:
	    if (val.length () != 1)
		return false;
	    return true;
	    
	default:
	    assert (false);
	    return false;
	}
	
	// never comes
    }

    public void itemStateChanged(ItemEvent e) {
        Object source = e.getItemSelectable();
	
        if (source == appTimeBox) {
	    //check whether it was selected or deselected.
	    if (e.getStateChange() == ItemEvent.DESELECTED) {
		bAppTimestamp = false;
		loopBox.setEnabled (true);		
	    }
	    
            else {
		bAppTimestamp = true;
		loopBox.setEnabled (false);
		loopBox.setSelected (false);
		bLoop = false;
	    }
        }
	else if (source == loopBox) {
	    if (e.getStateChange() == ItemEvent.DESELECTED) {
		bLoop = true;
	    }
	    else {
		bLoop = false;
	    }
	}
    }
}
