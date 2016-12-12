package com.mapzen.android.lost.internal;

import android.app.Activity;
import android.app.PendingIntent;

/**
 * Created by sarahlensing on 12/12/16.
 */

public interface DialogDisplayer {
  void displayDialog(final Activity activity, final int requestCode, final PendingIntent
      pendingIntent);
}
