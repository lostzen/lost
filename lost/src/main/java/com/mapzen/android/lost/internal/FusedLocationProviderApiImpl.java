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

    private LocationListener locationListener;
    private boolean mockMode;

    private final FusionEngine fusionEngine;
    private final MockEngine mockEngine;

    public FusedLocationProviderApiImpl(Context context) {
        fusionEngine = new FusionEngine(context, this);
        mockEngine = new MockEngine(context, this);
    }

    @Override
    public Location getLastLocation() {
        if (mockMode) {
            return mockEngine.getLastLocation();
        } else {
            return fusionEngine.getLastLocation();
        }
    }

    @Override
    public void removeLocationUpdates(LocationListener listener) {
        fusionEngine.setRequest(null);
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

        if (mockMode) {
            mockEngine.setRequest(request);
        } else {
            fusionEngine.setRequest(request);
        }
    }

    @Override
    public void requestLocationUpdates(LocationRequest request, PendingIntent callbackIntent) {
        throw new RuntimeException("Sorry, not yet implemented");
    }

    @Override
    public void setMockLocation(Location mockLocation) {
        mockEngine.setLocation(mockLocation);
    }

    @Override
    public void setMockMode(boolean isMockMode) {
        this.mockMode = isMockMode;
        if (isMockMode) {
            fusionEngine.setRequest(null);
        }
    }

    @Override
    public void setMockTrace(final File file) {
        mockEngine.setTrace(file);
    }

    @Override
    public void reportLocation(Location location) {
        if (locationListener != null) {
            locationListener.onLocationChanged(location);
        }
    }
}
