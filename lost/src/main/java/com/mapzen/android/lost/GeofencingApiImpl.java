package com.mapzen.android.lost;

import android.app.PendingIntent;

import java.util.List;

public class GeofencingApiImpl implements GeofencingApi {
    @Override
    public void addGeofences(List<Geofence> geofences, PendingIntent pendingIntent) {
        throw new RuntimeException("Sorry, not yet implemented");
    }

    @Override
    public void removeGeofences(List<String> geofenceRequestIds) {
        throw new RuntimeException("Sorry, not yet implemented");
    }

    @Override
    public void removeGeofences(PendingIntent pendingIntent) {
        throw new RuntimeException("Sorry, not yet implemented");
    }
}
