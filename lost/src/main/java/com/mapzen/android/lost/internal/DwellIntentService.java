package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.GeofencingIntentSender;
import com.mapzen.android.lost.api.LocationServices;

import android.app.IntentService;
import android.content.Intent;

/**
 * Service in charge of handling intents fired after a user has entered a geofence and remained
 * inside of it for a given loitering delay.
 */
public class DwellIntentService extends IntentService {

  public DwellIntentService() {
    super("DwellIntentService");
  }

  @Override protected void onHandleIntent(Intent intent) {
    GeofencingIntentSender intentSender = new GeofencingIntentSender(this,
        LocationServices.GeofencingApi);
    intentSender.sendIntent(intent);
  }
}
