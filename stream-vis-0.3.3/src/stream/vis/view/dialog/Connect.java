package stream.vis.view.dialog;

import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import stream.vis.util.SpringUtilities;
import stream.vis.util.InitManager;
import stream.vis.view.*;

/**
 * Dialog to connect to the server.
 * 
 * Xing 2010.10.15: User and Pwd are added.
 */

public class Connect extends JDialog implements ActionListener {
    
    /// Host field
    private JTextField hostField;
    
    /// Port field
    private JTextField portField;
    
  /// Username fieldinitHost
    private JTextField userField;
    
  /// Password field
    private JPasswordField pwdField;
    
    /// Frame who owns us
    private JFrame owner;
    
    /// Button for connecting
    private JButton okButton;
    
    /// Button to cancel connection
    private JButton cancelButton;
    
    /// The value of the host entered by the user
    private String host;
    
    /// The port
    private int port;
    
    /// The value of user
    private String user;
    
    /// The value of the password entered by the user
    private String pwd;
    
    
    /// Default host
    private static final String DEFAULT_HOST = "localhost";
    
    /// Default port
    private static final String DEFAULT_PORT = "9000";
    
    /// Default user
    private static final String DEFAULT_USER = "Alice";
    
    /// Default port
    private static final String DEFAULT_PWD = "111";
    
    /// Error message to show if the host field is empty
    private static final String emptyHostMesg = 
	"Host field empty";
    
    /// Error message to show if the port field is empty
    private static final String invalidPortMesg = 
	"Invalid value for port";
    
  /// Error message to show if the user is empty
    private static final String emptyUserMesg = 
	"User field empty";
    
    /// Error message to show if the password is empty
    private static final String emptyPwdMesg = 
	"Password field empty";
    
    
    
    public Connect (JFrame owner) {
	super (owner);

	String defaultHost = System.getProperty("stream.default_host");
	String defaultPort = System.getProperty("stream.default_port");
	String defaultUser = System.getProperty("stream.default_user");
	String defaultPwd = System.getProperty("stream.default_pwd");
	
	if(defaultHost == null) defaultHost = InitManager.getDefaultHost();
	if(defaultPort == null) defaultPort = InitManager.getDefaultPort();
	if(defaultUser == null) defaultUser = InitManager.getDefaultUser();
	if(defaultPwd == null) defaultPwd = InitManager.getDefaultPwd();
	
	setModal (true);
	setResizable (false);	
	setTitle ("STREAM Visualizer - Connect");
	
	// The main pane contains the text fields and their titles
	Component mainPane = createMainPane (defaultHost, defaultPort, defaultUser, defaultPwd);
	
	// The button pane contains the ok and cancel buttons
	Component buttonPane = createButtonPane ();
	
	// The image pane contains the stream logo
	Component logo = createLogo ();
	
	// Pack all these together
	pack (logo, mainPane, buttonPane);	
	
	pack();
	setLocationRelativeTo (owner);
	
	host = null;
	port = -1;
    }
    
    public String getHost () {
	return host;
    }
    
    public int getPort () {
	return port;
    }
    
    public String getUser () {
    	return user;
        }
    
    public String getPwd () {
    	return pwd;
        }
    
    private JPanel createMainPane (String initHost, String initPort, String initUser, String initPwd) {
	
	// create text boxes for the host and port
	hostField = new JTextField(initHost, 16);
	portField = new JTextField(initPort, 5);
	userField = new JTextField(initUser, 16);
	pwdField = new JPasswordField(initPwd, 16);
	
	JLabel hostLabel = new JLabel ("Host");
	JLabel portLabel = new JLabel ("Port");
	JLabel userLabel = new JLabel ("Username");
	JLabel pwdLabel = new JLabel ("Password");
	
	JPanel mainPane = new JPanel ();
	mainPane.setLayout (new SpringLayout ());
	
	mainPane.add (hostLabel);
	mainPane.add (hostField);
	mainPane.add (portLabel);
	mainPane.add (portField);
	mainPane.add (userLabel);
	mainPane.add (userField);
	mainPane.add (pwdLabel);
	mainPane.add (pwdField);
	
	mainPane.setBorder (new TitledBorder ("Server Information"));
	SpringUtilities.makeCompactGrid (mainPane, 
					 4, 2,     
					 5, 5,     
					 5, 5);
	
	return mainPane;
    }
    
    private JPanel createButtonPane () {
	okButton = new JButton ("OK");
	cancelButton = new JButton ("Cancel");
	
	JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
	buttonPane.add(okButton);
	buttonPane.add(cancelButton);
	
	okButton.addActionListener (this);
	cancelButton.addActionListener (this);
	
	getRootPane().setDefaultButton (okButton);
	
	return buttonPane;
    }
    
    private JLabel createLogo () {	
	// left-hand side of the box is the STREAM logo
	JLabel icon = new JLabel(IconStore.getIcon("logo"));
	icon.setBorder(new BevelBorder(BevelBorder.LOWERED));
	
	return icon;
    }
    
    private void pack (Component logo, 
		       Component mainPane, 
		       Component buttonPane) {
    	JPanel rightPane = new JPanel ();
	rightPane.setLayout(new BoxLayout(rightPane, BoxLayout.Y_AXIS));
	rightPane.add (mainPane);
	rightPane.add (buttonPane);
	
	Container contentPane = getContentPane ();
	contentPane.setLayout (new BoxLayout(contentPane, BoxLayout.X_AXIS));
	contentPane.add (logo);
	contentPane.add (rightPane);
    }
    
    public void actionPerformed (ActionEvent e) {
	Object source = e.getSource ();
	
	if (source == okButton) {
	    host = hostField.getText().trim();
	    
	    if (host.length() == 0) {
		JOptionPane.showMessageDialog (owner, emptyHostMesg, "Error",
					       JOptionPane.ERROR_MESSAGE);
		System.out.println("host name empty");
		return;		
	    }
	    
	    String portStr = portField.getText().trim();
	    
	    if (portStr.length () == 0) {
		JOptionPane.showMessageDialog (owner, invalidPortMesg, "Error",
					       JOptionPane.ERROR_MESSAGE);
		return;
	    }
	    
	    try {
		port = Integer.parseInt (portStr);
	    }
	    catch (NumberFormatException n) {
		JOptionPane.showMessageDialog (owner, invalidPortMesg, "Error",
					       JOptionPane.ERROR_MESSAGE);
		return;
	    }		
	    
        user = userField.getText().trim();
	    
	    if (user.length() == 0) {
		JOptionPane.showMessageDialog (owner, emptyUserMesg, "Error",
					       JOptionPane.ERROR_MESSAGE);
		System.out.println("user name empty");
		return;		
	    }
	    
	    char[] c = pwdField.getPassword();
	    pwd = new String(c);
	    
	    if (pwd.length() == 0) {
		JOptionPane.showMessageDialog (owner, emptyPwdMesg, "Error",
					       JOptionPane.ERROR_MESSAGE);
		System.out.println("Password is empty");
		return;		
	    }
	    
	    
	    
	    
	    setVisible (false);
	}
	
	else if (source == cancelButton) {
	    host = null;
	    setVisible (false);
	}
    }
}
