package com.mapzen.android.lost.api;

import android.app.PendingIntent;

import java.util.List;

public interface GeofencingApi {
    @Deprecated
    void addGeofences(List<Geofence> geofences, PendingIntent pendingIntent);

    void addGeofences(GeofencingRequest geofencingRequest, PendingIntent pendingIntent);

    void removeGeofences(List<String> geofenceRequestIds);

    void removeGeofences(PendingIntent pendingIntent);
}
