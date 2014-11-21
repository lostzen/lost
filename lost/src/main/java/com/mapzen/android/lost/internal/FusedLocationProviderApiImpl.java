package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.FusedLocationProviderApi;
import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;

import android.app.PendingIntent;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;

import java.util.List;

/**
 * Implementation of the {@link FusedLocationProviderApi}.
 */
public class FusedLocationProviderApiImpl implements FusedLocationProviderApi {
    /** Location updates more than 60 seconds old are considered stale. */
    public static final int RECENT_UPDATE_THRESHOLD_IN_MILLIS = 60 * 1000;

    private final LocationManager locationManager;

    Clock clock = new SystemClock();

    public FusedLocationProviderApiImpl(Context context) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
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

    @Override
    public void removeLocationUpdates(LocationListener listener) {
        throw new RuntimeException("Sorry, not yet implemented");
    }

    @Override
    public void removeLocationUpdates(PendingIntent callbackIntent) {
        throw new RuntimeException("Sorry, not yet implemented");
    }

    @Override
    public void requestLocationUpdates(LocationRequest request, LocationListener listener,
            Looper looper) {
        throw new RuntimeException("Sorry, not yet implemented");
    }

    @Override
    public void requestLocationUpdates(LocationRequest request, LocationListener listener) {
        throw new RuntimeException("Sorry, not yet implemented");
    }

    @Override
    public void requestLocationUpdates(LocationRequest request, PendingIntent callbackIntent) {
        throw new RuntimeException("Sorry, not yet implemented");
    }

    @Override
    public void setMockLocation(Location mockLocation) {
        throw new RuntimeException("Sorry, not yet implemented");
    }

    @Override
    public void setMockMode(boolean isMockMode) {
        throw new RuntimeException("Sorry, not yet implemented");
    }
}
