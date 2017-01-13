package com.mapzen.android.lost.internal;

import android.app.Activity;
import android.app.PendingIntent;
import android.os.Parcelable;

/**
 * Interface for displaying a dialog in an {@link android.app.Activity} for a given code and
 * {@link android.app.PendingIntent}.
 */
public interface DialogDisplayer extends Parcelable {
  /**
   * Implementing class should display a dialog in the given {@link android.app.Activity} passing
   * the request and {@link android.app.PendingIntent}.
   * @param activity
   * @param requestCode
   * @param pendingIntent
   */
  void displayDialog(final Activity activity, final int requestCode, final PendingIntent
      pendingIntent);
}
