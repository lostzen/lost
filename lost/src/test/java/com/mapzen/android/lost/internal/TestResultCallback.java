package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.Result;
import com.mapzen.android.lost.api.ResultCallback;
import com.mapzen.android.lost.api.Status;

import android.support.annotation.NonNull;

public class TestResultCallback implements ResultCallback<Result> {

  private Status status;

  @Override public void onResult(@NonNull Result result) {
    status = result.getStatus();
  }

  public Status getStatus() {
    return status;
  }
}
