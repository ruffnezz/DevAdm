package com.trainoft.deviceadmin.utils;

import android.util.Log;

public class Constants {
	
	public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	public static final String APP_VERSION = "appVersion";
	public static final String REG_ID = "registration_id";
	public static final double VERSION_CODE = 2.2;
	public static final String appId = "appID_smartpush";
	
	
	// gcm constants
	public static final String SERVER_URL = "https://smart-push.net/smart/GCM_Server/register.php";	
	public static final String SENDER_ID = "379544002902";	
	public static final String TAG = "Smart Admin GCM";
	public static final String DISPLAY_MESSAGE_ACTION = "com.androidhive.pushnotifications.DISPLAY_MESSAGE";
	public static final String EXTRA_MESSAGE = "message";
	
	
	// device admin constants
	public static final int MAX_FAILED_ATTEMPTS = 3; // Maximum failed attempts for password
	public static final long LOCK_SCREEN_OFFSETT = 1*60*1000; // milliseconds	
	public static final int PASSSWORD_MIN_LENGTH = 6; // minimum length for password	
	public static final String OPERATION_TAG = "Operation triggered";	
	public static final int ENABLE_ALERT =0;	
	public static final int DISABLE_ALERT = 1;	
	public static final String ADMIN_PASSWORD = "admin_password";
	
	
	// shared preference constants
	public static boolean disable_permission_granted = false;
	public static final String ADMIN_ENABLED = "admin_enabled";
	
	
	// general constants
	public static final boolean PRINT_LOG = true;
	
	// print log
	public static void printLog(String tag, String message){
		if(PRINT_LOG)
			Log.e(tag, message);
	}

}
