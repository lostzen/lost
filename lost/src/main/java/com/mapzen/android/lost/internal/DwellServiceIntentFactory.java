package com.mapzen.android.lost.internal;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * Class to handle creating {@link Intent} and {@link PendingIntent} objects to be sent to
 * {@link DwellIntentService}
 */
public class DwellServiceIntentFactory implements IntentFactory {

  @Override public Intent createIntent(Context context) {
    return new Intent(context, DwellIntentService.class);
  }

  @Override
  public PendingIntent createPendingIntent(Context context, int pendingIntentId, Intent intent) {
    return PendingIntent.getService(context, pendingIntentId, intent, 0);
  }
}
