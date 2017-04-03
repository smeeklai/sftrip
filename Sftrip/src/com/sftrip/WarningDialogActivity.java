package com.sftrip;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class WarningDialogActivity extends Activity implements OnClickListener {

	private TextView userStatus;
	private TextView rd;
	private Button btn_ignore;
	private Button btn_alert;
	private Vibrator vibrator;
	private MediaPlayer mp1;
	
	private void init() {
		userStatus = (TextView)findViewById(R.id.text_warn_user_status);
		rd = (TextView)findViewById(R.id.text_warn_remaining_distance);
		btn_ignore = (Button)findViewById(R.id.btn_ignore);
		btn_alert = (Button)findViewById(R.id.btn_Alert);
		String status = getIntent().getExtras().getString("userStatus");
		String remainingDistance = getIntent().getExtras().getString("remainingDistance");
		userStatus.setText(status);
		rd.setText(remainingDistance + " meters");
		vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
		mp1 = MediaPlayer.create(this, R.raw.door_bell_cut);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.warn_dialog);
		init();
		long[] pattern = {0, 500, 200, 500, 200};
		mp1.start();
		vibrator.vibrate(pattern, -1);
		btn_ignore.setOnClickListener(this);
		btn_alert.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v == btn_alert) {
			Intent intent = new Intent(this, AlertActivity.class);
			startActivity(intent);
			finish();
		} else {
			finish();
		}
	}

}
