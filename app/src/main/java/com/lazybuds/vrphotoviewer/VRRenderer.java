package com.lazybuds.vrphotoviewer;

import android.content.Context;
import android.util.Log;

import com.lazybuds.vrphotoviewer.listeners.FolderSelectHandler;
import com.lazybuds.vrphotoviewer.objects.Event;
import com.lazybuds.vrphotoviewer.objects.Selectable;
import com.lazybuds.vrphotoviewer.utils.BitmapUtil;
import com.lazybuds.vrphotoviewer.utils.FileBrowser;
import com.lazybuds.vrphotoviewer.utils.FileBrowserUtil;
import com.lazybuds.vrphotoviewer.utils.ImageViewer;

import org.rajawali3d.Object3D;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.vr.renderer.RajawaliVRRenderer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VRRenderer extends RajawaliVRRenderer {

    private List<Selectable> selectableObjects;
    private FileBrowser browser;
    private int selectedIndex;
    private ImageViewer imageViewer;

    public void initScene() {

        selectableObjects = new ArrayList<>();

        DirectionalLight light = new DirectionalLight(1f, -1f, 0f);
        light.setPower(5f);
        getCurrentScene().addLight(light);

        getCurrentCamera().setFarPlane(1000);
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
    }

    public VRRenderer(Context context) {
        super(context);
    }

    public void clickOrMagnetTap() {
        if (selectedIndex == -1 || selectableObjects == null) {
            return;
        }
        if(selectedIndex>=selectableObjects.size()){
            return;
        }
        selectableObjects.get(selectedIndex).onSelect();
    }

    @Override
    public void onRender(long elapsedTime, double deltaTime) {
        super.onRender(elapsedTime, deltaTime);
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
    }

    public boolean isLookingAtObject(Selectable target) {
        return super.isLookingAtObject((Object3D)target, 10.0F);
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
            imageViewer.setFolderSelectHandler(new ImageBackSelectHandler());
            imageViewer.init(file, this, selectableObjects);
        }else{
            imageViewer.show(file);
        }
    }
}
