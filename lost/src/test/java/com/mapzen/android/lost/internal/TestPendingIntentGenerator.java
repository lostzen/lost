package com.mapzen.android.lost.internal;

import org.mockito.Mockito;

import android.app.PendingIntent;
import android.content.Context;

class TestPendingIntentGenerator extends PendingIntentGenerator {

  TestPendingIntentGenerator(Context context) {
    super(context);
  }

  @Override public PendingIntent generatePendingIntent() {
    return Mockito.mock(PendingIntent.class);
  }
}
