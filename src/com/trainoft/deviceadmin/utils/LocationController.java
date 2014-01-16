package com.trainoft.deviceadmin.utils;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

public class LocationController {

	private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 0; // in
																		// Meters
	private static final long MINIMUM_TIME_BETWEEN_UPDATES = 0; // in
																// Milliseconds
	protected LocationManager locationManagerNet;
	MyLocationListener net = new MyLocationListener();
	String best = null;

	private static volatile LocationController instance;
	Context c;
	public double lat;
	public double lng;

	private LocationController(Context ctx) {
		this.c = ctx;
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * get current location
	 */
	public void getCurrentLocation(Context con) {
		if (c == null)
			return;
		locationManagerNet = (LocationManager) c
				.getSystemService(Context.LOCATION_SERVICE);

		Criteria criteria = new Criteria();

		best = locationManagerNet.getBestProvider(criteria, true);

		Location locationNet = null;

		Constants.printLog("Location Controller: Best Provider: ", best);

		if (best == null) {
			lat = 0;
			lng = 0;
			return;
		}

		if (!locationManagerNet.isProviderEnabled(best)) {
			lat = 0;
			lng = 0;
			return;
		}

		locationManagerNet.requestLocationUpdates(best,
				MINIMUM_TIME_BETWEEN_UPDATES,
				MINIMUM_DISTANCE_CHANGE_FOR_UPDATES, net);
		locationNet = locationManagerNet.getLastKnownLocation(best);

		if (locationNet == null) {
			lat = 0;
			lng = 0;
			return;
		}

		lat = locationNet.getLatitude();
		lng = locationNet.getLongitude();

		Constants.printLog("Location Controller: lat lng ", lat + " "
				+ lng + "");
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * Location controller instance
	 */
	public static LocationController getInstance(Context ctx) {
		if (instance == null) {
			synchronized (LocationController.class) {
				if (instance == null) {
					instance = new LocationController(ctx);
				}
			}
		}
		return instance;
	}

	
	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * Distance bw two points returned in kilometers
	 */
	public float distanceBw(double lat1, double lng1, double lat2, double lng2) {

		Location locationA = new Location("point A");

		locationA.setLatitude(lat1);
		locationA.setLongitude(lng1);

		Location locationB = new Location("point B");

		locationB.setLatitude(lat2);
		locationB.setLongitude(lng2);

		return (locationA.distanceTo(locationB)) / 1000;
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * check Internet connection
	 */
	public boolean isConnectedToInternet(Context con) {
		ConnectivityManager connManager = (ConnectivityManager) con
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo bestCon = connManager.getActiveNetworkInfo();

		if (bestCon != null) {
			if (bestCon.isConnected())
				return true;
		}
		Constants.printLog("Location Controller:",
				"Internet not connected ");
		return false;
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * Location listener
	 */
	private class MyLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}

	}

}
