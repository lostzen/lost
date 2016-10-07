package com.mapzen.android.lost.api;

import com.mapzen.android.lost.internal.FusionEngine;
import com.mapzen.android.lost.internal.GeofenceIntentHelper;
import com.mapzen.android.lost.internal.GeofencingApiImpl;
import com.mapzen.android.lost.internal.ParcelableGeofence;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import java.util.ArrayList;

/**
 * Handles generating an intent populated with relevant extras from an {@link Intent} fired by
 * {@link android.location.LocationManager#addProximityAlert(double, double, float, long,
 * PendingIntent)}
 */
public class GeofencingIntentSender {

  private Context context;
  private FusionEngine engine;
  private GeofencingApiImpl geofencingApi;
  private GeofenceIntentHelper intentHelper;

  public GeofencingIntentSender(Context context, GeofencingApi geofencingApi) {
    this.context = context;
    this.geofencingApi = (GeofencingApiImpl) geofencingApi;
    engine = new FusionEngine(context, null);
    intentHelper = new GeofenceIntentHelper();
  }

  public void sendIntent(Intent intent) {
    if (!shouldSendIntent(intent)) {
      return;
    }

    Intent toSend = generateIntent(intent, engine.getLastLocation());

    int intentId = intentHelper.extractIntentId(intent);
    PendingIntent pendingIntent = geofencingApi.pendingIntentForIntentId(intentId);
    try {
      pendingIntent.send(context, 0, toSend);
    } catch (PendingIntent.CanceledException e) {
      e.printStackTrace();
    }
  }

  public boolean shouldSendIntent(Intent intent) {
    int transition = intentHelper.transitionForIntent(intent);
    int intentId = intentHelper.extractIntentId(intent);
    ParcelableGeofence geofence = (ParcelableGeofence) geofencingApi.geofenceForIntentId(intentId);
    return (geofence.getTransitionTypes() & transition) != 0;
  }

  public Intent generateIntent(Intent intent, Location location) {
    int intentId = intentHelper.extractIntentId(intent);
    Geofence geofence = geofencingApi.geofenceForIntentId(intentId);
    ArrayList<Geofence> geofences = new ArrayList<>();
    geofences.add(geofence);

    Intent toSend = new Intent();
    toSend.putExtra(GeofencingApi.EXTRA_TRANSITION, intentHelper.transitionForIntent(intent));
    toSend.putExtra(GeofencingApi.EXTRA_GEOFENCE_LIST, geofences);
    toSend.putExtra(GeofencingApi.EXTRA_TRIGGERING_LOCATION, location);
    return toSend;
  }
}
