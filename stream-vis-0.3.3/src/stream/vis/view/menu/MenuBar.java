package stream.vis.view.menu;

import javax.swing.*;
import stream.vis.data.Client;
import stream.vis.view.ClientView;

public class MenuBar extends JMenuBar {

    private FileMenu fileMenu;
    private CommandMenu commandMenu;
    private ViewMenu viewMenu;
    private InputsMenu inputsMenu;
    private DemoMenu demoMenu;
    private HelpMenu helpMenu;

    public MenuBar (Client client, ClientView clientView) {
	super ();
	fileMenu = new FileMenu (client, clientView);
	commandMenu = new CommandMenu(client, clientView);
	viewMenu = new ViewMenu (client, clientView);
	inputsMenu = new InputsMenu(client, clientView);
	demoMenu = new DemoMenu(client, clientView);
	helpMenu = new HelpMenu (client, clientView);
	
	add (fileMenu);
	add (commandMenu);
	add (viewMenu);
	add (inputsMenu);
	add (demoMenu);
	add (helpMenu);
    }

    public FileMenu getFileMenu() {return fileMenu;}

}
