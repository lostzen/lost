package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.FusedLocationProviderApi;
import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;

import android.app.PendingIntent;
import android.content.Context;
import android.location.Location;
import android.os.Looper;

import java.io.File;

/**
 * Implementation of the {@link FusedLocationProviderApi}.
 */
public class FusedLocationProviderApiImpl implements
        FusedLocationProviderApi, LocationEngine.Callback {

    private final Context context;
    private LocationEngine locationEngine;
    private LocationListener locationListener;
    private boolean mockMode;

    public FusedLocationProviderApiImpl(Context context) {
        this.context = context;
        locationEngine = new FusionEngine(context, this);
    }

    @Override
    public Location getLastLocation() {
        return locationEngine.getLastLocation();
    }

    @Override
    public void requestLocationUpdates(LocationRequest request, LocationListener listener) {
        this.locationListener = listener;
        locationEngine.setRequest(request);
    }

    @Override
    public void requestLocationUpdates(LocationRequest request, LocationListener listener,
            Looper looper) {
        throw new RuntimeException("Sorry, not yet implemented");
    }

    @Override
    public void requestLocationUpdates(LocationRequest request, PendingIntent callbackIntent) {
        throw new RuntimeException("Sorry, not yet implemented");
    }

    @Override
    public void removeLocationUpdates(LocationListener listener) {
        locationEngine.setRequest(null);
    }

    @Override
    public void removeLocationUpdates(PendingIntent callbackIntent) {
        throw new RuntimeException("Sorry, not yet implemented");
    }

    @Override
    public void setMockMode(boolean isMockMode) {
        if (mockMode != isMockMode) {
            toggleMockMode();
        }
    }

    private void toggleMockMode() {
        mockMode = !mockMode;
        locationEngine.setRequest(null);
        if (mockMode) {
            locationEngine = new MockEngine(context, this);
        } else {
            locationEngine = new FusionEngine(context, this);
        }
    }

    @Override
    public void setMockLocation(Location mockLocation) {
        if (locationEngine instanceof MockEngine) {
            ((MockEngine) locationEngine).setLocation(mockLocation);
        }
    }

    @Override
    public void setMockTrace(final File file) {
        if (locationEngine instanceof MockEngine) {
            ((MockEngine) locationEngine).setTrace(file);
        }
    }

    @Override
    public void reportLocation(Location location) {
        if (locationListener != null) {
            locationListener.onLocationChanged(location);
        }
    }
}
