package com.lightcyclesoftware.goldenfrogcodesample;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by ewilliams on 1/9/15.
 */
public class GoldenFrogPreferences
{
    private final LatLng mPinLatLng;
    private final CameraPosition mMapCameraPosition;
    private final int mGeofenceId;

    public GoldenFrogPreferences(
            LatLng pPinLatLng,
            CameraPosition pMapCameraPosition,
            int pGeofenceId) {
        // Set the instance fields from the constructor
        this.mPinLatLng = pPinLatLng;
        this.mMapCameraPosition = pMapCameraPosition;
        this.mGeofenceId = pGeofenceId;
    }

    // Instance field getters
    public LatLng getPinLatLng() {
        return mPinLatLng;
    }
    public CameraPosition getMapCameraPosition() {
        return mMapCameraPosition;
    }
    public int geGeofenceId() {
        return mGeofenceId;
    }
}
