package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LostApiClient;

class TestConnectionCallbacks implements LostApiClient.ConnectionCallbacks {

  private boolean connected = false;
  private FusedLocationServiceConnectionManager connectionManager;
  private boolean managerConnectedOnConnect = false;
  private boolean idleOnDisconnected = false;

  public void setConnectionManager(FusedLocationServiceConnectionManager manager) {
    connectionManager = manager;
  }

  @Override public void onConnected() {
    connected = true;
    if (connectionManager != null) {
      managerConnectedOnConnect = connectionManager.isConnected();
    }
  }

  @Override public void onConnectionSuspended() {
    connected = false;
    if (connectionManager != null) {
      idleOnDisconnected = !connectionManager.isConnected() && !connectionManager.isConnecting();
    }
  }

  public boolean isConnected() {
    return connected;
  }

  public boolean isManagerConnectedOnConnect() {
    return managerConnectedOnConnect;
  }

  public boolean isIdleOnDisconnected() {
    return idleOnDisconnected;
  }
}
