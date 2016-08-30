package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.PendingResult;
import com.mapzen.android.lost.api.Result;
import com.mapzen.android.lost.api.ResultCallback;

import android.support.annotation.NonNull;

import java.util.concurrent.TimeUnit;

public class FusedLocationPendingResult extends PendingResult {

  @NonNull @Override public Result await() {
    return null;
  }

  @NonNull @Override public Result await(long time, @NonNull TimeUnit timeUnit) {
    return null;
  }

  @Override public void cancel() {

  }

  @Override public boolean isCanceled() {
    return false;
  }

  @Override public void setResultCallback(@NonNull ResultCallback callback) {

  }

  @Override public void setResultCallback(@NonNull ResultCallback callback, long time,
      @NonNull TimeUnit timeUnit) {

  }
}
