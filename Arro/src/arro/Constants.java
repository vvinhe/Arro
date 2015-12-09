package arro;

import org.eclipse.graphiti.util.ColorConstant;
import org.eclipse.graphiti.util.IColorConstant;

public class Constants {
	public static final int LABEL_WIDTH = 100;
	public static final int PAD_SIZE = 14;
	public static final int HALF_PAD_SIZE = PAD_SIZE / 2;

	public final static String HIDDEN_RESOURCE = ".";
	public final static String NODE_EXT = "anod";
	public final static String MESSAGE_EXT = "amsg";
	public final static String DEVICE_EXT = "adev"; // FIXME make same extension; ArroNode doesn't know if refers device or node!!
	public final static String DIAGRAM_TYPE = "Arro"; // must match with plugin.xml
	
	public final static String FUNCTION_BLOCK = "Function";
	public final static String CODE_BLOCK = "Code";
	

	public final static String PROP_PAD_INPUT_KEY = "pad_input";
	public final static String PROP_PICT_KEY = "pictogram";
	public final static String PROP_PICT_NODE = "node";
	public final static String PROP_PICT_BOX = "box";
	public final static String PROP_PICT_PAD = "pad";
	public final static String PROP_PICT_CONNECTION = "connection";
	public final static String PROP_PICT_PASSIVE = "passive";
	
	public final static String PROP_TRUE_VALUE = "true";
	public final static String PROP_FALSE_VALUE = "false";
	
	public static final String PROP_UNDO_NODE_KEY = "undo_node";
	public static final String PROP_REDO_NODE_KEY = "redo_node";
	public static final String PROP_UNDO_PAD_KEY = "undo_pad";
	public static final String PROP_REDO_PAD_KEY = "redo_pad";
	public static final String PROP_UNDO_CONNECTION_KEY = "undo_connection";
	public static final String PROP_REDO_CONNECTION_KEY = "redo_connection";
	
	public static final String PROP_DOMAIN_NODE_KEY = "domain_node";  // domain diagram ID
	public static final String PROP_PAD_NAME_KEY = "pad_name";
	public static final Object PROP_SOURCE_PAD_KEY = "source_pad";
	public static final Object PROP_TARGET_PAD_KEY = "target_pad";
	
	
	public static final IColorConstant PAD_TEXT_FOREGROUND = new ColorConstant(51, 51, 153);
         
	public static final IColorConstant PAD_FOREGROUND_INPUT = new ColorConstant(255, 50, 0);
     
	public static final IColorConstant PAD_FOREGROUND_OUTPUT = new ColorConstant(50, 255, 0);
     
	public static final IColorConstant PAD_BACKGROUND_INPUT = new ColorConstant(255, 153, 153);

	public static final IColorConstant PAD_BACKGROUND_OUTPUT = new ColorConstant(153, 255, 153);

	public static final IColorConstant ANCHOR_FG = new ColorConstant(0, 0, 0);

	public static final IColorConstant ANCHOR_BG = new ColorConstant(200, 200, 200);

	public static final IColorConstant CLASS_TEXT_FOREGROUND = new ColorConstant(51, 51, 153);
         
	public static final IColorConstant CLASS_FOREGROUND = new ColorConstant(255, 102, 0);
     
	public static final IColorConstant CLASS_BACKGROUND = new ColorConstant(255, 204, 153);

	public static final int FunctionFile = 1;
	public static final int DeviceDiagram = 2;
	public static final int MessageDiagram = 3;
	public static final int PrimitiveMessageDiagram = 4;
	
	public static final String PLUGIN_ID = "arro";

	public static final String ATTR_LAUNCH_IP_ADDRESS = PLUGIN_ID + ".launch_ip_address";
	public static final String CONSOLE_NAME = "Target";
	public static final String ATTR_LAUNCH_PROJECT = PLUGIN_ID + ".launch_project";

	public static String FOLDER_DEVICES = "diagrams";
	public static String FOLDER_DIAGRAMS = "diagrams";
	public static String FOLDER_MESSAGES = "messages";




}
