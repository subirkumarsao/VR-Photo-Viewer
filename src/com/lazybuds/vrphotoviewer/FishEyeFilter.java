package com.lazybuds.vrphotoviewer;

import android.graphics.Bitmap;

public class FishEyeFilter {

	public static void filter(Bitmap src, Bitmap des, int r) {
		int width = src.getWidth();
		int height = src.getHeight();

		int shiftx = width / 2;
		int shifty = height / 2;
		try {

			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {

					int x = i - shiftx;
					int y = j - shifty;

					double h = Math.sqrt((x * x) + (y * y));

					double h_ = getL(h, r);

					int x_ = x;
					int y_ = y;
					if (h != 0) {
						x_ = (int)((x * h_) / h);
						y_ = (int)((y * h_) / h);
					}

					des.setPixel(x_ + shiftx, y_ + shifty, src.getPixel(i, j));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static int get(int x, int y, int r) {

		return 0;
	}

	public static double getL(double x, int r) {
		double h = Math.sqrt((x * x) + (r * r));
		double x1 = x * (h - r) / h;
		return (x - x1);
	}
}
