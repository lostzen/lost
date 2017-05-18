package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.BaseRobolectricTest;
import com.mapzen.android.lost.api.LocationCallback;
import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.android.lost.api.PendingResult;
import com.mapzen.android.lost.api.ResultCallback;
import com.mapzen.android.lost.api.Status;
import com.mapzen.lost.BuildConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.concurrent.TimeUnit;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.robolectric.RuntimeEnvironment.application;

@RunWith(RobolectricTestRunner.class)
@SuppressWarnings("MissingPermission")
@Config(constants = BuildConfig.class, sdk = 21, manifest = Config.NONE)
public class FusedLocationProviderApiImplTest extends BaseRobolectricTest {

  private LostApiClient connectedClient;
  private LostApiClient disconnectedClient;
  private FusedLocationProviderApiImpl api;
  private IFusedLocationProviderService service = mock(IFusedLocationProviderService.class);
  private FusedLocationServiceConnectionManager connectionManager;

  @Before public void setUp() throws Exception {
    LostClientManager.shared().clearClients();
    connectedClient = new LostApiClient.Builder(RuntimeEnvironment.application).build();
    connectedClient.connect();

    // do not call connect on this!
    disconnectedClient = new LostApiClient.Builder(RuntimeEnvironment.application).build();

    connectionManager = spy(new FusedLocationServiceConnectionManager());
    Mockito.doCallRealMethod().when(connectionManager).setEventCallbacks(any(
        FusedLocationServiceConnectionManager.EventCallbacks.class));
    Mockito.doCallRealMethod().when(connectionManager).connect(any(Context.class), any(
        LostApiClient.ConnectionCallbacks.class));
    api = new FusedLocationProviderApiImpl(connectionManager);
    api.connect(application, null);
    api.service = service;
  }

  @Test public void disconnect_shouldBeHarmlessBeforeConnect() {
    assertThat(!disconnectedClient.isConnected());
    disconnectedClient.disconnect();
    assertThat(!disconnectedClient.isConnected());
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
    connectedClient.disconnect();
    api.getLastLocation(connectedClient);
  }

  @Test public void getLastLocation_shouldCallService() throws Exception {
    api.getLastLocation(connectedClient);
    verify(service).getLastLocation();
  }

  @Test(expected = IllegalStateException.class)
  public void getLocationAvailability_shouldThrowIfNotConnected() throws Exception {
    connectedClient.disconnect();
    api.getLocationAvailability(connectedClient);
  }

  @Test public void getLocationAvailability_shouldCallService() throws Exception {
    api.getLocationAvailability(connectedClient);
    verify(service).getLocationAvailability();
  }

  @Test(expected = IllegalStateException.class)
  public void requestLocationUpdates_listener_shouldThrowIfNotConnected() throws Exception {
    connectedClient.disconnect();
    api.requestLocationUpdates(connectedClient, LocationRequest.create(),
        new TestLocationListener());
  }

  @Test(expected = IllegalStateException.class)
  public void requestLocationUpdates_pendingIntent_shouldThrowIfNotConnected() throws Exception {
    connectedClient.disconnect();
    api.requestLocationUpdates(connectedClient, LocationRequest.create(),
        mock(PendingIntent.class));
  }

  @Test(expected = IllegalStateException.class)
  public void requestLocationUpdates_callback_shouldThrowIfNotConnected() throws Exception {
    connectedClient.disconnect();
    api.requestLocationUpdates(connectedClient, LocationRequest.create(),
        new TestLocationCallback(), Looper.myLooper());
  }

  @Test public void requestLocationUpdates_listener_shouldCallService() throws Exception {
    LocationRequest request = LocationRequest.create();
    LocationListener listener = new TestLocationListener();
    api.requestLocationUpdates(connectedClient, request, listener);
    verify(service).requestLocationUpdates(request);
  }

  @Test public void requestLocationUpdates_pendingIntent_shouldCallService() throws Exception {
    LocationRequest request = LocationRequest.create();
    PendingIntent pendingIntent = mock(PendingIntent.class);
    api.requestLocationUpdates(connectedClient, request, pendingIntent);
    verify(service).requestLocationUpdates(request);
  }

  @Test public void requestLocationUpdates_callback_shouldCallService() throws Exception {
    LocationRequest request = LocationRequest.create();
    TestLocationCallback callback = new TestLocationCallback();
    Looper looper = Looper.myLooper();
    api.requestLocationUpdates(connectedClient, request, callback, looper);
    verify(service).requestLocationUpdates(request);
  }

  @Test(expected = RuntimeException.class)
  public void requestLocationUpdates_listenerWithLooper_shouldThrowExceptionIfNotYetImplemented() {
    LocationRequest request = LocationRequest.create();
    TestLocationListener listener = new TestLocationListener();
    api.requestLocationUpdates(connectedClient, request, listener, null);
  }

  @Test(expected = IllegalStateException.class)
  public void removeLocationUpdates_listener_shouldThrowIfNotConnected() throws Exception {
    connectedClient.disconnect();
    api.removeLocationUpdates(connectedClient, new TestLocationListener());
  }

  @Test(expected = IllegalStateException.class)
  public void removeLocationUpdates_pendingIntent_shouldThrowIfNotConnected() throws Exception {
    connectedClient.disconnect();
    api.removeLocationUpdates(connectedClient, mock(PendingIntent.class));
  }

  @Test(expected = IllegalStateException.class)
  public void removeLocationUpdates_callback_shouldThrowIfNotConnected() throws Exception {
    connectedClient.disconnect();
    api.removeLocationUpdates(connectedClient, new TestLocationCallback());
  }

  @Test public void removeLocationUpdates_listener_shouldCallService() throws Exception {
    LocationListener listener = new TestLocationListener();
    api.removeLocationUpdates(connectedClient, listener);
    verify(service).removeLocationUpdates();
  }

  @Test public void removeLocationUpdates_pendingIntent_shouldCallService() throws Exception {
    PendingIntent callbackIntent = mock(PendingIntent.class);
    api.removeLocationUpdates(connectedClient, callbackIntent);
    verify(service).removeLocationUpdates();
  }

  @Test public void removeLocationUpdates_callback_shouldCallService() throws Exception {
    TestLocationCallback callback = new TestLocationCallback();
    api.removeLocationUpdates(connectedClient, callback);
    verify(service).removeLocationUpdates();
  }

  @Test(expected = IllegalStateException.class)
  public void setMockMode_shouldThrowIfNotConnected() throws Exception {
    connectedClient.disconnect();
    api.setMockMode(connectedClient, true);
  }

  @Test(expected = IllegalStateException.class)
  public void setMockLocation_shouldThrowIfNotConnected() throws Exception {
    connectedClient.disconnect();
    api.setMockLocation(connectedClient, new Location("test"));
  }

  @Test(expected = IllegalStateException.class)
  public void setMockTrace_shouldThrowIfNotConnected() throws Exception {
    connectedClient.disconnect();
    api.setMockTrace(connectedClient, "path", "name");
  }

  @Test public void setMockMode_shouldCallService() throws Exception {
    api.setMockMode(connectedClient, true);
    verify(service).setMockMode(true);
  }

  @Test public void setMockLocation_shouldCallService() throws Exception {
    Location location = new Location("test");
    api.setMockLocation(connectedClient, location);
    verify(service).setMockLocation(location);
  }

  @Test public void setMockTrace_shouldCallService() throws Exception {
    api.setMockTrace(connectedClient, "path", "name");
    verify(service).setMockTrace("path", "name");
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

  @Test public void onServiceConnected_shouldRegisterServiceCallbacks() throws Exception {
    TestServiceStub binder = new TestServiceStub();
    api.onServiceConnected(binder);
    assertThat(binder.callback).isNotNull();
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

  @Test public void onDisconnect_shouldUnregisterServiceCallbacks() throws Exception {
    Context context = mock(Context.class);
    api.onConnect(context);
    api.onDisconnect();
    verify(service).init(null);
  }

  @Test public void removeLocationUpdates_shouldReturnStatusSuccessIfListenerRemoved() {
    TestResultCallback callback = new TestResultCallback();
    TestLocationListener listener = new TestLocationListener();
    api.requestLocationUpdates(connectedClient, LocationRequest.create(), listener);
    PendingResult<Status> result = api.removeLocationUpdates(connectedClient, listener);
    result.setResultCallback(callback);
    assertThat(callback.status.isSuccess()).isTrue();
  }

  @Test public void removeLocationUpdates_shouldNotReturnStatusSuccessIfListenerNotRemoved() {
    TestResultCallback callback = new TestResultCallback();
    TestLocationListener listener = new TestLocationListener();
    LostClientManager.shared().removeListener(connectedClient, listener);
    PendingResult<Status> result = api.removeLocationUpdates(connectedClient, listener);
    result.setResultCallback(callback);
    assertThat(callback.status).isNull();
  }

  @Test public void removeLocationUpdates_shouldReturnStatusSuccessIfPendingIntentRemoved() {
    TestResultCallback callback = new TestResultCallback();
    PendingIntent pendingIntent = mock(PendingIntent.class);
    api.requestLocationUpdates(connectedClient, LocationRequest.create(), pendingIntent);
    PendingResult<Status> result = api.removeLocationUpdates(connectedClient, pendingIntent);
    result.setResultCallback(callback);
    assertThat(callback.status.isSuccess()).isTrue();
  }

  @Test public void removeLocationUpdates_shouldNotReturnStatusSuccessIfPendingIntentNotRemoved() {
    TestResultCallback callback = new TestResultCallback();
    PendingIntent pendingIntent = mock(PendingIntent.class);
    PendingResult<Status> result = api.removeLocationUpdates(connectedClient, pendingIntent);
    result.setResultCallback(callback);
    assertThat(callback.status).isNull();
  }

  @Test public void removeLocationUpdates_shouldReturnStatusSuccessIfCallbackRemoved() {
    TestResultCallback resultCallback = new TestResultCallback();
    LocationCallback locationCallback = new TestLocationCallback();
    api.requestLocationUpdates(connectedClient, LocationRequest.create(), locationCallback,
        Looper.myLooper());
    PendingResult<Status> result = api.removeLocationUpdates(connectedClient, locationCallback);
    result.setResultCallback(resultCallback);
    assertThat(resultCallback.status.isSuccess()).isTrue();
  }

  @Test public void removeLocationUpdates_shouldNotReturnStatusSuccessIfCallbackNotRemoved() {
    TestResultCallback callback = new TestResultCallback();
    LocationCallback locationCallback = new TestLocationCallback();
    PendingResult<Status> result = api.removeLocationUpdates(connectedClient, locationCallback);
    result.setResultCallback(callback);
    assertThat(callback.status).isNull();
  }

  @Test public void removeLocationUpdates_shouldModifyOnlyClientListeners() {
    TestLocationListener listener = new TestLocationListener();
    api.requestLocationUpdates(connectedClient, LocationRequest.create(), listener);

    LostApiClient otherClient = new LostApiClient.Builder(RuntimeEnvironment.application).build();
    otherClient.connect();
    api.requestLocationUpdates(otherClient, LocationRequest.create(), listener);
    api.removeLocationUpdates(connectedClient, listener);

    assertThat(api.getLocationListeners().get(connectedClient)).isEmpty();
    assertThat(api.getLocationListeners().get(otherClient).size()).isEqualTo(1);
  }

  @Test public void removeLocationUpdates_shouldKillEngineIfNoListenersStillActive()
      throws Exception {
    TestLocationListener listener = new TestLocationListener();
    api.requestLocationUpdates(connectedClient, LocationRequest.create(), listener);
    api.removeLocationUpdates(connectedClient, listener);
    verify(service).removeLocationUpdates();
  }

  @Test public void removeLocationUpdates_shouldNotKillEngineIfListenerStillActive()
      throws Exception {
    TestLocationListener listener1 = new TestLocationListener();
    TestLocationListener listener2 = new TestLocationListener();
    api.requestLocationUpdates(connectedClient, LocationRequest.create(), listener1);
    api.requestLocationUpdates(connectedClient, LocationRequest.create(), listener2);
    api.removeLocationUpdates(connectedClient, listener1);
    verify(service, never()).removeLocationUpdates();
  }

  @Test public void requestLocationUpdates_listener_shouldReturnFusedLocationPendingResult() {
    PendingResult<Status> result = api.requestLocationUpdates(connectedClient,
        LocationRequest.create(), new TestLocationListener());
    assertThat(result.await().getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
    assertThat(result.await(1000, TimeUnit.MILLISECONDS).getStatus().getStatusCode()).isEqualTo(
        Status.SUCCESS);
    assertThat(result.isCanceled()).isFalse();
    com.mapzen.android.lost.internal.TestResultCallback
        callback = new com.mapzen.android.lost.internal.TestResultCallback();
    result.setResultCallback(callback);
    assertThat(callback.getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
    com.mapzen.android.lost.internal.TestResultCallback
        otherCallback = new com.mapzen.android.lost.internal.TestResultCallback();
    result.setResultCallback(otherCallback, 1000, TimeUnit.MILLISECONDS);
    assertThat(otherCallback.getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
  }

  @Test public void requestLocationUpdates_pendingIntent_shouldReturnFusedLocationPendingResult() {
    PendingResult<Status> result = api.requestLocationUpdates(connectedClient,
        LocationRequest.create(), mock(PendingIntent.class));
    assertThat(result.await().getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
    assertThat(result.await(1000, TimeUnit.MILLISECONDS).getStatus().getStatusCode()).isEqualTo(
        Status.SUCCESS);
    assertThat(result.isCanceled()).isFalse();
    com.mapzen.android.lost.internal.TestResultCallback
        callback = new com.mapzen.android.lost.internal.TestResultCallback();
    result.setResultCallback(callback);
    assertThat(callback.getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
    com.mapzen.android.lost.internal.TestResultCallback
        otherCallback = new com.mapzen.android.lost.internal.TestResultCallback();
    result.setResultCallback(otherCallback, 1000, TimeUnit.MILLISECONDS);
    assertThat(otherCallback.getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
  }

  @Test public void requestLocationUpdates_callback_shouldReturnFusedLocationPendingResult() {
    PendingResult<Status> result = api.requestLocationUpdates(connectedClient,
        LocationRequest.create(), new TestLocationCallback(), Looper.myLooper());
    assertThat(result.await().getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
    assertThat(result.await(1000, TimeUnit.MILLISECONDS).getStatus().getStatusCode()).isEqualTo(
        Status.SUCCESS);
    assertThat(result.isCanceled()).isFalse();
    com.mapzen.android.lost.internal.TestResultCallback
        callback = new com.mapzen.android.lost.internal.TestResultCallback();
    result.setResultCallback(callback);
    assertThat(callback.getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
    com.mapzen.android.lost.internal.TestResultCallback
        otherCallback = new com.mapzen.android.lost.internal.TestResultCallback();
    result.setResultCallback(otherCallback, 1000, TimeUnit.MILLISECONDS);
    assertThat(otherCallback.getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
  }

  @Test public void setMockMode_shouldReturnFusedLocationPendingResult() {
    PendingResult<Status> result = api.setMockMode(connectedClient, true);
    assertThat(result.await().getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
    assertThat(result.await(1000, TimeUnit.MILLISECONDS).getStatus().getStatusCode()).isEqualTo(
        Status.SUCCESS);
    assertThat(result.isCanceled()).isFalse();
    com.mapzen.android.lost.internal.TestResultCallback
        callback = new com.mapzen.android.lost.internal.TestResultCallback();
    result.setResultCallback(callback);
    assertThat(callback.getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
    com.mapzen.android.lost.internal.TestResultCallback
        otherCallback = new com.mapzen.android.lost.internal.TestResultCallback();
    result.setResultCallback(otherCallback, 1000, TimeUnit.MILLISECONDS);
    assertThat(otherCallback.getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
  }

  @Test public void setMockLocation_shouldReturnFusedLocationPendingResult() {
    PendingResult<Status> result = api.setMockLocation(connectedClient, new Location("test"));
    assertThat(result.await().getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
    assertThat(result.await(1000, TimeUnit.MILLISECONDS).getStatus().getStatusCode()).isEqualTo(
        Status.SUCCESS);
    assertThat(result.isCanceled()).isFalse();
    com.mapzen.android.lost.internal.TestResultCallback
        callback = new com.mapzen.android.lost.internal.TestResultCallback();
    result.setResultCallback(callback);
    assertThat(callback.getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
    com.mapzen.android.lost.internal.TestResultCallback
        otherCallback = new com.mapzen.android.lost.internal.TestResultCallback();
    result.setResultCallback(otherCallback, 1000, TimeUnit.MILLISECONDS);
    assertThat(otherCallback.getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
  }

  @Test public void setMockTrace_shouldReturnFusedLocationPendingResult() {
    PendingResult<Status> result = api.setMockTrace(connectedClient, "path", "name");
    assertThat(result.await().getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
    assertThat(result.await(1000, TimeUnit.MILLISECONDS).getStatus().getStatusCode()).isEqualTo(
        Status.SUCCESS);
    assertThat(result.isCanceled()).isFalse();
    com.mapzen.android.lost.internal.TestResultCallback
        callback = new com.mapzen.android.lost.internal.TestResultCallback();
    result.setResultCallback(callback);
    assertThat(callback.getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
    com.mapzen.android.lost.internal.TestResultCallback
        otherCallback = new com.mapzen.android.lost.internal.TestResultCallback();
    result.setResultCallback(otherCallback, 1000, TimeUnit.MILLISECONDS);
    assertThat(otherCallback.getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
  }

  private class TestResultCallback implements ResultCallback<Status> {
    private Status status;

    @Override public void onResult(@NonNull Status result) {
      status = result;
    }
  }
}
