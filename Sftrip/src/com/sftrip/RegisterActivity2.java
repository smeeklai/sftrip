package com.sftrip;


import java.util.concurrent.ExecutionException;
import org.json.JSONException;
import org.json.JSONObject;
import com.sftrip.library.Register;
import com.sftrip.library.SystemFunctions;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity2 extends Activity implements
		android.view.View.OnClickListener {

	private static String REQUIRE_RELATIVE_MOBILENUM1 = "Mobile number 1 is required";
	private static final String SERVER_CONNECTION_FAILURE = "Can't connect to the server. Please try again.";
	private static String KEY_SUCCESS = "success";
	private static String KEY_ERROR = "error";
	private static String KEY_ERROR_MSG = "error_msg";
	private static String TAG = "Register Activity 2";
	private SystemFunctions systemFunctions;
	private AlertDialog.Builder ad;
	private Context mContext;
	private EditText edit_relative_mobileNum1;
	private EditText edit_relative_mobileNum2;
	private EditText edit_relative_mobileNum3;
	private Button btn_register;
	private String mobileNum1;
	private String mobileNum2;
	private String mobileNum3;
	private String email;
	private String password;
	private String firstname;
	private String lastname;
	private String mobilePhone;

	private void init() {
		mContext = this;
		systemFunctions = new SystemFunctions(this);
		edit_relative_mobileNum1 = (EditText) findViewById(R.id.edit_rela_mo_num1);
		edit_relative_mobileNum2 = (EditText) findViewById(R.id.edit_rela_mo_num2);
		edit_relative_mobileNum3 = (EditText) findViewById(R.id.edit_rela_mo_num3);
		btn_register = (Button) findViewById(R.id.btn_finish);
		email = getIntent().getExtras().getString("email", null);
		password = getIntent().getExtras().getString("password", null);
		firstname = getIntent().getExtras().getString("firstname", null);
		lastname = getIntent().getExtras().getString("lastname", null);
		mobilePhone = getIntent().getExtras().getString("mobilePhone", null);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register2);
		init();
		if(systemFunctions.servicesConnected(this)) {
			btn_register.setOnClickListener(this);
		} else {
			Log.e(TAG, "No valid Google Play Services APK found.");
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (systemFunctions.isUserOnline()) {
			mobileNum1 = edit_relative_mobileNum1.getText().toString();
			mobileNum2 = edit_relative_mobileNum2.getText().toString();
			mobileNum3 = edit_relative_mobileNum3.getText().toString();
			if (mobileNum1.isEmpty()) {
				edit_relative_mobileNum1.setError(REQUIRE_RELATIVE_MOBILENUM1);
			}  else {
				Register register = new Register(mContext);
				try {
					JSONObject json = register.execute(
							new String[] { email, password, firstname,
									lastname, mobilePhone, mobileNum1,
									mobileNum2, mobileNum3}).get();
					checkJsonResult(json);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODOAuto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private void checkJsonResult(JSONObject json) {
		try {
			if (json.getString(KEY_SUCCESS) != null) {
				String res = json.getString(KEY_SUCCESS);
				if (Integer.parseInt(res) == 1) {
					// user successfully stored favorite place
					String message = "You have successfully registered";
					ad = new AlertDialog.Builder(this);
					ad.setTitle(R.string.congratulation);
					ad.setMessage(message);
					ad.setPositiveButton(R.string.ok, new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							Intent intent = new Intent(RegisterActivity2.this,
									TutorialActivity.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
							finish();
						}
					});
					ad.show();
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

}
