package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.lost.BuildConfig;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowEnvironment;
import org.robolectric.shadows.ShadowLocationManager;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.util.ReflectionHelpers;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Environment;
import android.os.Looper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.content.Context.LOCATION_SERVICE;
import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static com.mapzen.android.lost.api.FusedLocationProviderApi.KEY_LOCATION_CHANGED;
import static com.mapzen.android.lost.api.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
import static com.mapzen.android.lost.api.LocationRequest.PRIORITY_HIGH_ACCURACY;
import static org.fest.assertions.api.Assertions.assertThat;
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
        new ComponentName("com.mapzen.lost","FusedLocationProviderService"), stubBinder);
  }

  @Test public void getLastLocation_shouldCallService() {
    api.getLastLocation();
    verify(service).getLastLocation();
  }

  @Test public void requestLocationUpdates_listener_shouldCallService() {
    LocationRequest request = LocationRequest.create();
    LocationListener listener = new TestLocationListener();
    api.requestLocationUpdates(request, listener);
    verify(service).requestLocationUpdates(request, listener);
  }

  @Test(expected= RuntimeException.class)
  public void requestLocationUpdates_shouldThrowException() {
    LocationRequest request = LocationRequest.create();
    api.requestLocationUpdates(request, null, null);
  }

  @Test
  public void requestLocationUpdates_pendingIntent_shouldCallService() {
    LocationRequest request = LocationRequest.create();
    PendingIntent callbackIntent = mock(PendingIntent.class);
    api.requestLocationUpdates(request, callbackIntent);
    verify(service).requestLocationUpdates(request, callbackIntent);
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

  class TestLocationListener implements LocationListener {

    @Override public void onLocationChanged(Location location) {

    }

    @Override public void onProviderDisabled(String provider) {

    }

    @Override public void onProviderEnabled(String provider) {

    }
  }
}
