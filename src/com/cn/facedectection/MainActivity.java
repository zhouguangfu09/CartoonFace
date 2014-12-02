package com.cn.facedectection;

import com.cn.utils.BitmapUtils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.graphics.RectF;
import android.graphics.Region.Op;
import android.media.FaceDetector;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;  

public class MainActivity extends Activity {
	//布局中的UI元素定义
	private ImageView imageView;
	private Button loadButton, detectButton, restoreButton;
	private SeekBar featherSeekbar, thresholdSeekBar, gaussSeekBar;
	private TextView featherTextView, thresholdTextView, gausTextView;

	private Bitmap mFaceBitmap;//读取的位图
	Bitmap bitmap = null; //人脸检测的图片必须是rgb565格式，bitmap为转换后的结果
	
	FaceDetector faceDetector = null;//人脸检测对象
	FaceDetector.Face[] face;//检测室人脸数组

	private String picturePath = "";//加载的图片路径

	private boolean isFace = false;//是否为脸
	private final int N_MAX = 2;//检测脸的最大数量
	private int feather_size = 5;//羽化的大小
	private int gauss = 20;//高斯降噪的大小
	
	private static String TAG = "APP";//调试输出tag
	private final int ALBUM_REQUEST_CODE = 1;//打开图库所需的返回结果

	//更新算法处理后的Imageview
	public Handler mHandler = new Handler()  
	{  
		public void handleMessage(Message msg)  
		{  
			switch(msg.what)  
			{  
			case 1:
				if(!isFace){
					Toast.makeText(MainActivity.this, "未检测到人脸！", Toast.LENGTH_LONG).show();
					restoreImage();
				}
				else{
					Toast.makeText(MainActivity.this, "检测到人脸！", Toast.LENGTH_LONG).show();
					detectButton.setEnabled(false);
				}

				mFaceBitmap = BitmapUtils.getGrayBitmap(mFaceBitmap) ;
				imageView.setImageBitmap(mFaceBitmap);
				break;  
			default:  
				break;        
			}  
			super.handleMessage(msg);  
		}  
	};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		//初始化布局中UI控件
		imageView = (ImageView)findViewById(R.id.image);
		loadButton = (Button)findViewById(R.id.load);
		detectButton = (Button)findViewById(R.id.detect);
		restoreButton = (Button)findViewById(R.id.restore);


		featherSeekbar = (SeekBar)findViewById(R.id.feather);
		thresholdSeekBar = (SeekBar)findViewById(R.id.threshold);
		gaussSeekBar = (SeekBar)findViewById(R.id.gauss);

		featherTextView = (TextView)findViewById(R.id.ferther_text);
		thresholdTextView = (TextView)findViewById(R.id.threshold_text);
		gausTextView = (TextView)findViewById(R.id.gauss_text);


		//初始化imageview加载的图像
		mFaceBitmap = BitmapUtils.decodeSampledBitmapFromResource(getResources(), R.drawable.demo, 300, 300);
		imageView.setImageBitmap(mFaceBitmap); 
		imageView.invalidate();

		//还原图像
		restoreButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				restoreImage();
				detectButton.setEnabled(true);
			}
		});


		//加载图像
		loadButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(Intent.createChooser(intent,
						"Select Picture"), ALBUM_REQUEST_CODE);
			}
		});

		//检测图像
		detectButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
                //漫画效果滤波
				ComicFilter();

				//初始化人脸检测
				initFaceDetect();
                
				//开启一个新的人脸检测thread
				Thread thread = new Thread(new Runnable()  
				{  
					@Override  
					public void run()  
					{  
						detectFace();
						Message message=new Message();  
						message.what=1;  
						mHandler.sendMessage(message); 
					}  
				});  
				thread.start();  
			}
		});

		//羽化值调节
		featherSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar arg0, int value, boolean arg2) {
				// TODO Auto-generated method stub
				feather_size = value;
				featherTextView.setText("羽化("+value+")");
			}
		});
		
		//高斯降噪值调节
		gaussSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar arg0, int value, boolean arg2) {
				// TODO Auto-generated method stub
				gauss = value;
				gausTextView.setText("降噪("+value+")");
			}
		});

		//阈值调节
		thresholdSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seakbar) {
				// TODO Auto-generated method stub
				mFaceBitmap = BitmapUtils.getGrayBitmap(mFaceBitmap) ;
				imageView.setImageBitmap(BitmapUtils.binaryzation(mFaceBitmap, seakbar.getProgress()));
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar arg0, int value, boolean arg2) {
				// TODO Auto-generated method stub
				thresholdTextView.setText("阈值("+value+")");
			}
		});


	}

	//恢复图像
	public void restoreImage(){
		if(!mFaceBitmap.isRecycled()){
			mFaceBitmap.recycle();
			System.gc();
		}

		if(!picturePath.equals("")){				
			mFaceBitmap = BitmapUtils.getimage(picturePath);
			imageView.setImageBitmap(mFaceBitmap); 
			imageView.invalidate();
		}else{
			mFaceBitmap = BitmapUtils.decodeSampledBitmapFromResource(getResources(), R.drawable.demo, 300, 300);
			imageView.setImageBitmap(mFaceBitmap); 
			imageView.invalidate();
		}
	}

	//初始化人脸检测参数
	public void initFaceDetect(){
		this.bitmap = mFaceBitmap.copy(Config.RGB_565, true);
		faceDetector = new FaceDetector(bitmap.getWidth(), bitmap.getHeight(), N_MAX);
		face = new FaceDetector.Face[N_MAX];
	}
	
	//检测是否有人脸，图片小于100x100像素不予检测
	public boolean checkFace(Rect rect){
		int s = rect.width() * rect.height();

		if(s < 10000){
			isFace = false;
			return false;
		}
		else{
			isFace = true;
			return true;	
		}
	}

	//检测人脸
	public void detectFace(){

		int nFace = faceDetector.findFaces(bitmap, face);
		Log.i(TAG, "检测到人脸：n = " + nFace);

		for(int i=0; i<nFace; i++){
			android.media.FaceDetector.Face f  = face[i];
			PointF midPoint = new PointF();

			//获取双眼的距离
			float distance = f.eyesDistance();
			f.getMidPoint(midPoint);

			//粗略获取人脸检测所在的区域
			Rect faceRect = new Rect((int)(midPoint.x - distance), (int)(midPoint.y - distance),
					(int)(midPoint.x + distance), (int)(midPoint.y + distance));

			if(checkFace(faceRect)){
				int width = mFaceBitmap.getWidth();
				int height = mFaceBitmap.getHeight();

				Paint paint = new Paint();
				paint.setStrokeJoin(Paint.Join.ROUND);
				paint.setStrokeCap(Paint.Cap.ROUND);
				
				//对人脸检测图像进行高斯降噪
				mFaceBitmap = BitmapUtils.gaussBlur(mFaceBitmap, gauss, gauss/3);

				//创建新的bitmap，以便选取图像中嘴巴和眼睛所在的区域
				Bitmap newBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);

                
				//				imageProcess((int)midPoint.x, (int)(midPoint.y + distance), 2*distance/3);
				//				imageProcess(eyeRight.x, eyeRight.y, distance/2);
				//				imageProcess(eyeLeft.x, eyeLeft.y, distance/2);
				//羽化嘴巴和眼睛所在的区域
				imageProcess((int)(midPoint.x - 5*distance/4), (int)(midPoint.y - distance/2), (int)(5*distance/2), (int)(2*distance));

				// 提取眼睛和嘴巴所在的区域
				Canvas canvas = new Canvas(newBitmap);
				Path path = new Path();

				//				path.addCircle(eyeLeft.x, eyeLeft.y, distance/2, Path.Direction.CCW);
				//				path.addCircle(eyeRight.x, eyeRight.y, distance/2, Path.Direction.CCW);
				//				path.addCircle(midPoint.x, midPoint.y + distance, distance/2, Path.Direction.CCW);
				//				path.addCircle(midPoint.x, midPoint.y + 3*distance/5, distance, Path.Direction.CCW);
				path.addRect(midPoint.x- distance, midPoint.y-distance/2, midPoint.x+distance, midPoint.y+5*distance/4, Path.Direction.CCW);


				canvas.clipPath(path,Op.REPLACE);
				canvas.drawBitmap(mFaceBitmap, 0, 0, paint);
                
				//提取区域后的结果替换当前显示的位图
				mFaceBitmap = newBitmap.copy(Config.ARGB_8888, true);
			}
		}
	}

	//羽化圆形选取的边缘
	//	public void imageProcess(int cx, int cy, float radius) {
	//		com.cn.utils.ImageData image = new com.cn.utils.ImageData(mFaceBitmap);
	//
	//		int width = (int)radius + cx;
	//		int height = (int)radius + cy;
	//
	//		int max = (int)(radius) ;
	//		int diff = max - feather_size;
	//
	//
	//		int R, G, B;
	//		for (int y = cy - (int)radius; y < height; y++) {
	//			for (int x = cx - (int)radius; x < width; x++) {
	//				R = image.getRComponent(x, y); 
	//				G = image.getGComponent(x, y);
	//				B = image.getBComponent(x, y);
	//
	//				int dx = cx - x;
	//				int dy = cy - y;
	//
	//				float distSq = (float) Math.sqrt((dx * dx + dy * dy)/2.0);
	//				float v = ((float) distSq / diff) * 255;
	//
	//				R = (int) (R + (v));
	//				G = (int) (G + (v));
	//				B = (int) (B + (v));
	//				R = (R > 255 ? 255 : (R < 0 ? 0 : R));
	//				G = (G > 255 ? 255 : (G < 0 ? 0 : G));
	//				B = (B > 255 ? 255 : (B < 0 ? 0 : B));
	//				image.setPixelColor(x, y, R, G, B);
	//			}
	//		}
	//		mFaceBitmap = image.getDstBitmap();
	//	}

	//羽化眼睛和嘴巴所在区域的边缘
	public void imageProcess(int cx, int cy, int width, int height){
		int ratio = width > height ? height * 32768 / width : width * 32768 / height;
		com.cn.utils.ImageData image = new com.cn.utils.ImageData(mFaceBitmap);

		float size = (float) (feather_size/50.0);
		
		int max = width * width/4 + height * height/4;
		int min = (int) (max * size);
		int diff = max - min;

		Log.e(TAG, "diff: "+diff);
		cx = cx + width/2;
		cy = cy + height/2;

		int R, G, B;
		for (int y = cy - height/2; y < cy + height/2; y++) {
			for (int x = cx - width/2; x < cx + width/2; x++) {
				// 获取RGB三原色
				R = image.getRComponent(x, y); 
				G = image.getGComponent(x, y);
				B = image.getBComponent(x, y);

				// Calculate distance to center and adapt aspect ratio
				int dx = cx - x;
				int dy = cy - y;
				if (width > height) {
					dx = (dx * ratio) >> 15;
				} else {
					dy = (dy * ratio) >> 15;
				}
				int distSq = dx * dx + dy * dy;
				float v = ((float) distSq / diff) * 255;
				R = (int) (R + (v));
				G = (int) (G + (v));
				B = (int) (B + (v));
				R = (R > 255 ? 255 : (R < 0 ? 0 : R));
				G = (G > 255 ? 255 : (G < 0 ? 0 : G));
				B = (B > 255 ? 255 : (B < 0 ? 0 : B));
				image.setPixelColor(x, y, R, G, B);
			}
		}
		mFaceBitmap = image.getDstBitmap();
	}

	//漫画效果滤波
	public void ComicFilter() {
		com.cn.utils.ImageData image = new com.cn.utils.ImageData(mFaceBitmap);
		mFaceBitmap = image.getDstBitmap();

		int width = image.getWidth();
		int height = image.getHeight();

		int R, G, B, pixel;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				R = image.getRComponent(x, y); 
				G = image.getGComponent(x, y);
				B = image.getBComponent(x, y);

				// R = |g – b + g + r| * r / 256;
				pixel = G - B + G + R;
				if (pixel < 0)
					pixel = -pixel;
				pixel = pixel * R / 256;
				if (pixel > 255)
					pixel = 255;
				R = pixel;

				// G = |b – g + b + r| * r / 256;
				pixel = B - G + B + R;
				if (pixel < 0)
					pixel = -pixel;
				pixel = pixel * R / 256;
				if (pixel > 255)
					pixel = 255;
				G = pixel;

				// B = |b – g + b + r| * g / 256;
				pixel = B - G + B + R;
				if (pixel < 0)
					pixel = -pixel;
				pixel = pixel * G / 256;
				if (pixel > 255)
					pixel = 255;
				B = pixel;
				image.setPixelColor(x, y, R, G, B);
			}
		}
		Bitmap bitmap = image.getDstBitmap();
		mFaceBitmap = BitmapUtils.getGrayBitmap(bitmap);
	}

	//加载图库中的图片
	@Override
	protected void onActivityResult(int requestCode,int resultCode,Intent data){
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == ALBUM_REQUEST_CODE && resultCode == RESULT_OK && null != data) {
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };

			Cursor cursor = getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			cursor.moveToFirst();

			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			picturePath = cursor.getString(columnIndex);
			cursor.close();

			if(!picturePath.equals("")){
				if(!mFaceBitmap.isRecycled()){
					mFaceBitmap.recycle();
					System.gc();
				}

				mFaceBitmap = BitmapUtils.getimage(picturePath);
				imageView.setImageBitmap(mFaceBitmap); 
				imageView.invalidate();

				detectButton.setEnabled(true);
			}
		}
	}

}

