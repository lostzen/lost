package com.mapzen.android.lost.internal;

import android.content.Context;

public class FusionEngineFactory implements LocationEngineFactory {

  public Context context;
  public LocationEngine.Callback callback;

  @Override public LocationEngine createDefaultLocationEngine() {
    return new FusionEngine(context, callback);
  }
}