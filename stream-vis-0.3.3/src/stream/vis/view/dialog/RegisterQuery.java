package stream.vis.view.dialog;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.text.*;

public class RegisterQuery extends JDialog 
    implements ActionListener, ItemListener {
    
    /// The frame who owns us
    JFrame owner;
    
    /// The area where the user enters the text
    private JTextArea textArea;
    
    /// Check box to indicate whether the output of the query is desired
    private JCheckBox outputChBox;
    
    /// Check box to indicate whether the output should be named
    private JCheckBox nameChBox;
    
    /// Button to register the query
    private JButton registerButton;
    
    /// Button to cancel the query registration
    private JButton cancelButton;
    
    /// Query text entered by the user
    String queryText;    
    
    /// Is the output of the query required
    boolean outputRequired;
    
    /// Does the user want to assign a name
    boolean isNamed;
    
    /// Message to be displayed when query text is empty and user
    /// hits "Register"
    private static final String emptyQueryMsg = 
	"Empty Query!";
    
    /// Message to be displayed when the query has neither output 
    /// nor named for use by other queries
    private static final String nonFuncQueryMsg =
	"Query is non-functional: " +
	"It has no visible output nor a name for use in other queries.";
    
    public RegisterQuery (JFrame owner) {
	super (owner, "Register Query");
	
	this.owner = owner;
	this.queryText = null;
	this.outputRequired = true;
	this.isNamed = false;
	
	setModal (true);
	
	// Create a scrollable text area
	Component textPane = createTextArea ();
	
	// Create the pane containing the check boxes
	Component checkBoxPane = createCheckBoxPane ();
	
	// Create the pane containing the buttons
	Component buttonPane = createButtonPane ();
	
	// Pack them all 
	Container contentPane = getContentPane ();
	contentPane.setLayout (new BorderLayout ());
	contentPane.add (checkBoxPane, BorderLayout.NORTH);
	contentPane.add (textPane, BorderLayout.CENTER);
	contentPane.add (buttonPane, BorderLayout.SOUTH);
	pack ();
	
	setLocationRelativeTo (owner);	
    }
    
    public String getQueryText () {
	return queryText;
    }
    
    public boolean isNamed () {
	return isNamed;
    }
    
    public boolean outputRequired () {
	return outputRequired;
    }
    
    public void reset () {	
	queryText = null;
	outputRequired = true;
	isNamed = false;
	
	textArea.setText ("");
	textArea.grabFocus();
	outputChBox.setSelected (true);
	nameChBox.setSelected(false);
    }
    
    private JScrollPane createTextArea () {
	textArea = new JTextArea(5, 30);
	textArea.setWrapStyleWord(true);
	JScrollPane pane = new JScrollPane(textArea);
	pane.setVerticalScrollBarPolicy
	    (JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	return pane;
    }
    
    private JPanel createCheckBoxPane () {
	outputChBox = new JCheckBox ("Display Output");
	outputChBox.setSelected (true);
	nameChBox = new JCheckBox ("Assign Name");
	
	outputChBox.addItemListener (this);
	nameChBox.addItemListener (this);
	
	JPanel pane = new JPanel(new GridLayout(0,1));
	pane.add (outputChBox);
	pane.add (nameChBox);
	
	return pane;
    }
    
    private JPanel createButtonPane () {
	registerButton = new JButton ("Register");
	cancelButton = new JButton ("Cancel");
	
	getRootPane().setDefaultButton (registerButton);
	registerButton.addActionListener (this);
	cancelButton.addActionListener (this);
	
	JPanel pane = new JPanel ();
	pane.add (registerButton);
	pane.add (cancelButton);
	
	return pane;
    }
    
    public void actionPerformed (ActionEvent e) {
	Object source = e.getSource ();
	
	if (source == registerButton) {
	    // If we succeed in getting query details we can make
	    // ourselves invisible
	    if (getQueryDetails())
		setVisible (false);
	}
	
	else if (source == cancelButton) {
	    queryText = null;
	    setVisible (false);
	}
    }
    
    public void itemStateChanged (ItemEvent e) {
	if (e.getSource () == outputChBox) {
	    if (e.getStateChange() == ItemEvent.SELECTED)
		outputRequired = true;
	    else if (e.getStateChange() == ItemEvent.DESELECTED)
		outputRequired = false;		
	}
	
	if (e.getSource () == nameChBox) {
	    if (e.getStateChange () == ItemEvent.SELECTED)
		isNamed = true;
	    else if (e.getStateChange () == ItemEvent.DESELECTED)
		isNamed = false;
	}
    }
    
    private boolean getQueryDetails () {

	queryText = textArea.getText().trim();
	
	if (queryText.length() == 0) {
	    JOptionPane.showMessageDialog (owner, emptyQueryMsg, "Error",
					   JOptionPane.ERROR_MESSAGE);
	    queryText = null;
	    return false;
	}
	
	// Add a semicolon if it does not exist
	if (!queryText.endsWith (";")) {	    
	    queryText = queryText + ";";
	}
	
	if (!outputRequired && !isNamed) {
	    JOptionPane.showMessageDialog (owner, nonFuncQueryMsg, "Error",
					   JOptionPane.ERROR_MESSAGE);
	    queryText = null;
	    return false;
	}
	
	return true;
    }
}
