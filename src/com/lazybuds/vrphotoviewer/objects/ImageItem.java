package com.lazybuds.vrphotoviewer.objects;

import rajawali.Object3D;
import rajawali.materials.Material;
import rajawali.materials.textures.ATexture.TextureException;
import rajawali.materials.textures.Texture;
import rajawali.primitives.Plane;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import com.lazybuds.vrphotoviewer.Constants;
import com.lazybuds.vrphotoviewer.listeners.OnBlurListener;
import com.lazybuds.vrphotoviewer.listeners.OnSelectListener;
import com.lazybuds.vrphotoviewer.listeners.OnViewListener;
import com.lazybuds.vrphotoviewer.util.BitmapUtil;

public class ImageItem extends Object3D implements Selectable {

	private Plane plane;

	private Material material;
	
	private Texture texture;
	
	private String image;
	
	private double Z_DISPLACE = 0f;
	
	private double z;
	
	private boolean isViewed;
	
	private boolean isDisabled;
	
	private String value;
	
	private OnSelectListener onSelectListener;
	
	private OnBlurListener onBlurListener;
	
	private OnViewListener onViewListener;
		
	private Bitmap bitmap;
	
	public ImageItem(double z) {
		
		this.z= z;
		
		setRotX(90);
		setRotY(90);
		setRotZ(90);
		
		plane = new Plane(10f,10f,1, 1, 1);
		
		material = new Material();
		plane.setMaterial(material);
		material.setColor(Color.WHITE);
		material.setColorInfluence(0f);
		
		plane.setRotZ(90);
		
		addChild(plane);
		setZ(z);
	}

	@Override
	public void onView() {
		setZ(z+Z_DISPLACE);
		isViewed = true;
		material.setColorInfluence(0f);
		if(onViewListener!=null){
			Event e = new Event();
			onViewListener.onView(e);
		}
	}

	@Override
	public void onBlur() {
		setZ(z);
		material.setColorInfluence(0.5f);
		isViewed = false;
		if(onBlurListener!=null){
			Event e = new Event();
			onBlurListener.onBlur(e);
		}
	}

	@Override
	public void onSelect() {
		if(onSelectListener!=null){
			Event e = new Event();
			e.current = this;
			onSelectListener.onSelect(e);
		}
	}
	
	public float[] getModelView(){
		return plane.getModelViewMatrix().getFloatValues();
	}
	
	public float[] getModel(){
		return plane.getModelMatrix().getFloatValues();
	}
	
	public boolean isViewed() {
		return isViewed;
	}

	public void setOnSelectListener(OnSelectListener onSelectListener) {
		this.onSelectListener = onSelectListener;
	}

	public void setOnBlurListener(OnBlurListener onBlurListener) {
		this.onBlurListener = onBlurListener;
	}

	public void setOnViewListener(OnViewListener onViewListener) {
		this.onViewListener = onViewListener;
	}

	@Override
	public boolean isDisabled() {
		return isDisabled;
	}

	@Override
	public void setDisabled(boolean isDisabled) {
		this.isDisabled = isDisabled;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public void release(){
		
		texture = null;
		material = null;
		plane = null;
		onSelectListener = null;
		onViewListener = null;
		onBlurListener = null;
		bitmap.recycle();
		bitmap = null;
	}
	
	public void finalize(){
		Log.e("DISTROY", this.image);
	}
	
	public void setImage(String image) {
		this.image = image;
		if(bitmap!=null){
			bitmap.recycle();
		}
		bitmap = BitmapUtil.getResizedBitmap(Constants.IMAGE_WIDTH, Constants.IMAGE_HIGHT, image);
		
		if(texture!=null){
			material.removeTexture(texture);
		}
		
		texture =new Texture("texture", bitmap);
		texture.setBitmap(bitmap);
		
		try {
			material.addTexture(texture);
		} catch (TextureException e) {
			Log.e("ERROR",e.getMessage());
		}
	}

}
