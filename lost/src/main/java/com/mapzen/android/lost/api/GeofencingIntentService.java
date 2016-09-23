package com.mapzen.android.lost.api;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;

/**
 * Handles receiving proximity alerts triggered by the {@link android.location.LocationManager} and
 * adds extras to the {@link PendingIntent} that is fired to the original caller as called from
 * {@link GeofencingApi#addGeofences(LostApiClient, GeofencingRequest, PendingIntent)}
 */
public class GeofencingIntentService extends IntentService {

  public static final String ACTION_GEOFENCING_SERVICE =
      "com.mapzen.lost.action.ACTION_GEOFENCING_SERVICE";

  public static final String EXTRA_PENDING_INTENT = "pending_intent";
  public static final String EXTRA_GEOFENCE = "geofence";

  public GeofencingIntentService() {
    super("GeofencingIntentService");
  }

  private GeofencingIntentSender intentGenerator =
      new GeofencingIntentSender(this);

  @Override protected void onHandleIntent(Intent intent) {
    intentGenerator.sendIntent(intent);
  }

}
