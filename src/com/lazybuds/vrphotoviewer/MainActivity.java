package com.lazybuds.vrphotoviewer;

import java.io.File;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

public class MainActivity extends Activity implements
		MagnetoSensor.OnCardboardTriggerListener {

	private static final int HEIGHT = 800;
	private static final int WIDTH = 800;

	private File[] directories = null;
	private int dirPosition = 0;

	private File[] images = null;
	private int position = 0;
	private MagnetoSensor mMagnetSensor;
	private Bitmap des;

	boolean imageFound = false;

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

		configureDisplay();

		this.mMagnetSensor = new MagnetoSensor(this);
		this.mMagnetSensor.setOnCardboardTriggerListener(this);

		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linear_layout);

		linearLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				nextImage();
			}
		});

		loadDrawables();
		loadFirstImage();
		displayImage();
	}

	private void loadFirstImage() {
		imageFound = false;
		for (int i = 0; i < directories.length; i++) {
			if (!directories[i].isDirectory()) {
				continue;
			}
			File[] files = directories[i].listFiles();
			for (int j = 0; j < files.length; j++) {
				if (isImage(files[j])) {
					images = files;
					imageFound = true;
					position = j;
					dirPosition = i;
					break;
				}
			}
			if (imageFound) {
				break;
			}
		}
	}

	private boolean isImage(File file) {
		if ((file == null) || !file.exists() || file.isDirectory()) {
			return false;
		}
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(file.getPath(), options);
		return (options.outWidth != -1) && (options.outHeight != -1);
	}

	public void loadDrawables() {
		File imageDir = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_DCIM).toString());
		directories = imageDir.listFiles();
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

		if (des != null) {
			des.recycle();
		}

		Bitmap src = null;

		if (!imageFound) {
			src = BitmapFactory.decodeResource(getResources(),
					R.drawable.no_image);
		} else {
			src = BitmapFactory.decodeFile(images[position].getAbsolutePath());
		}

		Bitmap resized = Bitmap.createScaledBitmap(src, WIDTH, HEIGHT, true);

		src.recycle();
		src = null;

		des = Bitmap.createBitmap(WIDTH, HEIGHT, resized.getConfig());

		FishEyeFilter.filter(resized, des, 800);

		imageViewLeft.setImageBitmap(des);
		imageViewRight.setImageBitmap(des);
		resized.recycle();
		resized = null;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		this.mMagnetSensor.start();
	}

	@Override
	protected void onPause() {
		super.onPause();
		this.mMagnetSensor.stop();
	}

	public void nextImage() {
		if(!imageFound){
			return;
		}

		do {
			position++;
			if ((position >= images.length)) {
				do {
					dirPosition++;
					if (dirPosition >= directories.length) {
						dirPosition = 0;
					}
				} while (!directories[dirPosition].isDirectory()
						|| (directories[dirPosition].list().length == 0));
				images = directories[dirPosition].listFiles();
				position = 0;
			}
		} while (!isImage(images[position]));
		displayImage();
	}

	@Override
	public void onCardboardTrigger() {
		nextImage();
	}
}
