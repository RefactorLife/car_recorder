package com.carrecorder.conf;

import android.annotation.SuppressLint;

@SuppressLint("SdCardPath")
public class EnvConf {
	public static final String AUDIO_PATH_WEAK_LIGHT_1 = "/sdcard/Android/speedradio.wav";
	public static final String AUDIO_PATH_WEAK_LIGHT_2 = "/sdcard/Android/lightradio2.wav";
	public static final String AUDIO_PATH_SLOW_SPEED = "/sdcard/Android/lightradio.wav";
	public static final String AUDIO_PATH_NOTICE_1 = "/sdcard/Android/right.wav";
	public static final String AUDIO_PATH_NOTICE_2 = "/sdcard/Android/go.wav";
	public static final String NOTICE_SLOWDOWN_SEPPD = "车速太快，请注意减速行驶";
	public static final String NOTICE_WEAK_LIGHT_1 = "光线较暗请开灯";
	public static final String NOTICE_WEAK_LIGHT_2 = "光线较暗请立即开灯";
	public static final String NOTICE_WEAK_LIGHT_1_MUTIL_LINE = " 光线较暗 \n请立即开灯";
	public static final String NOTICE_WEAK_LIGHT_2_MUTIL_LINE = "光线非常暗\n请立即开灯";
	public static final String NOTICE_GOOD_LIGHT = "光线良好";
	public static final String NOTICE_TUTORIAL_1 = "点击右侧按钮开始记录";
	public static final String NOTICE_TUTORIAL_2 = "开始监测";
	public static final int WEAK_LIGHT_1 = 1;
	public static final int WEAK_LIGHT_2 = 2;
	public static final int SLOW_DOWN = 3;
	public static final int TUTORIAL_1 = 4;
	public static final int TUTORIAL_2 = 5;
	public static final int SPEED_CHIP_SPEED = 4000;//速度仪表盘旋转速度 5s/圈
	public static final int GREEN = 0x336633;
	public static final int MAX_SPEED=10;//限速80km/h
}
