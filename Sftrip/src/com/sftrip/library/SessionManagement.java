package com.sftrip.library;

import com.sftrip.LoginActivity;
import java.util.HashMap;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SessionManagement {
	SharedPreferences pref;
	Editor editor;
	Context _context;
	int PRIVATE_MODE = 0;
	private static final String PREF_NAME = "SftripPref";
	private static final String IS_LOGIN = "IsLoggedIn";
	private static final String USER_NAME = "";
	private static final String CURRENT_TRIP = "UserCurrentTrip";
	//private static final String USER_GCM_ID = "userGCMID";
	private static final String USER_CURRENT_RD = "UserCurrentRD";
	private static final String USER_CURRENT_STATUS = "UserCurrentStatus";
	private static final String SENDING_DATA_PERIOD = "SendingDataPeriod";
	
	public SessionManagement(Context context) {
		this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
	}
	
	// Get Login State
    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGIN, false);
    }
	
    /**
     * Create login session
     * */
	public void createLoginSession(String email) {
		editor.putBoolean(IS_LOGIN, true);
		editor.putString(USER_NAME, email);
		editor.commit();
	}
	
	public void addUserCurrentTripData(int tripID, int period) {
		editor.putInt(CURRENT_TRIP, tripID);
		editor.putInt(SENDING_DATA_PERIOD, period);
		editor.commit();
	}
	
	/*public void addUserGCMId(String gcmID) {
		editor.putString(USER_GCM_ID, gcmID);
		editor.commit();
	}*/
	
	public void addUserCurrentStatusAndRd(String status, String rd) {
		editor.putString(USER_CURRENT_STATUS, status);
		editor.putString(USER_CURRENT_RD, rd);
		editor.commit();
	}
	
	public String getUserCurrentStatus() {
		return pref.getString(USER_CURRENT_STATUS, null);
	}
	
	public String getUserCurrentRd() {
		return pref.getString(USER_CURRENT_RD, null);
	}
	
	/*public String getUserGCMId() {
		return pref.getString(USER_GCM_ID, null);
	}*/
	
	public String getUserName() {
    	return pref.getString(USER_NAME, null);
    }
	
	public int getUserTripID() {
		return pref.getInt(CURRENT_TRIP, 0);
	}
	
	public int getSendingDataPeriod() {
		return pref.getInt(SENDING_DATA_PERIOD, 0);
	}
	
	public void clearUserCurrentTrip() {
		editor.remove(CURRENT_TRIP);
		editor.remove(USER_CURRENT_STATUS);
		editor.remove(USER_CURRENT_RD);
		editor.remove(SENDING_DATA_PERIOD);
		editor.commit();
	}
	
	/**
     * Check login method wil check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     * */
	 public boolean checkLogin(){
	        // Check login status
	        if(!this.isLoggedIn()){
	            // user is not logged in redirect him to Login Activity
	            return false;
	        } else {
	        	return true;
	        }
	         
	    }
	 
	 /**
	     * Clear session details
	     * */
	    public void logoutUser(){
	        // Clearing all data from Shared Preferences
	        editor.clear();
	        editor.commit();
	    }
}
