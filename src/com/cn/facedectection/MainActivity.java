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
	//�����е�UIԪ�ض���
	private ImageView imageView;
	private Button loadButton, detectButton, restoreButton;
	private SeekBar featherSeekbar, thresholdSeekBar, gaussSeekBar;
	private TextView featherTextView, thresholdTextView, gausTextView;

	private Bitmap mFaceBitmap;//��ȡ��λͼ
	Bitmap bitmap = null; //��������ͼƬ������rgb565��ʽ��bitmapΪת����Ľ��
	
	FaceDetector faceDetector = null;//����������
	FaceDetector.Face[] face;//�������������

	private String picturePath = "";//���ص�ͼƬ·��

	private boolean isFace = false;//�Ƿ�Ϊ��
	private final int N_MAX = 2;//��������������
	private int feather_size = 5;//�𻯵Ĵ�С
	private int gauss = 20;//��˹����Ĵ�С
	
	private static String TAG = "APP";//�������tag
	private final int ALBUM_REQUEST_CODE = 1;//��ͼ������ķ��ؽ��

	//�����㷨������Imageview
	public Handler mHandler = new Handler()  
	{  
		public void handleMessage(Message msg)  
		{  
			switch(msg.what)  
			{  
			case 1:
				if(!isFace){
					Toast.makeText(MainActivity.this, "δ��⵽������", Toast.LENGTH_LONG).show();
					restoreImage();
				}
				else{
					Toast.makeText(MainActivity.this, "��⵽������", Toast.LENGTH_LONG).show();
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

		//��ʼ��������UI�ؼ�
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


		//��ʼ��imageview���ص�ͼ��
		mFaceBitmap = BitmapUtils.decodeSampledBitmapFromResource(getResources(), R.drawable.demo, 300, 300);
		imageView.setImageBitmap(mFaceBitmap); 
		imageView.invalidate();

		//��ԭͼ��
		restoreButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				restoreImage();
				detectButton.setEnabled(true);
			}
		});


		//����ͼ��
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

		//���ͼ��
		detectButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
                //����Ч���˲�
				ComicFilter();

				//��ʼ���������
				initFaceDetect();
                
				//����һ���µ��������thread
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

		//��ֵ����
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
				featherTextView.setText("��("+value+")");
			}
		});
		
		//��˹����ֵ����
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
				gausTextView.setText("����("+value+")");
			}
		});

		//��ֵ����
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
				thresholdTextView.setText("��ֵ("+value+")");
			}
		});


	}

	//�ָ�ͼ��
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

	//��ʼ������������
	public void initFaceDetect(){
		this.bitmap = mFaceBitmap.copy(Config.RGB_565, true);
		faceDetector = new FaceDetector(bitmap.getWidth(), bitmap.getHeight(), N_MAX);
		face = new FaceDetector.Face[N_MAX];
	}
	
	//����Ƿ���������ͼƬС��100x100���ز�����
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

	//�������
	public void detectFace(){

		int nFace = faceDetector.findFaces(bitmap, face);
		Log.i(TAG, "��⵽������n = " + nFace);

		for(int i=0; i<nFace; i++){
			android.media.FaceDetector.Face f  = face[i];
			PointF midPoint = new PointF();

			//��ȡ˫�۵ľ���
			float distance = f.eyesDistance();
			f.getMidPoint(midPoint);

			//���Ի�ȡ����������ڵ�����
			Rect faceRect = new Rect((int)(midPoint.x - distance), (int)(midPoint.y - distance),
					(int)(midPoint.x + distance), (int)(midPoint.y + distance));

			if(checkFace(faceRect)){
				int width = mFaceBitmap.getWidth();
				int height = mFaceBitmap.getHeight();

				Paint paint = new Paint();
				paint.setStrokeJoin(Paint.Join.ROUND);
				paint.setStrokeCap(Paint.Cap.ROUND);
				
				//���������ͼ����и�˹����
				mFaceBitmap = BitmapUtils.gaussBlur(mFaceBitmap, gauss, gauss/3);

				//�����µ�bitmap���Ա�ѡȡͼ������ͺ��۾����ڵ�����
				Bitmap newBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);

                
				//				imageProcess((int)midPoint.x, (int)(midPoint.y + distance), 2*distance/3);
				//				imageProcess(eyeRight.x, eyeRight.y, distance/2);
				//				imageProcess(eyeLeft.x, eyeLeft.y, distance/2);
				//����ͺ��۾����ڵ�����
				imageProcess((int)(midPoint.x - 5*distance/4), (int)(midPoint.y - distance/2), (int)(5*distance/2), (int)(2*distance));

				// ��ȡ�۾���������ڵ�����
				Canvas canvas = new Canvas(newBitmap);
				Path path = new Path();

				//				path.addCircle(eyeLeft.x, eyeLeft.y, distance/2, Path.Direction.CCW);
				//				path.addCircle(eyeRight.x, eyeRight.y, distance/2, Path.Direction.CCW);
				//				path.addCircle(midPoint.x, midPoint.y + distance, distance/2, Path.Direction.CCW);
				//				path.addCircle(midPoint.x, midPoint.y + 3*distance/5, distance, Path.Direction.CCW);
				path.addRect(midPoint.x- distance, midPoint.y-distance/2, midPoint.x+distance, midPoint.y+5*distance/4, Path.Direction.CCW);


				canvas.clipPath(path,Op.REPLACE);
				canvas.drawBitmap(mFaceBitmap, 0, 0, paint);
                
				//��ȡ�����Ľ���滻��ǰ��ʾ��λͼ
				mFaceBitmap = newBitmap.copy(Config.ARGB_8888, true);
			}
		}
	}

	//��Բ��ѡȡ�ı�Ե
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

	//���۾��������������ı�Ե
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
				// ��ȡRGB��ԭɫ
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

	//����Ч���˲�
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

				// R = |g �C b + g + r| * r / 256;
				pixel = G - B + G + R;
				if (pixel < 0)
					pixel = -pixel;
				pixel = pixel * R / 256;
				if (pixel > 255)
					pixel = 255;
				R = pixel;

				// G = |b �C g + b + r| * r / 256;
				pixel = B - G + B + R;
				if (pixel < 0)
					pixel = -pixel;
				pixel = pixel * R / 256;
				if (pixel > 255)
					pixel = 255;
				G = pixel;

				// B = |b �C g + b + r| * g / 256;
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

	//����ͼ���е�ͼƬ
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

