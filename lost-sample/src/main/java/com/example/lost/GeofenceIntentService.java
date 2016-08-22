package com.example.lost;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class GeofenceIntentService extends IntentService {
  private static final String TAG = GeofenceIntentService.class.getSimpleName();

  public GeofenceIntentService() {
    super(TAG);
  }

  @Override protected void onHandleIntent(Intent intent) {
    Log.d(TAG, "intent = " + intent);
    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
        .setSmallIcon(R.drawable.ic_launcher)
        .setContentTitle("Geofence!")
        .setContentText(intent.toString());
    NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    nm.notify(101, builder.build());
  }
}
