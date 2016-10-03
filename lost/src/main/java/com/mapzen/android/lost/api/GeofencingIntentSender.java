package com.mapzen.android.lost.api;

import com.mapzen.android.lost.internal.FusionEngine;
import com.mapzen.android.lost.internal.GeofencingApiImpl;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Set;

/**
 * Handles generating an intent populated with relevant extras from an {@link Intent} fired by
 * {@link android.location.LocationManager#addProximityAlert(double, double, float, long,
 * PendingIntent)}
 */
public class GeofencingIntentSender {

  public static final String EXTRA_ENTERING = "entering";

  private Context context;
  private FusionEngine engine;
  private GeofencingApiImpl geofencingApi;

  public GeofencingIntentSender(Context context) {
    this.context = context;
    engine = new FusionEngine(context, null);
    geofencingApi = (GeofencingApiImpl) LocationServices.GeofencingApi;
  }

  public void sendIntent(Intent intent) {
    Intent toSend = generateIntent(intent, engine.getLastLocation());

    int intentId = extractIntentId(intent);
    PendingIntent pendingIntent = geofencingApi.pendingIntentForIntentId(intentId);
    try {
      pendingIntent.send(context, 0, toSend);
    } catch (PendingIntent.CanceledException e) {
      e.printStackTrace();
    }
  }

  public Intent generateIntent(Intent intent, Location location) {
    Bundle extras = intent.getExtras();

    int transition;
    if (extras.containsKey(EXTRA_ENTERING)) {
      if (extras.getBoolean(EXTRA_ENTERING)) {
        transition = Geofence.GEOFENCE_TRANSITION_ENTER;
      } else {
        transition = Geofence.GEOFENCE_TRANSITION_EXIT;
      }
    } else {
      transition = Geofence.GEOFENCE_TRANSITION_DWELL;
    }

    int intentId = extractIntentId(intent);
    Geofence geofence = geofencingApi.geofenceForIntentId(intentId);
    ArrayList<Geofence> geofences = new ArrayList<>();
    geofences.add(geofence);

    Intent toSend = new Intent();
    toSend.putExtra(GeofencingApi.EXTRA_TRANSITION, transition);
    toSend.putExtra(GeofencingApi.EXTRA_GEOFENCE_LIST, geofences);
    toSend.putExtra(GeofencingApi.EXTRA_TRIGGERING_LOCATION, location);
    return toSend;
  }

  private int extractIntentId(Intent intent) {
    Set<String> categories = intent.getCategories();
    String intentStr = categories.iterator().next();
    return Integer.valueOf(intentStr);
  }
}
