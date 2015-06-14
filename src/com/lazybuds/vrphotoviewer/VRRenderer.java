package com.lazybuds.vrphotoviewer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import rajawali.Object3D;
import rajawali.lights.DirectionalLight;
import rajawali.vr.RajawaliVRRenderer;
import android.content.Context;
import android.opengl.Matrix;
import android.util.Log;

import com.lazybuds.vrphotoviewer.listeners.FolderSelectHandler;
import com.lazybuds.vrphotoviewer.objects.Event;
import com.lazybuds.vrphotoviewer.objects.Selectable;
import com.lazybuds.vrphotoviewer.objects.VRPlane;
import com.lazybuds.vrphotoviewer.util.BitmapUtil;
import com.lazybuds.vrphotoviewer.util.FileBrowser;
import com.lazybuds.vrphotoviewer.util.FileBrowserUtil;
import com.lazybuds.vrphotoviewer.util.ImageViewer;

public class VRRenderer extends RajawaliVRRenderer {

	private float[] headView;
	private VRPlane leftWall;
	private VRPlane rightWall;
	private VRPlane backWall;
	private List<Selectable> selectableObjects;
	private FileBrowser browser;
	private int selectedIndex;
	private boolean busy;
	private ImageViewer imageViewer;

	public void initScene() {

		headView = new float[16];
		selectableObjects = new ArrayList<Selectable>();
		this.busy = false;

		DirectionalLight light = new DirectionalLight(1f, -1f, 0f);
		light.setPower(5f);
		getCurrentScene().addLight(light);

		light = new DirectionalLight(1f, 1f, 0f);
		light.setPower(5f);
		getCurrentScene().addLight(light);

		getCurrentCamera().setFarPlane(1000);

		getCurrentScene().setBackgroundColor(Constants.SCENE_BG_COLOR);

		leftWall = new VRPlane("Left Wall", Constants.WALL_COLOR, 20, 20);
		leftWall.setX(-20);
		leftWall.plane.setRotY(-90);

		rightWall = new VRPlane("Right Wall", Constants.WALL_COLOR, 20, 20);
		rightWall.setX(20);
		rightWall.plane.setRotY(90);

		backWall = new VRPlane("Back Wall", Constants.WALL_COLOR, 20, 20);
		backWall.setZ(20);
		backWall.plane.setRotY(180);

		getCurrentScene().addChild(leftWall);
		getCurrentScene().addChild(rightWall);
		getCurrentScene().addChild(backWall);

		addBrowserObjects(null);

		super.initScene();

	}

	private void addBrowserObjects(File path) {
		if (path == null) {
			path = FileBrowserUtil.getFile(Constants.DEFAULT_ROOT);
		}
		if (browser == null) {
			browser = new FileBrowser(this);
			BrowserFolderSelectHandler folderSelectHandler = new BrowserFolderSelectHandler();
			browser.setFolderSelectHandler(folderSelectHandler);
			browser.init(path, selectableObjects);
			browser.loadMenu();
		} else {
			browser.browse(path);
		}
		this.busy = false;
	}

	public VRRenderer(Context context) {
		super(context);
	}

	public void clickOrMagnetTap() {
		if (selectedIndex == -1 || selectableObjects == null)
			return;
		selectableObjects.get(selectedIndex).onSelect();
		this.busy = false;
	}

	@Override
	public void onDrawFrame(GL10 glUnused) {
		mHeadTransform.getHeadView(headView, 0);
		int index = -1;
		for (int i = 0; i < selectableObjects.size(); i++) {
			Selectable selectable = selectableObjects.get(i);
			if (selectable != null && !selectable.isDisabled()
					&& isLookingAtObject(selectable)) {
				selectable.onView();
				index = i;
				break;
			}
		}
		selectedIndex = index;

		blurOthers();

		super.onDrawFrame(glUnused);
	}

	private void blurOthers() {
		for (int i = 0; i < selectableObjects.size(); i++) {
			if (i == selectedIndex) {
				continue;
			}
			if (selectableObjects.get(i).isViewed()) {
				selectableObjects.get(i).onBlur();
			}
		}

	}

	private boolean isLookingAtObject(Selectable selectable) {
		if (!(selectable instanceof Object3D)) {
			return false;
		}

		float[] initVec = { 0, 0, 0, 1.0f };
		float[] objPositionVec = new float[4];

		Matrix.multiplyMM(selectable.getModelView(), 0, headView, 0,
				selectable.getModel(), 0);
		Matrix.multiplyMV(objPositionVec, 0, selectable.getModelView(), 0,
				initVec, 0);

		float pitch = (float) Math.atan2(objPositionVec[1], -objPositionVec[2]);
		float yaw = (float) Math.atan2(objPositionVec[0], -objPositionVec[2]);

		return Math.abs(pitch) < Constants.PITCH_LIMIT
				&& Math.abs(yaw) < Constants.YAW_LIMIT;
	}

	class BrowserFolderSelectHandler implements FolderSelectHandler {

		@Override
		public void onFolderSelect(Event e) {
			File file = new File((String) e.value);
			if (file.isDirectory() && file.canRead()) {
				try {
					addBrowserObjects(file.getCanonicalFile());
				} catch (IOException e1) {
					Log.e("ERROR",e1.getMessage());
				}
			}
			if (file.isFile()) {
				if (BitmapUtil.isImage(file)) {
					if (browser != null) {
						browser.hide();
						showImage(file);
					}
				}
			}
		}
	}

	class ImageBackSelectHandler implements FolderSelectHandler {

		@Override
		public void onFolderSelect(Event e) {
			imageViewer.hide();
			
			File file = new File((String) e.value);
			if (file.isDirectory() && file.canRead()) {
				try {
					addBrowserObjects(file.getCanonicalFile());
				} catch (IOException e1) {
					Log.e("ERROR",e1.getMessage());
				}
			}
		}

	}

	public void showImage(File file) {
		if (imageViewer == null) {
			imageViewer = new ImageViewer();
			imageViewer.init(file, this, selectableObjects);
			imageViewer.setFolderSelectHandler(new ImageBackSelectHandler());
		}else{
			imageViewer.show(file);
		}
	}
}
