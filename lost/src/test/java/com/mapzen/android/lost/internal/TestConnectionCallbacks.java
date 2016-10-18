package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LostApiClient;

class TestConnectionCallbacks implements LostApiClient.ConnectionCallbacks {

  private boolean connected = false;

  @Override public void onConnected() {
    connected = true;
  }

  @Override public void onConnectionSuspended() {
    connected = false;
  }

  public boolean isConnected() {
    return connected;
  }
}
