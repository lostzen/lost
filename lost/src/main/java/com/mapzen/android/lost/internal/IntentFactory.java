package com.mapzen.android.lost.internal;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public interface IntentFactory {
  Intent createIntent(Context context);
  PendingIntent createPendingIntent(Context context, int pendingIntentId, Intent intent);
}
