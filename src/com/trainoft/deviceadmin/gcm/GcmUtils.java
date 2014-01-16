package com.trainoft.deviceadmin.gcm;

import android.util.Log;

import com.trainoft.deviceadmin.utils.Constants;

public final class GcmUtils {

	public static final String SERVER_URL = "https://smart-push.net/smart/GCM_Server/register.php";

	// sender id
	//public static String SENDER_ID = "379544002902";
	//public static final String SENDER_ID = "1234";
	// old id: 378013620721

	// Tag used on log messages.
	public static final String TAG = "SmartPush GCM";
	

	// //////////////////////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * log outputs while testing
	 */
	public static void printLog(String tag, String message) {
		if (!Constants.PRINT_LOG){
			return;
		}
		if (message == null) {
			message = "null";
		}
		Log.e(tag, message);

	}
}
