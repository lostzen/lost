package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.PendingResult;
import com.mapzen.android.lost.api.Result;
import com.mapzen.android.lost.api.ResultCallback;
import com.mapzen.android.lost.api.Status;

import android.support.annotation.NonNull;

import java.util.concurrent.TimeUnit;

public class FusedLocationPendingResult extends PendingResult {

  private boolean hasResult = false;

  public FusedLocationPendingResult(boolean hasResult) {
    this.hasResult = hasResult;
  }

  @NonNull @Override public Result await() {
    return generateResult();
  }

  @NonNull @Override public Result await(long time, @NonNull TimeUnit timeUnit) {
    return generateResult();
  }

  @Override public void cancel() {

  }

  @Override public boolean isCanceled() {
    return false;
  }

  @Override public void setResultCallback(@NonNull ResultCallback callback) {
    if (hasResult) {
      callback.onResult(generateResult());
    }
  }

  @Override public void setResultCallback(@NonNull ResultCallback callback, long time,
      @NonNull TimeUnit timeUnit) {
    if (hasResult) {
      callback.onResult(generateResult());
    }
  }

  private Result generateResult() {
    return new Result() {
      @Override public Status getStatus() {
        return new Status(Status.SUCCESS);
      }
    };
  }
}
