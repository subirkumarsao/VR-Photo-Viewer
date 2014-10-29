package com.lazybuds.vrphotoviewer;

import java.util.ArrayList;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;

public class MagnetoSensor {
	
	private TriggerDetector triggetDetector;
	private Thread detectorThread;

	public MagnetoSensor(Context context) {
		this.triggetDetector = new TriggerDetector(context);
	}

	public void start() {
		this.detectorThread = new Thread(this.triggetDetector);
		this.detectorThread.start();
	}

	public void stop() {
		if (this.detectorThread != null) {
			this.detectorThread.interrupt();
			this.triggetDetector.stop();
		}
	}

	public void setOnCardboardTriggerListener(
			OnCardboardTriggerListener listener) {
		this.triggetDetector.setOnCardboardTriggerListener(listener, new Handler());
	}

	private static class TriggerDetector implements Runnable,
			SensorEventListener {
		
		private static final int SEGMENT_SIZE = 20;
		private static final int NUM_SEGMENTS = 2;
		private static final int WINDOW_SIZE = 40;
		
		private SensorManager mSensorManager;
		private Sensor mMagnetometer;
		private ArrayList<float[]> mSensorData;
		private float[] mOffsets = new float[SEGMENT_SIZE];
		private MagnetoSensor.OnCardboardTriggerListener mListener;
		private Handler mHandler;

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public TriggerDetector(Context context) {
			this.mSensorData = new ArrayList();
			this.mSensorManager = ((SensorManager) context
					.getSystemService("sensor"));
			this.mMagnetometer = this.mSensorManager.getDefaultSensor(NUM_SEGMENTS);
		}

		public synchronized void setOnCardboardTriggerListener(
				MagnetoSensor.OnCardboardTriggerListener listener,
				Handler handler) {
			this.mListener = listener;
			this.mHandler = handler;
		}

		private void addData(float[] values, long time) {
			if (this.mSensorData.size() > WINDOW_SIZE) {
				this.mSensorData.remove(0);
			}
			this.mSensorData.add(values);

			evaluateModel();
		}

		private void evaluateModel() {
			if (this.mSensorData.size() < WINDOW_SIZE) {
				return;
			}
			float[] means = new float[NUM_SEGMENTS];
			float[] maximums = new float[NUM_SEGMENTS];
			float[] minimums = new float[NUM_SEGMENTS];

			float[] baseline = (float[]) this.mSensorData.get(this.mSensorData
					.size() - 1);
			for (int i = 0; i < NUM_SEGMENTS; i++) {
				int segmentStart = SEGMENT_SIZE * i;

				float[] mOffsets = computeOffsets(segmentStart, baseline);

				means[i] = computeMean(mOffsets);
				maximums[i] = computeMaximum(mOffsets);
				minimums[i] = computeMinimum(mOffsets);
			}
			float min1 = minimums[0];
			float max2 = maximums[1];
			if ((min1 < 30.0F) && (max2 > 130.0F)) {
				handleButtonPressed();
			}
		}

		private void handleButtonPressed() {
			this.mSensorData.clear();
			synchronized (this) {
				if (this.mListener != null) {
					this.mHandler.post(new Runnable() {
						public void run() {
							MagnetoSensor.TriggerDetector.this.mListener
									.onCardboardTrigger();
						}
					});
				}
			}
		}

		private float[] computeOffsets(int start, float[] baseline) {
			for (int i = 0; i < SEGMENT_SIZE; i++) {
				float[] point = (float[]) this.mSensorData.get(start + i);
				float[] o = { point[0] - baseline[0], point[1] - baseline[1],
						point[NUM_SEGMENTS] - baseline[NUM_SEGMENTS] };
				float magnitude = (float) Math.sqrt(o[0] * o[0] + o[1] * o[1]
						+ o[NUM_SEGMENTS] * o[NUM_SEGMENTS]);
				this.mOffsets[i] = magnitude;
			}
			return this.mOffsets;
		}

		private float computeMean(float[] offsets) {
			float sum = 0.0F;
			for (float o : offsets) {
				sum += o;
			}
			return sum / offsets.length;
		}

		private float computeMaximum(float[] offsets) {
			float max = (1.0F / -1.0F);
			for (float o : offsets) {
				max = Math.max(o, max);
			}
			return max;
		}

		private float computeMinimum(float[] offsets) {
			float min = (1.0F / 1.0F);
			for (float o : offsets) {
				min = Math.min(o, min);
			}
			return min;
		}

		public void run() {
			Process.setThreadPriority(-19);
			Looper.prepare();
			this.mSensorManager.registerListener(this, this.mMagnetometer, 0);
			Looper.loop();
		}

		public void stop() {
			this.mSensorManager.unregisterListener(this);
		}

		public void onSensorChanged(SensorEvent event) {
			if (event.sensor.equals(this.mMagnetometer)) {
				float[] values = event.values;
				if ((values[0] == 0.0F) && (values[1] == 0.0F)
						&& (values[NUM_SEGMENTS] == 0.0F)) {
					return;
				}
				addData((float[]) event.values.clone(), event.timestamp);
			}
		}

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	}

	public static abstract interface OnCardboardTriggerListener {
		public abstract void onCardboardTrigger();
	}
}
