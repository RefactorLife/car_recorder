package com.carrecorder.utils.animation;

import android.widget.ImageView;

public class SpeedPoindRoatation extends CompassRoatation{

	public SpeedPoindRoatation(ImageView image) {
		super(image);
	}
	/**
	 * speed unit km/s
	 * @param speed
	 */
	public void rotatePonit(double speed)
	{
		float degree = 0;//修正 方便P图的时候只需要把指针P的水平
		if(speed>180)
		{
			rotatePonit(180);
			return;
		}
		if(speed<0)
			return;
		degree += (float) (-speed/180*270);
		this.rotate(degree);
	}
	
}
