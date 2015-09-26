package com.carrecorder.utils.camera;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.carrecorder.utils.debug.Log;
import com.carrecorder.utils.time.TimeUtil;

import android.media.MediaRecorder;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.graphics.PixelFormat;
import android.hardware.Camera;

public class CameraUtils {
	public static MediaRecorder initMediaRecorder(MediaRecorder mMediaRecorder,
			SurfaceHolder mSurfaceHolder) {
		if (mMediaRecorder == null)
			mMediaRecorder = new MediaRecorder();
		else
			mMediaRecorder.reset();
		mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
		mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
		mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		mMediaRecorder.setVideoSize(320, 240);
		mMediaRecorder.setVideoFrameRate(15);
		return mMediaRecorder;
	}

	public static Camera stopCamera(Camera camera) {
		camera.stopPreview();
		// camera.unlock();
		camera.release();
		camera = null;
		return camera;
	}

	public static Camera resumeCamera(Camera camera) {
		// camera.lock();
		camera.startPreview();
		return camera;
	}

	public static Camera openCamera(Camera camera, SurfaceHolder mSurfaceHolder)
			throws IOException {
		camera = Camera.open(Camera.getNumberOfCameras());
		Camera.Parameters parameters = camera.getParameters();
		parameters.setPreviewFrameRate(5); // 5 pictures/s
		parameters.setPictureFormat(PixelFormat.JPEG);// set the
														// format of
														// output of
														// picture
		parameters.set("jpeg-quality", 100);// picture quality
		camera.setParameters(parameters);
		camera.setPreviewDisplay(mSurfaceHolder);
		camera.startPreview();
		return camera;
	}

	public static File startRecord(MediaRecorder mMediaRecorder,
			File mRecVedioPath) throws IOException {
		File mRecAudioFile = File.createTempFile(
				"Video" + TimeUtil.getTimeStr(), ".3gp", mRecVedioPath);
		mMediaRecorder.setOutputFile(mRecAudioFile.getAbsolutePath());
		mMediaRecorder.prepare();
		mMediaRecorder.start();// start record
		return mRecAudioFile;
	}

	public static MediaRecorder stopRecord(MediaRecorder mMediaRecorder) {
		Log.logAL("stop");
		mMediaRecorder.stop();
		Log.logAL("release");
		mMediaRecorder.release();
		mMediaRecorder = null;
		return mMediaRecorder;
	}

	/*
	 * 生成video文件名字 creates the name of video file
	 */
	public static void videoRename(File mRecAudioFile, int parentId) {
		// String path = Environment.getExternalStorageDirectory()
		// .getAbsolutePath()
		// + "/hfdatabase/video/"
		// + String.valueOf(parentId) + "/";
		// String fileName = new SimpleDateFormat("yyyyMMddHHmmss")
		// .format(new Date()) + ".3gp";
		// File out = new File(path);
		// if (!out.exists()) {
		// out.mkdirs();
		// }
		// out = new File(path, fileName);
		// if (mRecAudioFile.exists())
		// mRecAudioFile.renameTo(out);
	}

	public static boolean delFile(File mFile) {
		if (mFile.exists() && mFile.isFile()) {
			mFile.delete();
			return true;
		}
		return false;
	}
}
