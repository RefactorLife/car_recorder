package com.carrecorder.utils.animation.speed;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

public class SpeedChipRotation {
	private ImageView chip1, chip2;
	private int speed;

	/**
	 * 
	 * @param chip1
	 * @param chip2
	 * @param speed
	 *            means speed ms / cycle
	 */
	public SpeedChipRotation(ImageView chip1, ImageView chip2, int speed) {
		this.chip1 = chip1;
		this.chip2 = chip2;
		this.speed = speed;
	}

	public void startRun() {
		SpeedChipRotation.rotate(360f, chip1, this.speed);
		SpeedChipRotation.rotate(-360f, chip2, this.speed);
	}

	public static void rotate(float degree, ImageView image, int speed) {
		// 创建旋转动画（反向转过degree度）
		RotateAnimation ra = new RotateAnimation(0f, degree,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		// 设置动画的持续时间
		int offect = Math.abs(speed);
		ra.setDuration(offect);
		// 设置动画结束后的保留状态
		ra.setFillAfter(false);
		ra.setRepeatCount(RotateAnimation.INFINITE);
		// 启动动画
		ra.setInterpolator(new LinearInterpolator());
		image.startAnimation(ra);
	}

	public static void stopRotateAndInvisibility(ImageView image) {
		// 创建旋转动画（反向转过degree度）
		AlphaAnimation alphaAnimation = new AlphaAnimation(0, 0);
		// 设置动画的持续时间
		int offect = Math.abs(1);
		alphaAnimation.setDuration(offect);
		alphaAnimation.setFillAfter(true);
		// 启动动画
		image.startAnimation(alphaAnimation);
	}
}
