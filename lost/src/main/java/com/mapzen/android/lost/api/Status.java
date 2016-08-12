package com.mapzen.android.lost.api;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;

public class Status {

  public static final int SUCCESS = 0;
  public static final int RESOLUTION_REQUIRED = 6;
  public static final int INTERRUPTED = 14;
  public static final int INTERNAL_ERROR = 8;
  public static final int TIMEOUT = 15;
  public static final int CANCELLED = 16;
  public static final int SETTINGS_CHANGE_UNAVAILABLE = 8502;

  private final int statusCode;
  private final String statusMessage;
  private final PendingIntent mPendingIntent;

  public Status(int statusCode) {
    this(statusCode, null);
  }

  public Status(int statusCode, PendingIntent pendingIntent) {
    String statusMessage;
    switch (statusCode) {
      case SUCCESS:
        statusMessage = "SUCCESS";
        break;
      case RESOLUTION_REQUIRED:
        statusMessage = "RESOLUTION_REQUIRED";
        break;
      case SETTINGS_CHANGE_UNAVAILABLE:
        statusMessage = "SETTINGS_CHANGE_UNAVAILABLE";
        break;
      case INTERNAL_ERROR:
        statusMessage = "INTERNAL_ERROR";
        break;
      case INTERRUPTED:
        statusMessage = "INTERRUPTED";
        break;
      case TIMEOUT:
        statusMessage = "TIMEOUT";
        break;
      case CANCELLED:
        statusMessage = "CANCELLED";
        break;
      default:
        statusMessage = "UNKNOWN STATUS";
        break;
    }
    this.statusCode = statusCode;
    this.statusMessage = statusMessage;
    this.mPendingIntent = pendingIntent;
  }

  public void startResolutionForResult(Activity activity, int requestCode) throws
      IntentSender.SendIntentException {
    if(this.hasResolution()) {
      activity.startIntentSenderForResult(this.mPendingIntent.getIntentSender(), requestCode, (Intent)null, 0, 0, 0);
    }
  }

  public String getStatusMessage() {
    return this.statusMessage;
  }

  public boolean hasResolution() {
    return this.mPendingIntent != null;
  }

  public boolean isSuccess() {
    return this.statusCode == SUCCESS;
  }

  public boolean isCanceled() {
    return this.statusCode == CANCELLED;
  }

  public boolean isInterrupted() {
    return this.statusCode == INTERRUPTED;
  }

  public int getStatusCode() {
    return this.statusCode;
  }

  public PendingIntent getResolution() {
    return this.mPendingIntent;
  }

}
