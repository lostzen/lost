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
import static android.location.LocationManager.PASSIVE_PROVIDER;

/**
 * Location engine that fuses GPS and network locations.
 */
public class FusionEngine extends LocationEngine implements LocationListener {
    private static final String TAG = FusionEngine.class.getSimpleName();

    /** Location updates more than 60 seconds old are considered stale. */
    public static final long RECENT_UPDATE_THRESHOLD_IN_MILLIS = 60 * 1000;
    public static final long RECENT_UPDATE_THRESHOLD_IN_NANOS =
            RECENT_UPDATE_THRESHOLD_IN_MILLIS * 1000000;

    private final LocationManager locationManager;

    private Location gpsLocation;
    private Location networkLocation;

    static Clock clock = new SystemClock();

    public FusionEngine(Context context, Callback callback) {
        super(context, callback);
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
            try {
                final Location location = locationManager.getLastKnownLocation(provider);
                if (location != null) {
                    final float accuracy = location.getAccuracy();
                    final long time = location.getTime();
                    if (time > minTime && accuracy < bestAccuracy) {
                        bestLocation = location;
                        bestAccuracy = accuracy;
                        bestTime = time;
                    } else if (time < minTime && bestAccuracy == Float.MAX_VALUE
                            && time > bestTime) {
                        bestLocation = location;
                        bestTime = time;
                    }
                }
            } catch (SecurityException e) {
                Log.e(TAG, "Permissions not granted for provider: " + provider, e);
            }
        }

        return bestLocation;
    }

    @Override
    public boolean isProviderEnabled(String provider) {
        return locationManager.isProviderEnabled(provider);
    }

    @Override
    protected void enable() {
        switch (getRequest().getPriority()) {
            case LocationRequest.PRIORITY_HIGH_ACCURACY:
                enableGps();
                enableNetwork();
                break;
            case LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY:
                enableNetwork();
                break;
            case LocationRequest.PRIORITY_LOW_POWER:
                enableNetwork();
                break;
            case LocationRequest.PRIORITY_NO_POWER:
                enablePassive();
                break;
            default:
                break;
        }
    }

    @Override
    protected void disable() {
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    private void enableGps() {
        try {
            locationManager.requestLocationUpdates(GPS_PROVIDER,
                    getRequest().getFastestInterval(),
                    getRequest().getSmallestDisplacement(),
                    this);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Unable to register for GPS updates.", e);
        }
    }

    private void enableNetwork() {
        try {
            locationManager.requestLocationUpdates(NETWORK_PROVIDER,
                    getRequest().getFastestInterval(),
                    getRequest().getSmallestDisplacement(),
                    this);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Unable to register for network updates.", e);
        }
    }

    private void enablePassive() {
        try {
            locationManager.requestLocationUpdates(PASSIVE_PROVIDER,
                    getRequest().getFastestInterval(),
                    getRequest().getSmallestDisplacement(),
                    this);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Unable to register for passive updates.", e);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (GPS_PROVIDER.equals(location.getProvider())) {
            gpsLocation = location;
            if (getCallback() != null && isBetterThan(gpsLocation, networkLocation)) {
                getCallback().reportLocation(this, location);
            }
        } else if (NETWORK_PROVIDER.equals(location.getProvider())) {
            networkLocation = location;
            if (getCallback() != null && isBetterThan(networkLocation, gpsLocation)) {
                getCallback().reportLocation(this, location);
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
        final Callback callback = getCallback();
        if (callback != null) {
            callback.reportProviderEnabled(this, provider);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        final Callback callback = getCallback();
        if (callback != null) {
            callback.reportProviderDisabled(this, provider);
        }
    }

    public static boolean isBetterThan(Location locationA, Location locationB) {
        if (locationA == null) {
            return false;
        }

        if (locationB == null) {
            return true;
        }

        if (SystemClock.getTimeInNanos(locationA) > SystemClock.getTimeInNanos(locationB) +
                RECENT_UPDATE_THRESHOLD_IN_NANOS) {
            return true;
        }

        if (!locationA.hasAccuracy()) {
            return false;
        }

        if (!locationB.hasAccuracy()) {
            return true;
        }

        return locationA.getAccuracy() < locationB.getAccuracy();
    }
}
