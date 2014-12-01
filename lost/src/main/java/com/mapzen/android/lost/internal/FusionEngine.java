package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationRequest;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;

public class FusionEngine implements LocationListener {
    private static final String TAG = FusionEngine.class.getSimpleName();

    /** Location updates more than 60 seconds old are considered stale. */
    public static final int RECENT_UPDATE_THRESHOLD_IN_MILLIS = 60 * 1000;

    private final Callback callback;
    private final LocationManager locationManager;

    static Clock clock = new SystemClock();

    public FusionEngine(Context context, Callback callback) {
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.callback = callback;
    }

    public Location getLastLocation() {
        final List<String> providers = locationManager.getAllProviders();
        final long minTime = clock.getCurrentTimeInMillis() - RECENT_UPDATE_THRESHOLD_IN_MILLIS;

        Location bestLocation = null;
        float bestAccuracy = Float.MAX_VALUE;
        long bestTime = Long.MIN_VALUE;

        for (String provider : providers) {
            final Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                final float accuracy = location.getAccuracy();
                final long time = location.getTime();
                if (time > minTime && accuracy < bestAccuracy) {
                    bestLocation = location;
                    bestAccuracy = accuracy;
                    bestTime = time;
                } else if (time < minTime && bestAccuracy == Float.MAX_VALUE && time > bestTime) {
                    bestLocation = location;
                    bestTime = time;
                }
            }
        }

        return bestLocation;
    }

    public void setRequest(LocationRequest request) {
        try {
            locationManager.requestLocationUpdates(GPS_PROVIDER, request.getFastestInterval(),
                    request.getSmallestDisplacement(), this);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Unable to register for GPS updates.", e);
        }

        try {
            locationManager.requestLocationUpdates(NETWORK_PROVIDER, request.getFastestInterval(),
                    request.getSmallestDisplacement(), this);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Unable to register for network updates.", e);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (callback != null) {
            callback.reportLocation(location);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    public interface Callback {
        public void reportLocation(Location location);
    }
}
