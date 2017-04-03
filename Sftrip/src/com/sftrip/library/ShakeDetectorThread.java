package com.sftrip.library;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class ShakeDetectorThread extends Thread implements SensorEventListener {

	//private volatile Thread _thread;
	private SensorManager sensorManager;
	private SystemFunctions systemFunctions;
	private long lastUpdate;
	private long startTime;
	private int counter;
	private long time;
	public ShakeDetectorThread(Context mContext) {
		sensorManager = (SensorManager) mContext
				.getSystemService(Context.SENSOR_SERVICE);
		startTime = 0;
		lastUpdate = 0;
		counter = 0;
		time = 0;
		systemFunctions = new SystemFunctions(mContext);
	}

	public void startDetection() {
		// register this class as a listener for the orientation and
		// accelerometer sensors
		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	public void stopDetection() {
		sensorManager.unregisterListener(this);
	}
	

	public void run() {
		startDetection();
	}
	
	private boolean isShaking(SensorEvent event) {
		float[] values = event.values;
		// Movement
		float x = values[0];
		float y = values[1];
		float z = values[2];

		float accelationSquareRoot = (x * x + y * y + z * z)
				/ (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
		if (accelationSquareRoot >= 7) {
			return true;
		}
		return false;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			if (isShaking(event) == true) {

				lastUpdate = System.currentTimeMillis();
				Log.e("dif start time", lastUpdate - startTime + "");
				if (counter == 5) {
					systemFunctions.launchSendSMSActivity();
					startTime = 0;
					counter = 0;
				} else {
					if (startTime == 0 || lastUpdate - startTime >= 6000) {
						startTime = System.currentTimeMillis();
						time = startTime;
						counter = 0;
						Log.e("Shake", "User starts shaking");
					} else if ((lastUpdate - time) / 1000 >= 1) {
						counter++;
						Log.e("counter", counter + "");
						time = System.currentTimeMillis();
					}
					//Log.e("dif time", lastUpdate - time + "");
				}
 			}
		}
	}

}
