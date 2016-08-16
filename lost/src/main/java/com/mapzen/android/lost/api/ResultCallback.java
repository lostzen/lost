package com.mapzen.android.lost.api;

import android.support.annotation.NonNull;

public interface ResultCallback<R extends Result> {
  void onResult(@NonNull R result);
}
