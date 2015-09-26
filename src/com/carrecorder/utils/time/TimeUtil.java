package com.carrecorder.utils.time;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class TimeUtil {
	/*
	 * 格式化时间
	 */
	public static String format(int i) {
		String s = i + "";
		if (s.length() == 1) {
			s = "0" + s;
		}
		return s;
	}

	public static Date getNowSqlDate() {
		java.util.Date now = new java.util.Date();

		Date date = new Date(now.getDate());
		return date;
	}

	public static String getTimeStr() {
		Date date = new Date(System.currentTimeMillis());
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String parse = dateFormat.format(date);
		return parse;
	}
}
