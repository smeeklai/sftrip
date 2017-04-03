package com.sftrip.library;

import java.util.List;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.sftrip.AlertActivity;
import com.sftrip.MapActivity;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;

public class SystemFunctions {
	private static final String TAG = "Google Play Services";
	private Context mContext;
	private boolean isGPSEnabled;
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	private SessionManagement sm;
	private PowerManager pm;
	private PowerManager.WakeLock wakeLock = null;
	private KeyguardManager keyguardManager;
	private KeyguardLock keyguardLock;
	private Vibrator vibrator;
	private boolean stopUnauthorization = false;
	private static int WHISTLE_STATE = 0;

	public SystemFunctions(Context context) {
		mContext = context;
		isGPSEnabled = false;
		sm = new SessionManagement(context);
		pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
		keyguardManager = (KeyguardManager) mContext
				.getSystemService(Context.KEYGUARD_SERVICE);
		vibrator = (Vibrator) mContext
				.getSystemService(Context.VIBRATOR_SERVICE);
	}

	public boolean isUserOnline() {
		ConnectivityManager cm = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isUserEnabledGPS() {
		final LocationManager locationManager = (LocationManager) mContext
				.getSystemService(Context.LOCATION_SERVICE);

		isGPSEnabled = locationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER);

		if (isGPSEnabled == true) {
			return true;
		} else {
			return false;
		}
	}

	public boolean getGpsStatus() {
		return isGPSEnabled;
	}

	public void setGpsStatus(boolean status) {
		isGPSEnabled = status;
	}

	public boolean servicesConnected(Activity activity) {
		// Check that Google Play services is available
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(mContext);

		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {
			// In debug mode, log the status
			Log.d(TAG, "Connected successfully");

			// Continue
			return true;
			// Google Play services was not available for some reason
		} else {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, activity,
						CONNECTION_FAILURE_RESOLUTION_REQUEST).show();
			} else {
				Log.i(TAG, "This device is not supported.");
				activity.finish();
			}
			return false;

		}
	}

	public void enableGPSDialog(Context context) {
		Intent gpsOptionsIntent = new Intent(
				android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		context.startActivity(gpsOptionsIntent);
	}

	public void enableGoogleLocationService(Context context) {
		Intent gpsOptionsIntent = new Intent(
				android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		context.startActivity(gpsOptionsIntent);
	}

	public void enableNetworkDialog(Context context) {
		Intent networkOptionsIntent = new Intent(
				android.provider.Settings.ACTION_WIRELESS_SETTINGS);
		context.startActivity(networkOptionsIntent);
	}

	public String getUserEmail() {
		return sm.getUserName();
	}

	public void createUserTripData(int tripID, int period) {
		sm.addUserCurrentTripData(tripID, period);
	}

	public int getUserTripID() {
		return sm.getUserTripID();
	}

	public void removeUserCurrentTrip() {
		sm.clearUserCurrentTrip();
	}

	public void storeUserStatusAndRd(String status, String rd) {
		sm.addUserCurrentStatusAndRd(status, rd);
	}

	public String getUserCurrentStatus() {
		return sm.getUserCurrentStatus();
	}

	public String getUserCurrentRd() {
		return sm.getUserCurrentRd();
	}

	public int getSendingDataPeriod() {
		return sm.getSendingDataPeriod();
	}

	public boolean isActivityInForeground(String myPackage) {
		ActivityManager manager = (ActivityManager) mContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager
				.getRunningTasks(1);

		ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
		if (componentInfo.getPackageName().equals(myPackage)) {
			return true;
		}
		return false;
	}

	//@SuppressWarnings("deprecation")
	public void wakeScreen() {
		if (wakeLock == null) {
			wakeLock = pm
					.newWakeLock(
							(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP
									| PowerManager.ON_AFTER_RELEASE),
							"TAG");
		}
		if (keyguardLock == null) {
			keyguardLock = keyguardManager.newKeyguardLock("TAG");
		}
		//if (wakeLock != null && wakeLock.isHeld() == false) {
			wakeLock.acquire();
		//}
		keyguardLock.disableKeyguard();
	}

	public void releaseWakeLock() {
		if (wakeLock != null) {
			wakeLock.release();
			wakeLock = null;
		}
	}

	public void closeAllActivity() {
		Intent intent = new Intent(mContext, MapActivity.class);
		intent.putExtra("Exits", true);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		mContext.startActivity(intent);
	}

	public boolean isValidEmail(CharSequence target) {
		if (target == null) {
			return false;
		} else {
			return android.util.Patterns.EMAIL_ADDRESS.matcher(target)
					.matches();
		}
	}

	public void vibratePhone(long pattern) {
		vibrator.vibrate(pattern);
	}

	public void logout() {
		sm.logoutUser();
	}

	public void launchSendSMSActivity() {
		wakeScreen();
		setWhistleState(1);
		Intent intent = new Intent(mContext, AlertActivity.class);
		intent.putExtra("setSound", true);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mContext.startActivity(intent);
	}

	public int getWhistleState() {
		return WHISTLE_STATE;
	}

	public void setWhistleState(int state) {
		WHISTLE_STATE = state;
	}

	public boolean getStopUnauthorization() {
		return stopUnauthorization;
	}

	public void setStopUnauthorization(boolean state) {
		stopUnauthorization = state;
	}

}
