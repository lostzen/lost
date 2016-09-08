package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.Geofence;
import com.mapzen.android.lost.api.GeofencingApi;
import com.mapzen.android.lost.api.GeofencingRequest;
import com.mapzen.android.lost.api.LostApiClient;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.location.LocationManager;
import android.support.annotation.RequiresPermission;

import java.util.HashMap;
import java.util.List;

/**
 * Implementation of the {@link GeofencingApi}.
 */
public class GeofencingApiImpl implements GeofencingApi {

  private final LocationManager locationManager;
  private final HashMap<String, PendingIntent> pendingIntentMap;

  GeofencingApiImpl(Context context) {
    locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    pendingIntentMap = new HashMap<>();
  }

  @RequiresPermission(anyOf = {Manifest.permission.ACCESS_COARSE_LOCATION,
          Manifest.permission.ACCESS_FINE_LOCATION})
  @Override
  public void addGeofences(LostApiClient client, GeofencingRequest geofencingRequest,
      PendingIntent pendingIntent)
      throws SecurityException {
    List<Geofence> geofences = geofencingRequest.getGeofences();
    addGeofences(client, geofences, pendingIntent);
  }

  @RequiresPermission(anyOf = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION})
  @Override public void addGeofences(LostApiClient client, List<Geofence> geofences,
      PendingIntent pendingIntent)
      throws SecurityException {
    for (Geofence geofence : geofences) {
      addGeofence(client, geofence, pendingIntent);
    }
  }

  @RequiresPermission(anyOf = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION})
  private void addGeofence(LostApiClient client, Geofence geofence, PendingIntent pendingIntent)
          throws SecurityException {
    ParcelableGeofence pGeofence = (ParcelableGeofence) geofence;
    String requestId = String.valueOf(pGeofence.hashCode());
    locationManager.addProximityAlert(
            pGeofence.getLatitude(),
            pGeofence.getLongitude(),
            pGeofence.getRadius(),
            pGeofence.getDuration(),
            pendingIntent);
    if (pGeofence.getRequestId() != null && !pGeofence.getRequestId().isEmpty()) {
      requestId = pGeofence.getRequestId();
    }
    pendingIntentMap.put(requestId, pendingIntent);
  }

  @RequiresPermission(anyOf = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION})
  @Override public void removeGeofences(LostApiClient client, List<String> geofenceRequestIds) {
    for (String geofenceRequestId : geofenceRequestIds) {
      removeGeofences(client, geofenceRequestId);
    }
  }

  @RequiresPermission(anyOf = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION})
  private void removeGeofences(LostApiClient client, String geofenceRequestId)
      throws SecurityException {
    PendingIntent pendingIntent = pendingIntentMap.get(geofenceRequestId);
    removeGeofences(client, pendingIntent);
  }

  @RequiresPermission(anyOf = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION})
  @Override public void removeGeofences(LostApiClient client, PendingIntent pendingIntent)
    throws SecurityException {
      locationManager.removeProximityAlert(pendingIntent);
      pendingIntentMap.values().remove(pendingIntent);
  }
}
