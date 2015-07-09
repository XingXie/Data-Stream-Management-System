package stream.vis.view.input;

import stream.vis.data.*;
import stream.vis.net.*;
import stream.vis.util.FatalException;
import stream.vis.view.ClientView;
import stream.vis.view.dialog.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Timer;
import java.util.TimerTask;

public class TableFeederView extends JFrame 
    implements ActionListener, ChangeListener {

    private static final int REFRESH_DELAY = 100;
    // Xing: change the maximum input rate
    private static final int MAX_RATE = 100000;
    
    /// the Table we are related to 
    private NamedTable table;
    
    /// The table feeder object who actually does all the work
    private TableFeeder feeder;
    
    //------------------------------------------------------------
    // GUI related
    //------------------------------------------------------------
    
    /// Button that shows (Start/Pause/Resume)
    private JButton controlButton;

    /// Button that bind stream/table with file
    private JButton bindButton;
    
    /// Adjuster for rate of tuple input
    private JSlider rateAdjuster;

    /// display of the current sending rate
    private JLabel rateLabel;
    
    /// Statistics about how many tuples have been sent
    private JLabel sentLabel;
    
    /// Refresher to refresh that statistics about the inputting of tuples
    private Refresher refresher;
    
    /// The parent clientView
    private ClientView clientView;

    /// constructor
    public TableFeederView(NamedTable t, ClientView cv) { 
	super();

	table = t;
	clientView = cv;

	if (table.isStream())
	    setTitle("Stream Source: " + table.getTableName());
	else
	    setTitle ("Relation Source: " + table.getTableName());	
	
	feeder = null;
	refresher = null;

	// create the start/pause button
	controlButton = new JButton("Start");
	controlButton.setEnabled(false);
	controlButton.addActionListener(this);

	bindButton = new JButton("Bind");
	bindButton.addActionListener(this);

	rateAdjuster = new JSlider(JSlider.HORIZONTAL, 1, MAX_RATE, 1);
	rateAdjuster.setEnabled(false);
	rateAdjuster.addChangeListener(this);
	
	// the label of actual number of tuples sent
	sentLabel = new JLabel("Tuples Sent: 0");

	// the label of sending rate
	rateLabel = new JLabel("Rate: 1 tuples/sec"); 
	
	// the box containing the control button
	JPanel topPane = new JPanel(new FlowLayout());
	topPane.add(controlButton);
	topPane.add(bindButton);
	
	// pack rate-adjuster and progressBar
	JPanel ratePanel=new JPanel();
	ratePanel.setLayout(new BoxLayout(ratePanel,BoxLayout.PAGE_AXIS));    
	ratePanel.add(rateAdjuster);
	ratePanel.add(sentLabel);
	ratePanel.add(rateLabel);

	// add everything into the dialog
        getContentPane().add(topPane, BorderLayout.NORTH);
        getContentPane().add(ratePanel, BorderLayout.CENTER);
	
	// close the feeder if the window is closed
	addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    if(refresher != null) {
		 	refresher.stop ();
		    }
		    if(feeder != null) { 
			clientView.unbindTableFeeder (table);
			feeder = null;
			controlButton.setText("Start");
			controlButton.setEnabled(false);
		    }
		}
	    });
	
	pack();
    }
    
    public void actionPerformed(ActionEvent e) {
	JButton source = (JButton)e.getSource();
	if(source == controlButton) {	
	    if (controlButton.getText().equals("Start")) {
		controlButton.setText("Pause");
		refresher = new Refresher ();
		refresher.start();
		feeder.start();
		bindButton.setEnabled(false);
	    } 
	
	    else if (controlButton.getText().equals ("Pause")) {
		controlButton.setText("Resume");
		feeder.pause();
	    }
	
	    else if (controlButton.getText().equals ("Resume")) {
		controlButton.setText ("Pause");
		feeder.unpause ();
	    }
	    else {
		assert (false);
	    }
	}
	else if (source == bindButton) {
	    InputInfo inputInfo = new InputInfo (this, table);
	    inputInfo.setVisible (true);
	}
    }    

    public void bindTableFeeder (String fileName,
				   boolean bLoop, boolean bAppTs) {	
	    if (feeder != null) {
		clientView.unbindTableFeeder(table);
	    }
	    
	    feeder = clientView.bindTableFeeder(table, fileName, bLoop, bAppTs);
       	    if(refresher != null) {
		refresher.stop ();
	    }
	    controlButton.setText("Start");
	    controlButton.setEnabled(true);
	    rateLabel.setText("Rate: 1 tuples/sec");
	    rateAdjuster.setEnabled(true);
    }

    public void stateChanged(ChangeEvent e) {
	JSlider source = (JSlider)e.getSource();
	
	if (!source.getValueIsAdjusting()) {
	    int newRate = (int)source.getValue();
	    feeder.setRate(newRate);
	    rateLabel.setText("Rate: " + newRate + " tuples/sec");
	}
    }
    
    int numTuplesSent = 0;
    private void refresh () {
	// Number of tuples sent
	if(feeder ==null) System.out.println(" FEEDER is NULL!! ");
	numTuplesSent = feeder.getNumTuplesSent();
	
	// update rate label
	SwingUtilities.invokeLater (new Runnable () {
		public void run () {
		    sentLabel.setText("Tuples Sent: " + numTuplesSent);
		}
	    });    
    }
    
    private class Refresher extends TimerTask {
	Timer timer = null;
	
	public void start () {
	    timer = new Timer ();
	    timer.schedule (this, 0, REFRESH_DELAY);
	}
	
	public void run () {
	    refresh ();
	    //SwingUtilities.invokeLater (new Runnable() {
	    //public void run () {
	    //refresh ();
	    //}
	    //});    
	}
	
	public void stop () {
	    if(timer != null) timer.cancel();
	}
    }
}


