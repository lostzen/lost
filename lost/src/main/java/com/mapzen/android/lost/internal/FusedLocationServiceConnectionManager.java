package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.android.lost.api.LostApiClient.ConnectionCallbacks;

import android.content.Context;
import android.os.IBinder;

import java.util.HashSet;
import java.util.Set;

public class FusedLocationServiceConnectionManager {

  public interface EventCallbacks {
    void onConnect(Context context);
    void onServiceConnected(IBinder binder);
    void onDisconnect(LostApiClient client, boolean stopService, boolean disconnectService);
  }

  private enum ConnectState { IDLE, CONNECTING, CONNECTED }

  private EventCallbacks eventCallbacks;
  private ConnectState connectState;
  Set<ConnectionCallbacks> connectionCallbacks;

  public FusedLocationServiceConnectionManager() {
    connectionCallbacks = new HashSet<>();
    connectState = ConnectState.IDLE;
  }

  public void setEventCallbacks(EventCallbacks callbacks) {
    eventCallbacks = callbacks;
  }

  public void addCallbacks(ConnectionCallbacks callbacks) {
    if (callbacks != null) {
      connectionCallbacks.add(callbacks);
    }
  }

  public boolean isConnected() {
    return connectState == ConnectState.CONNECTED;
  }

  public boolean isConnecting() {
    return connectState == ConnectState.CONNECTING;
  }

  public void connect(Context context, ConnectionCallbacks callbacks) {
    if (connectState == ConnectState.IDLE) {
      connectState = ConnectState.CONNECTING;

      if (eventCallbacks != null) {
        eventCallbacks.onConnect(context);
      }
    }
    addCallbacks(callbacks);
  }

  public void disconnect(LostApiClient client, boolean stopService) {
    if (connectState != ConnectState.IDLE) {
      boolean disconnectService = (connectState == ConnectState.CONNECTED);
      connectState = ConnectState.IDLE;
      if (eventCallbacks != null) {
        eventCallbacks.onDisconnect(client, stopService, disconnectService);
      }
    }
  }

  public void onServiceConnected(IBinder binder) {
    if (connectState != ConnectState.IDLE) {
      if (eventCallbacks != null) {
        eventCallbacks.onServiceConnected(binder);
      }
      connectState = ConnectState.CONNECTED;
      if (!connectionCallbacks.isEmpty()) {
        for (LostApiClient.ConnectionCallbacks callbacks : connectionCallbacks) {
          callbacks.onConnected();
        }
      }
    }
  }

  public void onServiceDisconnected() {
    if (connectState != ConnectState.IDLE) {
      if (!connectionCallbacks.isEmpty()) {
        for (LostApiClient.ConnectionCallbacks callbacks : connectionCallbacks) {
          callbacks.onConnectionSuspended();
        }
      }
      connectState = ConnectState.IDLE;
    }
  }
}
