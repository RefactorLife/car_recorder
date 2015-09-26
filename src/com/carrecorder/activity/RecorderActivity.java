package com.carrecorder.activity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import com.carrecorder.conf.ActivityConf;
import com.carrecorder.conf.EnvConf;
import com.carrecorder.db.table.Record;
import com.carrecorder.sensor.GPS;
import com.carrecorder.sensor.GPSListener;
import com.carrecorder.utils.Common;
import com.carrecorder.utils.animation.CompassRoatation;
import com.carrecorder.utils.animation.SpeedPoindRoatation;
import com.carrecorder.utils.animation.speed.SpeedAnimation;
import com.carrecorder.utils.animation.speed.SpeedChipRotation;
import com.carrecorder.utils.camera.CameraUtils;
import com.carrecorder.utils.debug.Log;
import com.carrecorder.utils.time.TimeUtil;
import com.db.DBExecutor;
import com.db.sql.Sql;
import com.db.sql.SqlFactory;
import myjob.carrecorder.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.SurfaceHolder.Callback;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

public class RecorderActivity extends Activity implements GPSListener {
	// final par
	private final int isChoice = 1;

	// view
	private SurfaceView mSurfaceview;
	private TextView timer;
	private TextView timerTextView;
	private TextView lightTextView;
	private TextView gpsTextView;
	private TextView orentationTextView;
	private TextView speedTextView;
	private TextView distTextView;
	private TextView loadingTextView;
	private ImageView compassView;
	private ImageView speedChip1;
	private ImageView speedChip2;
	private ImageView speedRedPoint;
	private ImageView speedRoundLine;
	private ImageView speedBarBtn;
	private ImageView loadingImage;

	// intent
	private int recorderCheckBoxStatus;
	private int lightNoticeCheckBoxStatus;
	private int overspeedNoticeCheckBoxStatus;

	// camera
	private MediaRecorder mMediaRecorder;
	private SurfaceHolder mSurfaceHolder;
	protected Camera cameraDevice;
	private File mRecVedioPath;
	private File mFileForSave;
	private boolean bool;
	private boolean isPreview;
	private boolean isRecording = false;
	private int hour = 0;
	private int minute = 0;
	private int second = 0;
	private TextToSpeech text2speech;

	// sensor & keep screen light
	private SensorManager mySensorManager;
	private SensorEventListener sensorEventListener;
	private BrightnessManager brightnessManager;
	private int clo = 0;
	private int range;// the flag for notice text
	private int k = 0;
	private int m = 0;
	private float f = 1.0f;
	private boolean symbol1;

	// Other
	private GPS gps;// GPS
	private boolean isHaveGpsInfo = true;
	private DBExecutor dbExecutor;// DB
	private CompassRoatation compassRoatation;// Animation
	private SpeedAnimation speedAnimation;
	private SpeedPoindRoatation speedPoindRoatation;// speed Animation
	private int maxSpeed = EnvConf.MAX_SPEED;// 限速

	// test
	private Runnable testRunnable;

	private void initView() {
		// to full screen show
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_recorder);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		compassView = (ImageView) findViewById(R.id.compass_in);
		speedChip1 = (ImageView) findViewById(R.id.speed_chip_1);
		speedChip2 = (ImageView) findViewById(R.id.speed_chip_2);
		speedRedPoint = (ImageView) findViewById(R.id.speed_red_point);
		speedRoundLine = (ImageView) findViewById(R.id.speed_round_line);
		speedBarBtn = (ImageView) findViewById(R.id.speed_bar_btn);
		loadingImage = (ImageView) findViewById(R.id.loading_image);
		mSurfaceview = (SurfaceView) this.findViewById(R.id.surface_camera);
		timer = (TextView) findViewById(R.id.timer);
		gpsTextView = (TextView) findViewById(R.id.gps_textview);
		timerTextView = (TextView) findViewById(R.id.timer_textview);
		lightTextView = (TextView) findViewById(R.id.light_textview);
		speedTextView = (TextView) findViewById(R.id.speed_text_view);
		distTextView = (TextView) findViewById(R.id.dist_text_view);
		orentationTextView = (TextView) findViewById(R.id.orentation_textview);
		loadingTextView = (TextView) findViewById(R.id.loading_text);
	}

	private void initSensor() {
		mySensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	}

	private void initListener() {
		speedBarBtn.setOnClickListener(new StartBtnLisener());
	}

	private void initIntentData() {
		Intent intent = getIntent();
		recorderCheckBoxStatus = intent.getIntExtra(ActivityConf.intent_range1,
				0);
		lightNoticeCheckBoxStatus = intent.getIntExtra(
				ActivityConf.intent_range2, 0);
		overspeedNoticeCheckBoxStatus = intent.getIntExtra(
				ActivityConf.intent_range3, 0);
	}

	private void initDynamicView() {
		if (recorderCheckBoxStatus == isChoice) {
			// mVideoStartBtn.setVisibility(View.VISIBLE);
		} else {
			// mVideoStartBtn.setVisibility(View.GONE);
		}
		if (lightNoticeCheckBoxStatus != isChoice) {
			timerTextView.setVisibility(View.GONE);
			lightTextView.setVisibility(View.GONE);
		}
		// set timer invisible
		timer.setVisibility(View.GONE);
		// set buffer path
		mRecVedioPath = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/CarRecorder/video/");
		if (!mRecVedioPath.exists()) {
			mRecVedioPath.mkdirs();
		}
	}

	private void initCamera() {
		// bind the preview view , SurfaceHolder is a controller of SurfaceView
		SurfaceHolder holder = mSurfaceview.getHolder();
		holder.addCallback(new CarmeraCallBack());
		// holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);//this is
		// ignored, this value is set automatically when needed.
	}

	private void initOther() {
		text2speech = new TextToSpeech(getApplicationContext(),
				new MyText2SpeechListener());
		gps = new GPS(this);
		brightnessManager = new BrightnessManager();
		brightnessManager.keepAwake();
		compassRoatation = new CompassRoatation(compassView);
		speedPoindRoatation = new SpeedPoindRoatation(speedRedPoint);
		speedAnimation = new SpeedAnimation(speedChip1, speedChip2,
				EnvConf.SPEED_CHIP_SPEED, speedRoundLine);
		dbExecutor = DBExecutor.getInstance(this);
		voiceToast(EnvConf.TUTORIAL_1);
		handlerInit.post(taskInit);
		SpeedChipRotation.stopRotateAndInvisibility(loadingImage);
		loadingTextView.setVisibility(View.INVISIBLE);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();
		initIntentData();
		initSensor();
		initDynamicView();
		initCamera();
		initListener();
		initOther();
		// test();
	}

	private Handler handlerInit = new Handler();
	private Runnable taskInit = new Runnable() {
		@Override
		public void run() {
			speedAnimation.getChipRotation().startRun();// 速度碎片模块
			speedAnimation.getSpeedRoundAnimation().start();// 波浪动画
		}
	};
	private Handler handlerCameraPreview = new Handler();
	private Runnable taskCameraPreview = new Runnable() {
		@Override
		public void run() {
			Intent intent1 = getIntent();
			final int rank2_1 = intent1.getIntExtra("range2_1", 0);
			final int rank2_2 = intent1.getIntExtra("range2_2", 0);
			try {
				cameraDevice = CameraUtils.openCamera(cameraDevice,
						mSurfaceHolder);
			} catch (IOException e) {
				return;
			}
			isPreview = true;
			handlerCameraPreview.postDelayed(this, 8000);
			cameraDevice.setOneShotPreviewCallback(new PreviewCallback() {
				@Override
				public void onPreviewFrame(byte[] data, Camera camera) {
					YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21,
							camera.getParameters().getPreviewSize().width,
							camera.getParameters().getPreviewSize().height,
							null);
					int mWidth = camera.getParameters().getPreviewSize().width;
					int mHeight = camera.getParameters().getPreviewSize().height;
					ByteArrayOutputStream mJpegOutput = new ByteArrayOutputStream(
							data.length);
					yuvimage.compressToJpeg(new Rect(0, 0, mWidth, mHeight),
							100, mJpegOutput);
					Bitmap mBitmapIn = BitmapFactory.decodeByteArray(
							mJpegOutput.toByteArray(), 0, mJpegOutput.size());
					Bitmap bitmapNew = mBitmapIn.copy(Config.ARGB_8888, true);
					for (int i = 0; i < bitmapNew.getWidth(); i = i + 30) {
						for (int j = 0; j < bitmapNew.getHeight(); j = j + 30) {
							int col = bitmapNew.getPixel(i, j);
							int red = (col & 0x00FF0000) >> 16;
							int green = (col & 0x0000FF00) >> 8;
							int blue = (col & 0x000000FF);
							int grey = (int) ((float) red * 0.3 + (float) green
									* 0.59 + (float) blue * 0.11);
							m = m + 1;
							k = k + grey;
						}
					}
					bitmapNew.recycle();
					if (120 * m <= k) {
						timerTextView.setVisibility(View.GONE);
						lightTextView.setVisibility(View.GONE);
						k = 0;
						m = 0;
						return;
					}
					if (k >= 70 * m && k <= 100 * m) {
						if (rank2_1 == isChoice) {
							timerTextView.setText(EnvConf.NOTICE_WEAK_LIGHT_1);
							timerTextView.setVisibility(View.VISIBLE);
							timerTextView.setTextColor(Color.WHITE);
							lightTextView.setVisibility(View.GONE);
						}
						if (rank2_2 == isChoice) {
							voiceToast(EnvConf.WEAK_LIGHT_1);
						}
					}
					if (k <= 40 * m) {
						if (rank2_1 == isChoice) {
							lightTextView
									.setText(EnvConf.NOTICE_WEAK_LIGHT_2_MUTIL_LINE);
							lightTextView.setVisibility(View.VISIBLE);
							lightTextView.setTextColor(Color.WHITE);
							timerTextView.setVisibility(View.GONE);
							sparkBackground();
						}
						if (rank2_2 == isChoice) {
							voiceToast(EnvConf.WEAK_LIGHT_2);
						}
					}
					k = 0;
					m = 0;
				}
			});

		}

	};

	private Handler handlerSpeed = new Handler();
	private Runnable taskSpeed = new Runnable() {
		@Override
		public void run() {
			if (!isHaveGpsInfo && isRecording) {
				SpeedChipRotation.rotate(360f, loadingImage, 500);
				loadingTextView.setVisibility(View.VISIBLE);
			} else {
				isHaveGpsInfo = false;
			}
			handlerSpeed.postDelayed(taskSpeed, 10 * 1000);
			Intent intent2 = getIntent();
			final int rank3_2 = intent2.getIntExtra("range3_2", 0);
			if (gps != null) {
				double speed = gps.getSpeed();
				speed = speed * 3600 / 1000;
				if (speed >= maxSpeed) {
					if (rank3_2 == isChoice) {
						voiceToast(EnvConf.SLOW_DOWN);
						speedBarBtn
								.setBackgroundResource(R.drawable.speed_bar_bk_over_speed);
					}
				} else {
					speedBarBtn
							.setBackgroundResource(R.drawable.speed_bar_bk_start);
				}
			}

		}
	};

	private Handler handlerLight = new Handler();
	private Runnable taskLight = new Runnable() {

		@Override
		public void run() {
			Intent intent1 = getIntent();
			final int rank2_1 = intent1.getIntExtra("range2_1", 0);
			final int rank2_2 = intent1.getIntExtra("range2_2", 0);
			Sensor s = mySensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
			sensorEventListener = new SensorEventListener() {

				@Override
				public void onSensorChanged(SensorEvent event) {
					float[] values = event.values;
					if (values[0] <= 50 && values[0] > 10) {
						if (rank2_1 == isChoice) {
							lightTextView
									.setText(EnvConf.NOTICE_WEAK_LIGHT_1_MUTIL_LINE);
							lightTextView.setVisibility(View.VISIBLE);
							lightTextView.setTextColor(Color.RED);
						}
						if (rank2_2 == isChoice) {
							voiceToast(EnvConf.WEAK_LIGHT_1);
						}
					} else if (values[0] <= 10) {
						if (rank2_1 == isChoice) {
							lightTextView
									.setText(EnvConf.NOTICE_WEAK_LIGHT_2_MUTIL_LINE);
							lightTextView.setVisibility(View.VISIBLE);
							lightTextView.setTextColor(Color.WHITE);
							sparkBackground();
						}
						if (rank2_2 == isChoice) {
							voiceToast(EnvConf.WEAK_LIGHT_2);
						}
					} else if (values[0] > 50) {
						timerTextView.setVisibility(View.GONE);
						lightTextView.setVisibility(View.GONE);
					}
				}

				@Override
				public void onAccuracyChanged(Sensor sensor, int accuracy) {

				}
			};
			mySensorManager.registerListener(sensorEventListener, s,
					SensorManager.SENSOR_DELAY_NORMAL);
			symbol1 = true;
		}

	};

	/*
	 * Setting Timer to timing
	 */
	private Handler handler = new Handler();
	private Runnable task = new Runnable() {
		public void run() {
			if (bool) {
				handler.postDelayed(this, 1000);
				second++;
				if (second >= 60) {
					minute++;
					second = second % 60;
				}
				if (minute >= 60) {
					hour++;
					minute = minute % 60;
				}
				timer.setText(Common.formatTimeForShow(hour, minute, second));
			}
		}
	};

	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this)
				.setMessage("请选择操作")
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				})
				.setNeutralButton("后台运行",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								moveTaskToBack(true);
							}
						})
				.setPositiveButton("返回上一层",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								if (mMediaRecorder != null) {
									RecorderActivity.this.click();
								}
								if (symbol1) {
									mySensorManager
											.unregisterListener(sensorEventListener);
								}
								finish();
							}
						}).show();
	}

	@Override
	protected void onPause() {
		super.onPause();
		onBackPressed();
		// handler4.removeCallbacks(task4);
		brightnessManager.releaseAwake();
	}

	@Override
	protected void onResume() {
		super.onResume();
		brightnessManager.keepAwake();
	}

	@Override
	protected void onDestroy() {
		if (cameraDevice != null)
			CameraUtils.stopCamera(cameraDevice);
		brightnessManager.releaseAwake();
		brightnessManager.setBrightness(120);
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		super.onStop();
		brightnessManager.setBrightness(120);
		brightnessManager.releaseAwake();
	}

	private void speak(String str2speak, String path) {
		if (text2speech.isSpeaking()) {
			return;
		}
		text2speech.setLanguage(Locale.UK);
		text2speech.addSpeech(str2speak, path);
		text2speech.speak(str2speak, TextToSpeech.QUEUE_FLUSH, null);
	}

	private void voiceToast(int choice) {
		switch (choice) {
		case EnvConf.WEAK_LIGHT_1:
			speak(EnvConf.NOTICE_WEAK_LIGHT_1, EnvConf.AUDIO_PATH_WEAK_LIGHT_1);
			break;
		case EnvConf.WEAK_LIGHT_2:
			speak(EnvConf.NOTICE_WEAK_LIGHT_2, EnvConf.AUDIO_PATH_WEAK_LIGHT_2);
			break;
		case EnvConf.SLOW_DOWN:
			Log.logAL("over speed speak");
			speak(EnvConf.NOTICE_SLOWDOWN_SEPPD, EnvConf.AUDIO_PATH_SLOW_SPEED);
			break;
		case EnvConf.TUTORIAL_1:
			speak(EnvConf.NOTICE_TUTORIAL_1, EnvConf.AUDIO_PATH_NOTICE_1);
			break;
		case EnvConf.TUTORIAL_2:
			speak(EnvConf.NOTICE_TUTORIAL_2, EnvConf.AUDIO_PATH_NOTICE_2);
			break;
		default:
			break;
		}
	}

	public void sparkBackground() {
		Timer timer = new Timer();
		TimerTask taskcc = new TimerTask() {
			public void run() {
				runOnUiThread(new Runnable() {
					public void run() {
						if (clo == 0) {
							clo = 1;
							if (range == 2) {
								lightTextView.setBackgroundColor(Color.RED);
							}
						} else {
							clo = 0;
							if (range == 2) {
								lightTextView
										.setBackgroundColor(Color.TRANSPARENT);
							}
						}
					}
				});
			}
		};
		timer.schedule(taskcc, 1, 500);
	}

	private void setBrightness(float f) {
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.screenBrightness = f;
		getWindow().setAttributes(lp);
	}

	class MyText2SpeechListener implements TextToSpeech.OnInitListener {
		@Override
		public void onInit(int status) {
			if (status == TextToSpeech.SUCCESS) {
			} else {
			}
		}
	}

	class CarmeraCallBack implements Callback {
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			if (cameraDevice != null) {
				if (isPreview) {
					cameraDevice.stopPreview();
					isPreview = false;
				}
				cameraDevice.release();
				cameraDevice = null; // 记得释放
			}
			mSurfaceview = null;
			mSurfaceHolder = null;
			mMediaRecorder = null;
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			try {
				cameraDevice = CameraUtils.openCamera(cameraDevice,
						mSurfaceHolder);
				isPreview = true;
				handlerCameraPreview.postDelayed(taskCameraPreview, 2000);
			} catch (Exception e) {
				e.printStackTrace();
			}
			mSurfaceHolder = holder;
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			mSurfaceHolder = holder;
		}
	}

	class StartBtnLisener implements OnClickListener {
		@Override
		public void onClick(View v) {
			RecorderActivity.this.click();
		}
	}

	public void click() {
		if (!isRecording) {
			/*
			 * Click to record
			 */
			isRecording = !isRecording;
			SpeedChipRotation.rotate(360f, loadingImage, 500);
			loadingTextView.setVisibility(View.VISIBLE);
			gps.addListeners(RecorderActivity.this);
			voiceToast(EnvConf.TUTORIAL_2);
			speedBarBtn.setBackgroundResource(R.drawable.speed_bar_bk_start);

			// openCarmera
			if (recorderCheckBoxStatus != isChoice)
				return;
			if (isPreview && cameraDevice != null) {
				cameraDevice = CameraUtils.stopCamera(cameraDevice);
			}
			f = 0.01f;
			setBrightness(f);
			second = 0;
			minute = 0;
			hour = 0;
			bool = true;
			mMediaRecorder = CameraUtils.initMediaRecorder(mMediaRecorder,
					mSurfaceHolder);
			try {
				mFileForSave = CameraUtils.startRecord(mMediaRecorder,
						mRecVedioPath);
				timer.setVisibility(View.VISIBLE);
				handler.postDelayed(task, 1000);// view the timer
				if (lightNoticeCheckBoxStatus == isChoice
						&& recorderCheckBoxStatus == isChoice) {
					handlerLight.postDelayed(taskLight, 1000);
				}
				if (overspeedNoticeCheckBoxStatus == isChoice
						&& recorderCheckBoxStatus == isChoice) {
					handlerSpeed.postDelayed(taskSpeed, 1000);
					Log.logAL("into postDelay");
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			brightnessManager.setBrightness(255);
		} else {
			/*
			 * Click to stop recode
			 */
			speedTextView.setText(0.0 + "");
			distTextView.setText("0m");
			SpeedChipRotation.stopRotateAndInvisibility(loadingImage);
			loadingTextView.setVisibility(View.INVISIBLE);
			speedBarBtn.setBackgroundResource(R.drawable.speed_bar_bk);
			f = 100f;
			setBrightness(f);
			voiceToast(EnvConf.TUTORIAL_1);
			if (lightNoticeCheckBoxStatus == isChoice
					&& recorderCheckBoxStatus == isChoice) {
				handlerLight.removeCallbacks(taskLight);
			}
			if (overspeedNoticeCheckBoxStatus == isChoice
					&& recorderCheckBoxStatus == isChoice) {
				handlerSpeed.removeCallbacks(taskSpeed);
			}
			range = 4;
			gps.removeListeners(RecorderActivity.this);
			isRecording = !isRecording;
			if (recorderCheckBoxStatus != isChoice)
				return;

			// camera
			try {
				bool = false;
				timer.setText(Common.formatTimeForShow(hour, minute, second));
				// text2speech.shutdown();
				mMediaRecorder = CameraUtils.stopRecord(mMediaRecorder);
				RecorderActivity.this.saveDialog();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				cameraDevice = CameraUtils.openCamera(cameraDevice,
						mSurfaceHolder);
			} catch (Exception e) {
				e.printStackTrace();
			}
			isPreview = true;

		}
	}

	@Override
	public void GPS_receiver(Location location) {
		gps.updateDist(location);
		orentationTextView.setText(location.getBearing() + "°");
		gpsTextView.setText("经度"
				+ Common.formatDoubleLocation(location.getLatitude()) + "\n"
				+ "纬度" + Common.formatDoubleLocation(location.getLongitude()));
		gpsTextView.setAlpha(0.8f);
		gpsTextView.setTextColor(Color.WHITE);
		double speed = location.getSpeed() * 3600 / 1000;
		speedTextView.setText(Common.formatDouble(speed));
		distTextView.setText(Common.mDist2kmDistStr((int) gps.getDist()));
		compassRoatation.rotate(location.getBearing());
		speedPoindRoatation.rotatePonit(speed);
		isHaveGpsInfo = true;
		SpeedChipRotation.stopRotateAndInvisibility(loadingImage);
		loadingTextView.setVisibility(View.INVISIBLE);
	}

	class BrightnessManager {
		private PowerManager.WakeLock mWakeLock;

		@SuppressWarnings("deprecation")
		public BrightnessManager() {
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
					"My Tag");
		}

		public void keepAwake() {
			// onResume() 中调用：
			mWakeLock.acquire();
		}

		public void releaseAwake() {
			// onPause() 中调用释放WakeLock对象
			try {
				mWakeLock.release();
			} catch (Throwable th) {
				// ignoring this exception, probably wakeLock was already
				// released
			}

		}

		public void setBrightness(int level) {
			ContentResolver cr = getContentResolver();
			Settings.System.putInt(cr, "screen_brightness", level);
			Window window = getWindow();
			LayoutParams attributes = window.getAttributes();
			float flevel = level;
			attributes.screenBrightness = flevel / 255;
			getWindow().setAttributes(attributes);
		}
	}

	private void saveDialog() {
		new AlertDialog.Builder(this).setMessage("要保存视频吗?")
				.setNegativeButton("不了", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						CameraUtils.delFile(mFileForSave);
					}
				})
				.setNeutralButton("保存", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						try {
							Sql sql;
							sql = SqlFactory.insert(new Record((int) gps
									.getDist(), TimeUtil.getTimeStr(),
									mFileForSave.getName()));
							dbExecutor.execute(sql);
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}

					}
				}).show();
	}

	private Handler testHandler = new Handler();

	private void test() {
		testRunnable = new Runnable() {
			boolean isRuning = true;

			@Override
			public void run() {
				if (isRuning)
					testHandler.postDelayed(this, 4000);
				float degree = Common.getRandom(0, 360);
				compassRoatation.rotate(degree);
				double speed = Common.getRandom(0, 300);
				speedPoindRoatation.rotatePonit(speed);
				speedTextView.setText(speed + "");
			}
		};
		testHandler.postDelayed(testRunnable, 1000);
	}
}