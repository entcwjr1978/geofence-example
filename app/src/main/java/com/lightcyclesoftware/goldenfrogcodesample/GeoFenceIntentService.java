package com.lightcyclesoftware.goldenfrogcodesample;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

/**
 * Created by ewilliams on 1/8/15.
 */
public class GeoFenceIntentService extends IntentService {
    private static final String mIntentName = "GeoFenceIntentService";
    Handler mHandler;
    private static final long[] VIRBRATION_PATTERN = {0, 250, 200, 250, 150, 150, 75, 150, 75, 150};

    public GeoFenceIntentService() {
        super(mIntentName);
        mHandler = new Handler();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.e("goldenfrogcodesample", "Inside fence handler");
        GeofencingEvent mGeofencingEvent = GeofencingEvent.fromIntent(intent);

        if (MainActivity.mainActivityIsOpen()) {
            mHandler.post(new DisplayToast(this, "Geofence Event: " + getTransitionString(mGeofencingEvent.getGeofenceTransition())));
        } else {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_notify_golden_frog)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVibrate(VIRBRATION_PATTERN)
                .setContentTitle("Golden Frog Sample Notification")
                .setContentText("Geofence Event: " + getTransitionString(mGeofencingEvent.getGeofenceTransition()));
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(-1, mBuilder.build());
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
