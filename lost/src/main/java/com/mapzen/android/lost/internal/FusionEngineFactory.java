package com.mapzen.android.lost.internal;

import android.content.Context;

public class FusionEngineFactory implements LocationEngineFactory {

  private Context context;
  private LocationEngine.Callback callback;

  public void setContext(Context context) {
    this.context = context;
  }

  public void setCallback(LocationEngine.Callback callback) {
    this.callback = callback;
  }

  @Override public LocationEngine createDefaultLocationEngine() {
    return new FusionEngine(context, callback);
  }
}
