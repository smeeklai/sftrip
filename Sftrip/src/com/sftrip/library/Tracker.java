package com.sftrip.library;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import org.json.JSONException;
import org.json.JSONObject;
import com.sftrip.R;
import com.sftrip.TrackerActivity;
import com.sftrip.UserArrivedActivity;
import com.sftrip.WarningDialogActivity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class Tracker extends Service implements OnSignalsDetectedListener {

	private static final String PACKAGE_NAME = "com.sftrip";
	private static final int MILLISECONDS_PER_SECOND = 1000;
	private static String KEY_SUCCESS = "success";
	private static String KEY_ERROR = "error";
	private static String KEY_ERROR_MSG = "error_msg";
	private static String ARRIVED_STATUS = "arrived";
	private static String tag = "Tracker";
	private static String LOCATION_CHANGED = "User's location has changed";
	private final static int NOT_WHISTLE = 0;
	private final static int WHISTLED = 1;
	private final static int CANCEL_WHISTLE = 2;
	private LocationManager lm;
	private LocationListener networkLocationListener;
	private LocationListener gpsLocationListener;
	private String tripID;
	private static int notiID;
	private int locationInterval;
	private NotificationManager mNM;
	private String currentLat;
	private String currentLng;
	private boolean arrivedStatus;
	private SystemFunctions systemFunctions;
	private String userStatus;
	private String remainignDistance;
	private Location curNetworkLocation = null;
	private Location curGPSLocation = null;
	private Location curBestLocation = null;
	private boolean gps_enabled = false;
	private boolean network_enabled = false;
	private boolean firstTime = true;
	private Handler handler;
	private Runnable runnable;
	private BroadcastReceiver mReceiver = null;
	private DetectorThread detectorThread;
	private RecorderThread recorderThread;
	private ShakeDetectorThread shakeDetectorThread;

	private void init() {
		lm = (LocationManager) getApplicationContext().getSystemService(
				Context.LOCATION_SERVICE);
		networkLocationListener = new MyNetworkLocationListener();
		gpsLocationListener = new MyGpsLocationListener();
		mNM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		arrivedStatus = false;
		systemFunctions = new SystemFunctions(getApplicationContext());
		tripID = systemFunctions.getUserTripID() + "";
		locationInterval = systemFunctions.getSendingDataPeriod() * 60
				* MILLISECONDS_PER_SECOND;
		notiID = Integer.parseInt(tripID);
		handler = new Handler();
		recorderThread = new RecorderThread();
		detectorThread = new DetectorThread(recorderThread);
		shakeDetectorThread = new ShakeDetectorThread(this);
		systemFunctions.setStopUnauthorization(false);
		mReceiver = new ShutdownReceiver();
		registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_SHUTDOWN));
	}

	@Override
	public void onCreate() {
		super.onCreate();
		init();
		Log.i(tag, "Start Tracking");
		showStartNotification();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("LocalService", "Received start id " + startId + ": " + intent);
		locationInterval = 60000;

		runnable = new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				if (curNetworkLocation != null || curGPSLocation != null) {
					chooseBestLocation(curNetworkLocation, curGPSLocation);
					if (curBestLocation != null) {
						Log.e("Best Location", curBestLocation.getProvider());
						uploadDataToServer(curBestLocation);
						Toast.makeText(getApplicationContext(),
								"Location is uploaded", Toast.LENGTH_LONG)
								.show();
					} else {
						Log.e("Best Location Error",
								"Can't choose any best location");
					}
					curNetworkLocation = null;
					curGPSLocation = null;
					curBestLocation = null;
				}
				lm.removeUpdates(networkLocationListener);
				lm.removeUpdates(gpsLocationListener);
				gps_enabled = lm
						.isProviderEnabled(LocationManager.GPS_PROVIDER);
				network_enabled = lm
						.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
				if (gps_enabled) {
					lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
							0, gpsLocationListener);
					Log.e(tag, "GPS is ebabled");
				}
				if (network_enabled) {
					lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
							20000, 0, networkLocationListener);
					Log.e(tag, "network is ebabled");
				}
				handler.postDelayed(runnable, locationInterval);
			}
		};
		handler.post(runnable);

		startWhistleAndShakingDetector();

		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.e("STOP_SERVICE", "Service has stopped");
		Log.e(tag, systemFunctions.getStopUnauthorization() + "");
		try {
			unregisterReceiver(mReceiver);
			mReceiver = null;
			systemFunctions.releaseWakeLock();
			mNM.cancel(notiID);
			lm.removeUpdates(networkLocationListener);
			lm.removeUpdates(gpsLocationListener);
			handler.removeCallbacks(runnable);
			stopWhistleAndShakingDetector();
			recorderThread = null;
			detectorThread = null;
			stopSelf();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	private void showStartNotification() {
		// In this sample, we'll use the same text for the ticker and the
		// expanded notification
		CharSequence content = getText(R.string.local_service_started);
		CharSequence title = getText(R.string.local_service_label);
		
		Uri sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.short_sms);
		
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this)
				.setSmallIcon(R.drawable.dash)
				.setContentTitle(title)
				.setContentText(content)
				.setTicker(content)
				.setVibrate(new long[] { 0, 1000, 0 })
				.setSound(sound);

		Intent resultIntent = new Intent(this, TrackerActivity.class);

		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(TrackerActivity.class);
		stackBuilder.addNextIntent(resultIntent);

		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
				PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);

		mNM.notify(notiID, mBuilder.build());
	}

	private void sendDataToActivity(String status, String remainingDistance) {
		Intent filterRes = new Intent("com.sftrip.library");
		filterRes.putExtra("userStatus", status);
		filterRes.putExtra("remainingDistance", remainingDistance);
		sendBroadcast(filterRes);
	}

	private void popUpAlertDialog() {
		systemFunctions.wakeScreen();
		Intent intent = new Intent(getApplicationContext(),
				WarningDialogActivity.class);
		intent.putExtra("userStatus", getUserStatus());
		intent.putExtra("remainingDistance", getRemainingDistance());
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		getApplication().startActivity(intent);
	}

	private void checkResult(JSONObject json) {
		try {
			if (json.getString(KEY_SUCCESS) != null) {
				String res = json.getString(KEY_SUCCESS);
				if (Integer.parseInt(res) == 1) {
					Log.v(tag, "Store data successfully");
					setUserStatus(json.getString("passengerStatus"));
					setRemainingDistance(json.getDouble("RD") + "");
					if (json.getInt(ARRIVED_STATUS) == 1) {
						setArrivedStatus(true);
					} else {
						setArrivedStatus(false);
					}
				} else if (json.getString(KEY_ERROR).equals("1")) {
					// Error in login
					String error_msg = json.getString(KEY_ERROR_MSG);
					Log.e(tag, error_msg);
					setArrivedStatus(false);
				} else {
					Log.e(tag, "Something is wrong");
					setArrivedStatus(false);
				}
			} else {
				Log.e(tag, "Something is wrong");
				setArrivedStatus(false);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void notifyUserHasArrived() {
		systemFunctions.wakeScreen();
		Intent intent = new Intent(getApplicationContext(),
				UserArrivedActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		getApplication().startActivity(intent);
	}

	public void setArrivedStatus(boolean status) {
		arrivedStatus = status;
	}

	public boolean getArrivedStatus() {
		return arrivedStatus;
	}

	public void setUserStatus(String status) {
		userStatus = status;
	}

	public String getUserStatus() {
		return userStatus;
	}

	public void setRemainingDistance(String rd) {
		remainignDistance = rd;
	}

	public String getRemainingDistance() {
		return remainignDistance;
	}

	private void uploadDataToServer(Location location) {
		if (location != null) {
			currentLat = location.getLatitude() + "";
			currentLng = location.getLongitude() + "";
			try {
				JSONObject json = new LogInfo().execute(
						new String[] { tripID, currentLat, currentLng }).get();
				Log.e("Upload data", "Upload data successfully");
				checkResult(json);
				if (getArrivedStatus() == true) {
					Log.v(tag, "User has arrived safely");
					notifyUserHasArrived();
					onDestroy();
				} else {
					systemFunctions.storeUserStatusAndRd(getUserStatus(),
							getRemainingDistance());
					if (systemFunctions.isActivityInForeground(PACKAGE_NAME) == true) {
						sendDataToActivity(getUserStatus(),
								getRemainingDistance());
					} else {
						if ("suspicious".equals(getUserStatus())) {
							popUpAlertDialog();
						} else if ("dangerous".equals(getUserStatus())
								|| "not moving".equals(getUserStatus())) {
							systemFunctions.launchSendSMSActivity();
						}
					}
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private Location chooseBestLocation(Location curNetworkLocation,
			Location curGPSLocation) {
		if (curNetworkLocation != null) {
			if (curGPSLocation != null) {
				if (curGPSLocation.getTime() >= curNetworkLocation.getTime()
						|| curGPSLocation.getAccuracy() < curNetworkLocation
								.getAccuracy()) {
					curBestLocation = curGPSLocation;
				}
			}
			curBestLocation = curNetworkLocation;
			Log.e("GPS Location", "GPS Location still unknown");
		} else if (curGPSLocation != null) {
			curBestLocation = curGPSLocation;
		} else {
			curBestLocation = null;
		}
		return curBestLocation;
	}

	/*private boolean compareNetworkLocation(Location curNetworkLocation,
			Location newNetworkLocation) {
		if (curNetworkLocation == null) {
			return true;
		} else if (newNetworkLocation.getAccuracy() < curNetworkLocation
				.getAccuracy()) {
			return true;
		} else if (newNetworkLocation.getTime() >= curNetworkLocation.getTime()){
			return true;
		} else {
			return false;
		}
	}*/

	private class MyGpsLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			//Toast.makeText(getApplicationContext(), location.getProvider(),
			//		Toast.LENGTH_LONG).show();
			DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			Date date = new Date(location.getTime());
			String formatted = format.format(date);
			Log.e(tag, location.getProvider());
			Log.e(tag, location.getAccuracy() + "");
			Log.e(tag, formatted);
			curGPSLocation = location;
			lm.removeUpdates(networkLocationListener);
			lm.removeUpdates(this);
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}

	}

	private class MyNetworkLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			Log.e(tag, "network changed");
			//Toast.makeText(getApplicationContext(), location.getProvider(),
			//		Toast.LENGTH_LONG).show();
			DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			Date date = new Date(location.getTime());
			String formatted = format.format(date);
			Log.e(tag, location.getProvider());
			Log.e(tag, location.getAccuracy() + "");
			Log.e(tag, formatted);
			curNetworkLocation = location;
			/*if (compareNetworkLocation(curNetworkLocation, location) == true) {
				Log.e(tag,
						"New network location is better than current network location");
				curNetworkLocation = location;
			}*/
			if (firstTime == true) {
				chooseBestLocation(curNetworkLocation, curGPSLocation);
				uploadDataToServer(curBestLocation);
				// curNetworkLocation = null;
			}
			firstTime = false;
			// lm.removeUpdates(networkLocationListener);
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}

	}

	@Override
	public void onWhistleDetected() {
		// TODO Auto-generated method stub
		Log.e("Whistle", systemFunctions.getWhistleState() + "");
		if (systemFunctions.getWhistleState() == NOT_WHISTLE) {
			systemFunctions.launchSendSMSActivity();
			systemFunctions.setWhistleState(WHISTLED);
		} else if (systemFunctions.getWhistleState() == CANCEL_WHISTLE) {
			stopWhistleAndShakingDetector();
		}
	}

	private void startWhistleAndShakingDetector() {
		systemFunctions.setWhistleState(NOT_WHISTLE);
		recorderThread.start();
		shakeDetectorThread.start();
		detectorThread.setOnSignalsDetectedListener(this);
		detectorThread.start();
	}

	private void stopWhistleAndShakingDetector() {
		recorderThread.stopRecording();
		detectorThread.removeOnSignalsDetectedListener();
		detectorThread.stopDetection();
		shakeDetectorThread.stopDetection();
	}
}
