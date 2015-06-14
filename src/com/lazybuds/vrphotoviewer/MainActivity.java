package com.lazybuds.vrphotoviewer;

import rajawali.vr.RajawaliVRActivity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;

import com.google.vrtoolkit.cardboard.sensors.MagnetSensor;

public class MainActivity extends RajawaliVRActivity implements MagnetSensor.OnCardboardTriggerListener {

	private VRRenderer mRenderer;

	private MagnetSensor magnetSensor;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN
						| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_FULLSCREEN
						| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		super.onCreate(savedInstanceState);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		

		mRenderer = new VRRenderer(this);
		mRenderer.setSurfaceView(mSurfaceView);
		
		mSurfaceView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mRenderer.clickOrMagnetTap();
			}
		});
		
		

		magnetSensor = new MagnetSensor(mRenderer.getContext());
		magnetSensor.setOnCardboardTriggerListener(this);
		magnetSensor.start();
		
		setRenderer(mRenderer);
	}

	@Override
	public void onCardboardTrigger() {
		mRenderer.clickOrMagnetTap();
	}
}
