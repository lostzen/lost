package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.Geofence;
import com.mapzen.android.lost.api.GeofencingApi;
import com.mapzen.android.lost.api.GeofencingRequest;

import android.app.PendingIntent;
import android.content.Context;
import android.location.LocationManager;

import java.util.List;

/**
 * Implementation of the {@link GeofencingApi}.
 */
public class GeofencingApiImpl implements GeofencingApi {

  private final LocationManager locationManager;

  GeofencingApiImpl(Context context) {
    locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
  }

  @Override
  public void addGeofences(GeofencingRequest geofencingRequest, PendingIntent pendingIntent)
      throws SecurityException {
    ParcelableGeofence geofence = (ParcelableGeofence) geofencingRequest.getGeofences().get(0);
    locationManager.addProximityAlert(
        geofence.getLatitude(),
        geofence.getLongitude(),
        geofence.getRadius(),
        geofence.getDuration(),
        pendingIntent);
  }

  @Override public void addGeofences(List<Geofence> geofences, PendingIntent pendingIntent) {
    throw new RuntimeException("Sorry, not yet implemented");
  }

  @Override public void removeGeofences(List<String> geofenceRequestIds) {
    throw new RuntimeException("Sorry, not yet implemented");
  }

  @Override public void removeGeofences(PendingIntent pendingIntent) {
    throw new RuntimeException("Sorry, not yet implemented");
  }
}
