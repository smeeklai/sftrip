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
import com.sftrip.library.SystemFunctions;
import com.sftrip.library.Tracker;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TrackerActivity extends Activity implements View.OnClickListener {

	private static final String TAG = "LogInfo";
	private static final String ERROR_INPUT_STREAM = "Something wrong with IS";
	private static final String ERROR_HTTP_CONNECTION = "Failed to download result..";
	private static final String ERROR_UNABLE_CONVERTING = "Error converting result ";
	private static final String ERROR_DATA_PARSING = "Error parsing data ";
	private static final String SERVER_CONNECTION_FAILURE = "Can't connect to the server. Please try again.";
	private static String logInfo2URL = "";
	private List<NameValuePair> params = null;
	static InputStream is = null;
	static JSONObject jObj = null;
	static String json = "";
	private ImageView btn_panic;
	private Button btn_stop;
	private SystemFunctions systemFunction;
	private Intent intent;
	private String userStatus;
	private String remainingDistance;
	private TextView user_status;
	private TextView rd;
	private RelativeLayout lin;
	private AlertDialog.Builder ad;
	private boolean startActivity = false;

	private void init() {
		logInfo2URL = "http://sit-edu4.sit.kmutt.ac.th/csc498/53270327/Boss/sftrip/index.php/logInfo/changeCRD";
		params = new ArrayList<NameValuePair>();
		btn_panic = (ImageView) findViewById(R.id.btn_panic);
		btn_stop = (Button) findViewById(R.id.btn_stop_tracking);
		user_status = (TextView) findViewById(R.id.user_status);
		user_status.setText(R.string.user_status_default);
		rd = (TextView) findViewById(R.id.remaining_distance);
		rd.setText(R.string.remaining_distance_default);
		lin = (RelativeLayout) findViewById(R.id.tracker_info);
		systemFunction = new SystemFunctions(this);
		if (getIntent().getExtras() != null) {
			Log.e("test", getIntent().toString());
			startActivity = getIntent().getExtras().getBoolean("startActivity");
			intent = new Intent(this, Tracker.class);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.panic);
		init();
		btn_panic.setOnClickListener(this);
		btn_stop.setOnClickListener(this);
		if (startActivity == true) {
			startTracking();
		}
	}

	private void startTracking() {
		startService(intent);
		Log.i("tracker", "start Tracker");
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == btn_panic) {
			Intent intent = new Intent(this, AlertActivity.class);
			startActivity(intent);
		} else if (v == btn_stop) {
			ad = new AlertDialog.Builder(this);
			ad.setTitle(R.string.warning);
			ad.setMessage(R.string.confirm_stop_tracking);
			ad.setIcon(android.R.drawable.ic_dialog_alert);
			ad.setPositiveButton(R.string.yes_button, new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					Intent i = new Intent(TrackerActivity.this, Tracker.class);
					stopService(i);
					systemFunction.removeUserCurrentTrip();
					Log.e("service", "stop the service");
					finish();
				}
			});
			ad.setNegativeButton(R.string.no_button, null);
			ad.show();
		}
	}

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				userStatus = bundle.getString("userStatus");
				remainingDistance = bundle.getString("remainingDistance");
			}
			Double rd_double = Double.parseDouble(remainingDistance);
			if (rd_double > 1000) {
				convertMeterToKilometer(rd_double);
				user_status.setText("Your status: " + userStatus);
				rd.setText("Remaining distance: " + remainingDistance + " KM");
			} else {
				user_status.setText("Your status: " + userStatus);
				rd.setText("Remaining distance: " + remainingDistance + " M");
			}
			responseToSituation(userStatus);
		}

	};

	private void convertMeterToKilometer(Double remainingDistance) {
		remainingDistance = remainingDistance / 1000;
		this.remainingDistance = new java.text.DecimalFormat("#.##")
				.format(remainingDistance);
	}

	private void responseToSituation(String situation) {
		if (situation.equals("dangerous")) {
			Log.e("User's situation", "dangerous");
			lin.setBackgroundResource(R.color.red2);
			ad = new AlertDialog.Builder(this);
			ad.setTitle(R.string.warning);
			ad.setMessage(R.string.RD_increasing_situation);
			ad.setIcon(android.R.drawable.ic_dialog_alert);
			ad.setPositiveButton(R.string.user_still_ok, new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					String userTripID = systemFunction.getUserTripID() + "";
					JSONObject json = null;
					try {
						json = new LogInfo2().execute(
								new String[] { userTripID }).get();
						checkJSONResult(json);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			ad.setNeutralButton(R.string.not_sure, null);
			ad.setNegativeButton(R.string.send_sms_now, new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(TrackerActivity.this,
							AlertActivity.class);
					startActivity(intent);
				}
			});
			ad.show();
		} else if (situation.equals("not moving")) {
			Log.e("User's situation", "not moving");
			lin.setBackgroundResource(R.color.red2);
			ad = new AlertDialog.Builder(this);
			ad.setTitle(R.string.warning);
			ad.setMessage(R.string.user_doesnot_move_situation);
			ad.setIcon(android.R.drawable.ic_dialog_alert);
			ad.setPositiveButton(R.string.user_still_ok, new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					String userTripID = systemFunction.getUserTripID() + "";
					JSONObject json = null;
					try {
						json = new LogInfo2().execute(
								new String[] { userTripID }).get();
						checkJSONResult(json);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			ad.setNeutralButton(R.string.not_sure, null);
			ad.setNegativeButton(R.string.send_sms_now, new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(TrackerActivity.this,
							AlertActivity.class);
					startActivity(intent);
				}
			});
			ad.show();
		} else if (situation.equals("suspicious")) {
			lin.setBackgroundResource(R.color.orange2);
		} else {
			lin.setBackgroundResource(R.color.green);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(receiver, new IntentFilter("com.sftrip.library"));
		if (startActivity != true) {
			try {
				userStatus = systemFunction.getUserCurrentStatus();
				remainingDistance = systemFunction.getUserCurrentRd();
				Double rd_double = Double.parseDouble(remainingDistance);
				if (rd_double > 1000) {
					convertMeterToKilometer(rd_double);
					user_status.setText("Your status: " + userStatus);
					rd.setText("Remaining distance: " + remainingDistance
							+ " KM");
				} else {
					user_status.setText("Your status: " + userStatus);
					rd.setText("Remaining distance: " + remainingDistance
							+ " M");
				}
				responseToSituation(userStatus);
			} catch (NullPointerException e) {
				ad = new AlertDialog.Builder(this);
				ad.setTitle(R.string.notification);
				ad.setMessage(R.string.trip_was_finished);
				ad.setIcon(android.R.drawable.ic_dialog_alert);
				ad.setPositiveButton(R.string.yes_button, new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Intent intent = new Intent(getApplicationContext(),
								MainActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
						finish();
					}
				});
				ad.setNegativeButton(R.string.no_button, new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						onDestroy();
						finish();
					}
				});
				ad.show();
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(receiver);
	}

	private class LogInfo2 extends AsyncTask<String, Void, JSONObject> {

		@Override
		protected JSONObject doInBackground(String... strings) {
			// TODO Auto-generated method stub
			params.add(new BasicNameValuePair("tripID", strings[0].toString()));

			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(logInfo2URL);
			// Making HTTP request
			try {
				// defaultHttpClient

				httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

				HttpResponse httpResponse = httpClient.execute(httpPost);
				StatusLine statusLine = httpResponse.getStatusLine();
				int statusCode = statusLine.getStatusCode();
				if (statusCode == 200) {
					HttpEntity httpEntity = httpResponse.getEntity();
					is = httpEntity.getContent();
				} else {
					Log.e(TAG, ERROR_HTTP_CONNECTION + statusCode);
				}

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				if (is != null) {
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(is, "iso-8859-1"), 8);
					StringBuilder sb = new StringBuilder();
					String line = null;
					while ((line = reader.readLine()) != null) {
						sb.append(line + "\n");
					}
					is.close();
					json = sb.toString();
					Log.e("JSON", json);
				} else {
					Log.e(TAG, ERROR_INPUT_STREAM);
				}
			} catch (Exception e) {
				Log.e("Buffer Error", ERROR_UNABLE_CONVERTING + e.toString());
			}

			// try parse the string to a JSON object
			try {
				jObj = new JSONObject(json);
			} catch (JSONException e) {
				Log.e("JSON Parser", ERROR_DATA_PARSING + e.toString());
			}

			// return JSON String
			return jObj;
		}

		protected void onPostExecute(JSONObject json) {
			super.onPostExecute(json);
		}

	}

	private void checkJSONResult(JSONObject json) {
		try {
			String msg = json.getString("msg");
			if (msg.equals("Unsuccessfully changed CRD. Please try again")) {
				ad = new AlertDialog.Builder(this);
				ad.setTitle(R.string.warning);
				ad.setMessage(msg);
				ad.setIcon(android.R.drawable.ic_dialog_alert);
				ad.setNegativeButton(R.string.ok, null);
				ad.show();
			} else {
				user_status.setText("Your status: normal");
				lin.setBackgroundResource(R.color.green);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
			Toast.makeText(this, SERVER_CONNECTION_FAILURE, Toast.LENGTH_LONG).show();
		}
	}
}
