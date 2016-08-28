package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.Geofence;
import com.mapzen.android.lost.api.GeofencingApi;
import com.mapzen.android.lost.api.GeofencingRequest;

import android.app.PendingIntent;
import android.content.Context;
import android.location.LocationManager;

import java.util.HashMap;
import java.util.List;

/**
 * Implementation of the {@link GeofencingApi}.
 */
public class GeofencingApiImpl implements GeofencingApi {

  private final LocationManager locationManager;
  private HashMap<String, PendingIntent> pendingIntentMap;

  GeofencingApiImpl(Context context) {
    locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    pendingIntentMap = new HashMap<>();
  }

  @Override
  public void addGeofences(GeofencingRequest geofencingRequest, PendingIntent pendingIntent)
      throws SecurityException {
    List<Geofence> geofences = geofencingRequest.getGeofences();
    addGeofences(geofences, pendingIntent);
  }

  @Override public void addGeofences(List<Geofence> geofences, PendingIntent pendingIntent)
      throws SecurityException {
    for (Geofence geofence : geofences) {
      addGeofence(geofence, pendingIntent);
    }
  }

  private void addGeofence(Geofence geofence, PendingIntent pendingIntent)
          throws SecurityException {
    ParcelableGeofence pGeofence = (ParcelableGeofence) geofence;
    String requestId = String.valueOf(pGeofence.hashCode());
    locationManager.addProximityAlert(
            pGeofence.getLatitude(),
            pGeofence.getLongitude(),
            pGeofence.getRadius(),
            pGeofence.getDuration(),
            pendingIntent);
    if(pGeofence.getRequestId()!=null && !pGeofence.getRequestId().isEmpty()){
      requestId = pGeofence.getRequestId();
    }
    pendingIntentMap.put(requestId, pendingIntent);
  }

  @Override public void removeGeofences(List<String> geofenceRequestIds) {
    for (String geofenceRequestId : geofenceRequestIds) {
      removeGeofences(geofenceRequestId);
    }
  }

  private void removeGeofences(String geofenceRequestId) {
    PendingIntent pendingIntent = pendingIntentMap.get(geofenceRequestId);
    removeGeofences(pendingIntent);
  }

  @Override public void removeGeofences(PendingIntent pendingIntent)
    throws SecurityException {
      locationManager.removeProximityAlert(pendingIntent);
      pendingIntentMap.values().remove(pendingIntent);
  }
}
