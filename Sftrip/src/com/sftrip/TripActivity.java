package com.sftrip;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
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

import com.sftrip.library.CustomSpinnerAdapter;
import com.sftrip.library.SystemFunctions;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

public class TripActivity extends Activity implements android.view.View.OnClickListener {

	private static final int CAMERA_REQUEST1 = 1888;
	private static final int CAMERA_REQUEST2 = 1889;
	private static final String TAG = "TripActivity";
	private static final String ERROR_INPUT_STREAM = "Something wrong with IS";
	private static final String ERROR_HTTP_CONNECTION = "Failed to download result..";
	private static final String ERROR_UNABLE_CONVERTING = "Error converting result ";
	private static final String ERROR_DATA_PARSING = "Error parsing data ";
	private static final String NON_DESTINATION_HAS_SELECTED = "Non destination has selected";
	private static final String NON_USER_CURRENT_LOCATION = "An unknown place";
	private static final String LP_OR_DN_ARE_NULL = "License plate and driver number cannot be empty unless you take pictures instead";
	private static final String SERVER_CONNECTION_FAILURE = "Can't connect to the server. Please try again.";
	private static String tripURL = "";
	private AlertDialog.Builder ad;
	private ArrayAdapter<String> adapter;
	private ArrayAdapter<String> adapter2;
	private Spinner spinnerColorPicker1;
	private Spinner spinnerColorPicker2;
	private SystemFunctions systemFunction;
	static InputStream is = null;
	static JSONObject jObj = null;
	static String json = "";
	private List<NameValuePair> params = null;
	private static String KEY_SUCCESS = "success";
	private static String KEY_ERROR = "error";
	private static String KEY_ERROR_MSG = "error_msg";
	private Button btn_start;
	private Button btn_camera1;
	private Button btn_camera2;
	private ImageView img_licensePlate;
	private ImageView img_driverNumber;
	private EditText edit_currentLocation;
	private EditText edit_destinationLocation;
	private EditText edit_licensePlate;
	private EditText edit_driverNumber;
	private RadioButton rb_3mins;
	private RadioButton rb_5mins;
	private RadioButton rb_7mins;
	private String taxiColor1;
	private String taxiColor2;
	private String licensePlate;
	private String driverNumber;
	private int locationPeriod;
	private String startLat;
	private String startLng;
	private String desLat;
	private String desLng;
	private String destination;
	private String userCurrentLocationDetial;
	private Bitmap licensePlatePic = null;
	private Bitmap driverNumberPic = null;
	private String licensePlatePic_str;
	private String driverNumberPic_str;
	private Integer[] listedColor = new Integer[] {R.color.white, R.color.blue5, R.color.green, 
			R.color.orange2, R.color.pink, R.color.red, R.color.violet, R.color.yellow, R.color.gold};
	private Integer[] listedColor2 = new Integer[] {R.drawable.no_color ,R.color.white, R.color.blue5, R.color.green, 
			R.color.orange2, R.color.pink, R.color.red, R.color.violet, R.color.yellow, R.color.gold};
	private String[] taxiColors = new String[] {"White", "Blue", "Green", "Orange", "Pink", "Red", "Violet", 
			"Yellow", "Gold"};
	private String[] taxiColors2 = new String[] {"", "White", "Blue", "Green", "Orange", "Pink", "Red", "Violet", 
			"Yellow", "Gold"};
	
	private void init() {
		tripURL = "http://sit-edu4.sit.kmutt.ac.th/csc498/53270327/Boss/sftrip/index.php/trip";
		destination = getIntent().getExtras().getString("destinationName",
				NON_DESTINATION_HAS_SELECTED);
		userCurrentLocationDetial = getIntent().getExtras().getString(
				"currentPlaceName", NON_USER_CURRENT_LOCATION);
		Log.i("des", destination);
		params = new ArrayList<NameValuePair>();
		btn_start = (Button) findViewById(R.id.btn_start);
		btn_camera1 = (Button) findViewById(R.id.btn_alert_cancel);
		btn_camera2 = (Button) findViewById(R.id.camera2);
		img_licensePlate = (ImageView) findViewById(R.id.showPic1);
		img_driverNumber = (ImageView) findViewById(R.id.showPic2);
		edit_currentLocation = (EditText) findViewById(R.id.edit_start);
		edit_currentLocation.setText(userCurrentLocationDetial);
		edit_destinationLocation = (EditText) findViewById(R.id.edit_to);
		edit_destinationLocation.setText(destination);
		edit_licensePlate = (EditText) findViewById(R.id.edit_license);
		edit_driverNumber = (EditText) findViewById(R.id.edit_driver_number);
		rb_3mins = (RadioButton) findViewById(R.id.rb_3mins);
		rb_5mins = (RadioButton) findViewById(R.id.rb_5mins);
		rb_7mins = (RadioButton) findViewById(R.id.rb_7mins);
		spinnerColorPicker1 = (Spinner) findViewById(R.id.favorite_taxi_color_options);
		spinnerColorPicker2 = (Spinner) findViewById(R.id.favorite_taxi_color_options2);
		adapter = new CustomSpinnerAdapter(this, R.layout.spinner_lists, listedColor, taxiColors, this);
		adapter2 = new CustomSpinnerAdapter(this, R.layout.spinner_lists, listedColor2, taxiColors2, this);
		spinnerColorPicker1.setAdapter(adapter);
		spinnerColorPicker2.setAdapter(adapter2);
	}

	protected void onResume() {
		super.onResume();
		if (!systemFunction.isUserOnline()) {
			ad = new AlertDialog.Builder(this);
			ad.setTitle(R.string.warning);
			ad.setMessage(R.string.enable_internet_connection);
			ad.setIcon(android.R.drawable.ic_dialog_alert);
			ad.setNegativeButton(R.string.cancel, new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					finish();
				}
			});
			ad.setPositiveButton(R.string.settings, new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					systemFunction.enableNetworkDialog(TripActivity.this);
				}
			});
			ad.show();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		systemFunction = new SystemFunctions(this);
		if (systemFunction.isUserOnline()) {
			setContentView(R.layout.info);
			init();
			this.getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

			btn_start.setOnClickListener(this);
			btn_camera1.setOnClickListener(this);
			btn_camera2.setOnClickListener(this);
			spinnerColorPicker1.setOnItemSelectedListener(itemSelectedListener1);
			spinnerColorPicker2.setOnItemSelectedListener(itemSelectedListener2);
		} else {
			ad = new AlertDialog.Builder(this);
			ad.setTitle(R.string.warning);
			ad.setMessage(R.string.enable_internet_connection);
			ad.setIcon(android.R.drawable.ic_dialog_alert);
			ad.setNegativeButton(R.string.cancel, new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					finish();
				}
			});
			ad.setPositiveButton(R.string.settings, new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					systemFunction.enableNetworkDialog(TripActivity.this);
				}
			});
			ad.show();
		}
	}

	private void response(JSONObject json) {
		try {
			if (json.getString(KEY_SUCCESS) != null) {
				String res = json.getString(KEY_SUCCESS);
				if (Integer.parseInt(res) == 1) {
					// user successfully stored favorite place
					systemFunction.removeUserCurrentTrip();
					int userTripID = json.getInt("tripID");
					systemFunction.createUserTripData(userTripID, locationPeriod);
					Intent intent = new Intent(TripActivity.this,
							TrackerActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra("startActivity", true);
					startActivity(intent);
					finish();
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
		if (v == btn_start) {
			licensePlate = edit_licensePlate.getText().toString();
			driverNumber = edit_driverNumber.getText().toString();
			locationPeriod = checkLocationPeriod();
			startLat = getIntent().getExtras().getDouble("userCurrentLat") + "";
			startLng = getIntent().getExtras().getDouble("userCurrentLng") + "";
			desLat = getIntent().getExtras().getDouble("userDestinationLat")
					+ "";
			desLng = getIntent().getExtras().getDouble("userDestinationLng")
					+ "";
			licensePlatePic_str = "";
			driverNumberPic_str = "";
			if (licensePlatePic != null) {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				licensePlatePic.compress(Bitmap.CompressFormat.JPEG, 90, stream);
				byte [] byte_arr = stream.toByteArray();
				licensePlatePic_str = Base64.encodeToString(byte_arr, Base64.DEFAULT);
			}
			if (driverNumberPic != null) {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				driverNumberPic.compress(Bitmap.CompressFormat.JPEG, 90, stream);
				byte [] byte_arr = stream.toByteArray();
				driverNumberPic_str = Base64.encodeToString(byte_arr, Base64.DEFAULT);
			}
			boolean didUserFillData = false;
			if (licensePlate.equals("") || driverNumber.equals("")) {
				if (licensePlatePic != null && driverNumberPic != null) {
					didUserFillData = true;
				}
			} else {
				didUserFillData = true;
			}
			if (didUserFillData == true){
				String taxiColor = taxiColor1 + taxiColor2;
				String[] test = new String[] { systemFunction.getUserEmail(),
						licensePlate, driverNumber, taxiColor, startLat,
						startLng, desLat, desLng, licensePlatePic_str, driverNumberPic_str};
				JSONObject json = null;
				try {
					json = new Trip(this).execute(test).get();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				response(json);
			} else {
				Toast.makeText(TripActivity.this, LP_OR_DN_ARE_NULL, Toast.LENGTH_LONG).show();
			}
		} else if (v == btn_camera1) {
			Intent cameraIntent1 = new Intent(
					android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			startActivityForResult(cameraIntent1, CAMERA_REQUEST1);
		} else if (v == btn_camera2) {
			Intent cameraIntent2 = new Intent(
					android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			startActivityForResult(cameraIntent2, CAMERA_REQUEST2);
		}
	}

	private int checkLocationPeriod() {
		int period = 3;
		if (rb_3mins.isChecked()) {
			period = 3;
		} else if (rb_5mins.isChecked()) {
			period = 5;
		} else if (rb_7mins.isChecked()) {
			period = 7;
		}
		return period;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAMERA_REQUEST1 && resultCode == RESULT_OK) {
			licensePlatePic = (Bitmap) data.getExtras().get("data");
			img_licensePlate.setImageBitmap(licensePlatePic);
		} else if (requestCode == CAMERA_REQUEST2 && resultCode == RESULT_OK) {
			driverNumberPic = (Bitmap) data.getExtras().get("data");
			img_driverNumber.setImageBitmap(driverNumberPic);
		} else {
			Log.e("Camera Intent", "Something is wrong");
		}
	}
	
	OnItemSelectedListener itemSelectedListener1 = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> adapterView, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			taxiColor1 = (String) adapterView.getItemAtPosition(position);
		}

		@Override
		public void onNothingSelected(AdapterView<?> adapterView) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	OnItemSelectedListener itemSelectedListener2 = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> adapterView, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			taxiColor2 = (String) adapterView.getItemAtPosition(position);
		}

		@Override
		public void onNothingSelected(AdapterView<?> adapterView) {
			// TODO Auto-generated method stub
			
		}
		
	};

	private class Trip extends AsyncTask<String, Void, JSONObject> {

		private ProgressDialog pdia;
		private Context mContext;
		public Trip(Context context) {
			// TODO Auto-generated constructor stub
			mContext = context;
		}
		
		@Override
		protected void onPreExecute(){ 
		   super.onPreExecute();
		        pdia = new ProgressDialog(mContext);
		        pdia.setMessage("Loading...");
		        pdia.show();    
		}

		@Override
		protected JSONObject doInBackground(String... strings) {
			// TODO Auto-generated method stub
			params.add(new BasicNameValuePair("email", strings[0].toString()));
			params.add(new BasicNameValuePair("taxiPlate", strings[1]
					.toString()));
			params.add(new BasicNameValuePair("taxiLicense", strings[2]
					.toString()));
			params.add(new BasicNameValuePair("taxiColor", strings[3]
					.toString()));
			params.add(new BasicNameValuePair("startLat", strings[4].toString()));
			params.add(new BasicNameValuePair("startLng", strings[5].toString()));
			params.add(new BasicNameValuePair("desLat", strings[6].toString()));
			params.add(new BasicNameValuePair("desLng", strings[7].toString()));
			params.add(new BasicNameValuePair("licensePlatePic", strings[8].toString()));
			params.add(new BasicNameValuePair("driverNumberPic", strings[9].toString()));
			// params.add(new BasicNameValuePair("finished",
			// strings[8].toString()));

			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(tripURL);
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
			pdia.dismiss();
		}

	}

}
