package com.mapzen.android.lost.internal;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import static org.mockito.Mockito.mock;

public class TestIntentFactory implements IntentFactory {

  Intent intent = mock(Intent.class);
  PendingIntent pendingIntent = mock(PendingIntent.class);

  @Override public Intent createIntent(Context context) {
    return intent;
  }

  @Override public PendingIntent createPendingIntent(Context context, int intentId, Intent intent) {
    return pendingIntent;
  }
}
