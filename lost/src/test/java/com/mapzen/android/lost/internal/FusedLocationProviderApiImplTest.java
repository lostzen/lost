package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.lost.BuildConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.location.Location;
import android.os.Looper;

import java.io.File;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.RuntimeEnvironment.application;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricGradleTestRunner.class)
@SuppressWarnings("MissingPermission")
@Config(constants = BuildConfig.class, sdk = 21, manifest = Config.NONE)
public class FusedLocationProviderApiImplTest {

  private LostApiClient client;
  private FusedLocationProviderApiImpl api;
  private FusedLocationProviderService service;

  @Before public void setUp() throws Exception {
    mockService();
    client = new LostApiClient.Builder(mock(Context.class)).build();
    api = new FusedLocationProviderApiImpl();
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

  @Test public void getLastLocation_shouldCallService() {
    api.getLastLocation(client);
    verify(service).getLastLocation(client);
  }

  @Test public void getLocationAvailability_shouldCallService() {
    api.getLocationAvailability(client);
    verify(service).getLocationAvailability(client);
  }

  @Test public void requestLocationUpdates_listener_shouldCallService() {
    LocationRequest request = LocationRequest.create();
    LocationListener listener = new TestLocationListener();
    api.requestLocationUpdates(client, request, listener);
    verify(service).requestLocationUpdates(client, request, listener);
  }

  @Test(expected = RuntimeException.class)
  public void requestLocationUpdates_shouldThrowException() {
    LocationRequest request = LocationRequest.create();
    TestLocationListener listener = new TestLocationListener();
    api.requestLocationUpdates(client, request, listener, null);
  }

  @Test public void requestLocationUpdates_callback_shouldCallService() {
    LocationRequest request = LocationRequest.create();
    TestLocationCallback callback = new TestLocationCallback();
    Looper looper = Looper.myLooper();
    api.requestLocationUpdates(client, request, callback, looper);
    verify(service).requestLocationUpdates(client, request, callback, looper);
  }

  @Test public void removeLocationUpdates_listener_shouldCallService() {
    LocationListener listener = new TestLocationListener();
    api.removeLocationUpdates(client, listener);
    verify(service).removeLocationUpdates(client, listener);
  }

  @Test public void removeLocationUpdates_pendingIntent_shouldCallService() {
    PendingIntent callbackIntent = mock(PendingIntent.class);
    api.removeLocationUpdates(client, callbackIntent);
    verify(service).removeLocationUpdates(client, callbackIntent);
  }

  @Test public void removeLocationUpdates_callback_shouldCallService() {
    TestLocationCallback callback = new TestLocationCallback();
    service.removeLocationUpdates(client, callback);
    verify(service).removeLocationUpdates(client, callback);
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

}
