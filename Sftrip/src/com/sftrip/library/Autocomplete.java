package com.sftrip.library;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.AsyncTask;
import android.util.Log;

public class Autocomplete extends AsyncTask<String, Void, ArrayList<String>> {
	private static final String LOG_TAG = "Sftrip";
	private static final String ERROR_IOEXCEPTION = "Error connecting to Places API";
	private static final String ERROR_MALFORMEDURLEXCEPTION = "Error processing Places API URL";
	private static final String ERROR_JSONEXCEPTION = "Cannot process JSON results";
	private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
	private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
	private static final String OUT_JSON = "/json";
	private static final String API_KEY = "AIzaSyAgFdrAZ2AAULMOnDgpRZljeMTQ3Zskb9E";
	private static ArrayList<String> resultList;
	private HttpURLConnection conn;
	private StringBuilder jsonResults;

	public Autocomplete() {
		conn = null;
		jsonResults = new StringBuilder();
	}

	@Override
	protected ArrayList<String> doInBackground(String... params) {
		// TODO Auto-generated method stub
		try {
			StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
			sb.append("?input=" + URLEncoder.encode(params[0].toString(), "utf8"));
			sb.append("&sensor=false");
			sb.append("&key=" + API_KEY);
			sb.append("&components=country:th");
			URL url = new URL(sb.toString());
			conn = (HttpURLConnection) url.openConnection();
			InputStreamReader in = new InputStreamReader(conn.getInputStream());

			// Load the results into a StringBuilder
			int read;
			char[] buff = new char[1024];
			while ((read = in.read(buff)) != -1) {
				jsonResults.append(buff, 0, read);
			}
		} catch (MalformedURLException e) {
			Log.e(LOG_TAG, ERROR_MALFORMEDURLEXCEPTION, e);
			return resultList;
		} catch (IOException e) {
			Log.e(LOG_TAG, ERROR_IOEXCEPTION, e);
			return resultList;
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}

		try {
			// Create a JSON object hierarchy from the results
			JSONObject jsonObj = new JSONObject(jsonResults.toString());
			JSONArray jArr = jsonObj.getJSONArray("predictions");
			
			resultList = new ArrayList<String>(jArr.length());
	        for (int i = 0; i < jArr.length(); i++) {
	            resultList.add(jArr.getJSONObject(i).getString("description"));
	        }
		} catch (JSONException e) {
			Log.e(LOG_TAG, ERROR_JSONEXCEPTION, e);
		}

		return resultList;
	}

	protected void onPostExecute(ArrayList<String> list) {
		super.onPostExecute(list);
	}

}
