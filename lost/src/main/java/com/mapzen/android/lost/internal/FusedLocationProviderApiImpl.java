package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.FusedLocationProviderApi;
import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;

import android.app.PendingIntent;
import android.location.Location;
import android.os.Looper;

public class FusedLocationProviderApiImpl implements FusedLocationProviderApi {
    @Override
    public Location getLastLocation() {
        throw new RuntimeException("Sorry, not yet implemented");
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
