package com.lightcyclesoftware.goldenfrogcodesample;

import android.app.Activity;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

/**
 * Created by ewilliams on 1/8/15.
 */
public class GeoFenceIntentService extends IntentService {
    private static final String mIntentName = "GeoFenceIntentService";
    Handler mHandler;
    private static final long[] VIRBRATION_PATTERN = {0, 250, 200, 250, 150, 150, 75, 150, 75, 150};
    public final static String EXTRA_MESSAGE = "com.lightcyclesoftware.goldenfrogcodesample.MESSAGE";


    public GeoFenceIntentService() {
        super(mIntentName);
        mHandler = new Handler();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.e("goldenfrogcodesample", "Inside fence handler");
        GeofencingEvent mGeofencingEvent = GeofencingEvent.fromIntent(intent);
        if (mGeofencingEvent.hasError()  && mGeofencingEvent.getErrorCode() == GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE) {
            /* experimental code to handle re-adding geofence when the Location is disabled*/

            /*
            Log.e("goldenfrogcodesample", "Geofence is gone connectivity is most likely gone");
            Intent service = new Intent(this, LocationService.class);
            service.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startService(service);
            return;
            */
        }

        //Intent used to open the app from the Notification
        Intent mNewIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, -1, mNewIntent, PendingIntent.FLAG_ONE_SHOT);
        Location mLocation = mGeofencingEvent.getTriggeringLocation();


        if (mGeofencingEvent.getGeofenceTransition() == Geofence.GEOFENCE_TRANSITION_EXIT) {

        if (MainActivity.mainActivityIsOpen()) {
            //We're in the app, display a Toast
            //mHandler.post(new DisplayToast(this, "Geofence Event: " + getTransitionString(mGeofencingEvent.getGeofenceTransition())));
            mHandler.post(new DisplayToast(this, getResources().getString(R.string.one_mile_message)));
        } else {
            //We're outside of the app, create a Notification
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.ic_stat_notify_golden_frog)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setVibrate(VIRBRATION_PATTERN)
                    .setContentTitle("Golden Frog Sample Notification")
                    .setColor(Color.argb(255, 138, 171, 61))
                    .setContentText(getResources().getString(R.string.one_mile_message));
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(-1, mBuilder.build());
        }

            //Send the Geofence data back to the MainActivity

            //Intent for passing location data back to MainActivity when we leave the Geofence
            Intent mIntent = new Intent(MainActivity.class.getName());
            mIntent.putExtra(EXTRA_MESSAGE, mLocation);

            /*Send a message to the BroadcastReceiver in MainActivity.  If we can't send a message successfully to
                the BroadcastReceiver in MainActivity, then it must be in a state where the receive is disabled.
                If it is disables, save the Geofence Exit Lat and Lon to the SharedPreferences, where MainActivity
                can use these values when it is re-opened.
            */

            if (!LocalBroadcastManager.getInstance(this).sendBroadcast(mIntent)) {
                    SharedPreferences mySharedPreferences = getSharedPreferences(MainActivity.class.getName(), Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = mySharedPreferences.edit();
                    String mStorageString = null;
                    mStorageString = Double.toString(mLocation.getLatitude());
                    editor.putString("mGeofenceCrossingMarkerLat", mStorageString);
                    mStorageString = Double.toString(mLocation.getLongitude());
                    editor.putString("mGeofenceCrossingMarkerLon", mStorageString);
                    editor.commit();
            }
        }
    }

        /**
        * Maps geofence transition types to their human-readable equivalents.
        * @param transitionType A transition type constant defined in Geofence
        * @return A String indicating the type of transition
        */
        private String getTransitionString(int transitionType) {
        switch (transitionType) {

            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return "GEOFENCE_TRANSITION_ENTER";

            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return "GEOFENCE_TRANSITION_EXIT";

            case Geofence.GEOFENCE_TRANSITION_DWELL:
                return "GEOFENCE_TRANSITION_DWELL";

            default:
                return "GEOFENCE_TRANSITION_UNKNOWN";
        }
    }
}
