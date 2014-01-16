package com.trainoft.deviceadmin;

import android.annotation.SuppressLint;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.trainoft.deviceadmin.utils.Constants;
import com.trainoft.deviceadmin.utils.CustomSharedPrefs;

public class DeviceAdminReceiver extends android.app.admin.DeviceAdminReceiver {
	DevicePolicyManager policy_manager;
	ComponentName device_admin_comp;

	void showToast(Context context, String msg) {
		String status = context.getString(R.string.admin_receiver_status, msg);
		Toast.makeText(context, status, Toast.LENGTH_SHORT).show();
	}

	/*@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if (intent.getAction().equals(ACTION_DEVICE_ADMIN_DISABLE_REQUESTED)) {
			Intent new_intent = new Intent(context, AlertDialogActivity.class);
			new_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			AlertDialogActivity.option = Constants.DISABLE_ALERT;
			context.startActivity(new_intent);

		} else {
			super.onReceive(context, intent);
		}
	}*/

	@Override
	public void onEnabled(Context context, Intent intent) {
		
		/*DeviceController instance = DeviceController.getInstance(context);
		instance.enablePasswordSecurity();*/
		CustomSharedPrefs prefs = CustomSharedPrefs.getInstance(context);
		prefs.putBoolean(Constants.ADMIN_ENABLED, true);
		showToast(context,
				context.getString(R.string.admin_receiver_status_enabled));
	}

	@Override
	public CharSequence onDisableRequested(Context context, Intent intent) {

		// return null;
		return context
				.getString(R.string.admin_receiver_status_disable_warning);
		
		/*Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(startMain);
        return "OOPS!";*/
		

	}

	@Override
	public void onDisabled(Context context, Intent intent) {
		CustomSharedPrefs prefs = CustomSharedPrefs.getInstance(context);
		prefs.putBoolean(Constants.ADMIN_ENABLED, false);
		showToast(context,
				context.getString(R.string.admin_receiver_status_disabled));
	}

	@Override
	public void onPasswordChanged(Context context, Intent intent) {
		showToast(context,
				context.getString(R.string.admin_receiver_status_pw_changed));
	}

	@Override
	public void onPasswordFailed(Context context, Intent intent) {
		showToast(context,
				context.getString(R.string.admin_receiver_status_pw_failed));
	}

	@Override
	public void onPasswordSucceeded(Context context, Intent intent) {
		showToast(context,
				context.getString(R.string.admin_receiver_status_pw_succeeded));
	}

	@SuppressLint("NewApi")
	@Override
	public void onPasswordExpiring(Context context, Intent intent) {
		DevicePolicyManager dpm = (DevicePolicyManager) context
				.getSystemService(Context.DEVICE_POLICY_SERVICE);
		long expr = dpm.getPasswordExpiration(new ComponentName(context,
				DeviceAdminReceiver.class));
		long delta = expr - System.currentTimeMillis();
		boolean expired = delta < 0L;
		String message = context
				.getString(expired ? R.string.expiration_status_past
						: R.string.expiration_status_future);
		showToast(context, message);
		Log.v("DeviceAdmin: ", message);
	}

}
