package com.lazybuds.vrphotoviewer.objects;

import android.graphics.Bitmap;
import android.graphics.Color;

import org.rajawali3d.Object3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.primitives.Plane;

import com.lazybuds.vrphotoviewer.utils.BitmapUtil;

public class VRPlane extends Object3D {

	public Plane plane;

	private Material material;

	public VRPlane(String label,int color, float height, float width) {

		plane = new Plane(height, width, 1, 1, 1);
		
		Bitmap bitmap = BitmapUtil.textAsString(label, 200, Color.WHITE,(int)height*100,(int)width*100);
		
		material = new Material();
		try {
			material.addTexture(new Texture("texture", bitmap));
		} catch (ATexture.TextureException e) {
			e.printStackTrace();
		}
		material.setColorInfluence(0.5f);
		material.setColor(color);
		
		plane.setMaterial(material);

		addChild(plane);
	}

}
