package com.trainoft.deviceadmin.gcm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.trainoft.deviceadmin.utils.Constants;
import com.trainoft.deviceadmin.utils.CustomSharedPrefs;
import com.trainoft.deviceadmin.utils.LocationController;
import com.trainoft.deviceadmin.utils.PhoneInfo;


public class GcmWorker {

	private static volatile GcmWorker instance = null;
	private Context context;

	// public String SENDER_ID = "946601137480";

	static final String TAG = "SmartPush GCM";

	GoogleCloudMessaging gcm;
	String regid;

	private static final int MAX_ATTEMPTS = 5;
	private static final int BACKOFF_MILLI_SECONDS = 2000;
	private static final Random random = new Random();

	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * GcmWorker private constructor
	 */
	private GcmWorker(Context con) {
		this.context = con;
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * GcmWorker static instance
	 */
	public static GcmWorker getInstance(Context con) {

		if (instance == null) {
			synchronized (GcmWorker.class) {
				if (instance == null)
					instance = new GcmWorker(con);
			}
		}
		return instance;
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * perform device registration
	 */
	public void doRegisteration(String Hash) {

		if (!Hash.equals("sayedjalilhassan"))
			return;

		LocationController loc = LocationController.getInstance(context);

		if (!loc.isConnectedToInternet(context)) {
			Log.e(TAG, "Connection refused.Please check " +
					"your internet connection");
			return;
		}

		if (checkPlayServices()) {

			gcm = GoogleCloudMessaging.getInstance(context);
			regid = getRegistrationId(context);

			if (regid.isEmpty()) {
				registerInBackground();
			} else {
				Log.i(TAG, "already registered");
			}
		} else {
			Log.e(TAG, "No valid Google Play Services APK found.");
		}
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * Check the device to make sure it has the Google Play Services APK. If it
	 * doesn't, display a dialog that allows users to download the APK from the
	 * Google Play Store or enable it in the device's system settings.
	 */
	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(context);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode,
						(Activity) context,
						Constants.PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.i(TAG, "This device is not supported.");
			}
			return false;
		}
		return true;
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * Stores the registration ID and the app versionCode in the application's
	 * {@code SharedPreferences}.
	 */
	private void storeRegistrationId(Context context, String regId) {

		int appVersion = getAppVersion(context);

		CustomSharedPrefs prefs = CustomSharedPrefs.getInstance(context);
		prefs.putString(Constants.REG_ID, regId);
		prefs.putInt(Constants.APP_VERSION, appVersion);
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * Gets the current registration ID for application on GCM service, if there
	 * is one. <p> If result is empty, the app needs to register.
	 * 
	 * @return registration ID, or empty string if there is no existing
	 * registration ID.
	 */
	private String getRegistrationId(Context context) {

		CustomSharedPrefs prefs = CustomSharedPrefs.getInstance(context);
		String registrationId = prefs.getString(Constants.REG_ID);

		if (registrationId.isEmpty() || registrationId.equals("0")) {
			Log.i(TAG, "Registration not found.");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = prefs.getInt(Constants.APP_VERSION);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			Log.i(TAG, "App version changed.");
			return "";
		}
		return registrationId;
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * Registers the application with GCM servers asynchronously. <p> Stores the
	 * registration ID and the app versionCode in the application's shared
	 * preferences.
	 */
	private void registerInBackground() {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";

				long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
				for (int i = 1; i <= MAX_ATTEMPTS; i++) {
					try {

						if (gcm == null) {
							gcm = GoogleCloudMessaging.getInstance(context);
						}
						regid = gcm.register("SENDER_ID"); // hardcoded sender id
						msg = "Device registered, registration ID=" + regid;
						GcmUtils.printLog(TAG, msg);

						// sent reg id to smartPush server
						sendRegistrationIdToBackend();

						// Persist the regID - no need to register again.
						storeRegistrationId(context, regid);


						i = MAX_ATTEMPTS;
						break;

					} catch (IOException ex) {
						GcmUtils
								.printLog(GcmUtils.TAG,
										"Failed to register on attempt " + i
												+ ":" + ex);
						if (i == MAX_ATTEMPTS) {
							break;
						}
						try {
							GcmUtils.printLog(GcmUtils.TAG,
									"Sleeping for " + backoff
											+ " ms before retry");
							Thread.sleep(backoff);
						} catch (InterruptedException e1) {
							// Activity finished before we complete - exit.
							GcmUtils
									.printLog(GcmUtils.TAG,
											"Thread interrupted: abort remaining retries!");
							Thread.currentThread().interrupt();
							return msg;
						}
						// increase backoff exponentially
						backoff *= 2;
					}
				}

				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				GcmUtils.printLog(TAG + " device registered", msg);
			}
		}.execute(null, null, null);
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * Sends the registration ID to your server over HTTP
	 * 
	 * @throws IOException
	 */
	private void sendRegistrationIdToBackend() throws IOException {

		CustomSharedPrefs prefs = CustomSharedPrefs.getInstance(context);
		String serverUrl = GcmUtils.SERVER_URL;

		Map<String, String> params = new HashMap<String, String>();

		params.put("regId", regid);
		PhoneInfo pi = new PhoneInfo(context);
		params.put("simId",
				pi.getSimID() == null ? pi.getAndroidID() : pi.getSimID());
		params.put("packageName", pi.getPackage());
		params.put("appkey", prefs.getString(Constants.appId));
		params.put("imei", pi.getIMEI());
		params.put("version", Constants.VERSION_CODE + "");

		post(serverUrl, params);

	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * Issue a POST request to smartPush server.
	 */
	private static void post(String endpoint, Map<String, String> params)
			throws IOException {

		URL url;
		try {
			url = new URL(endpoint);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("invalid url: " + endpoint);
		}
		StringBuilder bodyBuilder = new StringBuilder();
		Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
		// constructs the POST body using the parameters
		while (iterator.hasNext()) {
			Entry<String, String> param = iterator.next();
			bodyBuilder.append(param.getKey()).append('=')
					.append(param.getValue());
			if (iterator.hasNext()) {
				bodyBuilder.append('&');
			}
		}
		String body = bodyBuilder.toString();
		// Log.v(CommonUtilities.TAG, "Posting '" + body + "' to " + url);
		GcmUtils.printLog(TAG, "Posting '" + body + "' to " + url);
		byte[] bytes = body.getBytes();
		HttpURLConnection conn = null;
		try {

			GcmUtils.printLog(TAG, "> " + url);
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setFixedLengthStreamingMode(bytes.length);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded;charset=UTF-8");
			// post the request
			OutputStream out = conn.getOutputStream();
			out.write(bytes);
			out.close();
			// handle the response
			int status = conn.getResponseCode();
			if (status != 200) {
				throw new IOException("Post failed with error code " + status);
			}

			// response
			InputStream in = conn.getInputStream();
			BufferedReader r = new BufferedReader(new InputStreamReader(in));

			StringBuilder total = new StringBuilder();

			String line = null;

			while ((line = r.readLine()) != null) {
				total.append(line);
			}

			GcmUtils.printLog(TAG + " smartpush response: ",
					total.toString());
			if (total.toString().trim().equals("-1"))
				Log.e(TAG, "Invalid Appkey or packagename");

		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

}
