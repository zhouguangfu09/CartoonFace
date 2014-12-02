package com.cn.utils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;

/**
 * 图片操作类
 */
public class ImageData {
	private Bitmap srcBitmap;
	private Bitmap dstBitmap;

	private int width;
	private int height;

	protected int[] colorArray;

	public ImageData(Bitmap bmp) {
		srcBitmap = bmp;
		width = bmp.getWidth();
		height = bmp.getHeight();
		dstBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		initColorArray();
	}

	/**
	 * 图片复制
	 */
	public ImageData clone() {
		return new ImageData(this.srcBitmap);
	}

	/**
	 * 初始化颜色数组
	 */
	private void initColorArray() {
		colorArray = new int[width * height];
		srcBitmap.getPixels(colorArray, 0, width, 0, 0, width, height);
	}

	/**
	 * 获取像素颜色
	 */
	public int getPixelColor(int x, int y) {
		return colorArray[y * srcBitmap.getWidth() + x];
	}

	/**
	 * 获取像素颜色
	 * offset是相对于x,y的偏移
	 */
	public int getPixelColor(int x, int y, int offset) {
		return colorArray[y * srcBitmap.getWidth() + x + offset];
	}

	/**
	 * 获取RGB图片的R部分
	 */
	public int getRComponent(int x, int y) {
		return Color.red(colorArray[y * srcBitmap.getWidth() + x]);
	}

	/**
	 * 获取偏移offset后的RGB图片的R部分
	 */
	public int getRComponent(int x, int y, int offset) {
		return Color.red(colorArray[y * srcBitmap.getWidth() + x + offset]);
	}

	/**
	 * 获取RGB图片的G部分
	 */
	public int getGComponent(int x, int y) {
		return Color.green(colorArray[y * srcBitmap.getWidth() + x]);
	}

	/**
	 * 获取偏移offset后的RGB图片的R部分
	 */
	public int getGComponent(int x, int y, int offset) {
		return Color.green(colorArray[y * srcBitmap.getWidth() + x + offset]);
	}

	/**
	 * 获取RGB图片的B部分
	 */
	public int getBComponent(int x, int y) {
		return Color.blue(colorArray[y * srcBitmap.getWidth() + x]);
	}

	/**
	 * 获取偏移offset后的RGB图片的R部分
	 */
	public int getBComponent(int x, int y, int offset) {
		return Color.blue(colorArray[y * srcBitmap.getWidth() + x + offset]);
	}

	/**
	 * 设置像素的颜色（RGB值）
	 */
	public void setPixelColor(int x, int y, int r, int g, int b) {
		int rgbcolor = (255 << 24) + (r << 16) + (g << 8) + b;
		colorArray[((y * srcBitmap.getWidth() + x))] = rgbcolor;
	}

	/**
	 * 设置像素的颜色（RGB值）
	 */
	public void setPixelColor(int x, int y, int rgbcolor) {
		colorArray[((y * srcBitmap.getWidth() + x))] = rgbcolor;
	}

	/**
	 * 获取颜色数组
	 */
	public int[] getColorArray() {
		return colorArray;
	}

	/**
	 * 获取处理后的位图
	 */
	public Bitmap getDstBitmap() {
		dstBitmap.setPixels(colorArray, 0, width, 0, 0, width, height);
		return dstBitmap;
	}

	public int safeColor(int a) {
		if (a < 0)
			return 0;
		else if (a > 255)
			return 255;
		else
			return a;
	}

	/**
	 * 获取宽度
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * 获取高度
	 */
	public int getHeight() {
		return height;
	}

}
