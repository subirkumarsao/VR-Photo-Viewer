package com.lazybuds.vrphotoviewer.objects;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.lazybuds.vrphotoviewer.util.BitmapUtil;

import rajawali.Object3D;
import rajawali.materials.Material;
import rajawali.materials.textures.Texture;
import rajawali.materials.textures.ATexture.TextureException;
import rajawali.primitives.Plane;

public class VRPlane extends Object3D {

	public Plane plane;

	private Material material;

	public VRPlane(String label,int color, float height, float width) {

		setRotX(90);
		setRotY(90);
		setRotZ(90);

		plane = new Plane(height, width, 1, 1, 1);
		
		Bitmap bitmap = BitmapUtil.textAsString(label, 200, Color.WHITE,(int)height*100,(int)width*100);
		
		material = new Material();
		try {
			material.addTexture(new Texture("texture", bitmap));
		} catch (TextureException e) {
			e.printStackTrace();
		}
		material.setColorInfluence(0.5f);
		material.setColor(color);
		
		plane.setMaterial(material);
		
		plane.setRotZ(90);
		
		addChild(plane);
	}

}
