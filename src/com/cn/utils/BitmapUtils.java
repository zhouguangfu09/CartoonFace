package com.cn.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.graphics.Region.Op;
public class BitmapUtils {

	private static final float KR = 0.299f;
	private static final float KG = 0.587f;
	private static final float KB = 0.114f;

	public static void bitmapScale(Bitmap baseBitmap, Paint paint, float x, float y) {

		Bitmap scaleBitmap = Bitmap.createBitmap(
				(int) (baseBitmap.getWidth() * x),
				(int) (baseBitmap.getHeight() * y), baseBitmap.getConfig());
		Canvas canvas = new Canvas(scaleBitmap);
		Matrix matrix = new Matrix();
		matrix.setScale(x, y);
		canvas.drawBitmap(baseBitmap, matrix,paint);
	}

	/**
	 * 图片旋转
	 */
	public static void bitmapRotate(Bitmap baseBitmap, Paint paint,float degrees) {
		// 创建一个和原图一样大小的图片
		Bitmap afterBitmap = Bitmap.createBitmap(baseBitmap.getWidth(),
				baseBitmap.getHeight(), baseBitmap.getConfig());
		Canvas canvas = new Canvas(afterBitmap);
		Matrix matrix = new Matrix();
		// 根据原图的中心位置旋转
		matrix.setRotate(degrees, baseBitmap.getWidth() / 2,
				baseBitmap.getHeight() / 2);
		canvas.drawBitmap(baseBitmap, matrix, paint);
	}

	/**
	 * 图片移动
	 */
	public static void bitmapTranslate(Bitmap baseBitmap, Paint paint, float dx, float dy) {
		// 需要根据移动的距离来创建图片的拷贝图大小
		Bitmap afterBitmap = Bitmap.createBitmap(
				(int) (baseBitmap.getWidth() + dx),
				(int) (baseBitmap.getHeight() + dy), baseBitmap.getConfig());
		Canvas canvas = new Canvas(afterBitmap);
		Matrix matrix = new Matrix();
		// 设置移动的距离
		matrix.setTranslate(dx, dy);
		canvas.drawBitmap(baseBitmap, matrix, paint);
	}

	/**
	 * 倾斜图片
	 */
	public static void bitmapSkew(Bitmap baseBitmap, Paint paint, float dx, float dy) {
		// 根据图片的倾斜比例，计算变换后图片的大小，
		Bitmap afterBitmap = Bitmap.createBitmap(baseBitmap.getWidth()
				+ (int) (baseBitmap.getWidth() * dx), baseBitmap.getHeight()
				+ (int) (baseBitmap.getHeight() * dy), baseBitmap.getConfig());
		Canvas canvas = new Canvas(afterBitmap);
		Matrix matrix = new Matrix();
		// 设置图片倾斜的比例
		matrix.setSkew(dx, dy);
		canvas.drawBitmap(baseBitmap, matrix, paint);
	}

	public static Bitmap decodeFromResource(Context context, int id) {
		Resources res = context.getResources();
		Bitmap bitmap = BitmapFactory.decodeResource(res,id).copy(Bitmap.Config.ARGB_8888, true);
		return bitmap;
	}   

	/**
	 * 保存图片到SD卡
	 */
	public static void saveToSdCard(String path, Bitmap bitmap) {
		if (null != bitmap && null != path && !path.equalsIgnoreCase("")) {
			try {
				File file = new File(path);
				FileOutputStream outputStream = null;
				//创建文件，并写入内容
				outputStream = new FileOutputStream(new File(path), true);
				bitmap.compress(Bitmap.CompressFormat.PNG, 30, outputStream);
				outputStream.flush();
				outputStream.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}



		}
	}

	/**
	 * 复制bitmap
	 */
	public static Bitmap duplicateBitmap(Bitmap bmpSrc) {
		if (null == bmpSrc) {
			return null;
		}

		int bmpSrcWidth = bmpSrc.getWidth();
		int bmpSrcHeight = bmpSrc.getHeight();

		Bitmap bmpDest = Bitmap.createBitmap(bmpSrcWidth, bmpSrcHeight,
				Config.ARGB_8888);
		if (null != bmpDest) {
			Canvas canvas = new Canvas(bmpDest);
			final Rect rect = new Rect(0, 0, bmpSrcWidth, bmpSrcHeight);

			canvas.drawBitmap(bmpSrc, rect, rect, null);
		}

		return bmpDest;
	}

	/**
	 * bitmap转字节码
	 */
	public static byte[] bitampToByteArray(Bitmap bitmap) {
		byte[] array = null;
		try {
			if (null != bitmap) {
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
				array = os.toByteArray();
				os.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return array;
	}

	/**
	 * 字节码转bitmap
	 */
	public static Bitmap byteArrayToBitmap(byte[] array) {
		if (null == array) {
			return null;
		}

		return BitmapFactory.decodeByteArray(array, 0, array.length);
	}

	public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
			int reqWidth, int reqHeight) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}

	/**
	 * 等比例压缩图片
	 * reqWidth：压缩后图片的宽度
	 * reqHeight:压缩后图片的高度
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		return inSampleSize;
	}

	/**
	 * 旋转图像
	 */
	public static Bitmap rotateBitamp(Bitmap bmp, float degree) {
		int bmpWidth = bmp.getWidth();
		int bmpHeight = bmp.getHeight();
		Matrix matrix = new Matrix();
		matrix.postRotate(degree);
		Bitmap resizeBmp = Bitmap.createBitmap(bmp, 0, 0, bmpWidth, bmpHeight, matrix, true);
		return resizeBmp;

	}

	/**
	 * 图像灰度化
	 */
	public static Bitmap getGrayBitmap(Bitmap mBitmap) {
		Bitmap mGrayBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Config.ARGB_8888);
		Canvas mCanvas = new Canvas(mGrayBitmap);
		Paint mPaint = new Paint();

		ColorMatrix mColorMatrix = new ColorMatrix();
		mColorMatrix.setSaturation(0);
		ColorMatrixColorFilter mColorFilter = new ColorMatrixColorFilter(mColorMatrix);
		mPaint.setColorFilter(mColorFilter);
		mCanvas.drawBitmap(mBitmap, 0, 0, mPaint);

		return mGrayBitmap;         
	}

	/**
	 * 图像二值化
	 * threshold：二值化的阈值
	 */
	public static Bitmap binaryzation(Bitmap graymap, int threshold)  
	{  
		int width = graymap.getWidth();
		int height = graymap.getHeight();

		Bitmap binarymap = null;
		binarymap = graymap.copy(Config.ARGB_8888, true);

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int col = binarymap.getPixel(i, j);
				int alpha = col & 0xFF000000;
				int red = (col & 0x00FF0000) >> 16;
			int green = (col & 0x0000FF00) >> 8;
			int blue = (col & 0x000000FF);
			//			int gray = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
			int gray = (int) (red + green + blue)/3;

			if (gray <= threshold) {
				gray = 0;
			} else {
				gray = 255;
			}

			int newColor = alpha | (gray << 16) | (gray << 8) | gray;

			binarymap.setPixel(i, j, newColor);
			}
		}


		return binarymap;  
	} 

	/**
	 * 从图片路径获取图片
	 * srcPath：获取图片的路径
	 */
	public static Bitmap getimage(String srcPath) {
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		newOpts.inJustDecodeBounds = true;
		newOpts.inPreferredConfig = Bitmap.Config.RGB_565;
		Bitmap bitmap = BitmapFactory.decodeFile(srcPath,newOpts);

		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;

		float hh = 800f;
		float ww = 480f;

		int be = 1;
		if (w > h && w > ww) {
			be = (int) (newOpts.outWidth / ww);
		} else if (w < h && h > hh) {
			be = (int) (newOpts.outHeight / hh);
		}
		if (be <= 0)
			be = 1;
		newOpts.inSampleSize = be;
		bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
		return compressImage(bitmap);
	}

	/**
	 * 无损压缩图像到100k以内
	 */
	private static Bitmap compressImage(Bitmap image) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		int options = 100;
		while ( baos.toByteArray().length / 1024>100) {	
			baos.reset();
			image.compress(Bitmap.CompressFormat.JPEG, options, baos);
			options -= 10;
		}

		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
		return bitmap;
	}
	
	/**
	 * 对图像进行高斯模糊，降低图片噪声
	 * r:降噪的半径，单位是pixel
	 * fai一般取r/3.
	 */
	public static Bitmap gaussBlur(Bitmap src, int r, int sigma){
		int width = src.getWidth();
		int height = src.getHeight();

		int[] pixels = discolor(src);
		int[] copixels = simpleReverseColor(pixels);
		simpleGaussBlur(copixels, width, height, r, sigma);
		simpleColorDodge(pixels, copixels);

		Bitmap bitmap = Bitmap.createBitmap(pixels, width, height,
				Config.RGB_565);
		return bitmap;
	}

	/**
	 * 一般高斯模糊算法
	 */
	private static void simpleGaussBlur(int[] data, int width, int height, int radius,
			float sigma) {

		float pa = (float) (1 / (Math.sqrt(2 * Math.PI) * sigma));
		float pb = -1.0f / (2 * sigma * sigma);

		// 产生高斯变换矩阵
		float[] gaussMatrix = new float[radius * 2 + 1];
		float gaussSum = 0f;
		for (int i = 0, x = -radius; x <= radius; ++x, ++i) {
			float g = (float) (pa * Math.exp(pb * x * x));
			gaussMatrix[i] = g;
			gaussSum += g;
		}

		for (int i = 0, length = gaussMatrix.length; i < length; ++i) {
			gaussMatrix[i] /= gaussSum;
		}

		// x 方向
		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				float b = 0;
				gaussSum = 0;
				for (int j = -radius; j <= radius; ++j) {
					int k = x + j;
					if (k >= 0 && k < width) {
						int index = y * width + k;
						int color = data[index];
						int cb = (color & 0x000000ff);

						b += cb * gaussMatrix[j + radius];

						gaussSum += gaussMatrix[j + radius];
					}
				}

				int index = y * width + x;
				int cb = (int) (b / gaussSum);
				data[index] = cb << 16 | cb << 8 | cb | 0xff000000;
			}
		}

		// y 方向
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				float b = 0;
				gaussSum = 0;
				for (int j = -radius; j <= radius; ++j) {
					int k = y + j;
					if (k >= 0 && k < height) {
						int index = k * width + x;
						int color = data[index];
						int cb = (color & 0x000000ff);

						b += cb * gaussMatrix[j + radius];

						gaussSum += gaussMatrix[j + radius];
					}
				}

				int index = y * width + x;
				int cb = (int) (b / gaussSum);
				data[index] = cb << 16 | cb << 8 | cb | 0xff000000;
			}
		}
	}
	
	/**
	 * 图片反色子函数
	 */
	private static int[] simpleReverseColor(int[] pixels) {
		int length = pixels.length;
		int[] result = new int[length];
		for (int i = 0; i < length; ++i) {
			int color = pixels[i];
			int b = 255 - (color & 0x000000ff);
			result[i] = b << 16 | b << 8 | b | 0xff000000;
		}
		return result;
	}
	
	/**
	 * 图片颜色变淡
	 */
	private static void simpleColorDodge(int[] baseColor, int[] mixColor) {

		for (int i = 0, length = baseColor.length; i < length; ++i) {
			int bColor = baseColor[i];
			int bb = (bColor & 0x000000ff);

			int mColor = mixColor[i];
			int mb = (mColor & 0x000000ff);
			int nb = colorDodgeFormular(bb, mb);

			baseColor[i] = nb << 16 | nb << 8 | nb | 0xff000000;
		}

	}
	
	/**
	 * 图片反色
	 */
	public static int[] discolor(Bitmap bitmap) {

		int picHeight = bitmap.getHeight();
		int picWidth = bitmap.getWidth();

		int[] pixels = new int[picWidth * picHeight];
		bitmap.getPixels(pixels, 0, picWidth, 0, 0, picWidth, picHeight);

		for (int i = 0; i < picHeight; ++i) {
			for (int j = 0; j < picWidth; ++j) {
				int index = i * picWidth + j;
				int color = pixels[index];
				int r = (color & 0x00ff0000) >> 16;
				int g = (color & 0x0000ff00) >> 8;
				int b = (color & 0x000000ff);
				int grey = (int) (r * KR + g * KG + b * KB);
				pixels[index] = grey << 16 | grey << 8 | grey | 0xff000000;
			}
		}
		
		return pixels;

	}


	/**
	 * 图片颜色变淡公式
	 */
	private static int colorDodgeFormular(int base, int mix) {
		
		int result = base + (base * mix) / (255 - mix);
		result = result > 255 ? 255 : result;
		return result;
		
	}
}
