package com.lazybuds.vrphotoviewer.utils;

import android.util.Log;

import org.rajawali3d.vr.renderer.RajawaliVRRenderer;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.lazybuds.vrphotoviewer.Constants;
import com.lazybuds.vrphotoviewer.listeners.FolderSelectHandler;
import com.lazybuds.vrphotoviewer.listeners.OnSelectListener;
import com.lazybuds.vrphotoviewer.objects.Event;
import com.lazybuds.vrphotoviewer.objects.ImageItem;
import com.lazybuds.vrphotoviewer.objects.MenuItem;
import com.lazybuds.vrphotoviewer.objects.Selectable;


public class ImageViewer {

	private File currentPath;

	private RajawaliVRRenderer renderer;

	private File currentImage;

	private List<Selectable> selectableObjects;

	private ImageItem imageItem;

	private MenuItem back;

	private FolderSelectHandler folderSelectHandler;

	private BackSelectHandler backSelectHandler;

	private ImageSelectHandler imageSelectHandler;
	private File[] files;

	public void release() {

		renderer.getCurrentScene().removeChild(back);
		renderer.getCurrentScene().removeChild(imageItem);

		selectableObjects.remove(back);
		selectableObjects.remove(imageItem);

		selectableObjects = null;
		currentPath = null;
		renderer = null;
		currentImage = null;
		imageItem = null;
		back = null;
		folderSelectHandler = null;
		backSelectHandler = null;
		imageSelectHandler = null;
		files = null;
	}

	public ImageViewer() {

		backSelectHandler = new BackSelectHandler();
		imageSelectHandler = new ImageSelectHandler();
		imageItem = new ImageItem(Constants.MENU_DISTANCE);
		imageItem.setOnSelectListener(imageSelectHandler);
		back = new MenuItem("< BACK >", Constants.MENU_DISTANCE,
				Constants.FOLDER_COLOR);
		back.setOnSelectListener(backSelectHandler);
	}

	public void init(RajawaliVRRenderer renderer,
			List<Selectable> selectableObjects) {
		this.selectableObjects = selectableObjects;
		this.renderer = renderer;

		renderer.getCurrentScene().addChild(back);
		renderer.getCurrentScene().addChild(imageItem);
	}

	private void initControls() {
		float topIndex = Constants.MENU_TOP;
		back.setY(topIndex+4);
		imageItem.setY(topIndex - 8f);
	}

	class BackSelectHandler extends OnSelectListener {
		@Override
		public void onSelect(Event e) {
			onBackSelect();
		}

	}

	public void hide() {
		back.setY(-1000);
		imageItem.setY(-1000);
		selectableObjects.remove(back);
		selectableObjects.remove(imageItem);
	}

	public void show(File file) {
		this.currentImage = file;
		this.currentPath = file.getParentFile();
		this.files = this.currentPath.listFiles();
		selectableObjects.add(back);
		selectableObjects.add(imageItem);
		
		try {
			showImage(file.getCanonicalPath());
		} catch (IOException e) {
			Log.e("ERROR",e.getMessage());
		}
		initControls();
	}

	public void showImage(String imagePath) {
		imageItem.setImage(imagePath);
	}

	public void onBackSelect() {
		Event e = new Event();
		e.current = this;
		try {
			e.value = currentPath.getCanonicalPath();
		} catch (IOException e1) {
			Log.e("ERROR",e1.getMessage());
		}
		folderSelectHandler.onFolderSelect(e);
	}

	public void setFolderSelectHandler(FolderSelectHandler folderSelectHandler) {
		this.folderSelectHandler = folderSelectHandler;
	}

	public void showNext() {
		int index = getCurrentIndex();
		if (index == -1 || index == files.length - 1) {
			return;
		}
		for (int i = index+1; i < files.length; i++) {
			if (BitmapUtil.isImage(files[i])) {
				this.currentImage = files[i];
				try {
					showImage(files[i].getCanonicalPath());
				} catch (IOException e) {
					Log.e("ERROR",e.getMessage());
				}
				return;
			}
		}
	}

	public int getCurrentIndex() {
		try {
			String currentImagePath = currentImage.getCanonicalPath();
			for (int i = 0; i < files.length; i++) {
				if (files[i].getCanonicalPath().equalsIgnoreCase(
						currentImagePath)) {
					return i;
				}
			}
		} catch (IOException e) {
			Log.e("ERROR",e.getMessage());
		}
		return -1;
	}

	class ImageSelectHandler extends OnSelectListener {

		@Override
		public void onSelect(Event e) {
			showNext();
		}

	}
}
