package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.FusedLocationProviderApi;
import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;

import android.app.PendingIntent;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import java.util.List;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;

/**
 * Implementation of the {@link FusedLocationProviderApi}.
 */
public class FusedLocationProviderApiImpl implements FusedLocationProviderApi {
    public static final String TAG = FusedLocationProviderApiImpl.class.getSimpleName();

    /** Location updates more than 60 seconds old are considered stale. */
    public static final int RECENT_UPDATE_THRESHOLD_IN_MILLIS = 60 * 1000;

    private final LocationManager locationManager;
    private LocationListener locationListener;
    private float gpsAccuracy = Float.MAX_VALUE;
    private float networkAccuracy = Float.MAX_VALUE;
    private long fastestInterval;
    private float smallestDisplacement;

    private final GpsListener gpsListener = new GpsListener();
    private final NetworkListener networkListener = new NetworkListener();

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
        locationManager.removeUpdates(gpsListener);
        locationManager.removeUpdates(networkListener);
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
        this.locationListener = listener;
        this.fastestInterval = request.getFastestInterval();
        this.smallestDisplacement = request.getSmallestDisplacement();
        connectGpsListener();
        connectNetworkListener();
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

    private void connectGpsListener() {
        try {
            locationManager.requestLocationUpdates(GPS_PROVIDER, fastestInterval,
                    smallestDisplacement, gpsListener);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Unable to register for GPS updates.", e);
        }
    }

    private void connectNetworkListener() {
        try {
            locationManager.requestLocationUpdates(NETWORK_PROVIDER, fastestInterval,
                    smallestDisplacement, networkListener);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Unable to register for network updates.", e);
        }
    }

    private class GpsListener implements android.location.LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            gpsAccuracy = location.getAccuracy();
            if (locationListener != null && gpsAccuracy <= networkAccuracy) {
                locationListener.onLocationChanged(location);
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {
        }
    }

    private class NetworkListener implements android.location.LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            networkAccuracy = location.getAccuracy();
            if (locationListener != null && networkAccuracy <= gpsAccuracy) {
                locationListener.onLocationChanged(location);
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {
        }
    }
}
