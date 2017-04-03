package com.sftrip.library;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

public class Login extends AsyncTask<String, Void, JSONObject> {
	private static String loginURL = "";
	static InputStream is = null;
	static JSONObject jObj = null;
	static String json = "";
	private List<NameValuePair> params = null;

	public Login(Context context) {
		loginURL = "http://sit-edu4.sit.kmutt.ac.th/csc498/53270327/Boss/sftrip/index.php/checkLogin";
		params = new ArrayList<NameValuePair>();
	}

	protected JSONObject doInBackground(String... strings) {
		params.add(new BasicNameValuePair("email", strings[0].toString()));
		params.add(new BasicNameValuePair("password", strings[1].toString()));

		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(loginURL);
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
	}
}
