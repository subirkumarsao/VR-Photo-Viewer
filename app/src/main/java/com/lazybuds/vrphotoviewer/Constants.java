package com.lazybuds.vrphotoviewer;

import android.graphics.Color;
import android.os.Environment;

public class Constants {

	public static final String DEFAULT_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
	
	public static final int MAX_FILES = 5; 
	
	public static final float MENU_DISTANCE = -15;
	
	public static final float MENU_TOP = 4;
	
	public static final float MENU_GAP = 2.5f;

	public static final int FOLDER_COLOR = Color.GREEN;
	
	public static final int FILE_COLOR = Color.BLUE;
	
	public static final int NO_ACCESS_COLOR = Color.CYAN;
	
	public static final int IMAGE_WIDTH = 200;
	
	public static final int IMAGE_HEIGHT = 200;
	
}
