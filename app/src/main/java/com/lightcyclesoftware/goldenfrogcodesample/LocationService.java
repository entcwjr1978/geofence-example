package com.lightcyclesoftware.goldenfrogcodesample;

import android.app.Activity;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;


/**
 * Created by ewilliams on 1/12/15.
 */

/* NOT IN USE*/
/*Experimental Class used to reinstall Geofence with Google Play Services is Location Services are disabled while the app is closed*/
public class LocationService extends IntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
    private static final String mIntentName = "LocationService";
    private GoogleApiClient mGoogleApiClient;
    private boolean mResolvingError = false;

    public LocationService() {
        super(mIntentName);
    }


    @Override
    public void onConnected(Bundle bundle) {
        //open Geofence data from app

        SharedPreferences mySharedPreferences = getApplicationContext().getSharedPreferences(MainActivity.class.getName(), Activity.MODE_PRIVATE);
        String restoredText;
        Double tmpLat = null;
        Double tmpLon = null;

        restoredText = mySharedPreferences.getString("mMarkerLat", null);
        if (restoredText != null) {
            tmpLat = Double.parseDouble(restoredText);
        }

        restoredText = mySharedPreferences.getString("mMarkerLon", null);
        if (restoredText != null) {
            tmpLon = Double.parseDouble(restoredText);
        }

        //if there is a
        if (tmpLat != null && tmpLon != null) {
            Log.d("goldenFrog", "creating Geofence outside of app...");
            Geofence mGeofence = new Geofence.Builder().setCircularRegion(tmpLat, tmpLon, MainActivity.METERS_PER_MILE)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setLoiteringDelay(5000)
                    .setNotificationResponsiveness(1000)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL)
                    .setRequestId("goldenFrog").build();
            GeofencingRequest mGeofencingRequest = new GeofencingRequest.Builder().addGeofence(mGeofence).build();
            PendingIntent mPendingIntent = PendingIntent.getService(this, 0,
                    new Intent(this, GeoFenceIntentService.class), PendingIntent.FLAG_UPDATE_CURRENT);
            PendingResult<Status> mPendingResult = LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, mGeofencingRequest, mPendingIntent);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("goldenFrog", "connection is Suspended...");
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (!mResolvingError) {  // more about this later
            Log.d("goldenFrog", "Reconnecting...");
            Log.d("goldenFrog", "starting LocationService");
            buildGoogleApiClient();
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
            Log.d("goldenFrog", "The connection attempt failed...");
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (connectionResult.hasResolution()) {
                mResolvingError = true;
                mGoogleApiClient.connect();
        } else {
            mResolvingError = true;
        }
    }
}

