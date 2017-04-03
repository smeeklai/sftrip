package com.sftrip.library;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ShutdownReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		SystemFunctions systemFunctions = new SystemFunctions(context);
		if (intent.getAction().equals(Intent.ACTION_SHUTDOWN) && systemFunctions.getStopUnauthorization() == false) {
			Log.e("ShutdownReceiver", "Service has stopped unexpectedly");
			String email = systemFunctions.getUserEmail();
			String tripID = systemFunctions.getUserTripID() + "";
			String sendingDataPeriod = systemFunctions.getSendingDataPeriod() + "";
			new Panic().execute(new String[] {email, tripID, sendingDataPeriod});
			systemFunctions.setStopUnauthorization(true);
		}
	}

}
