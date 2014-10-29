package com.lazybuds.vrphotoviewer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

public class MainActivity extends Activity implements
		MagnetoSensor.OnCardboardTriggerListener {

	private List<Integer> images = new ArrayList<Integer>();
	private int position = 0;
	private MagnetoSensor mMagnetSensor;
	private Bitmap des;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		getWindow().getDecorView().setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

		setContentView(R.layout.activity_main);

		loadDrawables();

		configureDisplay();

		this.mMagnetSensor = new MagnetoSensor(this);
		this.mMagnetSensor.setOnCardboardTriggerListener(this);

		displayImage();
	}

	public void loadDrawables() {

		Field[] drawables = R.drawable.class.getFields();
		for (Field f : drawables) {
			try {

				if (f.getName().startsWith("sample")) {
					images.add(f.getInt(null));
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	public void configureDisplay() {

		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x + getStatusBarHeight();
		int height = size.y;

		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linear_layout);
		linearLayout.setBackgroundColor(Color.BLACK);
		
		ImageView imageViewLeft = (ImageView) findViewById(R.id.image_view_left);
		ImageView imageViewRight = (ImageView) findViewById(R.id.image_view_right);

		android.view.ViewGroup.LayoutParams layoutParams = imageViewLeft
				.getLayoutParams();
		layoutParams.width = width / 2;
		layoutParams.height = height / 2;

		imageViewLeft.setLayoutParams(layoutParams);
		imageViewLeft.setScaleType(ScaleType.CENTER);

		imageViewRight.setScaleType(ScaleType.CENTER);
		imageViewRight.setLayoutParams(layoutParams);

	}

	public int getStatusBarHeight() {
		int result = 0;
		int resourceId = getResources().getIdentifier("status_bar_height",
				"dimen", "android");
		if (resourceId > 0) {
			result = getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}

	public void displayImage() {

		ImageView imageViewLeft = (ImageView) findViewById(R.id.image_view_left);
		ImageView imageViewRight = (ImageView) findViewById(R.id.image_view_right);

		if(des!=null){
			des.recycle();
		}
		
		Bitmap src = BitmapFactory.decodeResource(getResources(), images.get(position));
		des = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
		
		FishEyeFilter.filter(src, des,800);
		
		
		imageViewLeft.setImageBitmap(des);
		imageViewRight.setImageBitmap(des);
		src.recycle();
		src = null;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void onResume() {
		super.onResume();
		this.mMagnetSensor.start();
	}

	protected void onPause() {
		super.onPause();
		this.mMagnetSensor.stop();
	}

	public void nextImage() {
		position++;
		if (position >= images.size()) {
			position = 0;
		}
		displayImage();
	}

	@Override
	public void onCardboardTrigger() {
		nextImage();
	}
}
