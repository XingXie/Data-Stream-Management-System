package stream.vis.view;

import java.util.*;
import javax.swing.*;

/**
 * Manages all of the icons used in the visualizer
 */
public class IconStore {
    /// The (class) path where all of the icons are located
    public static final String IMAGE_PATH = "/stream/vis/view/img/";
    
    /// Mapping from key to icon
    private Map map = new HashMap();
    
    private ImageIcon defaultIcon;
    
    private static IconStore s_store = new IconStore();
    
    /**
     * Constructor - loads all of the icons and stores the mapping
     */
    private IconStore() {
	
  	// load the image list (maps from entity type to icon filename)
  	ResourceBundle bundle = ResourceBundle.getBundle("stream.vis.view.img.ImageList"); 
  	PropertyResourceBundle list = (PropertyResourceBundle)bundle;
  	Enumeration keys = list.getKeys();
	
  	while (keys.hasMoreElements()) {
	    String key = (String) keys.nextElement();
	    
	    String imageName = IMAGE_PATH + list.getString(key);

	    // load the icon and place it into the map
	    ImageIcon icon = 
		new ImageIcon(getClass().getResource(imageName), key);
	    map.put(key, icon);
  	}
	
  	defaultIcon = (ImageIcon) map.get("default");
  	if (defaultIcon == null) 
	    defaultIcon = new ImageIcon();
    }
    
    /**
     * Returns an image icon for the given entity type
     */
    
    public static ImageIcon getIcon(String entityType) {
	ImageIcon icon = (ImageIcon) s_store.map.get(entityType);
	return icon;
    }
    
    /**
     * Returns the default (unknown) icon
     */
    public static ImageIcon getDefaultIcon() {
	return s_store.defaultIcon;
    }
}
