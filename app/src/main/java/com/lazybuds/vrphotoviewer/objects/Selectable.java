package com.lazybuds.vrphotoviewer.objects;


public interface Selectable {
	
	public abstract void onView(); 
	
	public abstract void onBlur();

	public abstract void onSelect();
	
	public float[] getModelView();
	
	public float[] getModel();
	
	public boolean isViewed();
	
	public boolean isDisabled();

	public void setDisabled(boolean isDisabled);
	
}
