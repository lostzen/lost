package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.GeofencingIntentSender;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

/**
 * Handles receiving proximity alerts triggered by the {@link android.location.LocationManager} and
 * adds extras to the {@link android.app.PendingIntent} that is fired to the original caller as
 * called from {@link com.mapzen.android.lost.api.GeofencingApi#addGeofences(
 * com.mapzen.android.lost.api.LostApiClient, com.mapzen.android.lost.api.GeofencingRequest,
 * android.app.PendingIntent)}
 */
public class GeofencingIntentService extends IntentService {

  public GeofencingIntentService() {
    super("GeofencingIntentService");
  }

  @Override protected void onHandleIntent(Intent intent) {
    GeofencingIntentSender intentGenerator = new GeofencingIntentSender(this);
    intentGenerator.sendIntent(intent);

    final Bundle extras = intent.getExtras();
    Handler handler = new Handler(Looper.getMainLooper());
    handler.post(new Runnable() {
      @Override public void run() {
        for (String key : extras.keySet()) {
          Object val = extras.get(key);
          Toast.makeText(GeofencingIntentService.this, key + "=" + val, Toast.LENGTH_SHORT).show();
        }
      }
    });
  }

}
