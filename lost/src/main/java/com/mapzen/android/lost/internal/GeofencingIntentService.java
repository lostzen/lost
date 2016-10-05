package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.GeofencingIntentSender;
import com.mapzen.android.lost.api.LocationServices;

import android.app.IntentService;
import android.content.Intent;

/**
 * Handles receiving proximity alerts triggered by the {@link android.location.LocationManager} and
 * adds extras to the {@link android.app.PendingIntent} that is fired to the original caller as
 * called from {@link com.mapzen.android.lost.api.GeofencingApi#addGeofences(
 * com.mapzen.android.lost.api.LostApiClient, com.mapzen.android.lost.api.GeofencingRequest,
 * android.app.PendingIntent)}
 */
public class GeofencingIntentService extends IntentService {

  public GeofencingIntentService() {
    super("GeofencingIntentService");
  }

  @Override protected void onHandleIntent(Intent intent) {
    GeofencingIntentSender intentGenerator = new GeofencingIntentSender(this,
        LocationServices.GeofencingApi);
    intentGenerator.sendIntent(intent);

    GeofencingDwellManager dwellManager = new GeofencingDwellManager(
        LocationServices.GeofencingApi);
    dwellManager.handleIntent(intent);
  }

}
