package com.mapzen.android.lost.internal;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.IntentSender;

/**
 * Handles displaying a dialog to the user so that they can optionally update their device location
 * settings.
 */
public class SettingsDialogDisplayer implements DialogDisplayer {

  private static final String SETTINGS_DIALOG_TAG = "settings-dialog";

  @Override public void displayDialog(final Activity activity, final int requestCode,
      final PendingIntent pendingIntent) {
    SettingsDialogFragment fragment = new SettingsDialogFragment();
    fragment.setOnClickListener(new DialogInterface.OnClickListener() {
      @Override public void onClick(DialogInterface dialogInterface, int i) {
        try {
          activity.startIntentSenderForResult(pendingIntent.getIntentSender(),
              requestCode, null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
          e.printStackTrace();
        }
      }
    });
    fragment.show(activity.getFragmentManager(), SETTINGS_DIALOG_TAG);
  }
}
