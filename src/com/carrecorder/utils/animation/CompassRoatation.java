package com.carrecorder.utils.animation;

import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

public class CompassRoatation {
	private ImageView image;
	private float currentDegree;
	private int speed = 2000;
	public CompassRoatation(ImageView image) {
		this.image = image;
		currentDegree=0f;
	}
	public void rotate(float degree)
	{
		// 创建旋转动画（反向转过degree度）
		RotateAnimation ra = new RotateAnimation(currentDegree, -degree,
				Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		// 设置动画的持续时间
		int offect = Math.abs((int)((currentDegree+degree)/360.0*speed));
		ra.setDuration(offect);
		// 设置动画结束后的保留状态
		ra.setFillAfter(true);
		// 启动动画
		image.startAnimation(ra);
		currentDegree = -degree;
	}
	public float getCurrentDegree()
	{
		return currentDegree;
	}
	public void setSpeed(int speed)
	{
		this.speed=speed;
	}
}
