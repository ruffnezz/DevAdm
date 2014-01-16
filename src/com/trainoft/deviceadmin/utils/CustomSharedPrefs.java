package com.trainoft.deviceadmin.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class CustomSharedPrefs {

	private static volatile CustomSharedPrefs instance = null;
	Context context;

	SharedPreferences prefs;
	Editor ed;

	public CustomSharedPrefs(Context con) {

		this.context = con;

		prefs = context.getSharedPreferences("TOT", Context.MODE_PRIVATE);

		ed = prefs.edit();
	}

	public static CustomSharedPrefs getInstance(Context con) {

		if (instance == null) {
			synchronized (CustomSharedPrefs.class) {
				if (instance == null)
					instance = new CustomSharedPrefs(con);
			}
		}
		return instance;
	}

	public void putString(String key, String value) {
		ed.putString(key, value);
		ed.commit();
	}

	public void putInt(String key, int value) {
		ed.putInt(key, value);
		ed.commit();
	}
	
	public void putLong(String key, long value) {
		ed.putLong(key, value);
		ed.commit();
	}

	public void putBoolean(String key, boolean value) {
		ed.putBoolean(key, value);
		ed.commit();
	}

	public String getString(String key) {

		return prefs.getString(key, "0");
	}

	public int getInt(String key) {

		return prefs.getInt(key, 0);
	}

	public boolean getBoolean(String key) {

		return prefs.getBoolean(key, false);
	}
	
	public long getLong(String key) {
		
		return prefs.getLong(key, 0);
	}

}
