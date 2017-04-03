package com.sftrip;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
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
import com.sftrip.library.UserFunctions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {

	private static String KEY_SUCCESS = "success";
	private static final String FAVOR_PLACE1 = "Favorite Place 1";
	private static final String FAVOR_PLACE2 = "Favorite Place 2";
	private static final String FAVOR_PLACE3 = "Favorite Place 3";
	private static final String SERVER_CONNECTION_FAILURE = "Can't connect to the server. Please try again.";
	private static final String GOOGLE_SERVICE_PROBLEM = "Sftrip cannot start due to the Google Service Problem";
	private boolean isFP1HasInfo, isFP2HasInfo, isFP3HasInfo = false;
	private double FP1Lat, FP2Lat, FP3Lat, FP1Lng, FP2Lng, FP3Lng = 0;
	private String FP1Title, FP2Title, FP3Title = "";
	private UserFunctions userFunctions;
	private SystemFunctions systemFunctions;
	private Context context;
	private Activity activity;
	private static String placesURL = "";
	static InputStream is = null;
	static JSONObject jObj = null;
	static String json = "";
	private List<NameValuePair> params = null;
	Context mContext;
	private int counter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen);
		init();
		if (systemFunctions.isUserOnline()) {
			if (systemFunctions.servicesConnected(activity)) {
				if (userFunctions.isUserLoggedIn(context)) {
					new FavoritePlacesInfo().execute(new String[] {
							systemFunctions.getUserEmail(), FAVOR_PLACE1 });
					new FavoritePlacesInfo().execute(new String[] {
							systemFunctions.getUserEmail(), FAVOR_PLACE2 });
					new FavoritePlacesInfo().execute(new String[] {
							systemFunctions.getUserEmail(), FAVOR_PLACE3 });
				} else {
					Intent login = new Intent(getApplicationContext(),
							LoginActivity.class);
					login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(login);
					finish();
				}
			} else {
				Toast.makeText(
						context,
						GOOGLE_SERVICE_PROBLEM,
						Toast.LENGTH_SHORT).show();
			}
		} else {
			AlertDialog.Builder ad = new AlertDialog.Builder(mContext);
			ad.setTitle(R.string.no_internet_connection);
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

	private void init() {
		context = this;
		userFunctions = new UserFunctions();
		systemFunctions = new SystemFunctions(context);
		//activity = this;
		mContext = this;
		
	}

	public class FavoritePlacesInfo extends AsyncTask<String, Void, JSONObject> {

		private String favor_place;

		public FavoritePlacesInfo() {
			placesURL = "http://sit-edu4.sit.kmutt.ac.th/csc498/53270327/Boss/sftrip/index.php/place/getInformation";
			params = new ArrayList<NameValuePair>();
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