package com.mapzen.android.lost.api;

import android.support.annotation.NonNull;

/**
 * Interface for receiving {@link Result} asynchronously when calling a {@link LocationServices}
 * API method.
 * @param <R> type of {@link Result} that will be returned.
 */
public interface ResultCallback<R extends Result> {
  /**
   * Called when result is available for API call.
   * @param result result of API call.
   */
  void onResult(@NonNull R result);
}
