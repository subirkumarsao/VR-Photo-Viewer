package com.lazybuds.vrphotoviewer.util;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import rajawali.vr.RajawaliVRRenderer;
import android.util.Log;

import com.lazybuds.vrphotoviewer.Constants;
import com.lazybuds.vrphotoviewer.listeners.FileSelectHandler;
import com.lazybuds.vrphotoviewer.listeners.FolderSelectHandler;
import com.lazybuds.vrphotoviewer.listeners.OnSelectListener;
import com.lazybuds.vrphotoviewer.objects.Event;
import com.lazybuds.vrphotoviewer.objects.MenuItem;
import com.lazybuds.vrphotoviewer.objects.Selectable;

public class FileBrowser {
	
	private List<Selectable> selectableObjects;
	
	private List<MenuItem> displayList;
	
	private File[] files;
	
	private FileSelectHandler fileSelectHandler;
	
	private FolderSelectHandler folderSelectHandler;
	
	private MenuItemSelectHandler menuItemSelectHandler;
	
	private File currentPath;
	
	private MenuItem parentFolder;
	
	private Queue<MenuItem> menuPool = new LinkedList<MenuItem>();
	
	private MenuItem up;
	
	private MenuItem down;
	
	private RajawaliVRRenderer renderer;
	
	int index;
	
	public void release() {
		removeMenuFromView(renderer);
		
		for(MenuItem menu:menuPool){
			renderer.getCurrentScene().removeChild(menu);
		}
		renderer.getCurrentScene().removeChild(up);
		renderer.getCurrentScene().removeChild(down);
		renderer.getCurrentScene().removeChild(parentFolder);
		
		displayList.clear();
		displayList.clear();
		displayList = null;
		files = null;
		renderer = null;
		parentFolder = null;
		up = null;
		down = null;
		menuItemSelectHandler = null;
		folderSelectHandler = null;
		fileSelectHandler = null;
		selectableObjects = null;
		files = null;
	}
	
	public void hide(){
		removeMenuFromView(renderer);
	}
	
	public FileBrowser(RajawaliVRRenderer renderer) {
		this.renderer = renderer;
	}

	public void init(File path, List<Selectable> selectableObjects) {
		this.selectableObjects = selectableObjects;
		currentPath = path;
		files = path.listFiles();
		index = 0;
		displayList = new ArrayList<MenuItem>();
		menuItemSelectHandler = new MenuItemSelectHandler();

		parentFolder = addMenu("[PARENT]",Constants.FOLDER_COLOR,menuItemSelectHandler);
		parentFolder.setValue("..");
		
		up = addMenu("< UP >",Constants.FOLDER_COLOR);
		up.setOnSelectListener(new OnSelectListener() {
			
			@Override
			public void onSelect(Event e) {
				if(index>=Constants.MAX_FILES){
					index-=Constants.MAX_FILES;
					loadMenu();
				}else{
					up.setDisabled(true);
				}
			}
		});
		down = addMenu("< DOWN >",Constants.FOLDER_COLOR);
		down.setOnSelectListener(new OnSelectListener() {
			
			@Override
			public void onSelect(Event e) {
				if(hashNext()){
					index+=Constants.MAX_FILES;
					loadMenu();
				}
			}
		});
		
		initMenuPool();
	}
	
	public void browse(File path){
		currentPath = path;
		files = path.listFiles();
		index = 0;
		loadMenu();
	}
	
	public void initMenuPool(){
		menuPool.clear();
		for(int i=0;i<Constants.MAX_FILES;i++){
			MenuItem menu = addMenu("", Constants.FILE_COLOR,menuItemSelectHandler);  
			menuPool.add(menu);
			menu.setY(-1000);
			renderer.getCurrentScene().addChild(menu);
		}
		
		renderer.getCurrentScene().addChild(parentFolder);
		renderer.getCurrentScene().addChild(up);
		renderer.getCurrentScene().addChild(down);
		
		parentFolder.setY(-1000);
		up.setY(-1000);
		down.setY(-1000);
		
	}
	
	public boolean hashNext(){
		return (files.length-index)>=Constants.MAX_FILES;
	}
	
	class MenuItemSelectHandler extends OnSelectListener{

		@Override
		public void onSelect(Event e) {
			MenuItem menuItem =  (MenuItem)e.current;
			String folderName = menuItem.getValue();
			onFileOrFolderSelect(folderName);
		}
		
	}
	
	public void loadMenu(){
		
		removeMenuFromView(renderer);
		
		int length = ((files.length-index)>=Constants.MAX_FILES)?Constants.MAX_FILES:(files.length-index);
		float topIndex = Constants.MENU_TOP;
	
		// Parent Folder
		parentFolder.setY(topIndex);
		displayList.add(parentFolder);
		topIndex-=Constants.MENU_GAP;
		Log.d("PATH", currentPath.getAbsolutePath());
		Log.d("PATH", Constants.DEFAULT_ROOT);
		Log.d("PATH", currentPath.getAbsolutePath().equalsIgnoreCase(Constants.DEFAULT_ROOT)+"");
		if(currentPath.getAbsolutePath().equalsIgnoreCase(Constants.DEFAULT_ROOT)){
			parentFolder.setDisabled(true);
		}else{
			parentFolder.setDisabled(false);
		}
		
		// UP Button 
		up.setY(topIndex);
		displayList.add(up);
		topIndex-=Constants.MENU_GAP;
		if(index==0){
			up.setDisabled(true);
		}else{
			up.setDisabled(false);
		}
		
		for (int i = 0; i < length; i++) {
			MenuItem menu = addMenu(files[index + i]);
			menu.setY(topIndex);
			displayList.add(menu);
			topIndex-=Constants.MENU_GAP;
		}
		
		// DOWN Button
		down.setY(topIndex);
		displayList.add(down);
		topIndex -= Constants.MENU_GAP;
		if (!hashNext()) {
			down.setDisabled(true);
		} else {
			down.setDisabled(false);
		}
		
		addMenuToView();
	}
	
	private void addMenuToView(){
		for(MenuItem menu:displayList){
			selectableObjects.add(menu);
			//renderer.getCurrentScene().addChild(menu);
		}
	}
	
	private void removeMenuFromView(RajawaliVRRenderer renderer){
		for(MenuItem menu:displayList){
			selectableObjects.remove(menu);
			
			menu.setY(-1000);
			if(menu==parentFolder || menu==up || menu==down){
				continue;
			}
			menuPool.add(menu);
		}
		displayList.clear();
	}
	
	
	private MenuItem addMenu(File file){
		MenuItem menuItem = menuPool.poll();

		int bgColor = 0;
		if(file.isDirectory()){
			if(file.canRead()){
				bgColor = Constants.FOLDER_COLOR;
			}else{
				bgColor = Constants.NO_ACCESS_COLOR;
			}
		}else{
			bgColor = Constants.FILE_COLOR;
		}
		
		menuItem.setLabel(file.getName());
		menuItem.setBgColor(bgColor);
		menuItem.setValue(file.getName());
		return menuItem;
	}
	
	private MenuItem addMenu(String label,int color){
		MenuItem item = new MenuItem(label,Constants.MENU_DISTANCE,color);
		return item;
	}
	
	private MenuItem addMenu(String label,int color, MenuItemSelectHandler handler){
		MenuItem item = new MenuItem(label,Constants.MENU_DISTANCE,color);
		item.setOnSelectListener(handler);
		return item;
	}
	
	public void onFileOrFolderSelect(String folderName){
		Event e = new Event();
		e.current = this;
		e.value = currentPath.getAbsolutePath()+"/"+folderName;
		folderSelectHandler.onFolderSelect(e);
	}

	public void setFileSelectHandler(FileSelectHandler fileSelectHandler) {
		this.fileSelectHandler = fileSelectHandler;
	}

	public void setFolderSelectHandler(FolderSelectHandler folderSelectHandler) {
		this.folderSelectHandler = folderSelectHandler;
	}
	
}
