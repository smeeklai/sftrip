package com.sftrip;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.sftrip.library.Panic;
import com.sftrip.library.SystemFunctions;

import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class AlertActivity extends Activity implements android.view.View.OnClickListener {
	
	private final static int NOT_WHISTLE = 0;
	private final static int CANCEL_WHISTLE = 2;
	private Button btn_cancel;
	private TextView time_counter;
	
	private SystemFunctions systemFunctions;
	private CountDownTimer timer;
	private AlertDialog.Builder ad;
	private static int COUNTDOWN_TIME = 15000;
	private static String KEY_SUCCESS = "success";
	private static String KEY_ERROR = "error";
	private static String KEY_ERROR_MSG = "error_msg";
	private static final String SERVER_CONNECTION_FAILURE = "Can't connect to the server. Please try again.";
	private Vibrator vibrator;
	private MediaPlayer mp1;
	private boolean setSound = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alert);
		init();
		startCountDownTimer();
		btn_cancel.setOnClickListener(this);
	}

	private void init() {
		btn_cancel = (Button) findViewById(R.id.btn_alert_cancel);
		time_counter = (TextView) findViewById(R.id.time_counter);
		systemFunctions = new SystemFunctions(this);
		vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
		mp1 = MediaPlayer.create(this, R.raw.police_siren);
		
		if (getIntent().getExtras() != null) {
			setSound = getIntent().getExtras().getBoolean("setSound");
		}
	}

	private void startCountDownTimer() {
		if(setSound == true) {
			mp1.start();
		}
		timer = new CountDownTimer(COUNTDOWN_TIME, 1000) {
		long[] pattern = {0, 800, 200};
			public void onTick(long millisUntilFinished) {
				if ((millisUntilFinished / 1000) == 1) {
					time_counter.setTextSize(60);
					time_counter.setText("sending...");
					vibrator.vibrate(pattern, 0);
				} else {
					time_counter.setText(millisUntilFinished / 1000 + "");
					vibrator.vibrate(pattern, -1);
				}
			}

			public void onFinish() {
				sendEmergencySMSs();
			}
		}.start();
	}
	
	public void sendEmergencySMSs() {
		String email = systemFunctions.getUserEmail();
		String tripID = systemFunctions.getUserTripID() + "";
		String sendingDataPeriod = systemFunctions.getSendingDataPeriod() + "";
		mp1.stop();
		vibrator.cancel();
		try {
			JSONObject json = new Panic().execute(new String[] {email, tripID, sendingDataPeriod}).get();
			triggerSMSSender(json);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(this, SERVER_CONNECTION_FAILURE, Toast.LENGTH_LONG).show();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(this, SERVER_CONNECTION_FAILURE, Toast.LENGTH_LONG).show();
		}
	}

	private void triggerSMSSender(JSONObject json) {
		try {
			if (json.getString(KEY_SUCCESS) != null) {
				String res = json.getString(KEY_SUCCESS);
				if (Integer.parseInt(res) == 1) {
					// user successfully stored favorite place
					systemFunctions.setWhistleState(CANCEL_WHISTLE);
					ad = new AlertDialog.Builder(this);
					ad.setTitle(R.string.congratulation);
					ad.setIcon(android.R.drawable.btn_star_big_on);
					ad.setPositiveButton(R.string.close, new OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							finish();
						}
					});
					ad.setMessage(R.string.sms_are_successfully_sent);
					ad.show();
				} else if (json.getString(KEY_ERROR).equals("1")) {
					// Error in login
					String error_msg = json.getString(KEY_ERROR_MSG);
					ad = new AlertDialog.Builder(this);
					ad.setTitle(R.string.error);
					ad.setIcon(android.R.drawable.btn_star_big_on);
					ad.setPositiveButton(R.string.close, null);
					ad.setMessage(error_msg);
					ad.show();
				} else {
					ad = new AlertDialog.Builder(this);
					ad.setTitle(R.string.error);
					ad.setIcon(android.R.drawable.btn_star_big_on);
					ad.setPositiveButton(R.string.close, null);
					ad.setMessage(R.string.something_wrong);
					ad.show();
				}
			} else {
				ad = new AlertDialog.Builder(this);
				ad.setTitle(R.string.error);
				ad.setIcon(android.R.drawable.btn_star_big_on);
				ad.setPositiveButton(R.string.close, null);
				ad.setMessage(R.string.something_wrong);
				ad.show();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
			Toast.makeText(this, SERVER_CONNECTION_FAILURE, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		systemFunctions.setWhistleState(NOT_WHISTLE);
		timer.cancel();
		mp1.stop();
		finish();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (systemFunctions.getWhistleState() == CANCEL_WHISTLE) {
			systemFunctions.setWhistleState(CANCEL_WHISTLE);
		} else {
			systemFunctions.setWhistleState(NOT_WHISTLE);
		}
		timer.cancel();
		mp1.stop();
		finish();
	}


}
