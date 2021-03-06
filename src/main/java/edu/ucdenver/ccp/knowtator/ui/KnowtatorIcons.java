package edu.ucdenver.ccp.knowtator.ui;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.HashMap;

public class KnowtatorIcons {
    public static final String OPEN_DOCUMENT_ICON = "icons8-TXT.png";
    public static final String CLOSE_DOCUMENT_ICON = "icons8-Close Window-48.png";
    public static final String LOAD_ANNOTATIONS_ICON = "icons8-XML-48.png";
    public static final String ADD_TEXT_ANNOTATION_ICON = "icons8-Create.png";
    public static final String REMOVE_TEXT_ANNOTATION_ICON = "icons8-Trash Can.png";
    public static final String SAVE_ANNOTATIONS_ICON = "icons8-Save.png";
    public static final String NEW_ANNOTATOR_ICON = "icons8-Edit Profile.png";
    public static final String NEW_HIGHLIGHTER_ICON = "icons8-Paint Palette-48.png";
    public static final String SWITCH_PROFILE_ICON = "icons8-User-48.png";
    public static final String INCREASE_TEXT_SIZE_ICON = "icons8-Increase Font.png";
    public static final String DECREASE_TEXT_SIZE_ICON = "icons8-Decrease Font.png";
    public static final String INCREMENT_RIGHT = "icons8-First-48.png";
    public static final String INCREMENT_LEFT = "icons8-Last-48.png";
    public static final String RUN_IAA_ICON = "icons8-Exchange-48.png";
    public static final String SHOW_NODE_ICON = "ShowClassIcon.png";

    public static final String UGLY_ICON = "";

    public static final String RELATIVE_PATH = "/icon/";

    public static final Integer MENU_ITEM_WIDTH = 20;
    public static final Integer MENU_ITEM_HEIGHT = 20;



    public static HashMap<String, ImageIcon> iconMap = new HashMap<>();


    static {
        ImageIcon uglyIcon = loadIcon(UGLY_ICON);
        iconMap.put(UGLY_ICON, uglyIcon);
    }

    public static ImageIcon getIcon(String iconName) {
        ImageIcon imageIcon = iconMap.get(iconName);
        if (imageIcon == null) {
            imageIcon = loadIcon(iconName);
            iconMap.put(iconName, imageIcon);
        }
        Image img = imageIcon.getImage();
        Image newimg = img.getScaledInstance(MENU_ITEM_WIDTH, MENU_ITEM_HEIGHT, Image.SCALE_SMOOTH);
        imageIcon = new ImageIcon(newimg);
        return imageIcon;
    }

    public static ImageIcon loadIcon(String iconName) {
        ImageIcon imageIcon = null;
        URL iconURL = KnowtatorIcons.class.getResource(RELATIVE_PATH + iconName);
        if (iconURL != null) {
            imageIcon = new ImageIcon(iconURL);
        }

        if (imageIcon == null) {
            imageIcon = iconMap.get(UGLY_ICON);
        }
        return imageIcon;
    }
}
