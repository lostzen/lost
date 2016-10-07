package com.mapzen.android.lost.internal;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * Created by sarahlensing on 10/5/16.
 */
public class TestDwellIntentFactory implements IntentFactory {
  @Override public Intent createIntent(Context context) {
    return null;
  }

  @Override
  public PendingIntent createPendingIntent(Context context, int pendingIntentId, Intent intent) {
    return null;
  }
}
