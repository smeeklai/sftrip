/**
 * Author: Ravi Tamada
 * URL: www.androidhive.info
 * twitter: http://twitter.com/ravitamada
 * */
package com.sftrip;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.widget.Toast;

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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.sftrip.MainActivity.FavoritePlacesInfo;
import com.sftrip.library.Login;
import com.sftrip.library.SessionManagement;
import com.sftrip.library.SystemFunctions;
import com.sftrip.library.UserFunctions;

public class LoginActivity extends Activity implements
		android.view.View.OnClickListener {
	private Button btnLogin;
	private Button btnLinkToRegister;
	private EditText inputEmail;
	private EditText inputPassword;
	private AlertDialog.Builder ad;
	private ProgressDialog pdia;
	Context mContext;
	private SystemFunctions systemFunctions;
	private static String KEY_SUCCESS = "success";
	private static String KEY_ERROR = "error";
	private static String KEY_ERROR_MSG = "error_msg";
	private static final String FAVOR_PLACE1 = "Favorite Place 1";
	private static final String FAVOR_PLACE2 = "Favorite Place 2";
	private static final String FAVOR_PLACE3 = "Favorite Place 3";
	private static final String SERVER_CONNECTION_FAILURE = "Can't connect to the server. Please try again.";
	private UserFunctions userFunction;
	private TextView go_register;
	private static String placesURL = "";
	static InputStream is = null;
	static JSONObject jObj = null;
	static String json = "";
	private List<NameValuePair> params = null;
	private int counter = 0;
	private boolean isFP1HasInfo, isFP2HasInfo, isFP3HasInfo = false;
	private double FP1Lat, FP2Lat, FP3Lat, FP1Lng, FP2Lng, FP3Lng = 0;
	private String FP1Title, FP2Title, FP3Title = "";

	private void init() {
		// Importing all assets like buttons, text fields
		mContext = this;
		inputEmail = (EditText) findViewById(R.id.edit_email);
		inputPassword = (EditText) findViewById(R.id.edit_pass);
		btnLogin = (Button) findViewById(R.id.btn_stop_tracking);
		systemFunctions = new SystemFunctions(this);
		userFunction = new UserFunctions();
		go_register = (TextView) findViewById(R.id.go_to_register);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		this.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		init();

		if (systemFunctions.isUserOnline()) {

			// Login button Click Event
			btnLogin.setOnClickListener(this);
			go_register.setOnClickListener(this);

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
					systemFunctions.enableNetworkDialog(mContext);
				}
			});
			ad.show();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (!systemFunctions.isUserOnline()) {
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
					systemFunctions.enableNetworkDialog(mContext);
				}
			});
			ad.show();
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == btnLogin) {
			String email = inputEmail.getText().toString();
			String password = inputPassword.getText().toString();
			if (email.isEmpty() || password.isEmpty()) {
				Toast.makeText(mContext, R.string.user_password_null_input,
						Toast.LENGTH_LONG).show();
			} else {
				// if (systemFunctions.isValidEmail(email) == true) {
				pdia = new ProgressDialog(mContext);
				pdia.setMessage("Loading...");
				pdia.show();
				Login test = new Login(mContext);
				try {
					JSONObject json = test.execute(
							new String[] { email, password }).get();
					if (json.getString(KEY_SUCCESS) != null) {
						String res = json.getString(KEY_SUCCESS);
						if (Integer.parseInt(res) == 1) {
							userFunction
									.createUserLoginSession(mContext, email);
							
							new FavoritePlacesInfo().execute(new String[] {
									systemFunctions.getUserEmail(),
									FAVOR_PLACE1 });
							new FavoritePlacesInfo().execute(new String[] {
									systemFunctions.getUserEmail(),
									FAVOR_PLACE2 });
							new FavoritePlacesInfo().execute(new String[] {
									systemFunctions.getUserEmail(),
									FAVOR_PLACE3 });

							// Close Login Screen
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
					Toast.makeText(this, SERVER_CONNECTION_FAILURE,
							Toast.LENGTH_LONG).show();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Toast.makeText(this, SERVER_CONNECTION_FAILURE,
							Toast.LENGTH_LONG).show();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Toast.makeText(this, SERVER_CONNECTION_FAILURE,
							Toast.LENGTH_LONG).show();
				} catch (NullPointerException e) {
					Toast.makeText(this, SERVER_CONNECTION_FAILURE,
							Toast.LENGTH_LONG).show();
				}
				// } else {
				// inputEmail.setError("The email is not in valid form");
				// }
			}
		} else if (v == go_register) {
			Intent i = new Intent(getApplicationContext(),
					RegisterActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			finish();
		}
	}

	public class FavoritePlacesInfo extends AsyncTask<String, Void, JSONObject> {

		private String favor_place;

		public FavoritePlacesInfo() {
			placesURL = "http://sit-edu4.sit.kmutt.ac.th/csc498/53270327/Boss/sftrip/index.php/place/getInformation";
			params = new ArrayList<NameValuePair>();
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected JSONObject doInBackground(String... strings) {
			// TODO Auto-generated method stub
			favor_place = strings[1];

			params.add(new BasicNameValuePair("email", strings[0].toString()));
			params.add(new BasicNameValuePair("placeID", strings[1].toString()));

			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(placesURL);
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
					Log.e("Log", "Failed to download result.." + statusCode);
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
					Log.e("Log", "Something wrong with IS");
				}
			} catch (Exception e) {
				Log.e("Buffer Error", "Error converting result " + e.toString());
			}

			// try parse the string to a JSON object
			try {
				jObj = new JSONObject(json);
			} catch (JSONException e) {
				Log.e("JSON Parser", "Error parsing data " + e.toString());
			}

			// return JSON String
			return jObj;
		}
		
		@Override
		protected void onPostExecute(JSONObject json) {
			super.onPostExecute(json);
			doesFavoritePlaceHaveInfo(systemFunctions.getUserEmail(),
					favor_place, json);
			if (counter == 3) {
				
				Intent intent = new Intent(getApplicationContext(),
						MapActivity.class);
				intent.putExtra("IsFB1HasInfo", isFP1HasInfo);
				intent.putExtra("FP1Lat", FP1Lat);
				intent.putExtra("FP1Lng", FP1Lng);
				intent.putExtra("FP1Title", FP1Title);
				intent.putExtra("IsFB2HasInfo", isFP2HasInfo);
				intent.putExtra("FP2Lat", FP2Lat);
				intent.putExtra("FP2Lng", FP2Lng);
				intent.putExtra("FP2Title", FP2Title);
				intent.putExtra("IsFB3HasInfo", isFP3HasInfo);
				intent.putExtra("FP3Lat", FP3Lat);
				intent.putExtra("FP3Lng", FP3Lng);
				intent.putExtra("FP3Title", FP3Title);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				pdia.dismiss();
				pdia = null;
				finish();
			}
		}

		private void doesFavoritePlaceHaveInfo(String email, String placeID,
				JSONObject json) {
			counter++;
			try {
				if (json.getString(KEY_SUCCESS).equals("1")) {
					if (placeID.equals(FAVOR_PLACE1)) {
						isFP1HasInfo = true;
						FP1Lat = Double.parseDouble(json.getString("latitude"));
						FP1Lng = Double.parseDouble(json.getString("longtitude"));
						FP1Title = json.getString("placeName");
					} else if (placeID.equals(FAVOR_PLACE2)) {
						isFP2HasInfo = true;
						FP2Lat = Double.parseDouble(json.getString("latitude"));
						FP2Lng = Double.parseDouble(json.getString("longtitude"));
						FP2Title = json.getString("placeName");
					} else if (placeID.equals(FAVOR_PLACE3)) {
						isFP3HasInfo = true;
						FP3Lat = Double.parseDouble(json.getString("latitude"));
						FP3Lng = Double.parseDouble(json.getString("longtitude"));
						FP3Title = json.getString("placeName");
					}
					Log.e("favorPlace", "success");
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
				Toast.makeText(mContext, SERVER_CONNECTION_FAILURE,
						Toast.LENGTH_LONG).show();
			}
		}
	}
}
