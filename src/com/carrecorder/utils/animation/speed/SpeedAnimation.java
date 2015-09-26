package com.carrecorder.utils.animation.speed;

import android.widget.ImageView;

public class SpeedAnimation {
	private SpeedChipRotation speedChipRotation;
	private SpeedRoundAnimation speedRoundAnimation;

	public SpeedAnimation(ImageView chip1, ImageView chip2, int speed,
			ImageView roundLine) {
		speedChipRotation = new SpeedChipRotation(chip1, chip2, speed);
		speedRoundAnimation = new SpeedRoundAnimation(speed/2, roundLine);
	}

	public SpeedChipRotation getChipRotation() {
		return this.speedChipRotation;
	}
	public SpeedRoundAnimation getSpeedRoundAnimation()
	{
		return this.speedRoundAnimation;
	}
}
