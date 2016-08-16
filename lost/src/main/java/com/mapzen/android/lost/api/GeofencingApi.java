package com.mapzen.android.lost.api;

import android.app.PendingIntent;

import java.util.List;

public interface GeofencingApi {

  void addGeofences(List<Geofence> geofences, PendingIntent pendingIntent);

  void removeGeofences(List<String> geofenceRequestIds);

  void removeGeofences(PendingIntent pendingIntent);
}
