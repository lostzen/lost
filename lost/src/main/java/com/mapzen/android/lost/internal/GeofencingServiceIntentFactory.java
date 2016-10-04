package com.mapzen.android.lost.internal;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class GeofencingServiceIntentFactory implements IntentFactory {

  @Override public Intent createIntent(Context context) {
    return new Intent(context, GeofencingIntentService.class);
  }

  @Override public PendingIntent createPendingIntent(Context context, int pendingIntentId,
      Intent intent) {
    return PendingIntent.getService(context, pendingIntentId, intent, 0);
  }
}
