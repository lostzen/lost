package com.mapzen.android.lost.internal;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

public class PendingIntentGenerator {

  private final Context context;

  public PendingIntentGenerator(Context context) {
    this.context = context;
  }

  public PendingIntent generatePendingIntent(boolean hasBleResolution) {
    final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
    return PendingIntent.getActivity(context, 0, intent, 0);
  }
}
