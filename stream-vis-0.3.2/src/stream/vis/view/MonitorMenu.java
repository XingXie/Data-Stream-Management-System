package stream.vis.view;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import stream.vis.data.Monitor;
import stream.vis.data.Client;
import stream.vis.data.QueryResult;
import stream.vis.util.FatalException;
import java.util.List;

public class MonitorMenu extends JPopupMenu {
    
    /// The global client object
    private Client client;
    
    /// The view of the client
    private ClientView clientView;
    
    /// The query plan view into which the monitor is added
    private QueryPlanView planView;
    
    /// The entityview for which we are adding hte monitor
    private EntityView entityView;
    
    /// The (Server side) identifer for the entity
    private String entityId;
    
    /// Descriptors for monitors that we will show on the menu.  The 
    /// index of the descriptors refers to the those defined in Monitor class
    private static String desc [] = {
	"Monitor CPU Usage",
	"Monitor Rate of Tuple flow",
	"Monitor Timestamps",
	"Monitor Join selectivity",
	"Monitor Cardinality",
	"Monitor Size (pages)"
    };
    
    public MonitorMenu (Client        _client, 
			ClientView    _clientView,
			EntityView    _entityView,
			String        _entityId,
			QueryPlanView _planView) {
	
	this.client     = _client;
	this.clientView = _clientView;
	this.entityView = _entityView;
	this.entityId   = _entityId;
	this.planView   = _planView;
    }
    
    // add a new menu item (monitor type) for the monitor menu
    public void addMonitor (int monId) {
	add (new MonitorMenuItem (monId));
    }
    
    private class MonitorMenuItem extends JMenuItem implements ActionListener {
	int monId;
	
	MonitorMenuItem (int monId) {
	    assert (monId >= 0 && monId < desc.length);
	    
	    this.monId = monId;
	    setText (desc [monId]);
	    addActionListener (this);
	}
	
	public void actionPerformed(ActionEvent e) {
	    try {
		Monitor monitor;
		QueryResult monitorData;
		
		// get the window size
		WinSizeDialog dialog = new WinSizeDialog ();
		dialog.setVisible (true);	    
		if (dialog.cancelled)
		    return;	    
		int winSize = (int)(dialog.winSize * Monitor.TIME_PER_SEC);
		
		// create a monitor and register with the client
		monitor = new Monitor (monId, entityId, winSize);	    
		monitorData = client.registerMonitor (monitor);
		planView.addMonitor (monitor, monitorData, entityView);
	    }
	    catch (FatalException f) {
		JOptionPane.showMessageDialog (this, f.toString(), "Error",
					       JOptionPane.ERROR_MESSAGE);
		System.exit (0);		
	    }
	}
    }
    
    private static String DEFAULT_SIZE = "2.0";
    private static float MIN_SIZE = 0.1f;
    private static float MAX_SIZE = 10.0f;
    
    /**
     * Dialog to get the window size from the user
     */
    private class WinSizeDialog extends JDialog implements ActionListener {
	JTextField winSizeField;
	JButton okButton;
	JButton cancelButton;
	float winSize;	
	boolean cancelled;
	
	WinSizeDialog () {
	    super (clientView, "Window Size");
	    setModal (true);
	    
	    // Main pane contains the window size field
	    Component mainPane = getMainPane ();
	    
	    // Button pane contains the button to finish dialog
	    Component buttonPane = getButtonPane ();
	    
	    Container contentPane = getContentPane ();
	    contentPane.setLayout (new BorderLayout (5, 5));
	    contentPane.add (mainPane, BorderLayout.NORTH);
	    contentPane.add (buttonPane, BorderLayout.CENTER);
	    pack ();
	    
	    setLocationRelativeTo (null);
	}
	
	JPanel getMainPane () {
	    
	    JLabel winSizeLabel;
	    
	    winSizeLabel = new JLabel ("Window Size (secs):");
	    winSizeField = new JTextField (DEFAULT_SIZE, 4);
	    
	    JPanel mainPane = new JPanel (new FlowLayout ());
	    mainPane.add (winSizeLabel);
	    mainPane.add (winSizeField);
	    
	    return mainPane;
	}
	
	JPanel getButtonPane () {
	    okButton = new JButton ("Ok");
	    cancelButton = new JButton ("Cancel");
	    
	    okButton.addActionListener (this);
	    cancelButton.addActionListener (this);
	    
	    JPanel buttonPane = new JPanel ();
	    buttonPane.add (okButton);
	    buttonPane.add (cancelButton);
	    
	    return buttonPane;
	}

	public void actionPerformed (ActionEvent e) { 
	    
	    // we will try to get the window size ...
	    if (e.getSource() == okButton) {		
		if (getWindowSize ()) {
		    cancelled = false;
		    setVisible (false);
		}
	    }
	    
	    // Return from the dialog: 
	    else if (e.getSource () == cancelButton) {
		cancelled = true;
		setVisible(false);
	    }
	}   
	
	boolean getWindowSize () {
	    
	    String winSizeStr = winSizeField.getText ();
	    
	    if (winSizeStr == null) {
		JOptionPane.showMessageDialog (clientView, 
					       "Empty Window Size",
					       "Error",
					       JOptionPane.ERROR_MESSAGE);
		return false;
	    }
	    
	    try {
		winSize = Float.parseFloat (winSizeStr);
		if (winSize <= MIN_SIZE) {
		    JOptionPane.showMessageDialog (clientView,
						   "Window Size too small",
						   "Error",
						   JOptionPane.ERROR_MESSAGE);
		    return false;
		}
		if (winSize >= MAX_SIZE) {
		    JOptionPane.showMessageDialog (clientView,
						   "Window Size too large",
						   "Error",
						   JOptionPane.ERROR_MESSAGE);
		    return false;
		}				
	    }
	    catch (NumberFormatException e) {
		JOptionPane.showMessageDialog (clientView,
					       "Invalid window size",
					       "Error",
					       JOptionPane.ERROR_MESSAGE);
		return false;
		
	    }	
	    return true;
	}
    }	
}
