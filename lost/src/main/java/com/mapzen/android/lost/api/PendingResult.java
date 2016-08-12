package com.mapzen.android.lost.api;


import android.support.annotation.NonNull;

import java.util.concurrent.TimeUnit;

public abstract class PendingResult<R extends Result> {
  public PendingResult() {
  }

  @NonNull
  public abstract R await();

  @NonNull
  public abstract R await(long time, @NonNull TimeUnit timeUnit);

  public abstract void cancel();

  public abstract boolean isCanceled();

  public abstract void setResultCallback(@NonNull ResultCallback<? super R> callback);

  public abstract void setResultCallback(@NonNull ResultCallback<? super R> callback, long time,
      @NonNull TimeUnit timeUnit);

}
