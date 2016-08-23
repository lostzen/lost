package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.lost.BuildConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.location.Location;
import android.os.Looper;

import java.io.File;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.RuntimeEnvironment.application;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, manifest = Config.NONE)
public class FusedLocationProviderApiImplTest {

  private FusedLocationProviderApiImpl api;
  private FusedLocationProviderService service;

  @Before public void setUp() throws Exception {
    mockService();
    api = new FusedLocationProviderApiImpl(application);
    api.connect(null);
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
    api.getLastLocation();
    verify(service).getLastLocation();
  }

  @Test public void getLocationAvailability_shouldCallService() {
    api.getLocationAvailability();
    verify(service).getLocationAvailability();
  }

  @Test public void requestLocationUpdates_listener_shouldCallService() {
    LocationRequest request = LocationRequest.create();
    LocationListener listener = new TestLocationListener();
    api.requestLocationUpdates(request, listener);
    verify(service).requestLocationUpdates(request, listener);
  }

  @Test(expected = RuntimeException.class)
  public void requestLocationUpdates_shouldThrowException() {
    LocationRequest request = LocationRequest.create();
    TestLocationListener listener = new TestLocationListener();
    api.requestLocationUpdates(request, listener, null);
  }

  @Test public void requestLocationUpdates_callback_shouldCallService() {
    LocationRequest request = LocationRequest.create();
    TestLocationCallback callback = new TestLocationCallback();
    Looper looper = Looper.myLooper();
    api.requestLocationUpdates(request, callback, looper);
    verify(service).requestLocationUpdates(request, callback, looper);
  }

  @Test public void removeLocationUpdates_listener_shouldCallService() {
    LocationListener listener = new TestLocationListener();
    api.removeLocationUpdates(listener);
    verify(service).removeLocationUpdates(listener);
  }

  @Test public void removeLocationUpdates_pendingIntent_shouldCallService() {
    PendingIntent callbackIntent = mock(PendingIntent.class);
    api.removeLocationUpdates(callbackIntent);
    verify(service).removeLocationUpdates(callbackIntent);
  }

  @Test public void removeLocationUpdates_callback_shouldCallService() {
    TestLocationCallback callback = new TestLocationCallback();
    service.removeLocationUpdates(callback);
    verify(service).removeLocationUpdates(callback);
  }

  @Test public void setMockMode_shouldCallService() {
    api.setMockMode(true);
    verify(service).setMockMode(true);
  }

  @Test public void setMockLocation_shouldCallService() {
    Location location = new Location("test");
    api.setMockLocation(location);
    verify(service).setMockLocation(location);
  }

  @Test public void setMockTrace_shouldCallService() {
    File file = new File("path", "name");
    api.setMockTrace(file);
    verify(service).setMockTrace(file);
  }

  @Test public void isProviderEnabled_shouldCallService() {
    String provider = "provider";
    api.isProviderEnabled(provider);
    verify(service).isProviderEnabled(provider);
  }

  @Test public void getListeners() {
    api.getListeners();
    verify(service).getListeners();
  }

}
