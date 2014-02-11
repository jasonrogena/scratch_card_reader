package org.cgiar.ilri.lab;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.util.Log;

public class PreferenceHandler {
	public static final String KEY_ACTIVE_CAMERA_HEIGHT="activeCameraHeight";
	public static final String KEY_ACTIVE_CAMERA_WIDTH="activeCameraWidth";
	public static final String KEY_BEST_PREVIEW_RATIO = "bestPreviewRatio";
	public static final String KEY_APP_CRASHED = "appCrashed";
	public static final String KEY_LAST_RECORDED_VERSION = "lastVersion";
	public static final String KEY_AVOID_INCREASING_BPR = "avoidIncreasingBPR";
	public static final String VALUE_TRUE = "true";
	public static final String VALUE_FALSE = "false";
	
	private Context context;
	
	public PreferenceHandler(Context context) {
		this.context = context;
		
		String lastRecordedVersion = getPreference(KEY_LAST_RECORDED_VERSION);
		try {
			int currentVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
			if(lastRecordedVersion == null || Integer.parseInt(lastRecordedVersion) != currentVersion){
				clearPreferences();
				setPreference(KEY_LAST_RECORDED_VERSION, String.valueOf(currentVersion));
			}
		} 
		catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	
	private void clearPreferences(){
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor=preferences.edit();
		editor.clear();
		editor.commit();
		Log.d("CAMERA", "Cleared all shared preferences");
	}
}
