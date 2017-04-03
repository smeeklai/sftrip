package com.sftrip.library;

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

import android.os.AsyncTask;
import android.util.Log;

public class Panic extends AsyncTask<String, Void, JSONObject> {
	static InputStream is = null;
	static JSONObject jObj = null;
	static String json = "";
	private List<NameValuePair> params = null;
	private static String SMS_URL;
	private static final String TAG = "Panic";
	private static final String ERROR_INPUT_STREAM = "Something wrong with IS";
	private static final String ERROR_HTTP_CONNECTION = "Failed to download result..";
	private static final String ERROR_UNABLE_CONVERTING = "Error converting result ";
	private static final String ERROR_DATA_PARSING = "Error parsing data ";
	
	public Panic() {
		params = new ArrayList<NameValuePair>();
		SMS_URL = "http://sit-edu4.sit.kmutt.ac.th/csc498/53270327/Boss/sftrip/index.php/sms";
	}
	
	@Override
	protected JSONObject doInBackground(String... strings) {
		params.add(new BasicNameValuePair("email", strings[0].toString()));
		params.add(new BasicNameValuePair("tripID", strings[1].toString()));
		params.add(new BasicNameValuePair("sendingDataPeriod", strings[2].toString()));

		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(SMS_URL);
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
