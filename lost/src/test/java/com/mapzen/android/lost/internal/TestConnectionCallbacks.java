package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LostApiClient;

class TestConnectionCallbacks implements LostApiClient.ConnectionCallbacks {

  private boolean connected = false;
  private FusedLocationServiceConnectionManager connectionManager;
  private boolean managerConnectedOnConnect = false;
  private boolean idleOnDisconnected = false;
  private LostApiClient client;
  private boolean clientConnectedOnConnect = false;

  public void setConnectionManager(FusedLocationServiceConnectionManager manager) {
    connectionManager = manager;
  }

  public void setLostClient(LostApiClient c) {
    client = c;
  }

  @Override public void onConnected() {
    connected = true;
    if (connectionManager != null) {
      managerConnectedOnConnect = connectionManager.isConnected();
    }
    if (client != null) {
      clientConnectedOnConnect = client.isConnected();
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

  public boolean isClientConnectedOnConnect() {
    return clientConnectedOnConnect;
  }
}
