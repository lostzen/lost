package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.android.lost.api.LostApiClient.ConnectionCallbacks;

import android.content.Context;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class FusedLocationServiceConnectionManager {

  public interface EventCallbacks {
    void onConnect(Context context);
    void onServiceConnected(IBinder binder);
    void onDisconnect();
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

  public void removeCallbacks(ConnectionCallbacks callbacks) {
    if (callbacks != null) {
      connectionCallbacks.remove(callbacks);
    }
  }

  public boolean isConnected() {
    return connectState == ConnectState.CONNECTED;
  }

  public boolean isConnecting() {
    return connectState == ConnectState.CONNECTING;
  }

  public void connect(Context context, ConnectionCallbacks callbacks) {
    addCallbacks(callbacks);
    if (connectState == ConnectState.IDLE) {
      connectState = ConnectState.CONNECTING;

      if (eventCallbacks != null) {
        eventCallbacks.onConnect(context);
      }
    }
  }

  public void disconnect() {
    if (connectState != ConnectState.IDLE) {
      connectState = ConnectState.IDLE;
      if (eventCallbacks != null) {
        eventCallbacks.onDisconnect();
      }
    }
  }

  public void onServiceConnected(IBinder binder) {
    if (connectState != ConnectState.IDLE) {
      connectState = ConnectState.CONNECTED;
      if (eventCallbacks != null) {
        eventCallbacks.onServiceConnected(binder);
      }

      if (!connectionCallbacks.isEmpty()) {
        final ArrayList<ConnectionCallbacks> copy = new ArrayList<>(connectionCallbacks);
        for (LostApiClient.ConnectionCallbacks callbacks : copy) {
          callbacks.onConnected();
        }
      }
    }
  }

  public void onServiceDisconnected() {
    if (connectState != ConnectState.IDLE) {
      connectState = ConnectState.IDLE;
      if (!connectionCallbacks.isEmpty()) {
        final ArrayList<ConnectionCallbacks> copy = new ArrayList<>(connectionCallbacks);
        for (LostApiClient.ConnectionCallbacks callbacks : copy) {
          callbacks.onConnectionSuspended();
        }
      }
    }
  }

  public Set<ConnectionCallbacks> getConnectionCallbacks() {
    return connectionCallbacks;
  }
}
