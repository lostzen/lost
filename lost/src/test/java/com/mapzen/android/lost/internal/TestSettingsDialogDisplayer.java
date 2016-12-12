package com.mapzen.android.lost.internal;

import android.app.Activity;
import android.app.PendingIntent;

public class TestSettingsDialogDisplayer implements DialogDisplayer {

  boolean displayed = false;

  @Override
  public void displayDialog(Activity activity, int requestCode, PendingIntent pendingIntent) {
    displayed = true;
  }

  public boolean isDisplayed() {
    return displayed;
  }
}
