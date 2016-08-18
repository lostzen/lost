package com.mapzen.android.lost.api;

import com.mapzen.android.lost.internal.LostApiClientImpl;

import android.content.Context;

public interface LostApiClient {

  interface ConnectionCallbacks {
    void onConnected();
    void onConnectionSuspended();
  }

  void connect();

  void disconnect();

  boolean isConnected();

  final class Builder {
    private final Context context;
    private ConnectionCallbacks connectionCallbacks;

    public Builder(Context context) {
      this.context = context;
    }

    public Builder addConnectionCallbacks(ConnectionCallbacks callbacks) {
      this.connectionCallbacks = callbacks;
      return this;
    }

    public LostApiClient build() {
      return new LostApiClientImpl(context, connectionCallbacks);
    }
  }
}
