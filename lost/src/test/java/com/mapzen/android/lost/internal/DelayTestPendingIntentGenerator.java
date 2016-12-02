package com.mapzen.android.lost.internal;

import org.mockito.Mockito;

import android.app.PendingIntent;
import android.content.Context;

class DelayTestPendingIntentGenerator extends PendingIntentGenerator {
  DelayTestPendingIntentGenerator(Context context) {
    super(context);
  }

  @Override public PendingIntent generatePendingIntent() {
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return Mockito.mock(PendingIntent.class);
  }
}
