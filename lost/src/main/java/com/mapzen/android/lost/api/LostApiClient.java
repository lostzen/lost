package com.mapzen.android.lost.api;

import com.mapzen.android.lost.internal.LostApiClientImpl;

import android.content.Context;

public interface LostApiClient {

  void connect();

  void disconnect();

  boolean isConnected();

  final class Builder {
    private final Context context;

    public Builder(Context context) {
      this.context = context;
    }

    public LostApiClient build() {
      return new LostApiClientImpl(context);
    }
  }
}
