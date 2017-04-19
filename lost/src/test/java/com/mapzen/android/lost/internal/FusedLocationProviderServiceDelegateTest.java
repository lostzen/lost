package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.BaseRobolectricTest;
import com.mapzen.android.lost.api.LocationAvailability;
import com.mapzen.android.lost.api.LocationCallback;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LocationResult;
import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.android.lost.api.PendingResult;
import com.mapzen.android.lost.api.Status;
import com.mapzen.android.lost.shadows.LostShadowLocationManager;
import com.mapzen.lost.BuildConfig;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.internal.ShadowExtractor;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowEnvironment;
import org.robolectric.shadows.ShadowLooper;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Environment;
import android.os.Looper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static android.content.Context.LOCATION_SERVICE;
import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static com.mapzen.android.lost.api.FusedLocationProviderApi.KEY_LOCATION_CHANGED;
import static com.mapzen.android.lost.api.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
import static com.mapzen.android.lost.api.LocationRequest.PRIORITY_HIGH_ACCURACY;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.RuntimeEnvironment.application;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@SuppressWarnings("MissingPermission")
@Config(constants = BuildConfig.class, sdk = 21, manifest = Config.NONE, shadows = {
        LostShadowLocationManager.class})
public class FusedLocationProviderServiceDelegateTest extends BaseRobolectricTest {
  private LostApiClient client;
  private FusedLocationProviderServiceDelegate api;
  private LocationManager locationManager;
  private LostShadowLocationManager shadowLocationManager;
  private LostApiClient otherClient;
  private ClientManager clientManager;

  @Before public void setUp() throws Exception {

    mockService();
    client = new LostApiClient.Builder(mock(Context.class)).build();
    otherClient = new LostApiClient.Builder(mock(Context.class)).build();
    clientManager = LostClientManager.shared();
    api = new FusedLocationProviderServiceDelegate(application, clientManager);
    locationManager = (LocationManager) application.getSystemService(LOCATION_SERVICE);
    shadowLocationManager = (LostShadowLocationManager) ShadowExtractor.extract(locationManager);
    client.connect();
  }

  @After public void tearDown() {
    client.disconnect();
    otherClient.disconnect();
  }

  private void mockService() {
    FusedLocationProviderService.FusedLocationProviderBinder stubBinder =
        mock(FusedLocationProviderService.FusedLocationProviderBinder.class);
    when(stubBinder.getService()).thenReturn(mock(FusedLocationProviderService.class));
    shadowOf(application).setComponentNameAndServiceForBindService(
        new ComponentName("com.mapzen.lost", "FusedLocationProviderService"), stubBinder);
  }

  @Test public void shouldNotBeNull() throws Exception {
    assertThat(api).isNotNull();
  }

  @Test public void getLastLocation_shouldReturnMostRecentLocation() throws Exception {
    Location location = new Location(GPS_PROVIDER);
    shadowLocationManager.setLastKnownLocation(GPS_PROVIDER, location);
    assertThat(api.getLastLocation()).isNotNull();
  }

  @Test public void requestLocationUpdates_shouldRegisterGpsAndNetworkListener() throws Exception {
    api.requestLocationUpdates(LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY));
    assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).hasSize(2);
  }

  @Test public void requestLocationUpdates_shouldNotifyOnLocationChangedGps() throws Exception {
    TestLocationListener listener = new TestLocationListener();
    LocationRequest request = LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY);
    api.requestLocationUpdates(request);
    LostClientManager.shared().addListener(client, request, listener);
    Location location = new Location(GPS_PROVIDER);
    shadowLocationManager.simulateLocation(location);
    assertThat(listener.getMostRecentLocation()).isEqualTo(location);
  }

  @Test public void requestLocationUpdates_shouldNotifyOnLocationChangedNetwork() throws Exception {
    TestLocationListener listener = new TestLocationListener();
    api.requestLocationUpdates(LocationRequest.create());
    LostClientManager.shared().addListener(client, LocationRequest.create(), listener);
    Location location = new Location(NETWORK_PROVIDER);
    shadowLocationManager.simulateLocation(location);
    assertThat(listener.getMostRecentLocation()).isEqualTo(location);
  }

  @Test public void requestLocationUpdates_shouldNotNotifyIfLessThanFastestIntervalGps()
      throws Exception {
    TestLocationListener listener = new TestLocationListener();
    LocationRequest request = LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY);
    request.setFastestInterval(5000);
    api.requestLocationUpdates(request);
    LostClientManager.shared().addListener(client, request, listener);

    final long time = System.currentTimeMillis();
    Location location1 = getTestLocation(GPS_PROVIDER, 0, 0, time);
    Location location2 = getTestLocation(GPS_PROVIDER, 1, 1, time + 1000);

    shadowLocationManager.simulateLocation(location1);
    shadowLocationManager.simulateLocation(location2);
    assertThat(listener.getMostRecentLocation()).isEqualTo(location1);
  }

  @Test public void requestLocationUpdates_shouldNotNotifyIfLessThanFastestIntervalNetwork()
      throws Exception {
    TestLocationListener listener = new TestLocationListener();
    LocationRequest request = LocationRequest.create();
    request.setFastestInterval(5000);
    api.requestLocationUpdates(request);
    LostClientManager.shared().addListener(client, request, listener);

    final long time = System.currentTimeMillis();
    Location location1 = getTestLocation(NETWORK_PROVIDER, 0, 0, time);
    Location location2 = getTestLocation(NETWORK_PROVIDER, 1, 1, time + 1000);

    shadowLocationManager.simulateLocation(location1);
    shadowLocationManager.simulateLocation(location2);
    assertThat(listener.getMostRecentLocation()).isEqualTo(location1);
  }

  @Test public void requestLocationUpdates_shouldNotNotifyIfLessThanSmallestDisplacementGps()
      throws Exception {
    TestLocationListener listener = new TestLocationListener();
    LocationRequest request = LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY);
    request.setSmallestDisplacement(200000);
    api.requestLocationUpdates(request);
    LostClientManager.shared().addListener(client, request, listener);

    final long time = System.currentTimeMillis();
    Location location1 = getTestLocation(GPS_PROVIDER, 0, 0, time);
    Location location2 = getTestLocation(GPS_PROVIDER, 1, 1, time + 1000);

    shadowLocationManager.simulateLocation(location1);
    shadowLocationManager.simulateLocation(location2);
    assertThat(listener.getMostRecentLocation()).isEqualTo(location1);
  }

  @Test public void requestLocationUpdates_shouldNotNotifyIfLessThanSmallestDisplacementNetwork()
      throws Exception {
    TestLocationListener listener = new TestLocationListener();
    LocationRequest request = LocationRequest.create();
    request.setSmallestDisplacement(200000);
    api.requestLocationUpdates(request);
    LostClientManager.shared().addListener(client, request, listener);

    final long time = System.currentTimeMillis();
    Location location1 = getTestLocation(NETWORK_PROVIDER, 0, 0, time);
    Location location2 = getTestLocation(NETWORK_PROVIDER, 1, 1, time + 1000);

    shadowLocationManager.simulateLocation(location1);
    shadowLocationManager.simulateLocation(location2);
    assertThat(listener.getMostRecentLocation()).isEqualTo(location1);
  }

  @Test public void requestLocationUpdates_shouldIgnoreNetworkWhenGpsIsMoreAccurate()
      throws Exception {
    TestLocationListener listener = new TestLocationListener();
    LocationRequest request = LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY);
    request.setFastestInterval(0);
    request.setSmallestDisplacement(0);
    api.requestLocationUpdates(request);
    LostClientManager.shared().addListener(client, request, listener);

    final long time = System.currentTimeMillis();
    Location gpsLocation = getTestLocation(GPS_PROVIDER, 0, 0, time);
    Location networkLocation = getTestLocation(NETWORK_PROVIDER, 0, 0, time + 1);

    gpsLocation.setAccuracy(10);
    networkLocation.setAccuracy(20);
    shadowLocationManager.simulateLocation(gpsLocation);
    shadowLocationManager.simulateLocation(networkLocation);
    assertThat(listener.getMostRecentLocation()).isEqualTo(gpsLocation);
  }

  @Test public void requestLocationUpdates_shouldIgnoreGpsWhenNetworkIsMoreAccurate()
      throws Exception {
    TestLocationListener listener = new TestLocationListener();
    LocationRequest request = LocationRequest.create();
    request.setFastestInterval(0);
    request.setSmallestDisplacement(0);
    api.requestLocationUpdates(request);
    LostClientManager.shared().addListener(client, request, listener);

    final long time = System.currentTimeMillis();
    Location networkLocation = getTestLocation(NETWORK_PROVIDER, 0, 0, time);
    Location gpsLocation = getTestLocation(GPS_PROVIDER, 0, 0, time + 1);

    networkLocation.setAccuracy(10);
    gpsLocation.setAccuracy(20);
    shadowLocationManager.simulateLocation(networkLocation);
    shadowLocationManager.simulateLocation(gpsLocation);
    assertThat(listener.getMostRecentLocation()).isEqualTo(networkLocation);
  }

  @Test public void requestLocationUpdates_shouldNotifyWithLocationAvailabilityInPendingIntent()
      throws Exception {
    Intent intent = new Intent(application, TestService.class);
    PendingIntent pendingIntent = PendingIntent.getService(application, 0, intent, 0);
    LocationRequest locationRequest =
        LocationRequest.create().setPriority(PRIORITY_BALANCED_POWER_ACCURACY);
    api.requestLocationUpdates(locationRequest);
    LostClientManager.shared().addPendingIntent(client, locationRequest, pendingIntent);
    Location location = new Location(NETWORK_PROVIDER);
    shadowLocationManager.simulateLocation(location);
    Intent nextStartedService = ShadowApplication.getInstance().getNextStartedService();
    assertThat(nextStartedService).isNotNull();
    assertThat(nextStartedService.getParcelableExtra(
        LocationAvailability.EXTRA_LOCATION_AVAILABILITY)).isNotNull();
  }

  @Test public void requestLocationUpdates_shouldNotifyWithLocationResultInPendingIntent()
      throws Exception {
    Intent intent = new Intent(application, TestService.class);
    PendingIntent pendingIntent = PendingIntent.getService(application, 0, intent, 0);
    LocationRequest locationRequest =
        LocationRequest.create().setPriority(PRIORITY_BALANCED_POWER_ACCURACY);

    LostClientManager.shared().addPendingIntent(client, locationRequest, pendingIntent);
    api.requestLocationUpdates(locationRequest);
    Location location = new Location(NETWORK_PROVIDER);
    shadowLocationManager.simulateLocation(location);

    Intent nextStartedService = ShadowApplication.getInstance().getNextStartedService();
    assertThat(nextStartedService).isNotNull();
    assertThat(
        nextStartedService.getParcelableExtra(LocationResult.EXTRA_LOCATION_RESULT)).isNotNull();
  }

  @Test public void removeLocationUpdates_shouldUnregisterAllListeners() throws Exception {
    api.requestLocationUpdates(LocationRequest.create());
    api.removeLocationUpdates();
    assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).isEmpty();
  }

  @Test public void setMockMode_shouldUnregisterAllListenersWhenTrue() throws Exception {
    LocationRequest request = LocationRequest.create();
    api.requestLocationUpdates(request);
    api.setMockMode(true);
    assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).isEmpty();
  }

  @Test public void setMockMode_shouldNotRegisterDuplicateListeners() throws Exception {
    LocationRequest request = LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY);
    api.setMockMode(true);
    api.requestLocationUpdates(request);
    api.setMockMode(false);
    api.requestLocationUpdates(request);
    assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).hasSize(2);
  }

  @Test public void setMockMode_shouldToggleEngines() {
    TestLocationListener listener = new TestLocationListener();
    LocationRequest request = LocationRequest.create();
    api.requestLocationUpdates(request);
    LostClientManager.shared().addListener(client, request, listener);

    api.setMockMode(true);
    TestLocationListener listener2 = new TestLocationListener();
    LocationRequest request2 = LocationRequest.create();
    api.requestLocationUpdates(request2);
    LostClientManager.shared().addListener(client, request2, listener2);

    assertThat(clientManager.getLocationListeners().get(client)).hasSize(2);
  }

  @Test public void requestLocationUpdates_shouldNotRegisterListenersWithMockModeOn()
      throws Exception {
    api.setMockMode(true);
    LocationRequest request = LocationRequest.create();
    api.requestLocationUpdates(request);
    assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).isEmpty();
  }

  @Test public void setMockLocation_shouldReturnMockLastLocation() throws Exception {
    Location mockLocation = new Location("mock");
    api.setMockMode(true);
    api.setMockLocation(mockLocation);
    assertThat(api.getLastLocation()).isEqualTo(mockLocation);
  }

  @Test public void setMockLocation_shouldInvokeListenerOnce() throws Exception {
    Location mockLocation = new Location("mock");
    api.setMockMode(true);
    TestLocationListener listener = new TestLocationListener();
    LocationRequest request = LocationRequest.create();
    api.requestLocationUpdates(request);
    LostClientManager.shared().addListener(client, request, listener);
    api.setMockLocation(mockLocation);
    assertThat(listener.getAllLocations()).hasSize(1);
    assertThat(listener.getMostRecentLocation()).isEqualTo(mockLocation);
  }

  public void setMockTrace_shouldInvokeListenerForEachLocation() throws Exception {
    api.setMockMode(true);
    api.setMockTrace(getTestGpxTrace());
    TestLocationListener listener = new TestLocationListener();
    LocationRequest request = LocationRequest.create();
    request.setFastestInterval(0);
    api.requestLocationUpdates(request);
    Thread.sleep(100);
    ShadowLooper.runUiThreadTasks();
    assertThat(listener.getAllLocations()).hasSize(3);
    assertThat(listener.getAllLocations().get(0).getLatitude()).isEqualTo(0.0);
    assertThat(listener.getAllLocations().get(0).getLongitude()).isEqualTo(0.1);
    assertThat(listener.getAllLocations().get(1).getLatitude()).isEqualTo(1.0);
    assertThat(listener.getAllLocations().get(1).getLongitude()).isEqualTo(1.1);
    assertThat(listener.getAllLocations().get(2).getLatitude()).isEqualTo(2.0);
    assertThat(listener.getAllLocations().get(2).getLongitude()).isEqualTo(2.1);
  }

  public void setMockTrace_shouldBroadcastSpeedWithLocation() throws Exception {
    api.setMockMode(true);
    api.setMockTrace(getTestGpxTrace());
    TestLocationListener listener = new TestLocationListener();
    LocationRequest request = LocationRequest.create();
    request.setFastestInterval(0);
    api.requestLocationUpdates(request);
    Thread.sleep(100);
    ShadowLooper.runUiThreadTasks();
    assertThat(listener.getAllLocations().get(0).getSpeed()).isEqualTo(10f);
    assertThat(listener.getAllLocations().get(1).getSpeed()).isEqualTo(20f);
    assertThat(listener.getAllLocations().get(2).getSpeed()).isEqualTo(30f);
  }

  public void setMockTrace_shouldRespectFastestInterval() throws Exception {
    api.setMockMode(true);
    api.setMockTrace(getTestGpxTrace());
    TestLocationListener listener = new TestLocationListener();
    LocationRequest request = LocationRequest.create();
    request.setInterval(100);
    api.requestLocationUpdates(request);
    Thread.sleep(100);
    ShadowLooper.runUiThreadTasks();
    assertThat(listener.getAllLocations()).hasSize(1);
    Thread.sleep(100);
    ShadowLooper.runUiThreadTasks();
    assertThat(listener.getAllLocations()).hasSize(2);
    Thread.sleep(100);
    ShadowLooper.runUiThreadTasks();
    assertThat(listener.getAllLocations()).hasSize(3);
  }

  private static Location getTestLocation(String provider, float lat, float lng, long time) {
    Location location = new Location(provider);
    location.setLatitude(lat);
    location.setLongitude(lng);
    location.setTime(time);
    return location;
  }

  private File getTestGpxTrace() throws IOException {
    String contents = Files.toString(new File("src/test/resources/lost.gpx"), Charsets.UTF_8);
    ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);
    File directory = Environment.getExternalStorageDirectory();
    File file = new File(directory, "lost.gpx");
    FileWriter fileWriter = new FileWriter(file, false);
    fileWriter.write(contents);
    fileWriter.close();
    return file;
  }

  @Test public void requestLocationUpdates_shouldNotifyBothListeners() {
    LocationRequest request = LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY);
    TestLocationListener listener1 = new TestLocationListener();
    TestLocationListener listener2 = new TestLocationListener();
    api.requestLocationUpdates(request);
    api.requestLocationUpdates(request);
    LostClientManager.shared().addListener(client, request, listener1);
    LostClientManager.shared().addListener(client, request, listener2);
    Location location = new Location(GPS_PROVIDER);
    location.setLatitude(40.0);
    location.setLongitude(70.0);
    shadowLocationManager.simulateLocation(location);
    assertThat(listener1.getAllLocations()).contains(location);
    assertThat(listener2.getAllLocations()).contains(location);
  }

  @Test public void requestLocationUpdates_shouldNotNotifyRemovedListener() {
    LocationRequest request = LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY);
    TestLocationListener listener1 = new TestLocationListener();
    TestLocationListener listener2 = new TestLocationListener();
    api.requestLocationUpdates(request);
    LostClientManager.shared().addListener(client, request, listener1);
    LostClientManager.shared().addListener(client, request, listener2);
    LostClientManager.shared().removeListener(client, listener2);
    Location location = new Location(GPS_PROVIDER);
    shadowLocationManager.simulateLocation(location);
    assertThat(listener1.getAllLocations()).contains(location);
    assertThat(listener2.getAllLocations()).doesNotContain(location);
  }

  @Test public void requestLocationUpdates_shouldRegisterGpsAndNetworkListenerViaPendingIntent()
      throws Exception {
    api.requestLocationUpdates(LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY));
    assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).hasSize(2);
  }

  @Test public void requestLocationUpdates_shouldNotifyOnLocationChangedGpsViaPendingIntent()
      throws Exception {
    Intent intent = new Intent(application, TestService.class);
    PendingIntent pendingIntent = PendingIntent.getService(application, 0, intent, 0);
    LocationRequest locationRequest = LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY);

    LostClientManager.shared().addPendingIntent(client, locationRequest, pendingIntent);
    api.requestLocationUpdates(locationRequest);
    Location location = new Location(GPS_PROVIDER);
    shadowLocationManager.simulateLocation(location);

    Intent nextStartedService = ShadowApplication.getInstance().getNextStartedService();
    assertThat(nextStartedService).isNotNull();
    assertThat(nextStartedService.getParcelableExtra(KEY_LOCATION_CHANGED)).isEqualTo(location);
  }

  @Test public void requestLocationUpdates_shouldNotifyOnLocationChangedNetworkViaPendingIntent()
      throws Exception {
    Intent intent = new Intent(application, TestService.class);
    PendingIntent pendingIntent = PendingIntent.getService(application, 0, intent, 0);
    LocationRequest locationRequest =
        LocationRequest.create().setPriority(PRIORITY_BALANCED_POWER_ACCURACY);

    LostClientManager.shared().addPendingIntent(client, locationRequest, pendingIntent);
    api.requestLocationUpdates(locationRequest);
    Location location = new Location(NETWORK_PROVIDER);
    shadowLocationManager.simulateLocation(location);

    Intent nextStartedService = ShadowApplication.getInstance().getNextStartedService();
    assertThat(nextStartedService).isNotNull();
    assertThat(nextStartedService.getParcelableExtra(KEY_LOCATION_CHANGED)).isEqualTo(location);
  }

  @Test public void removeLocationUpdates_shouldUnregisterAllPendingIntentListeners()
      throws Exception {
    LocationRequest locationRequest =
        LocationRequest.create().setPriority(PRIORITY_BALANCED_POWER_ACCURACY);
    api.requestLocationUpdates(locationRequest);
    api.removeLocationUpdates();
    assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).isEmpty();
  }

  @Test public void requestLocationUpdates_shouldNotNotifyRemovedPendingIntent() throws Exception {
    LocationRequest request = LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY);
    Intent intent1 = new Intent(application, TestService.class);
    Intent intent2 = new Intent(application, OtherTestService.class);
    PendingIntent pendingIntent1 = PendingIntent.getService(application, 0, intent1, 0);
    PendingIntent pendingIntent2 = PendingIntent.getService(application, 0, intent2, 0);
    api.requestLocationUpdates(request);
    api.requestLocationUpdates(request);
    LostClientManager.shared().addPendingIntent(client, request, pendingIntent1);
    LostClientManager.shared().addPendingIntent(client, request, pendingIntent2);

    LostClientManager.shared().removePendingIntent(client, pendingIntent2);
    Location location = new Location(GPS_PROVIDER);
    location.setLatitude(40.0);
    location.setLongitude(70.0);
    shadowLocationManager.simulateLocation(location);

    // Only one service should be started since the second pending intent request was removed.
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNotNull();
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNull();
  }

  @Test public void requestLocationUpdates_shouldReportResult() {
    TestLocationCallback callback = new TestLocationCallback();
    Looper looper = Looper.myLooper();
    LocationRequest request = LocationRequest.create();
    api.requestLocationUpdates(request);
    LostClientManager.shared().addLocationCallback(client, request, callback, looper);
    Location location = getTestLocation(NETWORK_PROVIDER, 0, 0, 0);
    shadowLocationManager.simulateLocation(location);
    assertThat(callback.getResult().getLastLocation()).isEqualTo(location);
  }

  @Test public void requestLocationUpdates_shouldReportAvailability() {
    Location location = new Location("test");
    shadowLocationManager.setLastKnownLocation(NETWORK_PROVIDER, location);
    TestLocationCallback callback = new TestLocationCallback();
    Looper looper = Looper.myLooper();
    LocationRequest request = LocationRequest.create();
    api.requestLocationUpdates(request);
    LostClientManager.shared().addLocationCallback(client, request, callback, looper);
    shadowLocationManager.setProviderEnabled(NETWORK_PROVIDER, true);
    assertThat(callback.getAvailability().isLocationAvailable()).isEqualTo(true);
  }

  @Test public void getLocationAvailability_gps_network_shouldBeAvailable() {
    shadowLocationManager.setProviderEnabled(GPS_PROVIDER, true);
    shadowLocationManager.setProviderEnabled(NETWORK_PROVIDER, true);
    Location location = new Location("test");
    shadowLocationManager.setLastKnownLocation(GPS_PROVIDER, location);

    LocationAvailability availability = api.getLocationAvailability();
    assertThat(availability.isLocationAvailable()).isTrue();
  }

  @Test public void getLocationAvailability_gps_network_shouldBeUnavailable() {
    shadowLocationManager.setProviderEnabled(GPS_PROVIDER, true);
    shadowLocationManager.setProviderEnabled(NETWORK_PROVIDER, true);

    LocationAvailability availability = api.getLocationAvailability();
    assertThat(availability.isLocationAvailable()).isFalse();
  }

  @Test public void getLocationAvailability_gps_shouldBeAvailable() {
    shadowLocationManager.setProviderEnabled(GPS_PROVIDER, true);
    Location location = new Location("test");
    shadowLocationManager.setLastKnownLocation(GPS_PROVIDER, location);

    LocationAvailability availability = api.getLocationAvailability();
    assertThat(availability.isLocationAvailable()).isTrue();
  }

  @Test public void getLocationAvailability_gps_shouldBeUnavailable() {
    shadowLocationManager.setProviderEnabled(GPS_PROVIDER, true);

    LocationAvailability availability = api.getLocationAvailability();
    assertThat(availability.isLocationAvailable()).isFalse();
  }

  @Test public void getLocationAvailability_network_shouldBeAvailable() {
    shadowLocationManager.setProviderEnabled(NETWORK_PROVIDER, true);
    Location location = new Location("test");
    shadowLocationManager.setLastKnownLocation(NETWORK_PROVIDER, location);

    LocationAvailability availability = api.getLocationAvailability();
    assertThat(availability.isLocationAvailable()).isTrue();
  }

  @Test public void getLocationAvailability_network_shouldBeUnavailable() {
    shadowLocationManager.setProviderEnabled(NETWORK_PROVIDER, true);

    LocationAvailability availability = api.getLocationAvailability();
    assertThat(availability.isLocationAvailable()).isFalse();
  }

  @Test public void getLocationAvailability_shouldBeUnavailable() {
    LocationAvailability availability = api.getLocationAvailability();
    assertThat(availability.isLocationAvailable()).isFalse();
  }

  @Test public void removeLocationUpdates_shouldNotKillEngineIfListenerStillActive()
      throws Exception {
    TestLocationListener listener = new TestLocationListener();
    api.requestLocationUpdates(LocationRequest.create());
    LostClientManager.shared().addListener(client, LocationRequest.create(), listener);
    api.requestLocationUpdates(LocationRequest.create());
    api.removeLocationUpdates();
    assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).isNotEmpty();
  }

  @Test public void removeLocationUpdates_shouldNotKillEngineIfIntentStillActive()
      throws Exception {
    api.requestLocationUpdates(LocationRequest.create());

    PendingIntent pendingIntent = PendingIntent.getService(application, 0, new Intent(), 0);
    api.requestLocationUpdates(LocationRequest.create());
    LostClientManager.shared().addPendingIntent(client, LocationRequest.create(), pendingIntent);

    api.removeLocationUpdates();
    assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).isNotEmpty();
  }

  @Test public void removeLocationUpdates_locationCallback_shouldUnregisterAllListeners() {
    LocationRequest request = LocationRequest.create();
    api.requestLocationUpdates(request);
    api.removeLocationUpdates();
    assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).isEmpty();
  }

  @Test public void requestLocationUpdates_shouldModifyOnlyClientListeners() {
    client.connect();
    api.requestLocationUpdates(LocationRequest.create());
    LostClientManager.shared().addListener(client, LocationRequest.create(),
        new TestLocationListener());

    otherClient.connect();

    assertThat(clientManager.getLocationListeners().get(client).size()).isEqualTo(1);
    assertThat(clientManager.getLocationListeners().get(otherClient)).isEmpty();
  }

  @Test public void requestLocationUpdates_shouldModifyOnlyClientPendingIntents() {
    client.connect();
    api.requestLocationUpdates(LocationRequest.create());
    LostClientManager.shared().addPendingIntent(client, LocationRequest.create(),
        mock(PendingIntent.class));

    otherClient.connect();

    assertThat(clientManager.getPendingIntents().get(client).size()).isEqualTo(1);
    assertThat(clientManager.getPendingIntents().get(otherClient)).isEmpty();
  }

  @Test public void requestLocationUpdates_shouldModifyOnlyClientLocationListeners() {
    client.connect();
    api.requestLocationUpdates(LocationRequest.create());
    LostClientManager.shared().addLocationCallback(client, LocationRequest.create(),
        new TestLocationCallback(), Looper.myLooper());

    otherClient.connect();

    assertThat(clientManager.getLocationCallbacks().get(client).size()).isEqualTo(1);
    assertThat(clientManager.getLocationCallbacks().get(otherClient)).isEmpty();
  }

  @Test public void removeLocationUpdates_shouldModifyOnlyClientPendingIntents() {
    PendingIntent pendingIntent = mock(PendingIntent.class);
    LocationRequest locationRequest = LocationRequest.create();

    client.connect();
    api.requestLocationUpdates(LocationRequest.create());
    LostClientManager.shared().addPendingIntent(client, locationRequest, pendingIntent);

    otherClient.connect();
    api.requestLocationUpdates(LocationRequest.create());
    LostClientManager.shared().addPendingIntent(otherClient, locationRequest, pendingIntent);

    api.removeLocationUpdates();
    LostClientManager.shared().removePendingIntent(client, pendingIntent);

    assertThat(clientManager.getPendingIntents().get(client)).isEmpty();
    assertThat(clientManager.getPendingIntents().get(otherClient).size()).isEqualTo(1);
  }

  @Test public void removeLocationUpdates_shouldModifyOnlyClientLocationListeners() {
    TestLocationCallback callback = new TestLocationCallback();

    client.connect();
    api.requestLocationUpdates(LocationRequest.create());
    LostClientManager.shared().addLocationCallback(client, LocationRequest.create(), callback,
        Looper.myLooper());

    otherClient.connect();
    api.requestLocationUpdates(LocationRequest.create());
    LostClientManager.shared().addLocationCallback(otherClient, LocationRequest.create(), callback,
        Looper.myLooper());

    LostClientManager.shared().removeLocationCallback(client, callback);
    api.removeLocationUpdates();

    assertThat(clientManager.getLocationCallbacks().get(client)).isEmpty();
    assertThat(clientManager.getLocationCallbacks().get(otherClient).size()).isEqualTo(1);
  }

  @Test public void reportLocation_shouldNotifyClientListener() {
    TestLocationListener listener = new TestLocationListener();
    client.connect();
    LostClientManager.shared().addListener(client, LocationRequest.create(), listener);

    TestLocationListener otherListener = new TestLocationListener();
    otherClient.connect();
    LostClientManager.shared().addListener(otherClient, LocationRequest.create(), otherListener);
    LostClientManager.shared().removeListener(otherClient, otherListener);

    Location location = new Location("test");
    api.reportLocation(location);

    assertThat(listener.getAllLocations()).contains(location);
    assertThat(otherListener.getAllLocations()).isEmpty();
  }

  @Test public void reportLocation_shouldNotifyPendingIntents() {
    Intent intent = new Intent(application, TestService.class);
    PendingIntent pendingIntent = PendingIntent.getService(application, 0, intent, 0);

    client.connect();
    api.requestLocationUpdates(LocationRequest.create());
    LostClientManager.shared().addPendingIntent(client, LocationRequest.create(), pendingIntent);

    PendingIntent otherPendingIntent = PendingIntent.getService(application, 0, intent, 0);
    otherClient.connect();
    api.requestLocationUpdates(LocationRequest.create());
    LostClientManager.shared().addPendingIntent(otherClient, LocationRequest.create(),
        otherPendingIntent);
    LostClientManager.shared().removePendingIntent(otherClient, otherPendingIntent);
    api.removeLocationUpdates();

    Location location = new Location("test");
    api.reportLocation(location);

    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNotNull();
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNull();
  }

  @Test public void reportLocation_shouldNotifyLocationCallbacks() {
    TestLocationCallback callback = new TestLocationCallback();
    client.connect();
    api.requestLocationUpdates(LocationRequest.create());
    LostClientManager.shared().addLocationCallback(client, LocationRequest.create(), callback,
        Looper.myLooper());

    TestLocationCallback otherCallback = new TestLocationCallback();
    otherClient.connect();
    api.requestLocationUpdates(LocationRequest.create());
    LostClientManager.shared().addLocationCallback(otherClient, LocationRequest.create(),
        otherCallback, Looper.myLooper());
    api.removeLocationUpdates();
    LostClientManager.shared().removeLocationCallback(otherClient, otherCallback);

    api.reportProviderEnabled(GPS_PROVIDER);

    assertThat(callback.getAvailability()).isNotNull();
    assertThat(otherCallback.getAvailability()).isNull();
  }

  @Test public void reportLocation_shouldNotifyBothListeners() {
    TestLocationListener listener = new TestLocationListener();
    client.connect();
    api.requestLocationUpdates(LocationRequest.create());
    LostClientManager.shared().addListener(client, LocationRequest.create(), listener);

    TestLocationListener otherListener = new TestLocationListener();
    otherClient.connect();
    LocationRequest otherRequest = LocationRequest.create();
    api.requestLocationUpdates(otherRequest);
    LostClientManager.shared().addListener(otherClient, otherRequest, otherListener);

    Location location = new Location("test");
    api.reportLocation(location);
    assertThat(listener.getAllLocations()).contains(location);
    assertThat(otherListener.getAllLocations()).contains(location);
  }

  @Test public void reportLocation_listener_shouldRespectFastestInterval() {
    TestLocationListener listener = new TestLocationListener();
    client.connect();
    LocationRequest request = LocationRequest.create();
    request.setFastestInterval(1000);
    api.requestLocationUpdates(request);
    LostClientManager.shared().addListener(client, request, listener);

    TestLocationListener otherListener = new TestLocationListener();
    otherClient.connect();
    LocationRequest otherRequest = LocationRequest.create();
    otherRequest.setFastestInterval(0);
    api.requestLocationUpdates(otherRequest);
    LostClientManager.shared().addListener(otherClient, otherRequest, otherListener);

    api.reportLocation(new Location("test"));
    listener.getAllLocations().clear();
    otherListener.getAllLocations().clear();

    Location location = new Location("test");
    api.reportLocation(location);
    assertThat(listener.getAllLocations()).isEmpty();
    assertThat(otherListener.getAllLocations().size()).isEqualTo(1);
  }

  @Test public void reportLocation_listener_shouldRespectSmallestDisplacement() {
    TestLocationListener listener = new TestLocationListener();
    client.connect();
    LocationRequest request = LocationRequest.create();
    request.setSmallestDisplacement(10);
    api.requestLocationUpdates(request);
    LostClientManager.shared().addListener(client, request, listener);

    TestLocationListener otherListener = new TestLocationListener();
    otherClient.connect();
    LocationRequest otherRequest = LocationRequest.create();
    otherRequest.setFastestInterval(0);
    api.requestLocationUpdates(otherRequest);
    LostClientManager.shared().addListener(otherClient, otherRequest, otherListener);

    api.reportLocation(new Location("test"));
    listener.getAllLocations().clear();
    otherListener.getAllLocations().clear();

    Location location = new Location("test");
    api.reportLocation(location);
    assertThat(listener.getAllLocations()).isEmpty();
    assertThat(otherListener.getAllLocations().size()).isEqualTo(1);
  }

  @Test public void reportLocation_listener_shouldRespectLargestIntervalAndSmallestDisplacement()
      throws InterruptedException {
    TestLocationListener listener = new TestLocationListener();
    client.connect();
    LocationRequest request = LocationRequest.create();
    request.setFastestInterval(1000);
    request.setSmallestDisplacement(10);
    api.requestLocationUpdates(request);
    LostClientManager.shared().addListener(client, request, listener);

    TestLocationListener otherListener = new TestLocationListener();
    otherClient.connect();
    LocationRequest otherRequest = LocationRequest.create();
    otherRequest.setFastestInterval(0);
    api.requestLocationUpdates(otherRequest);
    LostClientManager.shared().addListener(otherClient, otherRequest, otherListener);

    api.reportLocation(new Location("test"));
    listener.getAllLocations().clear();
    otherListener.getAllLocations().clear();

    Location location = new Location("test");
    location.setLatitude(70.0);
    location.setLongitude(40.0);
    api.reportLocation(location);
    assertThat(listener.getAllLocations()).isEmpty();
    assertThat(otherListener.getAllLocations().size()).isEqualTo(1);

    api.reportLocation(new Location("test"));
    assertThat(listener.getAllLocations()).isEmpty();
    assertThat(otherListener.getAllLocations().size()).isEqualTo(2);

    Thread.sleep(1000);
    api.reportLocation(location);
    assertThat(listener.getAllLocations().size()).isEqualTo(1);
    assertThat(otherListener.getAllLocations().size()).isEqualTo(3);
  }

  @Test public void reportLocation_shouldNotifyBothPendingIntents() {
    Intent intent = new Intent(application, TestService.class);
    PendingIntent pendingIntent = PendingIntent.getService(application, 0, intent, 0);
    client.connect();
    api.requestLocationUpdates(LocationRequest.create());
    LostClientManager.shared().addPendingIntent(client, LocationRequest.create(), pendingIntent);

    Intent otherIntent = new Intent(application, TestService.class);
    PendingIntent otherPendingIntent = PendingIntent.getService(application, 0, otherIntent, 0);
    otherClient.connect();
    LocationRequest otherRequest = LocationRequest.create();
    api.requestLocationUpdates(otherRequest);
    LostClientManager.shared().addPendingIntent(otherClient, LocationRequest.create(),
        otherPendingIntent);

    Location location = new Location("test");
    api.reportLocation(location);
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNotNull();
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNotNull();
  }

  @Test public void reportLocation_pendingIntent_shouldRespectFastestInterval() {
    Intent intent = new Intent(application, TestService.class);
    PendingIntent pendingIntent = PendingIntent.getService(application, 0, intent, 0);
    client.connect();
    LocationRequest request = LocationRequest.create();
    request.setFastestInterval(1000);
    api.requestLocationUpdates(request);
    LostClientManager.shared().addPendingIntent(client, request, pendingIntent);

    Intent otherIntent = new Intent(application, TestService.class);
    PendingIntent otherPendingIntent = PendingIntent.getService(application, 0, otherIntent, 0);
    otherClient.connect();
    LocationRequest otherRequest = LocationRequest.create();
    otherRequest.setFastestInterval(0);
    api.requestLocationUpdates(otherRequest);
    LostClientManager.shared().addPendingIntent(otherClient, otherRequest, otherPendingIntent);

    api.reportLocation(new Location("test"));
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNotNull();
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNotNull();

    Location location = new Location("test");
    api.reportLocation(location);
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNotNull();
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNull();
  }

  @Test public void reportLocation_pendingIntent_shouldRespectSmallestDisplacement() {
    Intent intent = new Intent(application, TestService.class);
    PendingIntent pendingIntent = PendingIntent.getService(application, 0, intent, 0);
    client.connect();
    LocationRequest request = LocationRequest.create();
    request.setFastestInterval(1000);
    api.requestLocationUpdates(request);
    LostClientManager.shared().addPendingIntent(client, request, pendingIntent);

    Intent otherIntent = new Intent(application, TestService.class);
    PendingIntent otherPendingIntent = PendingIntent.getService(application, 0, otherIntent, 0);
    otherClient.connect();
    LocationRequest otherRequest = LocationRequest.create();
    otherRequest.setFastestInterval(0);
    api.requestLocationUpdates(otherRequest);
    LostClientManager.shared().addPendingIntent(otherClient, otherRequest, otherPendingIntent);

    api.reportLocation(new Location("test"));
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNotNull();
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNotNull();

    Location location = new Location("test");
    api.reportLocation(location);
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNotNull();
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNull();
  }

  @Test public void reportLocation_pendingIntent_shouldRespectLargestIntervalSmallestDisplacement()
      throws InterruptedException {
    Intent intent = new Intent(application, TestService.class);
    PendingIntent pendingIntent = PendingIntent.getService(application, 0, intent, 0);
    client.connect();
    LocationRequest request = LocationRequest.create();
    request.setFastestInterval(1000);
    request.setSmallestDisplacement(10);
    api.requestLocationUpdates(request);
    LostClientManager.shared().addPendingIntent(client, request, pendingIntent);

    Intent otherIntent = new Intent(application, TestService.class);
    PendingIntent otherPendingIntent = PendingIntent.getService(application, 0, otherIntent, 0);
    otherClient.connect();
    LocationRequest otherRequest = LocationRequest.create();
    otherRequest.setFastestInterval(0);
    api.requestLocationUpdates(otherRequest);
    LostClientManager.shared().addPendingIntent(otherClient, otherRequest, otherPendingIntent);

    api.reportLocation(new Location("test"));
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNotNull();
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNotNull();

    Location location = new Location("test");
    location.setLatitude(70.0);
    location.setLongitude(40.0);
    api.reportLocation(location);
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNotNull();
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNull();

    api.reportLocation(new Location("test"));
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNotNull();
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNull();

    Thread.sleep(1000);
    api.reportLocation(location);
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNotNull();
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNotNull();
  }

  @Test public void reportLocation_shouldNotifyBothCallbacks() {
    TestLocationCallback callback = new TestLocationCallback();
    client.connect();
    api.requestLocationUpdates(LocationRequest.create());
    LostClientManager.shared().addLocationCallback(client, LocationRequest.create(), callback,
        Looper.myLooper());

    TestLocationCallback otherCallback = new TestLocationCallback();
    otherClient.connect();
    LocationRequest otherRequest = LocationRequest.create();
    api.requestLocationUpdates(otherRequest);
    LostClientManager.shared().addLocationCallback(otherClient, LocationRequest.create(),
        otherCallback, Looper.myLooper());

    Location location = new Location("test");
    api.reportLocation(location);
    assertThat(callback.getResult()).isNotNull();
    assertThat(otherCallback.getResult()).isNotNull();
  }

  @Test public void reportLocation_callback_shouldRespectFastestInterval() {
    TestLocationCallback callback = new TestLocationCallback();
    client.connect();
    LocationRequest request = LocationRequest.create();
    request.setFastestInterval(1000);
    api.requestLocationUpdates(request);
    LostClientManager.shared().addLocationCallback(client, request, callback, Looper.myLooper());

    TestLocationCallback otherCallback = new TestLocationCallback();
    otherClient.connect();
    LocationRequest otherRequest = LocationRequest.create();
    otherRequest.setFastestInterval(0);
    api.requestLocationUpdates(otherRequest);
    LostClientManager.shared().addLocationCallback(otherClient, otherRequest, otherCallback,
        Looper.myLooper());

    api.reportLocation(new Location("test"));
    callback.setResult(null);
    otherCallback.setResult(null);

    Location location = new Location("test");
    api.reportLocation(location);
    assertThat(callback.getResult()).isNull();
    assertThat(otherCallback.getResult()).isNotNull();
  }

  @Test public void reportLocation_callback_shouldRespectSmallestDisplacement() {
    TestLocationCallback callback = new TestLocationCallback();
    client.connect();
    LocationRequest request = LocationRequest.create();
    request.setSmallestDisplacement(10);
    api.requestLocationUpdates(request);
    LostClientManager.shared().addLocationCallback(client, request, callback, Looper.myLooper());

    TestLocationCallback otherCallback = new TestLocationCallback();
    otherClient.connect();
    LocationRequest otherRequest = LocationRequest.create();
    otherRequest.setFastestInterval(0);
    api.requestLocationUpdates(otherRequest);
    LostClientManager.shared().addLocationCallback(otherClient, otherRequest, otherCallback,
        Looper.myLooper());

    api.reportLocation(new Location("test"));
    callback.setResult(null);
    otherCallback.setResult(null);

    Location location = new Location("test");
    api.reportLocation(location);
    assertThat(callback.getResult()).isNull();
    assertThat(otherCallback.getResult()).isNotNull();
  }

  @Test public void reportLocation_callback_shouldRespectLargestIntervalAndSmallestDisplacement()
      throws InterruptedException {
    TestLocationCallback callback = new TestLocationCallback();
    client.connect();
    LocationRequest request = LocationRequest.create();
    request.setFastestInterval(1000);
    request.setSmallestDisplacement(10);
    api.requestLocationUpdates(request);
    LostClientManager.shared().addLocationCallback(client, request, callback, Looper.myLooper());

    TestLocationCallback otherCallback = new TestLocationCallback();
    otherClient.connect();
    LocationRequest otherRequest = LocationRequest.create();
    otherRequest.setFastestInterval(0);
    api.requestLocationUpdates(otherRequest);
    LostClientManager.shared().addLocationCallback(otherClient, otherRequest, otherCallback,
        Looper.myLooper());

    api.reportLocation(new Location("test"));
    callback.setResult(null);
    otherCallback.setResult(null);

    Location location = new Location("test");
    location.setLatitude(70.0);
    location.setLongitude(40.0);
    api.reportLocation(location);
    assertThat(callback.getResult()).isNull();
    assertThat(otherCallback.getResult()).isNotNull();

    api.reportLocation(new Location("test"));
    assertThat(callback.getResult()).isNull();
    assertThat(otherCallback.getResult()).isNotNull();

    Thread.sleep(1000);
    api.reportLocation(location);
    assertThat(callback.getResult()).isNotNull();
    assertThat(otherCallback.getResult()).isNotNull();
  }

  @Test public void requestLocationUpdates_listener_shouldReturnFusedLocationPendingResult() {
    PendingResult<Status> result = api.requestLocationUpdates(LocationRequest.create());
    assertThat(result.await().getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
    assertThat(result.await(1000, TimeUnit.MILLISECONDS).getStatus().getStatusCode()).isEqualTo(
        Status.SUCCESS);
    assertThat(result.isCanceled()).isFalse();
    TestResultCallback callback = new TestResultCallback();
    result.setResultCallback(callback);
    assertThat(callback.getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
    TestResultCallback otherCallback = new TestResultCallback();
    result.setResultCallback(otherCallback, 1000, TimeUnit.MILLISECONDS);
    assertThat(otherCallback.getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
  }

  @Test public void requestLocationUpdates_pendingIntent_shouldReturnFusedLocationPendingResult() {
    PendingResult<Status> result = api.requestLocationUpdates(LocationRequest.create());
    assertThat(result.await().getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
    assertThat(result.await(1000, TimeUnit.MILLISECONDS).getStatus().getStatusCode()).isEqualTo(
        Status.SUCCESS);
    assertThat(result.isCanceled()).isFalse();
    TestResultCallback callback = new TestResultCallback();
    result.setResultCallback(callback);
    assertThat(callback.getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
    TestResultCallback otherCallback = new TestResultCallback();
    result.setResultCallback(otherCallback, 1000, TimeUnit.MILLISECONDS);
    assertThat(otherCallback.getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
  }

  @Test public void requestLocationUpdates_callback_shouldReturnFusedLocationPendingResult() {
    LocationCallback locationCallback = new TestLocationCallback();
    PendingResult<Status> result = api.requestLocationUpdates(LocationRequest.create());
    assertThat(result.await().getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
    assertThat(result.await(1000, TimeUnit.MILLISECONDS).getStatus().getStatusCode()).isEqualTo(
        Status.SUCCESS);
    assertThat(result.isCanceled()).isFalse();
    TestResultCallback callback = new TestResultCallback();
    result.setResultCallback(callback);
    assertThat(callback.getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
    TestResultCallback otherCallback = new TestResultCallback();
    result.setResultCallback(otherCallback, 1000, TimeUnit.MILLISECONDS);
    assertThat(otherCallback.getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
  }

  @Test public void setMockMode_shouldReturnFusedLocationPendingResult() {
    PendingResult<Status> result = api.setMockMode(true);
    assertThat(result.await().getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
    assertThat(result.await(1000, TimeUnit.MILLISECONDS).getStatus().getStatusCode()).isEqualTo(
        Status.SUCCESS);
    assertThat(result.isCanceled()).isFalse();
    TestResultCallback callback = new TestResultCallback();
    result.setResultCallback(callback);
    assertThat(callback.getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
    TestResultCallback otherCallback = new TestResultCallback();
    result.setResultCallback(otherCallback, 1000, TimeUnit.MILLISECONDS);
    assertThat(otherCallback.getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
  }

  @Test public void setMockLocation_shouldReturnFusedLocationPendingResult() {
    PendingResult<Status> result = api.setMockLocation(new Location("test"));
    assertThat(result.await().getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
    assertThat(result.await(1000, TimeUnit.MILLISECONDS).getStatus().getStatusCode()).isEqualTo(
        Status.SUCCESS);
    assertThat(result.isCanceled()).isFalse();
    TestResultCallback callback = new TestResultCallback();
    result.setResultCallback(callback);
    assertThat(callback.getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
    TestResultCallback otherCallback = new TestResultCallback();
    result.setResultCallback(otherCallback, 1000, TimeUnit.MILLISECONDS);
    assertThat(otherCallback.getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
  }

  @Test public void setMockTrace_shouldReturnFusedLocationPendingResult() {
    PendingResult<Status> result = api.setMockTrace(new File("test"));
    assertThat(result.await().getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
    assertThat(result.await(1000, TimeUnit.MILLISECONDS).getStatus().getStatusCode()).isEqualTo(
        Status.SUCCESS);
    assertThat(result.isCanceled()).isFalse();
    TestResultCallback callback = new TestResultCallback();
    result.setResultCallback(callback);
    assertThat(callback.getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
    TestResultCallback otherCallback = new TestResultCallback();
    result.setResultCallback(otherCallback, 1000, TimeUnit.MILLISECONDS);
    assertThat(otherCallback.getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
  }

  public class TestService extends IntentService {
    public TestService() {
      super("test service");
    }

    @Override protected void onHandleIntent(Intent intent) {
    }
  }

  public class OtherTestService extends IntentService {
    public OtherTestService() {
      super("other test service");
    }

    @Override protected void onHandleIntent(Intent intent) {
    }
  }
}
