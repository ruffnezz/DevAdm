package com.trainoft.deviceadmin.views;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.trainoft.deviceadmin.DeviceAdminReceiver;
import com.trainoft.deviceadmin.DeviceController;
import com.trainoft.deviceadmin.R;
import com.trainoft.deviceadmin.utils.Constants;
import com.trainoft.deviceadmin.utils.CustomSharedPrefs;

public class MainActivity extends Activity implements View.OnClickListener {

	// Interaction with the DevicePolicyManager
	DevicePolicyManager policy_manager;
	ComponentName mDeviceAdminSample;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		

		policy_manager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		mDeviceAdminSample = new ComponentName(this, DeviceAdminReceiver.class);

		Button activate_button = (Button) findViewById(R.id.enable_button);
		activate_button.setOnClickListener(this);

		Button deactivate_button = (Button) findViewById(R.id.disable_button);
		deactivate_button.setOnClickListener(this);
		
		Button password_button = (Button) findViewById(R.id.set_password_button);
		password_button.setOnClickListener(this);
		
		
		

	}

	private boolean isActiveAdmin() {
		return policy_manager.isAdminActive(mDeviceAdminSample);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		DeviceController instance = DeviceController.getInstance(this);
		switch (arg0.getId()) {
		case R.id.enable_button:
			CustomSharedPrefs prefs = CustomSharedPrefs.getInstance(this);
			if(!prefs.getBoolean(Constants.ADMIN_ENABLED))
				instance.alertOnEnableRequest(this);
			else
				Constants.printLog("Admin already", "enabled");
			break;
		case R.id.disable_button:
			instance.alertOnDisableRequest(this);
			break;
		case R.id.set_password_button:
			instance.enablePasswordSecurity();
			break;
		default:
			break;
		}
	}

}
