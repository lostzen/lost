package com.example.lost;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class PendingIntentService extends IntentService {

  public static final String ACTION = "com.mapzen.lost.intent.action.PENDING_INTENT_SERVICE";

  public PendingIntentService() {
    super("PendingIntentService");
  }

  public PendingIntentService(String name) {
    super(name);
  }

  @Override protected void onHandleIntent(Intent intent) {
    Handler handler = new Handler(Looper.getMainLooper());
    handler.post(new Runnable() {
      @Override public void run() {
        Toast.makeText(PendingIntentService.this, R.string.pending_intent, Toast.LENGTH_SHORT)
            .show();
      }
    });
  }
}
