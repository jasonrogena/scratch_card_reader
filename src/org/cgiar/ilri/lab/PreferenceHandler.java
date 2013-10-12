package org.cgiar.ilri.lab;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class PreferenceHandler {
	public static final String KEY_ACTIVE_CAMERA_HEIGHT="activeCameraHeight";
	public static final String KEY_ACTIVE_CAMERA_WIDTH="activeCameraWidth";
	
	private Context context;
	
	public PreferenceHandler(Context context) {
		this.context = context;
	}
	
	public void setPreference(String key, String value){
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor=preferences.edit();
		editor.putString(key, value);
		editor.commit();
	}
	
	public String getPreference(String key){
		SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getString(key, null);
	}
}
