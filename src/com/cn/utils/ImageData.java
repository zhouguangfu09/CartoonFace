package com.cn.utils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;

/**
 * ͼƬ������
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
	 * ͼƬ����
	 */
	public ImageData clone() {
		return new ImageData(this.srcBitmap);
	}

	/**
	 * ��ʼ����ɫ����
	 */
	private void initColorArray() {
		colorArray = new int[width * height];
		srcBitmap.getPixels(colorArray, 0, width, 0, 0, width, height);
	}

	/**
	 * ��ȡ������ɫ
	 */
	public int getPixelColor(int x, int y) {
		return colorArray[y * srcBitmap.getWidth() + x];
	}

	/**
	 * ��ȡ������ɫ
	 * offset�������x,y��ƫ��
	 */
	public int getPixelColor(int x, int y, int offset) {
		return colorArray[y * srcBitmap.getWidth() + x + offset];
	}

	/**
	 * ��ȡRGBͼƬ��R����
	 */
	public int getRComponent(int x, int y) {
		return Color.red(colorArray[y * srcBitmap.getWidth() + x]);
	}

	/**
	 * ��ȡƫ��offset���RGBͼƬ��R����
	 */
	public int getRComponent(int x, int y, int offset) {
		return Color.red(colorArray[y * srcBitmap.getWidth() + x + offset]);
	}

	/**
	 * ��ȡRGBͼƬ��G����
	 */
	public int getGComponent(int x, int y) {
		return Color.green(colorArray[y * srcBitmap.getWidth() + x]);
	}

	/**
	 * ��ȡƫ��offset���RGBͼƬ��R����
	 */
	public int getGComponent(int x, int y, int offset) {
		return Color.green(colorArray[y * srcBitmap.getWidth() + x + offset]);
	}

	/**
	 * ��ȡRGBͼƬ��B����
	 */
	public int getBComponent(int x, int y) {
		return Color.blue(colorArray[y * srcBitmap.getWidth() + x]);
	}

	/**
	 * ��ȡƫ��offset���RGBͼƬ��R����
	 */
	public int getBComponent(int x, int y, int offset) {
		return Color.blue(colorArray[y * srcBitmap.getWidth() + x + offset]);
	}

	/**
	 * �������ص���ɫ��RGBֵ��
	 */
	public void setPixelColor(int x, int y, int r, int g, int b) {
		int rgbcolor = (255 << 24) + (r << 16) + (g << 8) + b;
		colorArray[((y * srcBitmap.getWidth() + x))] = rgbcolor;
	}

	/**
	 * �������ص���ɫ��RGBֵ��
	 */
	public void setPixelColor(int x, int y, int rgbcolor) {
		colorArray[((y * srcBitmap.getWidth() + x))] = rgbcolor;
	}

	/**
	 * ��ȡ��ɫ����
	 */
	public int[] getColorArray() {
		return colorArray;
	}

	/**
	 * ��ȡ������λͼ
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
	 * ��ȡ���
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * ��ȡ�߶�
	 */
	public int getHeight() {
		return height;
	}

}
