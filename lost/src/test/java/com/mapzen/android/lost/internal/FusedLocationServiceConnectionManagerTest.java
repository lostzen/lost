package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LostApiClient;

import org.junit.Test;

import android.content.Context;
import android.os.IBinder;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class FusedLocationServiceConnectionManagerTest {

  FusedLocationServiceConnectionManager connectionManager =
      new FusedLocationServiceConnectionManager();

  @Test public void shouldNotBeConnecting() {
    assertThat(connectionManager.isConnecting()).isFalse();
  }

  @Test public void shouldNotBeConnected() {
    assertThat(connectionManager.isConnected()).isFalse();
  }

  @Test public void setEventCallbacks_shouldSetCallbacks() {
    TestEventCallbacks eventCallbacks = new TestEventCallbacks();
    connectionManager.setEventCallbacks(eventCallbacks);
    eventCallbacks.onConnect(null);
    assertThat(eventCallbacks.connected).isTrue();
  }

  @Test public void addCallbacks_shouldAddCallbacks() {
    TestConnectionCallbacks connectionCallbacks = new TestConnectionCallbacks();
    connectionManager.addCallbacks(connectionCallbacks);
    connectionCallbacks.onConnected();
    assertThat(connectionCallbacks.isConnected()).isTrue();
  }

  @Test public void isConnected_shouldBeTrue() {
    connectionManager.connect(null, null);
    connectionManager.onServiceConnected(null);
    assertThat(connectionManager.isConnected()).isTrue();
  }

  @Test public void isConnecting_shouldBeTrue() {
    connectionManager.connect(null, null);
    assertThat(connectionManager.isConnecting()).isTrue();
  }

  @Test public void connect_shouldCallEventCallbacks() {
    TestEventCallbacks eventCallbacks = new TestEventCallbacks();
    connectionManager.setEventCallbacks(eventCallbacks);
    connectionManager.connect(null, null);
    assertThat(eventCallbacks.connected).isTrue();
  }

  @Test public void connect_shouldBeConnectingWhenEventCallbacksCalled() {
    TestEventCallbacks eventCallbacks = new TestEventCallbacks();
    connectionManager.setEventCallbacks(eventCallbacks);
    connectionManager.connect(null, null);
    assertThat(eventCallbacks.connectingOnConnected).isTrue();
  }

  @Test public void connect_shouldAddConnectionCallbacks() {
    TestConnectionCallbacks connectionCallbacks = new TestConnectionCallbacks();
    connectionManager.connect(null, connectionCallbacks);
    connectionManager.onServiceConnected(null);
    assertThat(connectionCallbacks.isConnected()).isTrue();
    connectionManager.onServiceDisconnected();
    assertThat(connectionCallbacks.isConnected()).isFalse();
  }

  @Test public void connect_shouldAddConnectionCallbacksBeforeEventCallbacksCalled() {
    TestEventCallbacks eventCallbacks = new TestEventCallbacks();
    TestConnectionCallbacks connectionCallbacks = new TestConnectionCallbacks();
    eventCallbacks.setConnectionCallbacks(connectionCallbacks);
    connectionManager.setEventCallbacks(eventCallbacks);
    connectionManager.connect(null, connectionCallbacks);
    assertThat(eventCallbacks.containsConnectionCallbacks).isTrue();
  }

  @Test public void connect_shouldNotCallEventCallbacks() {
    connectionManager.connect(null, null);
    TestEventCallbacks eventCallbacks = new TestEventCallbacks();
    connectionManager.setEventCallbacks(eventCallbacks);
    connectionManager.connect(null, null);
    assertThat(eventCallbacks.connected).isFalse();
  }

  @Test public void disconnect_shouldNotSetStateConnectingConnected() {
    connectionManager.connect(null, null);
    connectionManager.disconnect();
    assertThat(connectionManager.isConnecting()).isFalse();
    assertThat(connectionManager.isConnected()).isFalse();
  }

  @Test public void disconnect_shouldCallEventCallback() {
    TestEventCallbacks callbacks = new TestEventCallbacks();
    connectionManager.setEventCallbacks(callbacks);
    connectionManager.connect(null, null);
    connectionManager.disconnect();
    assertThat(callbacks.connected).isFalse();
  }

  @Test public void disconnect_shouldBeIdleOnEventCallbackCalled() {
    TestEventCallbacks callbacks = new TestEventCallbacks();
    connectionManager.setEventCallbacks(callbacks);
    connectionManager.connect(null, null);
    connectionManager.disconnect();
    assertThat(callbacks.idleOnDisconnect).isTrue();
  }

  @Test public void disconnect_shouldNotCallEventCallback() {
    TestEventCallbacks callbacks = new TestEventCallbacks();
    callbacks.onConnect(null);
    connectionManager.setEventCallbacks(callbacks);
    connectionManager.disconnect();
    assertThat(callbacks.connected).isTrue();
  }

  @Test public void onServiceConnected_shouldCallEventCallbacks() {
    TestEventCallbacks eventCallbacks = mock(TestEventCallbacks.class);
    connectionManager.setEventCallbacks(eventCallbacks);
    connectionManager.connect(null, null);
    IBinder binder = mock(IBinder.class);
    connectionManager.onServiceConnected(binder);
    verify(eventCallbacks).onServiceConnected(binder);
  }

  @Test public void onServiceConnected_shouldSetConnectionStateConnected() {
    connectionManager.connect(null, null);
    connectionManager.onServiceConnected(null);
    assertThat(connectionManager.isConnected()).isTrue();
  }

  @Test public void onServiceConnected_shouldCallConnectionCallbackAndBeConnected() {
    TestConnectionCallbacks connectionCallbacks = new TestConnectionCallbacks();
    connectionCallbacks.setConnectionManager(connectionManager);
    connectionManager.connect(null, connectionCallbacks);
    connectionManager.onServiceConnected(null);
    assertThat(connectionCallbacks.isConnected()).isTrue();
    assertThat(connectionCallbacks.isManagerConnectedOnConnect()).isTrue();
  }

  @Test public void onServiceConnected_shouldNotCallEventCallbacks() {
    TestEventCallbacks eventCallbacks = mock(TestEventCallbacks.class);
    connectionManager.setEventCallbacks(eventCallbacks);
    connectionManager.onServiceConnected(null);
    assertThat(eventCallbacks.serviceConnected).isFalse();
  }

  @Test public void onServiceConnected_shouldNotCallConnectionCallback() {
    TestConnectionCallbacks connectionCallbacks = new TestConnectionCallbacks();
    connectionManager.onServiceConnected(null);
    assertThat(connectionCallbacks.isConnected()).isFalse();
  }

  @Test public void onServiceConnected_managerShouldBeConnectedWhenEventCallbackCalled() {
    TestEventCallbacks eventCallbacks = new TestEventCallbacks();
    connectionManager.setEventCallbacks(eventCallbacks);
    connectionManager.connect(null, null);
    connectionManager.onServiceConnected(null);
    assertThat(eventCallbacks.connectedOnServiceConnected).isTrue();
  }

  @Test public void onServiceConnected_shouldHandleCallbacksRemovedOnConnect() throws Exception {
    LostApiClient.ConnectionCallbacks connectionCallbacks1 = new RemoveOnConnected();
    LostApiClient.ConnectionCallbacks connectionCallbacks2 = new RemoveOnConnected();
    connectionManager.connect(null, connectionCallbacks1);
    connectionManager.addCallbacks(connectionCallbacks2);
    connectionManager.onServiceConnected(null);
    assertThat(connectionManager.getConnectionCallbacks()).isEmpty();
  }

  @Test public void onServiceDisconnected_shouldCallConnectionCallback() {
    TestConnectionCallbacks connectionCallbacks = new TestConnectionCallbacks();
    connectionManager.connect(null, connectionCallbacks);
    connectionManager.onServiceDisconnected();
    assertThat(connectionCallbacks.isConnected()).isFalse();
  }

  @Test public void onServiceDisconnected_shouldBeIdleOnConnectionCallbackCalled() {
    TestConnectionCallbacks connectionCallbacks = new TestConnectionCallbacks();
    connectionCallbacks.setConnectionManager(connectionManager);
    connectionManager.connect(null, connectionCallbacks);
    connectionManager.onServiceDisconnected();
    assertThat(connectionCallbacks.isIdleOnDisconnected()).isTrue();
  }

  @Test public void onServiceDisconnected_shouldNotBeConnectingOrConnected() {
    connectionManager.connect(null, null);
    connectionManager.onServiceDisconnected();
    assertThat(connectionManager.isConnected()).isFalse();
    assertThat(connectionManager.isConnecting()).isFalse();
  }

  @Test public void onServiceDisconnected_shouldNotCallConnectionCallback() {
    TestConnectionCallbacks connectionCallbacks = new TestConnectionCallbacks();
    connectionCallbacks.onConnected();
    connectionManager.onServiceDisconnected();
    assertThat(connectionCallbacks.isConnected()).isTrue();
  }

  @Test public void onServiceDisconnected_shouldHandleCallbacksRemovedOnSuspend() throws Exception {
    LostApiClient.ConnectionCallbacks connectionCallbacks1 = new RemoveOnSuspended();
    LostApiClient.ConnectionCallbacks connectionCallbacks2 = new RemoveOnSuspended();
    connectionManager.connect(null, connectionCallbacks1);
    connectionManager.addCallbacks(connectionCallbacks2);
    connectionManager.onServiceDisconnected();
    assertThat(connectionManager.getConnectionCallbacks()).isEmpty();
  }

  class TestEventCallbacks implements FusedLocationServiceConnectionManager.EventCallbacks {

    private boolean connected = false;
    private boolean connectingOnConnected = false;

    private boolean serviceConnected = false;
    private boolean connectedOnServiceConnected = false;

    private boolean idleOnDisconnect = false;

    private LostApiClient.ConnectionCallbacks connectionCallbacks;
    private boolean containsConnectionCallbacks = false;

    @Override public void onConnect(Context context) {
      connected = true;
      connectingOnConnected = connectionManager.isConnecting();
      if (connectionCallbacks != null) {
        containsConnectionCallbacks = connectionManager.getConnectionCallbacks().contains(
            connectionCallbacks);
      }
    }

    @Override public void onServiceConnected(IBinder binder) {
      serviceConnected = true;
      connectedOnServiceConnected = connectionManager.isConnected();
    }

    public void setConnectionCallbacks(LostApiClient.ConnectionCallbacks connectionCallbacks) {
      this.connectionCallbacks = connectionCallbacks;
    }

    @Override
    public void onDisconnect() {
      connected = false;
      idleOnDisconnect = !connectionManager.isConnected() && !connectionManager.isConnecting();
    }
  }

  private class RemoveOnConnected implements LostApiClient.ConnectionCallbacks {
    @Override public void onConnected() {
      connectionManager.removeCallbacks(this);
    }

    @Override public void onConnectionSuspended() {
    }
  }

  private class RemoveOnSuspended implements LostApiClient.ConnectionCallbacks {
    @Override public void onConnected() {
    }

    @Override public void onConnectionSuspended() {
      connectionManager.removeCallbacks(this);
    }
  }
}
