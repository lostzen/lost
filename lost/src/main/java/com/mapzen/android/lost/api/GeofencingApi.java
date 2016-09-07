package com.mapzen.android.lost.api;

import android.app.PendingIntent;
import android.support.annotation.RequiresPermission;

import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public interface GeofencingApi {

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  void addGeofences(LostApiClient client, GeofencingRequest geofencingRequest,
      PendingIntent pendingIntent);

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  void addGeofences(LostApiClient client, List<Geofence> geofences, PendingIntent pendingIntent);

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  void removeGeofences(LostApiClient client, List<String> geofenceRequestIds);

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  void removeGeofences(LostApiClient client, PendingIntent pendingIntent);
}
