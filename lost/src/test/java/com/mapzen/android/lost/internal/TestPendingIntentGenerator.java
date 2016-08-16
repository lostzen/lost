package com.mapzen.android.lost.internal;

import org.mockito.Mockito;

import android.app.PendingIntent;
import android.content.Context;

public class TestPendingIntentGenerator extends PendingIntentGenerator {

  public TestPendingIntentGenerator(Context context) {
    super(context);
  }

  @Override public PendingIntent generatePendingIntent(boolean hasBleResolution) {
    return Mockito.mock(PendingIntent.class);
  }
}
