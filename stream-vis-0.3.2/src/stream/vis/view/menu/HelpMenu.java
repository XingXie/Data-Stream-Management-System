package stream.vis.view.menu;

import java.awt.event.*;
import javax.swing.*;
import stream.vis.data.Client;
import stream.vis.view.ClientView;

public class HelpMenu extends JMenu implements ActionListener {
    
    private JMenuItem onlineHelp;
    private JMenuItem about;
    private ClientView clientView;

    private static final String ABOUT_MSG =
	"STREAM Visualizer 0.1\n" +
	"Copyright 2005 The Board of Trustees of The Leland Stanford Junior University.\n" +
	"All Rights Reserved.\n\n" +
	"http://www-db.stanford.edu/stream";
    
    private static final String ONLINE_HELP = "Online help is available at:\n" 
	+ "http://www-db.stanford.edu/stream/demo";

    public HelpMenu (Client client, ClientView clientView) {
	super ("Help");
	setMnemonic (KeyEvent.VK_H);
	
	this.clientView = clientView;

	onlineHelp = new JMenuItem ("Online Help");
	about = new JMenuItem ("About STREAM Visualizer");
	
	add (onlineHelp);
	add (about);

	onlineHelp.addActionListener(this);
        about.addActionListener(this);
    }


    public void actionPerformed(ActionEvent e) {
	Object source = e.getSource ();
	
	if (source == onlineHelp) {
	    JOptionPane.showMessageDialog 
		(clientView, 
		 ONLINE_HELP,
		 "Online Help", JOptionPane.INFORMATION_MESSAGE);
	}
	else if (source == about) {
	    JOptionPane.showMessageDialog 
		(clientView, 
		 ABOUT_MSG,
		 "About Stream Visualizer", JOptionPane.INFORMATION_MESSAGE);
	}
    }
	
}
