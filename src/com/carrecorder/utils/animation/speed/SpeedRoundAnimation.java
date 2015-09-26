package com.carrecorder.utils.animation.speed;

import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

public class SpeedRoundAnimation {
	AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
	ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 1.35f, 1.0f,
			1.35f, Animation.RELATIVE_TO_SELF, 0.5f,
			Animation.RELATIVE_TO_SELF, 0.5f);
	AnimationSet animationSet = new AnimationSet(true);
	private ImageView imageView;

	public SpeedRoundAnimation(int span, ImageView imageView) {
		this.imageView = imageView;
		int offect = Math.abs(span);
		alphaAnimation.setDuration(offect);
		scaleAnimation.setDuration(offect);
		alphaAnimation.setRepeatCount(Animation.INFINITE);
		scaleAnimation.setRepeatCount(Animation.INFINITE);
		animationSet.addAnimation(scaleAnimation);
		animationSet.addAnimation(alphaAnimation);
	}

	public void start() {
		imageView.startAnimation(animationSet);
	}

	public void stop() {
		alphaAnimation.cancel();
	}
}
