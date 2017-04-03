package com.sftrip;

import java.util.concurrent.ExecutionException;
import org.json.JSONException;
import org.json.JSONObject;
import com.sftrip.library.FavoritePlaces;
import com.sftrip.library.SystemFunctions;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class FavoritePlaceActivity extends FragmentActivity implements
		OnItemSelectedListener, android.view.View.OnClickListener {

	private static final String addFavorPlaceUrl = "http://sit-edu4.sit.kmutt.ac.th/csc498/53270327/Boss/sftrip/index.php/place";
	private static final String updateFavorPlaceUrl = "http://sit-edu4.sit.kmutt.ac.th/csc498/53270327/Boss/sftrip/index.php/place/updateFavorPlace";
	private EditText placeNameBox;
	private Spinner saveTo;
	private Button btn_cancel;
	private Button btn_save;
	private SystemFunctions systemFunction;
	private String destination;
	private String currentUserEmail;
	private ArrayAdapter<CharSequence> adapter;
	private static String KEY_SUCCESS = "success";
	private static String KEY_ERROR = "error";
	private static String KEY_ERROR_MSG = "error_msg";
	private static final String SERVER_CONNECTION_FAILURE = "Can't connect to the server. Please try again.";
	private AlertDialog.Builder ad;
	private String placeName;
	private String lat;
	private String lng;
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog);
		init();

		btn_cancel.setOnClickListener(this);
		btn_save.setOnClickListener(this);

		adapter = ArrayAdapter.createFromResource(this, R.array.favorite,
				android.R.layout.simple_spinner_item);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		saveTo.setAdapter(adapter);

		saveTo.setOnItemSelectedListener(this);
	}

	private void init() {
		placeNameBox = (EditText) findViewById(R.id.favorite_place_name);
		saveTo = (Spinner) findViewById(R.id.favorite_taxi_color_options);
		btn_cancel = (Button) findViewById(R.id.btn_sfc_cancel);
		btn_save = (Button) findViewById(R.id.btn_sfc_save);
		systemFunction = new SystemFunctions(this);
		currentUserEmail = systemFunction.getUserEmail();
		mContext = this;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == btn_cancel) {
			finish();
		} else if (v == btn_save) {
			placeName = placeNameBox.getText().toString();
			lat = getIntent().getExtras().getDouble("lat", 0) + "";
			lng = getIntent().getExtras().getDouble("lng", 0) + "";
			if (!placeName.isEmpty()) {
				try {
					JSONObject json = new FavoritePlaces(this).execute(
							new String[] { currentUserEmail, destination,
									placeName, lat, lng, addFavorPlaceUrl })
							.get();
					response(json);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Toast.makeText(this, SERVER_CONNECTION_FAILURE, Toast.LENGTH_LONG).show();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Toast.makeText(this, SERVER_CONNECTION_FAILURE, Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(this, R.string.null_favorite_place_name,
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> adapterView, View view,
			int position, long id) {
		// TODO Auto-generated method stub
		String str = (String) adapterView.getItemAtPosition(position);
		setSelectedDestination(str);
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
	}

	private void setSelectedDestination(String destination) {
		this.destination = destination;
	}

	private void response(JSONObject json) {
		try {
			final View view = getLayoutInflater().inflate(R.layout.map, null);
			if (json.getString(KEY_SUCCESS) != null) {
				String res = json.getString(KEY_SUCCESS);
				if (Integer.parseInt(res) == 1) {
					// user successfully stored favorite place
					String message = "You have successfully stored "
							+ placeName + " in " + destination;
					ad = new AlertDialog.Builder(this);
					ad.setTitle(R.string.congratulation);
					ad.setMessage(message);
					ad.setPositiveButton(R.string.ok, new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							if (destination.equals("Favorite Place 1")) {
								view.findViewById(R.id.btn_fav1)
										.setBackgroundResource(
												R.drawable.button_selector_star);
								finish();
							} else if (destination.equals("Favorite Place 2")) {
								view.findViewById(R.id.btn_fav2)
										.setBackgroundResource(
												R.drawable.button_selector_star);
								finish();
							} else if (destination.equals("Favorite Place 3")) {
								view.findViewById(R.id.btn_fav3)
										.setBackgroundResource(
												R.drawable.button_selector_star);
								finish();
							} else {
								finish();
							}
						}
					});
					ad.show();
				} else if (json.getString(KEY_ERROR).equals("1")) {
					// Error in login
					String error_msg = json.getString(KEY_ERROR_MSG);
					ad = new AlertDialog.Builder(this);
					ad.setTitle(R.string.warning);
					ad.setIcon(android.R.drawable.btn_star_big_on);
					ad.setNegativeButton(R.string.close, new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							finish();
						}
					});
					ad.setPositiveButton(R.string.ok, new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							try {
								JSONObject json = new FavoritePlaces(mContext).execute(
										new String[] { currentUserEmail,
												destination, placeName, lat,
												lng, updateFavorPlaceUrl })
										.get();
								response(json);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (ExecutionException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					});
					ad.setMessage(error_msg);
					ad.show();
				} else {
					ad = new AlertDialog.Builder(this);
					ad.setTitle(R.string.error);
					ad.setIcon(android.R.drawable.btn_star_big_on);
					ad.setPositiveButton(R.string.close, new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							finish();
						}
					});
					ad.setMessage(R.string.something_wrong);
					ad.show();
				}
			} else {
				ad = new AlertDialog.Builder(this);
				ad.setTitle(R.string.error);
				ad.setIcon(android.R.drawable.btn_star_big_on);
				ad.setPositiveButton(R.string.close, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						finish();
					}
				});
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
