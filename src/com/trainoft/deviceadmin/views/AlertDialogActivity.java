package com.trainoft.deviceadmin.views;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;

import com.trainoft.deviceadmin.DeviceAdminReceiver;
import com.trainoft.deviceadmin.DeviceController;
import com.trainoft.deviceadmin.utils.Constants;

public class AlertDialogActivity extends Activity{
	
	public DevicePolicyManager policy_manager;
	public ComponentName device_admin_comp;
	public static int option =-1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		this.setTheme(android.R.style.Theme_Translucent_NoTitleBar);
		super.onCreate(savedInstanceState);
		
		policy_manager = (DevicePolicyManager)
				getSystemService(Context.DEVICE_POLICY_SERVICE);
		device_admin_comp = new ComponentName(this,
				DeviceAdminReceiver.class);
		
		if(option == Constants.ENABLE_ALERT){
			
		}
		
		if(option == Constants.DISABLE_ALERT){
			DeviceController instance = DeviceController.getInstance(this);
			instance.alertOnDisableRequest(AlertDialogActivity.this);
		}
			
		
	}

}
