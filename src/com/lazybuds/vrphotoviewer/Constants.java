package com.lazybuds.vrphotoviewer;

import android.graphics.Color;
import android.os.Environment;

public class Constants {

	public static final int SCENE_BG_COLOR = 0x6495ED;
	
	public static final int WALL_COLOR = 0xD2691E;

	public static final String DEFAULT_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
	
	public static final int MAX_FILES = 5; 
	
	public static final float MENU_DISTANCE = -30;
	
	public static final float MENU_TOP = 3;
	
	public static final float MENU_GAP = 2.5f;
	
	public static final float YAW_LIMIT = 0.06f;
	
	public static final float PITCH_LIMIT = 0.06f;
	
	public static final int FOLDER_COLOR = Color.GREEN;
	
	public static final int FILE_COLOR = Color.BLUE;
	
	public static final int NO_ACCESS_COLOR = Color.CYAN;
	
	public static final int IMAGE_WIDTH = 800;
	
	public static final int IMAGE_HIGHT = 800;
	
}
