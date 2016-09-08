package com.mapzen.android.lost.api;

import android.support.annotation.NonNull;

import java.util.concurrent.TimeUnit;

/**
 * Represents a result from calling an API method in {@link LocationServices}.
 * @param <R>
 */
public abstract class PendingResult<R extends Result> {
  public PendingResult() {
  }

  /**
   * Receive a result using this blocking call.
   * @return result of API call
   */
  @NonNull public abstract R await();

  /**
   * Receive a result using this blocking call and timeout.
   * @param time amount of time to issue timeout for
   * @param timeUnit unit that time is in
   * @return result of API call
   */
  @NonNull public abstract R await(long time, @NonNull TimeUnit timeUnit);

  /**
   * Cancel the result.
   */
  public abstract void cancel();

  /**
   * Is the result cancelled.
   * @return whether result is cancelled.
   */
  public abstract boolean isCanceled();

  /**
   * Receive a result asynchronously given a {@link ResultCallback}.
   * @param callback to receive result.
   */
  public abstract void setResultCallback(@NonNull ResultCallback<? super R> callback);

  /**
   * Receive a result asynchronously given a {@link ResultCallback} and timeout.
   * @param callback to receive result.
   * @param time amount of time to issue timeout for
   * @param timeUnit unit that time is in
   */
  public abstract void setResultCallback(@NonNull ResultCallback<? super R> callback, long time,
      @NonNull TimeUnit timeUnit);
}
