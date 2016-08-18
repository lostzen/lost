package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.Geofence;
import com.mapzen.android.lost.api.GeofencingApi;
import com.mapzen.android.lost.api.GeofencingRequest;

import android.app.PendingIntent;

import java.util.List;

/**
 * Implementation of the {@link GeofencingApi}.
 */
public class GeofencingApiImpl implements GeofencingApi {
  @Override
  public void addGeofences(GeofencingRequest geofencingRequest, PendingIntent pendingIntent) {
    throw new RuntimeException("Sorry, not yet implemented");
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
