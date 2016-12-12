package com.mapzen.android.lost.api;

import com.mapzen.android.lost.internal.DialogDisplayer;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.IntentSender;

/**
 * Represents the result of a {@link LocationServices} API call.
 */
public class Status implements Result {

  public static final int SUCCESS = 0;
  public static final int RESOLUTION_REQUIRED = 6;
  public static final int INTERRUPTED = 14;
  public static final int INTERNAL_ERROR = 8;
  public static final int TIMEOUT = 15;
  public static final int CANCELLED = 16;
  public static final int SETTINGS_CHANGE_UNAVAILABLE = 8502;

  private final int statusCode;
  private final String statusMessage;
  private final PendingIntent pendingIntent;
  private final DialogDisplayer dialogDisplayer;

  public Status(int statusCode, DialogDisplayer dialogDisplayer) {
    this(statusCode, dialogDisplayer, null);
  }

  public Status(int statusCode, DialogDisplayer dialogDisplayer, PendingIntent pendingIntent) {
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
    this.pendingIntent = pendingIntent;
    this.dialogDisplayer = dialogDisplayer;
  }

  /**
   * If the status code is {@link Status#RESOLUTION_REQUIRED}, then this method can be called to
   * start the resolution. For example, it will launch the Settings {@link Activity} so that the
   * user can update location settings when used with {@link SettingsApi#checkLocationSettings(
   * LostApiClient, LocationSettingsRequest)}
   * @param activity to launch for resolution.
   * @param requestCode associated with activity.
   * @throws IntentSender.SendIntentException
   */
  public void startResolutionForResult(final Activity activity, final int requestCode)
      throws IntentSender.SendIntentException {
    if (this.hasResolution()) {
      dialogDisplayer.displayDialog(activity, requestCode, pendingIntent);
    }
  }

  /**
   * Get the detailed status message for this {@link Status}.
   * @return status message.
   */
  public String getStatusMessage() {
    return this.statusMessage;
  }

  /**
   * Is there a resolution for this {@link Status}.
   * @return whether or not there is a resolution.
   */
  public boolean hasResolution() {
    return this.pendingIntent != null;
  }

  /**
   * Is the status code for this object {@link Status#SUCCESS}.
   * @return whether or not the status code is {@link Status#SUCCESS}.
   */
  public boolean isSuccess() {
    return this.statusCode == SUCCESS;
  }

  /**
   * Is the status code for this object {@link Status#CANCELLED}.
   * @return whether or not the status code is {@link Status#CANCELLED}.
   */
  public boolean isCanceled() {
    return this.statusCode == CANCELLED;
  }

  /**
   * Is the status code for this object {@link Status#INTERRUPTED}.
   * @return whether or not the status code is {@link Status#INTERRUPTED}.
   */
  public boolean isInterrupted() {
    return this.statusCode == INTERRUPTED;
  }

  /**
   * Get the status code.
   * @return the status code.
   */
  public int getStatusCode() {
    return this.statusCode;
  }

  /**
   * Get the {@link PendingIntent} if there is one. This method can return null.
   * @return the {@link PendingIntent}.
   */
  public PendingIntent getResolution() {
    return this.pendingIntent;
  }

  @Override public Status getStatus() {
    return this;
  }
}
