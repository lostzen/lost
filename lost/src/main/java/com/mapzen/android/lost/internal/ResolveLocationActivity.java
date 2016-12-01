package com.mapzen.android.lost.internal;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

/**
 * Used to enable bluetooth before firing intent to open location settings activity.
 */
public class ResolveLocationActivity extends Activity {

  private static final int REQUEST_CODE = 1;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
    settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivityForResult(settingsIntent, REQUEST_CODE);
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQUEST_CODE) {
      finish();
    }
  }
}
