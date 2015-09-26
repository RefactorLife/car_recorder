package com.carrecorder.sensor;

import java.util.Iterator;
import java.util.Vector;

import com.carrecorder.utils.debug.Log;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;

public class GPS {
	private Vector<GPSListener> gpsListeners = new Vector<GPSListener>();
	private Activity activity;
	private LocationManager locationManager;
	private Location location;
	private double dist;

	public GPS(Activity activity) {
		this.activity = activity;
		getLocation();
		dist = 0;
	}

	public double getSpeed() {
		if(location!=null)
		{
			return location.getSpeed();			
		}
		return 0;
	}

	public double getDist() {
		return dist;
	}

	public void updateDist(Location location) {
		if (this.location == null) {
			this.location = location;
			return;
		}
		double lat1 = this.location.getLatitude();
		double lat2 = location.getLatitude();
		double log1 = this.location.getLongitude();
		double log2 = location.getLongitude();
		double R = 6378137.0;
		double ns1 = (lat1 * Math.PI / 180.0);
		double ns2 = (lat2 * Math.PI / 180.0);
		double ew1 = (log1 * Math.PI / 180.0);
		double ew2 = (log2 * Math.PI / 180.0);
		double dew = ew1 - ew2;
		if (dew > Math.PI) {
			dew = 2 * Math.PI - dew;
		} else if (dew < -Math.PI) {
			dew = 2 * Math.PI + dew;
		}
		double dx = R * Math.cos(ns1) * dew;
		double dy = R * (ns1 - ns2);
		dist = Math.sqrt(dx * dx + dy * dy) + dist;
		this.location = location;
	}

	public void addListeners(GPSListener gpsListener) {
		gpsListeners.add(gpsListener);
	}

	public void removeListeners(GPSListener gpsListener) {
		gpsListeners.remove(gpsListener);
	}

	public void notifyListeners(Location location) {
		if (gpsListeners.isEmpty()) {
			return;
		}
		for (int i = 0; i < gpsListeners.size(); i++) {
			gpsListeners.get(i).GPS_receiver(location);
		}
	}

	public void getLocation() {

		// 获取位置管理服务
		String serviceName = Context.LOCATION_SERVICE;
		locationManager = (LocationManager) activity
				.getSystemService(serviceName);
		// to open GPS
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			activity.startActivityForResult(intent, 0);
			return;
		}
		// 查找到服务信息

		String provider = locationManager.getBestProvider(getCriteria(), true); // 获取GPS信息
		Location location = locationManager.getLastKnownLocation(provider); // 通过GPS获取位置
		if (location == null) {
			Log.logAL("location is null");
		}
		locationManager.addGpsStatusListener(listener);
		// 设置监听器，自动更新的最小时间为间隔N秒(1秒为1*1000，这样写主要为了方便)或最小位移变化超过N米
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				500, 1, new LocationListener() {

					@Override
					public void onStatusChanged(String arg0, int arg1,
							Bundle arg2) {
						// TODO Auto-generated method stub
						Log.logAL("statelite changed!!!");
					}

					@Override
					public void onProviderEnabled(String arg0) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onProviderDisabled(String arg0) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onLocationChanged(Location location) {
						notifyListeners(location);
						Log.logAL("Location:speed" + location.getSpeed());
					}
				});
	}

	private Criteria getCriteria() {
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE); // 高精度
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW); // 低功耗
		return criteria;
	}

	private GpsStatus.Listener listener = new GpsStatus.Listener() {
		public void onGpsStatusChanged(int event) {
			switch (event) {
			case GpsStatus.GPS_EVENT_FIRST_FIX:
				break;
			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
				GpsStatus gpsStatus = locationManager.getGpsStatus(null);
				int maxSatellites = gpsStatus.getMaxSatellites();
				Iterator<GpsSatellite> iters = gpsStatus.getSatellites()
						.iterator();
				int count = 0;
				while (iters.hasNext() && count <= maxSatellites) {
					GpsSatellite s = iters.next();
					count++;
				}
				break;
			case GpsStatus.GPS_EVENT_STARTED:
				break;
			case GpsStatus.GPS_EVENT_STOPPED:
				break;
			}
		};
	};

}
