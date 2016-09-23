package com.mapzen.android.lost.api;

import com.mapzen.android.lost.internal.FusionEngine;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import java.util.ArrayList;

import static com.mapzen.android.lost.api.GeofencingIntentService.EXTRA_GEOFENCE;
import static com.mapzen.android.lost.api.GeofencingIntentService.EXTRA_PENDING_INTENT;

/**
 * Handles generating an intent populated with relevant extras from an {@link Intent} fired by
 * {@link LocationManager#addProximityAlert(double, double, float, long, PendingIntent)}
 */
public class GeofencingIntentSender {

  public static final String EXTRA_ENTERING = "entering";

  private Context context;
  private FusionEngine engine;

  public GeofencingIntentSender(Context context) {
    this.context = context;
    engine = new FusionEngine(context, null);
  }

  public void sendIntent(Intent intent) {
    Intent toSend = generateIntent(intent, engine.getLastLocation());
    Bundle extras = intent.getExtras();
    PendingIntent pendingIntent = (PendingIntent) extras.get(EXTRA_PENDING_INTENT);
    try {
      pendingIntent.send(context, 0, toSend);
    } catch (PendingIntent.CanceledException e) {
      e.printStackTrace();
    }
  }

  public Intent generateIntent(Intent intent, Location location) {
    Bundle extras = intent.getExtras();

    int transition = 0;
    if (extras.containsKey(EXTRA_ENTERING)) {
      transition = Geofence.GEOFENCE_TRANSITION_ENTER;
    }

    Geofence geofence = (Geofence) extras.get(EXTRA_GEOFENCE);
    ArrayList<Geofence> geofences = new ArrayList<>();
    geofences.add(geofence);

    Intent toSend = new Intent();
    toSend.putExtra(GeofencingApi.EXTRA_TRANSITION, transition);
    toSend.putExtra(GeofencingApi.EXTRA_GEOFENCE_LIST, geofences);
    toSend.putExtra(GeofencingApi.EXTRA_TRIGGERING_LOCATION, location);
    return toSend;
  }
}
