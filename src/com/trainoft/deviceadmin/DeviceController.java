package com.trainoft.deviceadmin;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;



import com.trainoft.deviceadmin.utils.Constants;
import com.trainoft.deviceadmin.utils.CustomSharedPrefs;

public class DeviceController {

	public DevicePolicyManager policy_manager;
	public ComponentName device_admin_comp;
	Context context;
	private static final int REQUEST_CODE_ENABLE_ADMIN = 1;

	private static volatile DeviceController instance = null;

	private DeviceController(Context ctx) {
		this.context = ctx;

		policy_manager = (DevicePolicyManager) context
				.getSystemService(Context.DEVICE_POLICY_SERVICE);
		device_admin_comp = new ComponentName(context,
				DeviceAdminReceiver.class);
	}

	/*
	 * return a static instance of DeviceController
	 */
	public static DeviceController getInstance(Context ctx) {
		if (instance == null) {
			synchronized (DeviceController.class) {
				if (instance == null)
					instance = new DeviceController(ctx);
			}
		}
		return instance;
	}

	/*
	 * activate device administrator
	 */
	public void activateAdminControl() {

		Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
		intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
				device_admin_comp);
		intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
				context.getString(R.string.add_admin_extra_app_text));
		((Activity) context).startActivityForResult(intent,
				REQUEST_CODE_ENABLE_ADMIN);
		// return false - don't update checkbox until we're really active
		return;
	}

	public void disableAdminControl() {
		if (alertIfMonkey(context, R.string.monkey_reset_password)) {
			return;
		}
		policy_manager.removeActiveAdmin(device_admin_comp);
		
	}

	/*
	 * enable password security
	 */
	public void enablePasswordSecurity() {
		if (alertIfMonkey(context, R.string.monkey_reset_password)) {
			return;
		}
		setPasswordQuality();
		setPasswordMinimumLength();
		setPassword();
		Constants.printLog("password enabled", "true");
	}

	/*
	 * enable screen lock
	 */
	public void enableLockScreen() {
		lock_ScreenAfterTime();
	}

	/*
	 * set password standards that user must meet while setting password
	 */
	private void setPasswordQuality() {
		policy_manager.setPasswordQuality(device_admin_comp,
				DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC);
	}

	private void setPasswordMinimumLength() {
		policy_manager.setPasswordMinimumLength(device_admin_comp,
				Constants.PASSSWORD_MIN_LENGTH);
	}

	/*
	 * set a new password for device meeting the specified standards
	 */
	public void setPassword() {
		Intent intent = new Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD);
		context.startActivity(intent);
	}
	
	/*
	 * set the time for for auto screen lock
	 */
	private void lock_ScreenAfterTime() {
		policy_manager.setMaximumTimeToLock(device_admin_comp,
				Constants.LOCK_SCREEN_OFFSETT);
	}

	/*
	 * lock the screen immediately
	 */
	public void lock_Screen_Now() {
		policy_manager.lockNow();
	}

	/*
	 * wipe of phone data
	 */
	@SuppressLint("InlinedApi")
	public void wipeData() {
		// int flags = DevicePolicyManager.WIPE_EXTERNAL_STORAGE;
		// if (policy_manager.getCurrentFailedPasswordAttempts() > 3) {
		Constants.printLog(Constants.OPERATION_TAG, "Data wipe");
		// policy_manager.wipeData(flags);
		// }
	}

	/*
	 * disable camera - requires Api 14
	 */
	@SuppressLint("NewApi")
	public void disableCamera() {
		policy_manager.setCameraDisabled(device_admin_comp, true);
	}

	/*
	 * encrypt storage area - requires Api 11
	 */
	@SuppressLint("NewApi")
	public void encryptStorageArea() {
		policy_manager.setStorageEncryption(device_admin_comp, true);
	}

	/*
	 * If the "user" is a monkey, post an alert and notify the caller. This
	 * prevents automated test frameworks from stumbling into annoying or
	 * dangerous operations.
	 */
	private static boolean alertIfMonkey(Context context, int stringId) {
		if (ActivityManager.isUserAMonkey()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setMessage(stringId);
			builder.setPositiveButton(R.string.monkey_ok, null);
			builder.show();
			return true;
		} else {
			return false;
		}
	}

	public void alertOnDisableRequest(final Context con) {
		final EditText input = new EditText(con);
		final AlertDialog d = new AlertDialog.Builder(con)
				.setView(input)
				.setTitle("Admin Disable Request")
				.setMessage(
						"Your are about to disable this administrator."
								+ "\nPlease enter your password")
				.setPositiveButton(android.R.string.ok, null)
				.setNegativeButton(android.R.string.cancel, null).create();

		d.setOnShowListener(new DialogInterface.OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {

				Button b = d.getButton(AlertDialog.BUTTON_POSITIVE);
				b.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View view) {
						// TODO Do something
						String value = input.getText().toString();
						if (isValidInput(value)) {
							CustomSharedPrefs prefs = CustomSharedPrefs
									.getInstance(con);
							if (prefs.getString(Constants.ADMIN_PASSWORD)
									.equals(value)) {
								disableAdminControl();
							} else
								Toast.makeText(con, "wrong password", 2000)
										.show();
							d.dismiss();
							//((Activity)con).finish();
						}
						input.setText("");
						// Dismiss once everything is OK.
					}
				});
			}
		});
		d.show();
	}

	/*
	 * alert a dialog to enter a password while activating administrator
	 */
	public void alertOnEnableRequest(final Context con) {
		final EditText input = new EditText(con);
		final AlertDialog d = new AlertDialog.Builder(con)
				.setView(input)
				.setTitle("Admin Enable Request")
				.setMessage(
						"Your are about to enable this administrator."
								+ "\nPlease choose a password")
				.setPositiveButton(android.R.string.ok, null)
				.setNegativeButton(android.R.string.cancel, null).create();

		d.setOnShowListener(new DialogInterface.OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {

				Button b = d.getButton(AlertDialog.BUTTON_POSITIVE);
				b.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View view) {
						// TODO Do something
						String value = input.getText().toString();
						if (isValidInput(value)) {
							CustomSharedPrefs prefs = CustomSharedPrefs
									.getInstance(con);
							prefs.putString(Constants.ADMIN_PASSWORD, value);
							activateAdminControl();
							d.dismiss();
						}
						input.setText("");
						// Dismiss once everything is OK.
					}
				});
			}
		});
		d.show();
	}

	/*
	 * validate string input for password - requires api 9
	 */
	@SuppressLint("NewApi")
	public boolean isValidInput(String text) {
		if (text.isEmpty() || text == null)
			return false;
		return true;
	}

}
