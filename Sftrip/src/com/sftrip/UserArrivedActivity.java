package com.sftrip;

import com.sftrip.library.SystemFunctions;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class UserArrivedActivity extends Activity {
	
	private Button btn_arrived_finish;
	private SystemFunctions systemFunctions;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_arrived);
		systemFunctions = new SystemFunctions(this);
		btn_arrived_finish = (Button)findViewById(R.id.btn_arrived_finish);
		btn_arrived_finish.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				systemFunctions.closeAllActivity();
				finish();
			}
		});
	}

}
