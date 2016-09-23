package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.GeofencingIntentService;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class GeofencingServiceIntentFactory implements IntentFactory {

  @Override public Intent createIntent() {
    return new Intent(GeofencingIntentService.ACTION_GEOFENCING_SERVICE);
  }

  @Override public PendingIntent createPendingIntent(Context context, Intent intent) {
    return PendingIntent.getService(context, 1, intent, 0);
  }
}
