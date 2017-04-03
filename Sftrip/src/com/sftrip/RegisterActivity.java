/**
 * Author: Ravi Tamada
 * URL: www.androidhive.info
 * twitter: http://twitter.com/ravitamada
 * */
package com.sftrip;

import java.util.concurrent.ExecutionException;
import org.json.JSONException;
import org.json.JSONObject;
import com.sftrip.library.SystemFunctions;
import com.sftrip.library.UserFunctions;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends Activity implements
		android.view.View.OnClickListener {
	private static final String REQUIRE_EMAIL = "Email is required";
	private static final String REQUIRE_PASSWORD = "Password is required";
	private static final String REQUIRE_FIRSTNAME = "Your First name is required";
	private static final String REQUIRE_LASTNAME = "Your Lastname is required";
	private static final String REQUIRE_MOBILEPHONE = "Your Mobile phone is required";
	private static final String REQUIRE_CONFIRM_PASSWORD = "Confirm password is required";
	private static final String PASSWORD_CONFIRMATION_FAILURE = "Password and confirm password are not same";
	private Button btnRegister;
	private String email;
	private String password;
	private String confirm_password;
	private String firstname;
	private String lastname;
	private String mobilePhone;
	private EditText regis_email;
	private EditText regis_password;
	private EditText regis_firstName;
	private EditText regis_lastName;
	private EditText regis_mobilePhone;
	private EditText confirm_regis_password;
	private AlertDialog.Builder ad;
	private Context mContext;
	private SystemFunctions systemFunctions;

	private void init() {
		mContext = this;
		btnRegister = (Button) findViewById(R.id.btn_next);
		regis_email = (EditText) findViewById(R.id.regis_email);
		regis_password = (EditText) findViewById(R.id.regis_password);
		regis_firstName = (EditText) findViewById(R.id.regis_firstName);
		regis_lastName = (EditText) findViewById(R.id.regis_lastName);
		regis_mobilePhone = (EditText) findViewById(R.id.regis_mobilePhone);
		confirm_regis_password = (EditText)findViewById(R.id.comfirm_regis_pass);
		systemFunctions = new SystemFunctions(this);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);
		init();

		if (systemFunctions.isUserOnline()) {
			// Register Button Click event
			btnRegister.setOnClickListener(this);
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
		if (v == btnRegister) {
			email = regis_email.getText().toString();
			password = regis_password.getText().toString();
			confirm_password = confirm_regis_password.getText().toString();
			firstname = regis_firstName.getText().toString();
			lastname = regis_lastName.getText().toString();
			mobilePhone = regis_mobilePhone.getText().toString();
			if (email.isEmpty()) {
				regis_email.setError(REQUIRE_EMAIL);
			} else if (password.isEmpty()) {
				regis_password.setError(REQUIRE_PASSWORD);
			} else if (confirm_password.isEmpty()){
				confirm_regis_password.setError(REQUIRE_CONFIRM_PASSWORD);
			} else if (firstname.isEmpty()) {
				regis_firstName.setError(REQUIRE_FIRSTNAME);
			} else if (lastname.isEmpty()) {
				regis_lastName.setError(REQUIRE_LASTNAME);
			} else if (mobilePhone.isEmpty()) {
				regis_mobilePhone.setError(REQUIRE_MOBILEPHONE);
			} else if (systemFunctions.isValidEmail(email) == false) {
				regis_email.setError("The email is not in valid form");
			} else if (password.equalsIgnoreCase(confirm_password) == false){
				Toast.makeText(mContext, PASSWORD_CONFIRMATION_FAILURE, Toast.LENGTH_LONG).show();
			} else {
				Intent intent = new Intent(RegisterActivity.this, RegisterActivity2.class);
				intent.putExtra("email", email);
				intent.putExtra("password", password);
				intent.putExtra("firstname", firstname);
				intent.putExtra("lastname", lastname);
				intent.putExtra("mobilePhone", mobilePhone);
				startActivity(intent);
			}
		}
	}
}
