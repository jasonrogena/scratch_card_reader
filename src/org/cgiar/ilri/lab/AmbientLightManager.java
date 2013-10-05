package org.cgiar.ilri.lab;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class AmbientLightManager implements SensorEventListener {
	private static final float TOO_DARK_LUX = 45.0f;
	private static final float BRIGHT_ENOUGH_LUX = 450.0f;
	
	private final Context context;
	private Sensor lightSensor;
	private Preview preview;
	
	public AmbientLightManager(Context context) {
		this.context = context;
	}
	
	public void startMonitoring(Preview preview) {
		this.preview = preview;
		SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		if(lightSensor != null) {
			sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
		}
	}
	
	public void stopMonitoring() {
		if(lightSensor!=null){
			 SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		     sensorManager.unregisterListener(this);
		     preview = null;
		     lightSensor = null;
		}
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		//PSSHH!
	}

	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		float ambientLightLux = sensorEvent.values[0];
		if(preview != null){
			if(ambientLightLux<=TOO_DARK_LUX){
				Log.d("CAMERA", "Too dark, trying to start flashlight");
				preview.setTorch(true);
			}
			else if(ambientLightLux >= BRIGHT_ENOUGH_LUX){
				Log.d("CAMERA", "Bright enough, turning off flashlight");
				preview.setTorch(false);
			}
		}
	}

}
