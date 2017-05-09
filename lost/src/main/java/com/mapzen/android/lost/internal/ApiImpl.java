package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LostApiClient;

/**
 * Superclass for all {@link com.mapzen.android.lost.api.LocationServices} implementations.
 */
class ApiImpl {
  void throwIfNotConnected(LostApiClient client) {
    if (!client.isConnected()) {
      throw new IllegalStateException("LostApiClient is not connected.");
    }
  }
}
