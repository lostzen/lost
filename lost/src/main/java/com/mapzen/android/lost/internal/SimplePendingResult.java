package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.PendingResult;
import com.mapzen.android.lost.api.ResultCallback;
import com.mapzen.android.lost.api.Status;

import android.support.annotation.NonNull;

import java.util.concurrent.TimeUnit;

public class SimplePendingResult extends PendingResult<Status> {

  private boolean hasResult = false;
  private SettingsDialogDisplayer dialogDisplayer = new SettingsDialogDisplayer();

  public SimplePendingResult(boolean hasResult) {
    this.hasResult = hasResult;
  }

  @NonNull @Override public Status await() {
    return generateStatus();
  }

  @NonNull @Override public Status await(long time, @NonNull TimeUnit timeUnit) {
    return generateStatus();
  }

  @Override public void cancel() {

  }

  @Override public boolean isCanceled() {
    return false;
  }

  @Override public void setResultCallback(@NonNull ResultCallback callback) {
    if (hasResult) {
      callback.onResult(generateStatus());
    }
  }

  @Override public void setResultCallback(@NonNull ResultCallback callback, long time,
      @NonNull TimeUnit timeUnit) {
    if (hasResult) {
      callback.onResult(generateStatus());
    }
  }

  private Status generateStatus() {
    return new Status(Status.SUCCESS, dialogDisplayer);
  }
}
