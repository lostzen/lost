package com.mapzen.android.lost.internal;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public interface IntentFactory {
  Intent createIntent();
  PendingIntent createPendingIntent(Context context, Intent intent);
}
