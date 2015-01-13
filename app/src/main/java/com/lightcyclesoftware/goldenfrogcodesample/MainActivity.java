package com.lightcyclesoftware.goldenfrogcodesample;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status>, View.OnClickListener, GoogleMap.OnCameraChangeListener {

    private static boolean mainActivityIsOpen;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private Location mGeofenceCrossingLocation;
    private Marker mMarker;
    private Marker mGeofenceCrossingMarker;
    private MarkerOptions mMarkerOptions;
    private MarkerOptions mGeofenceCrossingMarkerOptions;
    private CameraPosition mCameraPosition;
    private Circle mCircle;
    private Button mButton;
    private boolean mResolvingError = false;

    private static final int REQUEST_RESOLVE_ERROR = 1001;
    public static final float METERS_PER_MILE = 1609.34f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivityIsOpen = true;
        mButton = (Button) findViewById(R.id.resetButton);
        mButton.setOnClickListener(this);
        setUpMapIfNeeded();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mainActivityIsOpen = true;
        IntentFilter filter = new IntentFilter(MainActivity.class.getName());
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(onGeofenceExit, filter);
        restoreAppState();
    }

    @Override
    protected void onPause() {
        mainActivityIsOpen = false;
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(onGeofenceExit);
        saveAppState();
        super.onPause();

    }


    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
        }
    }


    private void setUpMap() {
        if (mMap != null) {
            mMap.setOnCameraChangeListener(this);
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                if (!(mMarker == null)) {
                    mMarker.remove();
                }

                if (!(mGeofenceCrossingMarker == null)) {
                    mGeofenceCrossingMarker.remove();
                }

                if (!(mCircle == null)) {
                    mCircle.remove();
                }

                LatLng mLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                if (mMarkerOptions == null) {


                    mMarkerOptions = new MarkerOptions().position(mLatLng).title("Start");
                    mMarker = mMap.addMarker(mMarkerOptions);
                   /*For testing geofence radius*/
                   /*
                   mCircle = mMap.addCircle(new CircleOptions()
                           .center(mLatLng)
                           .radius(METERS_PER_MILE)
                           .strokeColor(Color.RED)
                           .fillColor(Color.argb(127, 0, 0, 255)));*/
                    // mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 16));
                } else {


                    mMarker = mMap.addMarker(mMarkerOptions);
                   /*For testing geofence radius*/
                   /*
                   mCircle = mMap.addCircle(new CircleOptions()
                           .center(mMarkerOptions.getPosition())
                           .radius(METERS_PER_MILE)
                           .strokeColor(Color.RED)
                           .fillColor(Color.argb(127, 0, 0, 255)));
                           */
                    // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mMarkerOptions.getPosition(), 16));
                }

                if (mGeofenceCrossingMarkerOptions != null) {
                    mGeofenceCrossingMarker = mMap.addMarker(mGeofenceCrossingMarkerOptions);
                }

                mMap.setMyLocationEnabled(true);

                if (mCameraPosition == null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 13));
                } else {
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
                }

                if (mGeofenceCrossingMarkerOptions == null && mainActivityIsOpen) {
                    Log.d("goldenFrog", "creating Geofence...");
                    Geofence mGeofence = new Geofence.Builder().setCircularRegion(mMarkerOptions.getPosition().latitude, mMarkerOptions.getPosition().longitude, METERS_PER_MILE)
                            .setExpirationDuration(Geofence.NEVER_EXPIRE)
                            .setLoiteringDelay(10000)
                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
                            .setRequestId("goldenFrog").build();
                    GeofencingRequest mGeofencingRequest = new GeofencingRequest.Builder().addGeofence(mGeofence).build();
                    PendingIntent mPendingIntent = PendingIntent.getService(this, 0,
                            new Intent(this, GeoFenceIntentService.class), PendingIntent.FLAG_UPDATE_CURRENT);
                    PendingResult<Status> mPendingResult = LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, mGeofencingRequest, mPendingIntent);
                    mPendingResult.setResultCallback(this);
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setUpMap();
    }

    @Override
    public void onConnected(Bundle bundle) {
        setUpMap();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (connectionResult.hasResolution()) {
            try {
                mResolvingError = true;
                connectionResult.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            mResolvingError = true;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        buildGoogleApiClient();
        if (!mResolvingError) {  // more about this later
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        saveAppState();
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        restoreAppState();
        setUpMap();
    }

    @Override
    public void onResult(Status status) {
        Log.d("goldenFrog", "Geofence status: " + status.isSuccess());
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == mButton.getId()) {
            SharedPreferences mySharedPreferences = getSharedPreferences(MainActivity.class.getName(), Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = mySharedPreferences.edit();
            editor.remove("mGeofenceCrossingLocation");
            editor.remove("mMarkerLat");
            editor.remove("mMarkerLon");
            editor.remove("mGeofenceCrossingMarkerLat");
            editor.remove("mGeofenceCrossingMarkerLon");
            editor.commit();
            mGeofenceCrossingLocation = null;
            if (mGeofenceCrossingMarker != null) {
                mGeofenceCrossingMarker.remove();
                mGeofenceCrossingMarker = null;
            }
            if (mMarker != null) {
                mMarker.remove();
                mMarker = null;
            }

            if (mCircle != null) {
                mCircle.remove();
                mCircle = null;
            }

            mMarkerOptions = null;
            mGeofenceCrossingMarkerOptions = null;
            if (mMap != null) {
                mCameraPosition = null;
            }
            setUpMap();
        }
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        mCameraPosition = cameraPosition;
    }

    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;

        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    public static boolean mainActivityIsOpen() {
        return mainActivityIsOpen;
    }

    private BroadcastReceiver onGeofenceExit = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mGeofenceCrossingLocation = (Location) intent.getParcelableExtra(GeoFenceIntentService.EXTRA_MESSAGE);
            mGeofenceCrossingMarkerOptions = new MarkerOptions().position(new LatLng(mGeofenceCrossingLocation.getLatitude(), mGeofenceCrossingLocation.getLongitude())).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title(getResources().getString(R.string.one_mile_message));
            Log.d("goldenFrog", "location updated");
            if (mMap != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mGeofenceCrossingLocation.getLatitude(), mGeofenceCrossingLocation.getLongitude()), 13));
            }
            setUpMap();
        }
    };

    private void saveAppState() {
        String mStorageString = null;
        SharedPreferences mySharedPreferences = getSharedPreferences(MainActivity.class.getName(), Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        mStorageString = mMarker != null ? Double.toString(mMarker.getPosition().latitude) : null;
        editor.putString("mMarkerLat", mStorageString);
        mStorageString = mMarker != null ? Double.toString(mMarker.getPosition().longitude) : null;
        editor.putString("mMarkerLon", mStorageString);
        mStorageString = mGeofenceCrossingMarker != null ? Double.toString(mGeofenceCrossingMarker.getPosition().latitude) : null;
        editor.putString("mGeofenceCrossingMarkerLat", mStorageString);
        mStorageString = mGeofenceCrossingMarker != null ? Double.toString(mGeofenceCrossingMarker.getPosition().longitude) : null;
        editor.putString("mGeofenceCrossingMarkerLon", mStorageString);
        if (mMap != null) {
            editor.putFloat("mMapZoomLevel", mMap.getCameraPosition().zoom);
            editor.putFloat("mMapBearing", mMap.getCameraPosition().bearing);
            editor.putFloat("mMapTilt", mMap.getCameraPosition().tilt);
        }
        mStorageString = mMap != null ? Double.toString(mMap.getCameraPosition().target.latitude) : null;
        editor.putString("mMapTargetLat", mStorageString);
        mStorageString = mMap != null ? Double.toString(mMap.getCameraPosition().target.longitude) : null;
        editor.putString("mMapTargetLon", mStorageString);
        editor.commit();
    }

    private void restoreAppState() {
        SharedPreferences mySharedPreferences = getApplicationContext().getSharedPreferences(MainActivity.class.getName(), Activity.MODE_PRIVATE);
        String restoredText = mySharedPreferences.getString("mGeofenceCrossingLocation", null);
        Double tmpLat = null;
        Double tmpLon = null;

        mMarkerOptions = null;
        mGeofenceCrossingMarkerOptions = null;

        restoredText = mySharedPreferences.getString("mMarkerLat", null);
        if (restoredText != null) {
            tmpLat = Double.parseDouble(restoredText);
        }

        restoredText = mySharedPreferences.getString("mMarkerLon", null);
        if (restoredText != null) {
            tmpLon = Double.parseDouble(restoredText);
        }

        if (tmpLat != null && tmpLon != null) {
            mMarkerOptions = new MarkerOptions().position(new LatLng(tmpLat, tmpLon)).title("Start");
        }

        tmpLat = null;
        tmpLon = null;

        restoredText = mySharedPreferences.getString("mGeofenceCrossingMarkerLat", null);
        if (restoredText != null) {
            tmpLat = Double.parseDouble(restoredText);
        }

        restoredText = mySharedPreferences.getString("mGeofenceCrossingMarkerLon", null);
        if (restoredText != null) {
            tmpLon = Double.parseDouble(restoredText);
        }

        if (tmpLat != null && tmpLon != null) {
            mGeofenceCrossingMarkerOptions = new MarkerOptions().position(new LatLng(tmpLat, tmpLon)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title(getResources().getString(R.string.one_mile_message));
        }

        Float tmpZoom = null;
        Float tmpBearing = null;
        Float tmpTilt = null;
        tmpLat = null;
        tmpLon = null;

        tmpZoom = mySharedPreferences.getFloat("mMapZoomLevel", -1);
        tmpBearing = mySharedPreferences.getFloat("mMapBearing", -1);
        tmpTilt = mySharedPreferences.getFloat("mMapTilt", -1);

        restoredText = mySharedPreferences.getString("mMapTargetLat", null);
        if (restoredText != null) {
            tmpLat = Double.parseDouble(restoredText);
        }

        restoredText = mySharedPreferences.getString("mMapTargetLon", null);
        if (restoredText != null) {
            tmpLon = Double.parseDouble(restoredText);
        }

        if (tmpLat != null && tmpLon != null && tmpZoom != -1 && tmpBearing != -1 && tmpTilt != -1) {
            mCameraPosition = CameraPosition.builder().zoom(tmpZoom).bearing(tmpBearing).tilt(tmpTilt).target(new LatLng(tmpLat, tmpLon)).build();
        }
    }
}
