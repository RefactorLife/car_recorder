package com.carrecorder.utils;

import java.text.DecimalFormat;
import java.util.Random;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.carrecorder.db.table.Record;

public class Common {
	public static String formatDouble(double num) {
		DecimalFormat df = new DecimalFormat("#.##");
		return df.format(num);
	}
	public static String formatDoubleLocation(double num) {
		DecimalFormat df = new DecimalFormat("#.########");
		return df.format(num);
	}
	/**
	 * 匹配第一个符合要求的字符串返回
	 * @param str
	 * @param regEx
	 * @return
	 */
	public static String regularStr(String str,String regEx)
	{
		try{
			Pattern pat = Pattern.compile(regEx);
			Matcher mat = pat.matcher(str);
			mat.find();
			return mat.group(0);
		}catch(Exception e)
		{
			
		}
		return "";
	}
	public static boolean hasElement(Vector<Record> showedRecords,Record element)
	{
		for (int i=0;i<showedRecords.size();i++)
		{
			if(showedRecords.get(i).getId()==element.getId())
				return true;
		}
		return false;
	}
	/*
	 * format time
	 */
	public static String format(int i) {
		String s = i + "";
		if (s.length() == 1) {
			s = "0" + s;
		}
		return s;
	}
	public static String formatTimeForShow(int hour,int minute,int second) {
		return Common.format(hour) + ":" + Common.format(minute) + ":"
				+ Common.format(second);
	}
	/**
	 * if dist<1000 then return xxx m
	 * else  			 return xxx km
	 * @return
	 */
	public static String mDist2kmDistStr(int dist)
	{
		String distStr;
		if(dist <1000)
		{
			distStr = dist + " m";
		}else
		{
			distStr = formatDouble(dist/1000.0) +" km";
		}
		return distStr;
	}
	public static int getRandom(int min, int max) {
		Random random = new Random();
		int s = random.nextInt(max) % (max - min + 1) + min;
		return s;
	}
}
