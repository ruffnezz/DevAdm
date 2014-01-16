/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.trainoft.deviceadmin.gcm;

import java.io.BufferedReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.trainoft.deviceadmin.utils.LocationController;
import com.trainoft.deviceadmin.utils.PhoneInfo;


/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {

	String best = null;

	protected double lat;
	protected double lng;
	private static final String TAG = "SmartPush GCMIntentService";

	private static AsyncTask<String, String, Void> regular_ad_task = null;
	private static AsyncTask<String, String, Void> instant_ad_task = null;

	BufferedReader buf;
	BufferedReader instant_buf;
	
	AsyncTask<Void, Void, Void> CassandraRegisterTask;

	public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		Bundle extras = intent.getExtras();
		
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		
		// String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) {
			
			GcmUtils.printLog("smartpush intentent service message", extras.toString());
			
			String message = extras.getString("triggerType");
			parseMessage(intent, message);
		}
		
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	
	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * parse the received message
	 */
	private void parseMessage(Intent intent, String message) {
		GcmUtils.printLog("smartpush message intent: ", message);
		if (message.equals("fetchAd")) {
			fetchAd(intent);
			return;
		}

		// fetch instant ad
		if (message.equals("instant_push")) {
			fetchInstantAd(intent);
			return;
		}

		// reset limit variables
		if (message.equals("changeAPD")) {
			resetAdsLimits(intent);
			return;
		}

		// upload location to cassandra server
		if (message.equals("location_log")) {
			uploadLocation(intent, this);
			return;
		}

	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * execute the regular ad task
	 */
	private void fetchAd(Intent intent) {
		
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * execute the instant ad task
	 */
	private void fetchInstantAd(Intent intent) {
		
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * reset daily ad limits for the host application
	 */
	private void resetAdsLimits(Intent intent) {
		
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * upload location to server
	 */
	private void uploadLocation(Intent intent, Context context) {

		String responseURL = intent.getExtras().getString("responseLocation");
		String location_hash = intent.getExtras().getString("SessionHash");
		String deviceId;

		PhoneInfo pi = new PhoneInfo(this);
		deviceId = pi.getSimID() == null ? pi.getAndroidID() : pi.getSimID();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String currentDateandTime = sdf.format(new Date());

		LocationController loc = LocationController.getInstance(context);
		loc.getCurrentLocation(context);

		// getCurrentLocation();
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("lat", loc.lat + ""));
		nameValuePairs.add(new BasicNameValuePair("lng", loc.lng + ""));
		nameValuePairs.add(new BasicNameValuePair("Uid", deviceId));
		nameValuePairs.add(new BasicNameValuePair("Time", currentDateandTime));
		nameValuePairs.add(new BasicNameValuePair("Hash",
				"d7a2a9cbc12e5a4e9eddc5735ebb99f4"));

		if (location_hash != null)
			nameValuePairs.add(new BasicNameValuePair("SessionHash",
					location_hash));

		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(responseURL);

			GcmUtils.printLog("GCM_INT_SERVICE url", responseURL);
			GcmUtils.printLog("GCM_INT_SERVICE deviceId", deviceId);
			GcmUtils.printLog("GCM_INT_SERVICE currentDateandTime",
					currentDateandTime);
			GcmUtils.printLog("GCM_INT_SERVICE lat lng", loc.lat + " "
					+ loc.lng + "");
			GcmUtils
					.printLog("GCM_INT_SERVICE loc hash ", location_hash);

			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = httpclient.execute(httppost);

			GcmUtils.printLog("GCM_INT_SERVICE loc stat", response
					.getStatusLine().getStatusCode() + "");

			if (response.getStatusLine().getStatusCode() == 200) {
				GcmUtils.printLog("GCM_INT_SERVICE location log",
						"Uploaded");
			}

		} catch (Exception e) {
			Log.e("GCM_INT_SERVICE http ",
					"Error in http connection " + e.toString());
		}
	}

	

}
