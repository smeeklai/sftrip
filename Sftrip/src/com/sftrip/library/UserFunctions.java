package com.sftrip.library;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

public class UserFunctions {

	// constructor
	public UserFunctions() {
	}

	public void createUserLoginSession(Context context, String email) {
		SessionManagement sm = new SessionManagement(context);
		sm.createLoginSession(email);
	}

	public boolean isUserLoggedIn(Context context) {
		SessionManagement sm = new SessionManagement(context);
		boolean checkStatus = sm.checkLogin();
		if (checkStatus == true) {
			// user logged in
			return true;
		} else {
			return false;
		}
	}

	/*public boolean isUserGCMIdInPrefs(Context context) {
		SessionManagement sm = new SessionManagement(context);
		if (sm.getUserGCMId() == null) {
			return false;
		} else {
			return true;
		}
	}*/
	
	/*public void storeUserGcmId(Context context, String userGcmId) {
		SessionManagement sm = new SessionManagement(context);
		sm.addUserGCMId(userGcmId);
	}*/

	public void logoutUser(Context context) {
		SessionManagement sm = new SessionManagement(context);
		sm.logoutUser();
	}

}
