package com.mapzen.android.lost.api;

import com.mapzen.android.lost.internal.LostApiClientImpl;
import com.mapzen.android.lost.internal.LostClientManager;

import android.content.Context;

import java.lang.ref.WeakReference;

public interface LostApiClient {

  interface ConnectionCallbacks {
    void onConnected();
    void onConnectionSuspended();
  }

  void connect();

  void disconnect();

  boolean isConnected();

  void unregisterConnectionCallbacks(ConnectionCallbacks callbacks);

  final class Builder {
    private final Context context;
    private WeakReference<ConnectionCallbacks> connectionCallbacks;

    public Builder(Context context) {
      this.context = context.getApplicationContext();
    }

    public Builder addConnectionCallbacks(ConnectionCallbacks callbacks) {
      this.connectionCallbacks = new WeakReference(callbacks);
      return this;
    }

    public LostApiClient build() {
      ConnectionCallbacks callbacks = null;
      if (connectionCallbacks != null) {
        callbacks = connectionCallbacks.get();
      }
      return new LostApiClientImpl(context, callbacks, LostClientManager.shared());
    }
  }
}
