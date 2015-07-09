package stream.vis.view.menu;

import java.io.*;
import java.awt.event.*;
import javax.swing.*;
import stream.vis.data.*;
import stream.vis.view.ClientView;
import stream.vis.view.dialog.InputInfo;
import stream.vis.util.InitManager;

public class DemoMenu extends JMenu implements ActionListener {

    ClientView clientView;
    Client client;
    JMenuItem oneQuery;
    JMenuItem simpleAggr;
    JMenuItem simpleJoin;
        
    public DemoMenu (Client client, ClientView clientView) {
	super ("Demo");
	this.clientView = clientView;
	this.client = client;
	setMnemonic (KeyEvent.VK_D);

        oneQuery = new JMenuItem ("Experiment");
	oneQuery.addActionListener (this);
	add (oneQuery);

        simpleAggr = new JMenuItem ("Simple Aggregation");
	simpleAggr.addActionListener (this);
	add (simpleAggr);

        simpleJoin = new JMenuItem ("Simple Join");
	simpleJoin.addActionListener (this);
	add (simpleJoin);
    }

    public void actionPerformed(ActionEvent e) {
	if (!(e.getSource() instanceof JMenuItem))
	    return;
	
	JMenuItem source = (JMenuItem)e.getSource();
	
	if (source == oneQuery) {
	    try {
		String regFilePath = InitManager.getDemoPath() + "/Experiment.demo";
		
		File file = new File(regFilePath);

		client.reset ();
		clientView.loadRegistryFromFile(file);
		
		//InputStream in = getClass().getResourceAsStream(regFilePath);
		//clientView.loadRegistryFromFile(in);

		// show prompt to user
		JOptionPane.showMessageDialog (clientView, 
					       "Registry for 'One Query' loaded.\nPress \"Generate Plan\" and \"Run\" to run it.", 
					       "Success",
					       JOptionPane.INFORMATION_MESSAGE);
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
	   
	}
	if (source == simpleAggr) {
	    try {
		String regFilePath = InitManager.getDemoPath() + "/simple_aggr.demo";
		
		File file = new File(regFilePath);

		client.reset ();
		clientView.loadRegistryFromFile(file);

		//InputStream in = getClass().getResourceAsStream(regFilePath);
		//clientView.loadRegistryFromFile(in);

		// show prompt to user
		JOptionPane.showMessageDialog (clientView, 
					       "Registry for 'Simple Aggregation' loaded.\nPress \"Generate Plan\" and \"Run\" to run it.", 
					       "Success",
					       JOptionPane.INFORMATION_MESSAGE);
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
	   
	}
	if (source == simpleJoin) {
	    try {
		String regFilePath = InitManager.getDemoPath() + "/simple_join.demo";
		
		File file = new File(regFilePath);
		
		client.reset ();
		clientView.loadRegistryFromFile(file);

		//InputStream in = getClass().getResourceAsStream(regFilePath);
		//clientView.loadRegistryFromFile(in);

		// show prompt to user
		JOptionPane.showMessageDialog (clientView, 
					       "Registry for 'Simple Join' loaded.\nPress \"Generate Plan\" and \"Run\" to run it.", 
					       "Success",
					       JOptionPane.INFORMATION_MESSAGE);
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
	   
	}
    }
}
