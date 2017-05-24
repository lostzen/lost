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

  /**
   * Connects the client so that it will be ready for use. This must be done before any of the
   * {@link LocationServices} APIs can be interacted with. When the client is connected, any
   * registered {@link ConnectionCallbacks} will receive a call to
   * {@link ConnectionCallbacks#onConnected()} and the client can then be used.
   */
  void connect();

  /**
   * Disconnects the client. To avoid {@link IllegalStateException}s, be sure to unregister any
   * location updates requested through the {@link FusedLocationProviderApi}.
   */
  void disconnect();

  /**
   * Returns whether or not the client is connected and ready to be used.
   * @return
   */
  boolean isConnected();

  /**
   * Unregisters callbacks added in {@link LostApiClient.Builder#addConnectionCallbacks(
   * ConnectionCallbacks)}. Use this method to avoid leaking resources.
   * @param callbacks
   */
  void unregisterConnectionCallbacks(ConnectionCallbacks callbacks);

  /**
   * {@link LostApiClient} builder class for creating and configuring new instances.
   */
  final class Builder {
    private final Context context;
    private WeakReference<ConnectionCallbacks> connectionCallbacks;

    /**
     * Creates a new object using the {@link Context}'s application context.
     * @param context
     */
    public Builder(Context context) {
      this.context = context.getApplicationContext();
    }

    /**
     * Adds {@link ConnectionCallbacks} to the client. It is strongly recommended that these
     * callbacks are used to determine when the client is connected and ready for use.
     * @param callbacks
     * @return
     */
    public Builder addConnectionCallbacks(ConnectionCallbacks callbacks) {
      this.connectionCallbacks = new WeakReference(callbacks);
      return this;
    }

    /**
     * Builds a new client given the properties currently configured on the builder.
     * @return
     */
    public LostApiClient build() {
      ConnectionCallbacks callbacks = null;
      if (connectionCallbacks != null) {
        callbacks = connectionCallbacks.get();
      }
      return new LostApiClientImpl(context, callbacks, LostClientManager.shared());
    }
  }
}
