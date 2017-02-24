package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.BaseRobolectricTest;
import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.lost.BuildConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Looper;

import java.io.File;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.RuntimeEnvironment.application;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@SuppressWarnings("MissingPermission")
@Config(constants = BuildConfig.class, sdk = 21, manifest = Config.NONE)
public class FusedLocationProviderApiImplTest extends BaseRobolectricTest {

  private LostApiClient client;
  private LostApiClient secondClient;
  private FusedLocationProviderApiImpl api;
  private FusedLocationProviderService service;
  private FusedLocationServiceConnectionManager connectionManager;

  @Before public void setUp() throws Exception {
    mockService();
    client = new LostApiClient.Builder(mock(Context.class)).build();

    // do not call connect on this!
    secondClient = new LostApiClient.Builder(mock(Context.class)).build();
    connectionManager = spy(new FusedLocationServiceConnectionManager());
    Mockito.doCallRealMethod().when(connectionManager).setEventCallbacks(any(
        FusedLocationServiceConnectionManager.EventCallbacks.class));
    Mockito.doCallRealMethod().when(connectionManager).connect(any(Context.class), any(
        LostApiClient.ConnectionCallbacks.class));
    api = new FusedLocationProviderApiImpl(connectionManager);
    api.connect(application, null);
    service = api.getService();
  }

  private void mockService() {
    FusedLocationProviderService.FusedLocationProviderBinder stubBinder = mock(
        FusedLocationProviderService.FusedLocationProviderBinder.class);
    when(stubBinder.getService()).thenReturn(mock(FusedLocationProviderService.class));
    shadowOf(application).setComponentNameAndServiceForBindService(
        new ComponentName("com.mapzen.lost", "FusedLocationProviderService"), stubBinder);
  }

  @Test public void disconnect_shouldBeHarmlessBeforeConnect() {
    assertThat(!secondClient.isConnected());
    secondClient.disconnect();
    assertThat(!secondClient.isConnected());
  }

  @Test public void shouldSetEventCallbacks() {
    verify(connectionManager).setEventCallbacks(any(
        FusedLocationServiceConnectionManager.EventCallbacks.class));
  }

  @Test public void isConnecting_shouldCallConnectionManager() {
    api.isConnecting();
    verify(connectionManager).isConnecting();
  }

  @Test public void addConnectionCallbacks_shouldCallConnectionManager() {
    TestConnectionCallbacks callbacks = new TestConnectionCallbacks();
    api.addConnectionCallbacks(callbacks);
    verify(connectionManager).addCallbacks(callbacks);
  }

  @Test public void connect_shouldCallConnectionManager() {
    Context context = mock(Context.class);
    TestConnectionCallbacks callbacks = new TestConnectionCallbacks();
    api.connect(context, callbacks);
    verify(connectionManager).connect(context, callbacks);
  }

  @Test public void disconnect_shouldCallConnectionManager() {
    api.disconnect();
    verify(connectionManager).disconnect();
  }

  @Test public void isConnected_shouldCallConnectionManager() {
    api.isConnected();
    verify(connectionManager).isConnected();
  }

  @Test(expected = IllegalStateException.class)
  public void getLastLocation_shouldThrowIfNotConnected() throws Exception {
    client.disconnect();
    api.getLastLocation(client);
  }

  @Test public void getLastLocation_shouldCallService() {
    new LostApiClient.Builder(mock(Context.class))
        .addConnectionCallbacks(new LostApiClient.ConnectionCallbacks() {
          @Override public void onConnected() {
            api.getLastLocation(client);
            verify(service).getLastLocation(client);
          }

          @Override public void onConnectionSuspended() {
          }
        }).build().connect();
  }

  @Test(expected = IllegalStateException.class)
  public void getLocationAvailability_shouldThrowIfNotConnected() throws Exception {
    client.disconnect();
    api.getLocationAvailability(client);
  }

  @Test public void getLocationAvailability_shouldCallService() {
    new LostApiClient.Builder(mock(Context.class))
        .addConnectionCallbacks(new LostApiClient.ConnectionCallbacks() {
          @Override public void onConnected() {
            api.getLocationAvailability(client);
            verify(service).getLocationAvailability(client);
          }

          @Override public void onConnectionSuspended() {
          }
        }).build().connect();
  }

  @Test(expected = IllegalStateException.class)
  public void requestLocationUpdates_listener_shouldThrowIfNotConnected() throws Exception {
    client.disconnect();
    api.requestLocationUpdates(client, LocationRequest.create(), new TestLocationListener());
  }

  @Test(expected = IllegalStateException.class)
  public void requestLocationUpdates_pendingIntent_shouldThrowIfNotConnected() throws Exception {
    client.disconnect();
    api.requestLocationUpdates(client, LocationRequest.create(),mock(PendingIntent.class));
  }

  @Test(expected = IllegalStateException.class)
  public void requestLocationUpdates_callback_shouldThrowIfNotConnected() throws Exception {
    client.disconnect();
    api.requestLocationUpdates(client, LocationRequest.create(), new TestLocationCallback(),
        Looper.myLooper());
  }

  @Test public void requestLocationUpdates_listener_shouldCallService() {
    new LostApiClient.Builder(mock(Context.class))
        .addConnectionCallbacks(new LostApiClient.ConnectionCallbacks() {
          @Override public void onConnected() {
            LocationRequest request = LocationRequest.create();
            LocationListener listener = new TestLocationListener();
            api.requestLocationUpdates(client, request, listener);
            verify(service).requestLocationUpdates(client, request, listener);
          }

          @Override public void onConnectionSuspended() {
          }
        }).build().connect();
  }

  @Test public void requestLocationUpdates_pendingIntent_shouldCallService() {
    new LostApiClient.Builder(mock(Context.class))
        .addConnectionCallbacks(new LostApiClient.ConnectionCallbacks() {
          @Override public void onConnected() {
            LocationRequest request = LocationRequest.create();
            PendingIntent pendingIntent = mock(PendingIntent.class);
            api.requestLocationUpdates(client, request, pendingIntent);
            verify(service).requestLocationUpdates(client, request, pendingIntent);
          }

          @Override public void onConnectionSuspended() {
          }
        }).build().connect();
  }

  @Test public void requestLocationUpdates_callback_shouldCallService() {
    new LostApiClient.Builder(mock(Context.class))
        .addConnectionCallbacks(new LostApiClient.ConnectionCallbacks() {
          @Override public void onConnected() {
            LocationRequest request = LocationRequest.create();
            TestLocationCallback callback = new TestLocationCallback();
            Looper looper = Looper.myLooper();
            api.requestLocationUpdates(client, request, callback, looper);
            verify(service).requestLocationUpdates(client, request, callback, looper);
          }

          @Override public void onConnectionSuspended() {
          }
        }).build().connect();
  }

  @Test(expected = RuntimeException.class)
  public void requestLocationUpdates_listenerWithLooper_shouldThrowExceptionIfNotYetImplemented() {
    LocationRequest request = LocationRequest.create();
    TestLocationListener listener = new TestLocationListener();
    api.requestLocationUpdates(client, request, listener, null);
  }

  @Test(expected = IllegalStateException.class)
  public void removeLocationUpdates_listener_shouldThrowIfNotConnected() throws Exception {
    client.disconnect();
    api.removeLocationUpdates(client, new TestLocationListener());
  }

  @Test(expected = IllegalStateException.class)
  public void removeLocationUpdates_pendingIntent_shouldThrowIfNotConnected() throws Exception {
    client.disconnect();
    api.removeLocationUpdates(client, mock(PendingIntent.class));
  }

  @Test(expected = IllegalStateException.class)
  public void removeLocationUpdates_callback_shouldThrowIfNotConnected() throws Exception {
    client.disconnect();
    api.removeLocationUpdates(client, new TestLocationCallback());
  }

  @Test public void removeLocationUpdates_listener_shouldCallService() {
    new LostApiClient.Builder(mock(Context.class))
        .addConnectionCallbacks(new LostApiClient.ConnectionCallbacks() {
          @Override public void onConnected() {
            LocationListener listener = new TestLocationListener();
            api.removeLocationUpdates(client, listener);
            verify(service).removeLocationUpdates(client, listener);
          }

          @Override public void onConnectionSuspended() {
          }
        }).build().connect();
  }

  @Test public void removeLocationUpdates_pendingIntent_shouldCallService() {
    new LostApiClient.Builder(mock(Context.class))
        .addConnectionCallbacks(new LostApiClient.ConnectionCallbacks() {
          @Override public void onConnected() {
            PendingIntent callbackIntent = mock(PendingIntent.class);
            api.removeLocationUpdates(client, callbackIntent);
            verify(service).removeLocationUpdates(client, callbackIntent);
          }

          @Override public void onConnectionSuspended() {
          }
        }).build().connect();
  }

  @Test public void removeLocationUpdates_callback_shouldCallService() {
    new LostApiClient.Builder(mock(Context.class))
        .addConnectionCallbacks(new LostApiClient.ConnectionCallbacks() {
          @Override public void onConnected() {
            TestLocationCallback callback = new TestLocationCallback();
            api.removeLocationUpdates(client, callback);
            verify(service).removeLocationUpdates(client, callback);
          }

          @Override public void onConnectionSuspended() {
          }
        }).build().connect();
  }

  @Test public void setMockMode_shouldCallService() {
    api.setMockMode(client, true);
    verify(service).setMockMode(client, true);
  }

  @Test public void setMockLocation_shouldCallService() {
    Location location = new Location("test");
    api.setMockLocation(client, location);
    verify(service).setMockLocation(client, location);
  }

  @Test public void setMockTrace_shouldCallService() {
    File file = new File("path", "name");
    api.setMockTrace(client, file);
    verify(service).setMockTrace(client, file);
  }

  @Test public void isProviderEnabled_shouldCallService() {
    String provider = "provider";
    api.isProviderEnabled(client, provider);
    verify(service).isProviderEnabled(client, provider);
  }

  @Test public void getListeners() {
    api.getLocationListeners();
    verify(service).getLocationListeners();
  }

  @Test public void onConnect_shouldStartService() throws Exception {
    Context context = mock(Context.class);
    api.onConnect(context);
    verify(context).startService(new Intent(context, FusedLocationProviderService.class));
  }

  @Test public void onConnect_shouldBindService() throws Exception {
    Context context = mock(Context.class);
    api.onConnect(context);
    verify(context).bindService(new Intent(context, FusedLocationProviderService.class), api,
        Context.BIND_AUTO_CREATE);
  }

  @Test public void onDisconnect_shouldUnbindServiceIfBound() throws Exception {
    Context context = mock(Context.class);
    api.onConnect(context);
    api.onDisconnect();
    verify(context).unbindService(api);
  }

  @Test public void onDisconnect_shouldNotUnbindServiceIfNotBound() throws Exception {
    Context context = mock(Context.class);
    api.onConnect(context);
    api.onServiceDisconnected(mock(ComponentName.class));
    api.onDisconnect();
    verify(context, never()).unbindService(api);
  }

  @Test public void onDisconnect_shouldNotAttemptToUnbindServiceMoreThanOnce() throws Exception {
    Context context = mock(Context.class);
    api.onConnect(context);
    api.onDisconnect();
    api.onDisconnect();
    verify(context, times(1)).unbindService(api);
  }
}
