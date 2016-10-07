package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.Geofence;
import com.mapzen.android.lost.api.GeofencingApi;

import android.content.Intent;

/**
 * Handles updating the {@link GeofencingApi}'s knowledge about which {@link Geofence} objects
 * the user has entered/exited so that the dwell transition can be properly dispatched.
 */
public class GeofencingDwellManager {

  GeofencingApiImpl geofencingApi;
  GeofenceIntentHelper intentHelper;

  public GeofencingDwellManager(GeofencingApi geofencingApi) {
    this.geofencingApi = (GeofencingApiImpl) geofencingApi;
    intentHelper = new GeofenceIntentHelper();
  }

  public void handleIntent(Intent intent) {
    int transition = intentHelper.transitionForIntent(intent);
    int intentId = intentHelper.extractIntentId(intent);
    ParcelableGeofence geofence = (ParcelableGeofence) geofencingApi.geofenceForIntentId(intentId);
    switch (transition) {
      case Geofence.GEOFENCE_TRANSITION_ENTER:
        geofencingApi.geofenceEntered(geofence, intentId);
        break;
      case Geofence.GEOFENCE_TRANSITION_EXIT:
        geofencingApi.geofenceExited(geofence);
      default:
        break;
    }
  }
}
