package com.sftrip;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sftrip.library.FavoritePlaces2;
import com.sftrip.library.PlacesAutoCompleteAdapter;
import com.sftrip.library.SystemFunctions;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

public class MapActivity extends FragmentActivity implements LocationListener,
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener,
		android.view.View.OnClickListener, OnMapClickListener,
		OnItemClickListener, OnMapLongClickListener, OnInfoWindowClickListener,
		android.location.LocationListener, OnMarkerClickListener {

	public static final int UPDATE_INTERVAL_IN_SECONDS = 10;
	private static final String FAVOR_PLACE1 = "Favorite Place 1";
	private static final String FAVOR_PLACE2 = "Favorite Place 2";
	private static final String FAVOR_PLACE3 = "Favorite Place 3";
	private static String KEY_SUCCESS = "success";
	private static String KEY_ERROR = "error";
	private static String KEY_ERROR_MSG = "error_msg";
	private static final String SERVER_CONNECTION_FAILURE = "Can't connect to the server. Please try again.";
	private static final String GOOGLE_SERVICE_PROBLEM = "Sftrip cannot start due to the Google Service Problem";
	private GoogleMap map;
	private LocationClient locMan;
	private Location lastLoc;
	private LocationManager manager;
	private Marker userMarker;
	private Marker userDestinationMarker;
	private Marker candidateFavoritePlaceMarker;
	private LatLng userCurrentLocation;
	private LatLng userDestinationLocation;
	private SystemFunctions systemFunctions;
	private Activity activity;
	private Geocoder geocoder;
	private Button btnCurrentLocation;
	private ImageView btnSearch;
	private AutoCompleteTextView autoCompView;
	private AlertDialog.Builder ad;
	private Context mContext;
	private SlidingDrawer slidingDrawer;
	private String destinationPlaceDetail;
	private String userPlaceDetial;
	private Button btn_fav_place1;
	private Button btn_fav_place2;
	private Button btn_fav_place3;
	private ArrayList<String> markerID;

	public void init() {
		systemFunctions = new SystemFunctions(this);
		activity = this;
		locMan = new LocationClient(this, this, this);
		manager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
		btnCurrentLocation = (Button) findViewById(R.id.btn_Location);
		btnSearch = (ImageView) findViewById(R.id.handle);
		geocoder = new Geocoder(this, Locale.getDefault());
		autoCompView = (AutoCompleteTextView) findViewById(R.id.searchPlaceAutoComplete_txt);
		mContext = this;
		userMarker = null;
		userDestinationMarker = null;
		candidateFavoritePlaceMarker = null;
		slidingDrawer = (SlidingDrawer) findViewById(R.id.drawer);
		btn_fav_place1 = (Button) findViewById(R.id.btn_fav1);
		btn_fav_place2 = (Button) findViewById(R.id.btn_fav2);
		btn_fav_place3 = (Button) findViewById(R.id.btn_fav3);
		markerID = new ArrayList<String>();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getIntent().getExtras().getBoolean("Exits", false) == true) {
			finish();
		}
		setContentView(R.layout.map);
		init();
		if (systemFunctions.servicesConnected(activity)) {
			if (isMapLoaded() == false) {
				createMap();
			}
			if (getIntent().getExtras().getBoolean("IsFB1HasInfo", false) == true) {
				markFavoritePlaceLocation(1);
			}
			if (getIntent().getExtras().getBoolean("IsFB2HasInfo", false) == true) {
				markFavoritePlaceLocation(2);
			}
			if (getIntent().getExtras().getBoolean("IsFB3HasInfo", false) == true) {
				markFavoritePlaceLocation(3);
			}

			autoCompView.setAdapter(new PlacesAutoCompleteAdapter(this,
					R.layout.list_item));
			autoCompView.setOnItemClickListener(this);
			btnCurrentLocation.setOnClickListener(this);
			btnSearch.setOnClickListener(this);
			btn_fav_place1.setOnClickListener(this);
			btn_fav_place2.setOnClickListener(this);
			btn_fav_place3.setOnClickListener(this);
		} else {
			Toast.makeText(this, GOOGLE_SERVICE_PROBLEM, Toast.LENGTH_SHORT)
					.show();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		locMan.connect();
	}

	@Override
	protected void onStop() {
		if (locMan.isConnected()) {
			locMan.removeLocationUpdates(this);
		}
		locMan.disconnect();
		super.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (systemFunctions.servicesConnected(activity)) {
			if (isMapLoaded() == false) {
				createMap();
			}
		} else {
			Toast.makeText(this, GOOGLE_SERVICE_PROBLEM, Toast.LENGTH_SHORT)
					.show();
		}
	}

	private double userLat;
	private double userLng;
	private LatLng lastLatLng;

	public LatLng getUserCurrentLocation(Location currentBestLocation,
			boolean useRecievedLocation) {
		if (useRecievedLocation == true) {
			Log.e("Map", "Get user's current location");
			lastLoc = currentBestLocation;
			Log.e("locman", locMan.toString());
			Log.e("lat", lastLoc.getLatitude() + "");
			Log.e("lng", lastLoc.getLongitude() + "");
			Log.e("provider", lastLoc.getProvider());
			userLat = lastLoc.getLatitude();
			userLng = lastLoc.getLongitude();
			lastLatLng = new LatLng(userLat, userLng);
		} else {
			Log.e("Map", "Get user's current location");
			lastLoc = locMan.getLastLocation();
			Log.e("locman", locMan.toString());
			Log.e("lat", lastLoc.getLatitude() + "");
			Log.e("lng", lastLoc.getLongitude() + "");
			Log.e("provider", lastLoc.getProvider());
			userLat = lastLoc.getLatitude();
			userLng = lastLoc.getLongitude();
			lastLatLng = new LatLng(userLat, userLng);
		}
		return lastLatLng;
	}

	private boolean isMapLoaded() {
		if (map == null) {
			return false;
		} else {
			return true;
		}
	}

	private void createMap() {
		map = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map)).getMap();
		if (isMapLoaded() == true) {
			map.getUiSettings().setCompassEnabled(false);
			map.getUiSettings().setRotateGesturesEnabled(false);
			map.setOnMapClickListener(this);
			map.setOnMapLongClickListener(this);
			map.setInfoWindowAdapter(new CustomInfoWindowAdapter());
			map.setOnInfoWindowClickListener(this);
			map.setOnMarkerClickListener(this);
		}
	}

	private void markUserCurrentLocation(LatLng userLocation, String placeDetial) {
		if (userMarker != null) {
			userMarker.remove();
		}
		userMarker = map.addMarker(new MarkerOptions().position(userLocation)
				.title(placeDetial));
		markerID.add(userMarker.getId());
	}

	private void markUserDestinationLocation(LatLng destinationLocation,
			String placeDetial) {
		if (userDestinationMarker != null) {
			userDestinationMarker.remove();
		}
		userDestinationMarker = map.addMarker(new MarkerOptions().position(
				destinationLocation).title(placeDetial)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.des_icon)));
		userDestinationMarker.showInfoWindow();

	}

	private void markFavoritePlaceLocation(int position) {
		if (position == 1) {
			btn_fav_place1.setBackgroundResource(R.drawable.btn_fav);
			Double latitude = getIntent().getExtras().getDouble("FP1Lat", 0);
			Log.e("FP1 lat", latitude + "");
			Double longitude = getIntent().getExtras().getDouble("FP1Lng", 0);
			Log.e("FP1 lat", longitude + "");
			String title = getIntent().getExtras().getString("FP1Title", "");
			LatLng favoritePlace1 = new LatLng(latitude, longitude);
			Marker favMarker1 = map.addMarker(new MarkerOptions()
					.position(favoritePlace1)
					.title(title)
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.fav_red)));
			markerID.add(favMarker1.getId());
		} else if (position == 2) {
			btn_fav_place2.setBackgroundResource(R.drawable.btn_fav2);
			Double latitude = getIntent().getExtras().getDouble("FP2Lat", 0);
			Log.e("FP2 lat", latitude + "");
			Double longitude = getIntent().getExtras().getDouble("FP2Lng", 0);
			Log.e("FP2 lat", longitude + "");
			String title = getIntent().getExtras().getString("FP2Title", "");
			LatLng favoritePlace2 = new LatLng(latitude, longitude);
			Marker favMarker2 = map.addMarker(new MarkerOptions()
					.position(favoritePlace2)
					.title(title)
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.fav_yel)));
			markerID.add(favMarker2.getId());
		} else if (position == 3) {
			btn_fav_place3.setBackgroundResource(R.drawable.btn_fav3);
			Double latitude = getIntent().getExtras().getDouble("FP3Lat", 0);
			Log.e("FP3 lat", latitude + "");
			Double longitude = getIntent().getExtras().getDouble("FP3Lng", 0);
			Log.e("FP3 lat", longitude + "");
			String title = getIntent().getExtras().getString("FP3Title", "");
			LatLng favoritePlace3 = new LatLng(latitude, longitude);
			Marker favMarker3 = map.addMarker(new MarkerOptions()
					.position(favoritePlace3)
					.title(title)
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.fav_green)));
			markerID.add(favMarker3.getId());
		}
	}

	private void markCandidateFavoritePlaceLocation(LatLng favoritePlace) {
		if (candidateFavoritePlaceMarker != null) {
			candidateFavoritePlaceMarker.remove();
		}
		candidateFavoritePlaceMarker = map.addMarker(new MarkerOptions()
				.position(favoritePlace)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.fav_gray)));
		markerID.add(candidateFavoritePlaceMarker.getId());
	}

	private void moveCameraToLocation(LatLng userLocation) {
		map.animateCamera(
				CameraUpdateFactory.newLatLngZoom(userLocation, 17.0f), 3000,
				null);
	}

	public void setUserCurrentLocation(LatLng currentLocation) {
		userCurrentLocation = currentLocation;
	}

	public void setUserDestinationLocation(LatLng destinationLocation) {
		userDestinationLocation = destinationLocation;
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		Log.e("LocationChanged", "Location Changed");
		if (isBetterLocation(location, lastLoc)) {
			Log.e("LocationChanged", "Better location");
			userCurrentLocation = getUserCurrentLocation(location, true);
			setUserCurrentLocation(userCurrentLocation);
			userPlaceDetial = getDetailPlaceFromLatLng(userLat, userLng);
			markUserCurrentLocation(userCurrentLocation, userPlaceDetial);
			moveCameraToLocation(userCurrentLocation);
			Log.e("CurrentBestLocation", location.getProvider());
			Log.e("lastLoc", lastLoc.getProvider());
			Log.e("CurrentBestLocation", location.getAccuracy() + "");
			Log.e("lastLoc", lastLoc.getAccuracy() + "");
			Log.e("CurrentBestLocationLat", location.getLatitude() + "");
			Log.e("CurrentBestLocationLng", location.getLongitude() + "");
			// manager.removeUpdates(this);
			// manager.removeUpdates(this);
		} else {
			Log.e("LocationChanged",
					"new location is not better than the old one");
		}
		manager.removeUpdates(this);
		manager.removeUpdates(this);
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		if (systemFunctions.isUserOnline()) {
			if (systemFunctions.isUserEnabledGPS() == true) {
				if (locMan.getLastLocation() == null) {
					if (manager
							.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
						manager.requestLocationUpdates(
								LocationManager.GPS_PROVIDER, 0, 20, this);
						manager.requestLocationUpdates(
								LocationManager.NETWORK_PROVIDER, 0, 20, this);
					} else {
						ad = new AlertDialog.Builder(this);
						ad.setTitle(R.string.warning);
						ad.setMessage(R.string.enable_google_location_service);
						ad.setIcon(android.R.drawable.ic_dialog_alert);
						ad.setNegativeButton(R.string.cancel,
								new OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										finish();
									}
								});
						ad.setPositiveButton(R.string.settings,
								new OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										systemFunctions
												.enableGoogleLocationService(mContext);
									}
								});
						ad.show();
					}
				} else {
					userCurrentLocation = getUserCurrentLocation(null, false);
					setUserCurrentLocation(userCurrentLocation);
					userPlaceDetial = getDetailPlaceFromLatLng(userLat, userLng);
					//userPlaceDetial = "test";
					markUserCurrentLocation(userCurrentLocation,
							userPlaceDetial);
					moveCameraToLocation(userCurrentLocation);
					manager.requestLocationUpdates(
							LocationManager.GPS_PROVIDER, 0, 20, this);
					manager.requestLocationUpdates(
							LocationManager.NETWORK_PROVIDER, 0, 20, this);
				}

			} else {
				ad = new AlertDialog.Builder(this);
				ad.setTitle(R.string.warning);
				ad.setMessage(R.string.enable_GPS);
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
						systemFunctions.enableGPSDialog(mContext);
					}
				});
				ad.show();
			}
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

	private static final int TWO_MINUTES = 1000 * 60 * 2;

	/**
	 * Determines whether one Location reading is better than the current
	 * Location fix
	 * 
	 * @param location
	 *            The new Location that you want to evaluate
	 * @param currentBestLocation
	 *            The current Location fix, to which you want to compare the new
	 *            one
	 */
	protected boolean isBetterLocation(Location location,
			Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use
		// the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than two minutes older, it must be
			// worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation
				.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(),
				currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and
		// accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate
				&& isFromSameProvider) {
			return true;
		}
		return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClick(View v) {
		if (v == btnCurrentLocation) {
			if (systemFunctions.isUserOnline()
					&& systemFunctions.isUserEnabledGPS()) {
				userCurrentLocation = getUserCurrentLocation(null, false);
				setUserCurrentLocation(userCurrentLocation);
				userPlaceDetial = getDetailPlaceFromLatLng(userLat, userLng);
				markUserCurrentLocation(userCurrentLocation, userPlaceDetial);
				moveCameraToLocation(userCurrentLocation);
				manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
						20, this);
				manager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER, 0, 20, this);

			} else {
				systemFunctions.enableGPSDialog(this);
			}
		} else if (v == btn_fav_place1) {
			if (systemFunctions.isUserOnline()) {
				try {
					JSONObject json = new FavoritePlaces2(this).execute(
							new String[] { systemFunctions.getUserEmail(),
									FAVOR_PLACE1 }).get();
					checkFavorPlace(json);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				systemFunctions.enableNetworkDialog(mContext);
			}
		} else if (v == btn_fav_place2) {
			if (systemFunctions.isUserOnline()) {
				try {
					JSONObject json = new FavoritePlaces2(this).execute(
							new String[] { systemFunctions.getUserEmail(),
									FAVOR_PLACE2 }).get();
					checkFavorPlace(json);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				systemFunctions.enableNetworkDialog(mContext);
			}
		} else if (v == btn_fav_place3) {
			if (systemFunctions.isUserOnline()) {
				try {
					JSONObject json = new FavoritePlaces2(this).execute(
							new String[] { systemFunctions.getUserEmail(),
									FAVOR_PLACE3 }).get();
					checkFavorPlace(json);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				systemFunctions.enableNetworkDialog(mContext);
			}
		}
	}

	private void checkFavorPlace(JSONObject json) {
		try {
			if (json.getString(KEY_SUCCESS) != null) {
				String res = json.getString(KEY_SUCCESS);
				if (Integer.parseInt(res) == 1) {
					// user successfully stored favorite place
					destinationPlaceDetail = json.getString("placeName");
					double userDesLat = Double.parseDouble(json
							.getString("latitude"));
					double userDesLng = Double.parseDouble(json
							.getString("longtitude"));
					Intent intent = new Intent(MapActivity.this,
							TripActivity.class);
					intent.putExtra("destinationName", destinationPlaceDetail);
					intent.putExtra("currentPlaceName", userPlaceDetial);
					intent.putExtra("userCurrentLat", userLat);
					intent.putExtra("userCurrentLng", userLng);
					intent.putExtra("userDestinationLat", userDesLat);
					intent.putExtra("userDestinationLng", userDesLng);
					startActivity(intent);
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
			Toast.makeText(this, SERVER_CONNECTION_FAILURE, Toast.LENGTH_LONG)
					.show();
		}
	}

	public LatLng searchLocation(String locationName) {
		LatLng location = null;
		try {
			List<Address> addresses = geocoder.getFromLocationName(
					locationName, 1);
			if (addresses.size() == 0) {
				location = null;
			} else {
				double lat = addresses.get(0).getLatitude();
				double lng = addresses.get(0).getLongitude();
				location = new LatLng(lat, lng);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		return location;
	}

	private String getDetailPlaceFromLatLng(double lat, double lng) {
		String locality = null;
		try {
			List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
			if (!addresses.isEmpty()) {
				locality = addresses.get(0).getAddressLine(1);
				return locality;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ad = new AlertDialog.Builder(this);
			ad.setTitle(R.string.error);
			ad.setIcon(android.R.drawable.btn_star_big_on);
			ad.setNegativeButton(R.string.cancel, new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					finish();
				}
			});
			ad.setMessage(R.string.restart_app);
			ad.show();
			// return null;
		}
		return null;
	}

	@Override
	public void onMapClick(LatLng point) {
		// TODO Auto-generated method stub
		double lat = point.latitude;
		double lng = point.longitude;
		destinationPlaceDetail = getDetailPlaceFromLatLng(lat, lng);
		markUserDestinationLocation(point, destinationPlaceDetail);
		setUserDestinationLocation(point);

	}

	@Override
	public void onMapLongClick(LatLng point) {
		// TODO Auto-generated method stub
		markCandidateFavoritePlaceLocation(point);
		double lat = point.latitude;
		double lng = point.longitude;
		Intent intent = new Intent(this, FavoritePlaceActivity.class);
		intent.putExtra("lat", lat);
		intent.putExtra("lng", lng);
		startActivity(intent);
	}

	@Override
	public void onInfoWindowClick(Marker marker) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, TripActivity.class);
		intent.putExtra("destinationName", destinationPlaceDetail);
		intent.putExtra("currentPlaceName", userPlaceDetial);
		intent.putExtra("userCurrentLat", userLat);
		intent.putExtra("userCurrentLng", userLng);
		intent.putExtra("userDestinationLat", userDestinationLocation.latitude);
		intent.putExtra("userDestinationLng", userDestinationLocation.longitude);
		startActivity(intent);
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view,
			int position, long id) {
		// TODO Auto-generated method stub
		if (systemFunctions.isUserOnline()
				&& systemFunctions.isUserEnabledGPS()) {
			destinationPlaceDetail = (String) adapterView
					.getItemAtPosition(position);
			slidingDrawer.close();
			if (destinationPlaceDetail.isEmpty()) {
				Toast.makeText(mContext, R.string.null_input, Toast.LENGTH_LONG)
						.show();
			} else {
				LatLng location = searchLocation(destinationPlaceDetail);
				if (location == null) {
					Toast.makeText(this, "Can't find this location. Please try other similar suggested places",
							Toast.LENGTH_LONG).show();
				} else {
					setUserDestinationLocation(location);
					markUserDestinationLocation(location,
							destinationPlaceDetail);
					moveCameraToLocation(location);
				}
			}
		} else {
			systemFunctions.enableGPSDialog(this);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map_options, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.logout:
			systemFunctions.logout();
			Intent mainAc = new Intent(MapActivity.this, MainActivity.class);
			startActivity(mainAc);
			finish();
			break;
		case R.id.tutorial:
			startActivity(new Intent(MapActivity.this, TutorialActivity.class));
			finish();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return super.onOptionsItemSelected(item);
	}

	/** Demonstrates customizing the info window and/or its contents. */
	class CustomInfoWindowAdapter implements InfoWindowAdapter {

		private final View mContents;

		CustomInfoWindowAdapter() {
			mContents = getLayoutInflater().inflate(
					R.layout.custom_info_contents, null);
		}

		@Override
		public View getInfoContents(Marker marker) {
			// TODO Auto-generated method stub
			render(marker, mContents);
			return mContents;

		}

		@Override
		public View getInfoWindow(Marker marker) {
			// TODO Auto-generated method stub
			return null;
		}

		private void render(Marker marker, View view) {
			String title = marker.getTitle();
			TextView titleUi = ((TextView) view.findViewById(R.id.title));

			if (title != null) {
				titleUi.setText(title);
			} else {
				titleUi.setText("");
			}
		}

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	
	@Override
	public boolean onMarkerClick(Marker marker) {
		// TODO Auto-generated method stub
		if (markerID.contains(marker.getId())) {
			return true;
		}
		return false;
	}
}
