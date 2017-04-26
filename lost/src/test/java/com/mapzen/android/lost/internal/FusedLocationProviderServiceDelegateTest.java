package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.BaseRobolectricTest;
import com.mapzen.android.lost.api.LocationAvailability;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LocationResult;
import com.mapzen.android.lost.api.LostApiClient;
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
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Environment;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static android.content.Context.LOCATION_SERVICE;
import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static com.mapzen.android.lost.api.FusedLocationProviderApi.KEY_LOCATION_CHANGED;
import static com.mapzen.android.lost.api.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
import static com.mapzen.android.lost.api.LocationRequest.PRIORITY_HIGH_ACCURACY;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.robolectric.RuntimeEnvironment.application;

@RunWith(RobolectricTestRunner.class)
@SuppressWarnings("MissingPermission")
@Config(constants = BuildConfig.class, sdk = 21, manifest = Config.NONE, shadows = {
        LostShadowLocationManager.class})
public class FusedLocationProviderServiceDelegateTest extends BaseRobolectricTest {
  private LostApiClient client;
  private FusedLocationProviderServiceDelegate delegate;
  private LocationManager locationManager;
  private LostShadowLocationManager shadowLocationManager;
  private LostApiClient otherClient;
  private ClientManager clientManager;

  @Before public void setUp() throws Exception {
    client = new LostApiClient.Builder(mock(Context.class)).build();
    otherClient = new LostApiClient.Builder(mock(Context.class)).build();
    clientManager = LostClientManager.shared();
    delegate = new FusedLocationProviderServiceDelegate(application, clientManager);
    locationManager = (LocationManager) application.getSystemService(LOCATION_SERVICE);
    shadowLocationManager = (LostShadowLocationManager) ShadowExtractor.extract(locationManager);
    client.connect();
  }

  @After public void tearDown() {
    client.disconnect();
    otherClient.disconnect();
  }

  @Test public void shouldNotBeNull() throws Exception {
    assertThat(delegate).isNotNull();
  }

  @Test public void getLastLocation_shouldReturnMostRecentLocation() throws Exception {
    Location location = new Location(GPS_PROVIDER);
    shadowLocationManager.setLastKnownLocation(GPS_PROVIDER, location);
    assertThat(delegate.getLastLocation()).isNotNull();
  }

  @Test public void requestLocationUpdates_shouldRegisterGpsAndNetworkListener() throws Exception {
    delegate.requestLocationUpdates(LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY));
    assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).hasSize(2);
  }

  @Test public void requestLocationUpdates_shouldNotifyOnLocationChangedGps() throws Exception {
    TestLocationListener listener = new TestLocationListener();
    LocationRequest request = LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY);
    delegate.requestLocationUpdates(request);
    LostClientManager.shared().addListener(client, request, listener);
    Location location = new Location(GPS_PROVIDER);
    shadowLocationManager.simulateLocation(location);
    assertThat(listener.getMostRecentLocation()).isEqualTo(location);
  }

  @Test public void requestLocationUpdates_shouldNotifyOnLocationChangedNetwork() throws Exception {
    TestLocationListener listener = new TestLocationListener();
    delegate.requestLocationUpdates(LocationRequest.create());
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
    delegate.requestLocationUpdates(request);
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
    delegate.requestLocationUpdates(request);
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
    delegate.requestLocationUpdates(request);
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
    delegate.requestLocationUpdates(request);
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
    delegate.requestLocationUpdates(request);
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
    delegate.requestLocationUpdates(request);
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
    delegate.requestLocationUpdates(locationRequest);
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
    delegate.requestLocationUpdates(locationRequest);
    Location location = new Location(NETWORK_PROVIDER);
    shadowLocationManager.simulateLocation(location);

    Intent nextStartedService = ShadowApplication.getInstance().getNextStartedService();
    assertThat(nextStartedService).isNotNull();
    assertThat(
        nextStartedService.getParcelableExtra(LocationResult.EXTRA_LOCATION_RESULT)).isNotNull();
  }

  @Test public void removeLocationUpdates_shouldUnregisterAllListeners() throws Exception {
    delegate.requestLocationUpdates(LocationRequest.create());
    delegate.removeLocationUpdates();
    assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).isEmpty();
  }

  @Test public void setMockMode_shouldUnregisterAllListenersWhenTrue() throws Exception {
    LocationRequest request = LocationRequest.create();
    delegate.requestLocationUpdates(request);
    delegate.setMockMode(true);
    assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).isEmpty();
  }

  @Test public void setMockMode_shouldNotRegisterDuplicateListeners() throws Exception {
    LocationRequest request = LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY);
    delegate.setMockMode(true);
    delegate.requestLocationUpdates(request);
    delegate.setMockMode(false);
    delegate.requestLocationUpdates(request);
    assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).hasSize(2);
  }

  @Test public void setMockMode_shouldToggleEngines() {
    TestLocationListener listener = new TestLocationListener();
    LocationRequest request = LocationRequest.create();
    delegate.requestLocationUpdates(request);
    LostClientManager.shared().addListener(client, request, listener);

    delegate.setMockMode(true);
    TestLocationListener listener2 = new TestLocationListener();
    LocationRequest request2 = LocationRequest.create();
    delegate.requestLocationUpdates(request2);
    LostClientManager.shared().addListener(client, request2, listener2);

    assertThat(clientManager.getLocationListeners().get(client)).hasSize(2);
  }

  @Test public void requestLocationUpdates_shouldNotRegisterListenersWithMockModeOn()
      throws Exception {
    delegate.setMockMode(true);
    LocationRequest request = LocationRequest.create();
    delegate.requestLocationUpdates(request);
    assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).isEmpty();
  }

  @Test public void setMockLocation_shouldReturnMockLastLocation() throws Exception {
    Location mockLocation = new Location("mock");
    delegate.setMockMode(true);
    delegate.setMockLocation(mockLocation);
    assertThat(delegate.getLastLocation()).isEqualTo(mockLocation);
  }

  @Test public void setMockLocation_shouldInvokeListenerOnce() throws Exception {
    Location mockLocation = new Location("mock");
    delegate.setMockMode(true);
    TestLocationListener listener = new TestLocationListener();
    LocationRequest request = LocationRequest.create();
    delegate.requestLocationUpdates(request);
    LostClientManager.shared().addListener(client, request, listener);
    delegate.setMockLocation(mockLocation);
    assertThat(listener.getAllLocations()).hasSize(1);
    assertThat(listener.getMostRecentLocation()).isEqualTo(mockLocation);
  }

  public void setMockTrace_shouldInvokeListenerForEachLocation() throws Exception {
    initTestGpxTrace();
    delegate.setMockMode(true);
    delegate.setMockTrace(Environment.getExternalStorageDirectory().getPath(), "lost.gpx");
    TestLocationListener listener = new TestLocationListener();
    LocationRequest request = LocationRequest.create();
    request.setFastestInterval(0);
    delegate.requestLocationUpdates(request);
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
    initTestGpxTrace();
    delegate.setMockMode(true);
    delegate.setMockTrace(Environment.getExternalStorageDirectory().getPath(), "lost.gpx");
    TestLocationListener listener = new TestLocationListener();
    LocationRequest request = LocationRequest.create();
    request.setFastestInterval(0);
    delegate.requestLocationUpdates(request);
    Thread.sleep(100);
    ShadowLooper.runUiThreadTasks();
    assertThat(listener.getAllLocations().get(0).getSpeed()).isEqualTo(10f);
    assertThat(listener.getAllLocations().get(1).getSpeed()).isEqualTo(20f);
    assertThat(listener.getAllLocations().get(2).getSpeed()).isEqualTo(30f);
  }

  public void setMockTrace_shouldRespectFastestInterval() throws Exception {
    initTestGpxTrace();
    delegate.setMockMode(true);
    delegate.setMockTrace(Environment.getExternalStorageDirectory().getPath(), "lost.gpx");
    TestLocationListener listener = new TestLocationListener();
    LocationRequest request = LocationRequest.create();
    request.setInterval(100);
    delegate.requestLocationUpdates(request);
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

  private void initTestGpxTrace() throws IOException {
    String contents = Files.toString(new File("src/test/resources/lost.gpx"), Charsets.UTF_8);
    ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);
    File directory = Environment.getExternalStorageDirectory();
    File file = new File(directory, "lost.gpx");
    FileWriter fileWriter = new FileWriter(file, false);
    fileWriter.write(contents);
    fileWriter.close();
  }

  @Test public void requestLocationUpdates_shouldNotifyBothListeners() {
    LocationRequest request = LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY);
    TestLocationListener listener1 = new TestLocationListener();
    TestLocationListener listener2 = new TestLocationListener();
    delegate.requestLocationUpdates(request);
    delegate.requestLocationUpdates(request);
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
    delegate.requestLocationUpdates(request);
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
    delegate.requestLocationUpdates(LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY));
    assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).hasSize(2);
  }

  @Test public void requestLocationUpdates_shouldNotifyOnLocationChangedGpsViaPendingIntent()
      throws Exception {
    Intent intent = new Intent(application, TestService.class);
    PendingIntent pendingIntent = PendingIntent.getService(application, 0, intent, 0);
    LocationRequest locationRequest = LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY);

    LostClientManager.shared().addPendingIntent(client, locationRequest, pendingIntent);
    delegate.requestLocationUpdates(locationRequest);
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
    delegate.requestLocationUpdates(locationRequest);
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
    delegate.requestLocationUpdates(locationRequest);
    delegate.removeLocationUpdates();
    assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).isEmpty();
  }

  @Test public void requestLocationUpdates_shouldNotNotifyRemovedPendingIntent() throws Exception {
    LocationRequest request = LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY);
    Intent intent1 = new Intent(application, TestService.class);
    Intent intent2 = new Intent(application, OtherTestService.class);
    PendingIntent pendingIntent1 = PendingIntent.getService(application, 0, intent1, 0);
    PendingIntent pendingIntent2 = PendingIntent.getService(application, 0, intent2, 0);
    delegate.requestLocationUpdates(request);
    delegate.requestLocationUpdates(request);
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
    delegate.requestLocationUpdates(request);
    LostClientManager.shared().addLocationCallback(client, request, callback, looper);
    Location location = getTestLocation(NETWORK_PROVIDER, 0, 0, 0);
    shadowLocationManager.simulateLocation(location);
    assertThat(callback.getResult().getLastLocation()).isEqualTo(location);
  }

  @Test public void requestLocationUpdates_shouldReportAvailability() {
    TestFusedLocationProviderCallback callback = new TestFusedLocationProviderCallback();
    delegate.init(callback);
    Location location = new Location("test");
    shadowLocationManager.setLastKnownLocation(NETWORK_PROVIDER, location);
    LocationRequest request = LocationRequest.create();
    delegate.requestLocationUpdates(request);
    shadowLocationManager.setProviderEnabled(NETWORK_PROVIDER, true);
    assertThat(callback.locationAvailability.isLocationAvailable()).isTrue();
  }

  @Test public void getLocationAvailability_gps_network_shouldBeAvailable() {
    shadowLocationManager.setProviderEnabled(GPS_PROVIDER, true);
    shadowLocationManager.setProviderEnabled(NETWORK_PROVIDER, true);
    Location location = new Location("test");
    shadowLocationManager.setLastKnownLocation(GPS_PROVIDER, location);

    LocationAvailability availability = delegate.getLocationAvailability();
    assertThat(availability.isLocationAvailable()).isTrue();
  }

  @Test public void getLocationAvailability_gps_network_shouldBeUnavailable() {
    shadowLocationManager.setProviderEnabled(GPS_PROVIDER, true);
    shadowLocationManager.setProviderEnabled(NETWORK_PROVIDER, true);

    LocationAvailability availability = delegate.getLocationAvailability();
    assertThat(availability.isLocationAvailable()).isFalse();
  }

  @Test public void getLocationAvailability_gps_shouldBeAvailable() {
    shadowLocationManager.setProviderEnabled(GPS_PROVIDER, true);
    Location location = new Location("test");
    shadowLocationManager.setLastKnownLocation(GPS_PROVIDER, location);

    LocationAvailability availability = delegate.getLocationAvailability();
    assertThat(availability.isLocationAvailable()).isTrue();
  }

  @Test public void getLocationAvailability_gps_shouldBeUnavailable() {
    shadowLocationManager.setProviderEnabled(GPS_PROVIDER, true);

    LocationAvailability availability = delegate.getLocationAvailability();
    assertThat(availability.isLocationAvailable()).isFalse();
  }

  @Test public void getLocationAvailability_network_shouldBeAvailable() {
    shadowLocationManager.setProviderEnabled(NETWORK_PROVIDER, true);
    Location location = new Location("test");
    shadowLocationManager.setLastKnownLocation(NETWORK_PROVIDER, location);

    LocationAvailability availability = delegate.getLocationAvailability();
    assertThat(availability.isLocationAvailable()).isTrue();
  }

  @Test public void getLocationAvailability_network_shouldBeUnavailable() {
    shadowLocationManager.setProviderEnabled(NETWORK_PROVIDER, true);

    LocationAvailability availability = delegate.getLocationAvailability();
    assertThat(availability.isLocationAvailable()).isFalse();
  }

  @Test public void getLocationAvailability_shouldBeUnavailable() {
    LocationAvailability availability = delegate.getLocationAvailability();
    assertThat(availability.isLocationAvailable()).isFalse();
  }

  @Test public void removeLocationUpdates_locationCallback_shouldUnregisterAllListeners() {
    LocationRequest request = LocationRequest.create();
    delegate.requestLocationUpdates(request);
    delegate.removeLocationUpdates();
    assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).isEmpty();
  }

  @Test public void requestLocationUpdates_shouldModifyOnlyClientListeners() {
    client.connect();
    delegate.requestLocationUpdates(LocationRequest.create());
    LostClientManager.shared().addListener(client, LocationRequest.create(),
        new TestLocationListener());

    otherClient.connect();

    assertThat(clientManager.getLocationListeners().get(client).size()).isEqualTo(1);
    assertThat(clientManager.getLocationListeners().get(otherClient)).isEmpty();
  }

  @Test public void requestLocationUpdates_shouldModifyOnlyClientPendingIntents() {
    client.connect();
    delegate.requestLocationUpdates(LocationRequest.create());
    LostClientManager.shared().addPendingIntent(client, LocationRequest.create(),
        mock(PendingIntent.class));

    otherClient.connect();

    assertThat(clientManager.getPendingIntents().get(client).size()).isEqualTo(1);
    assertThat(clientManager.getPendingIntents().get(otherClient)).isEmpty();
  }

  @Test public void requestLocationUpdates_shouldModifyOnlyClientLocationListeners() {
    client.connect();
    delegate.requestLocationUpdates(LocationRequest.create());
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
    delegate.requestLocationUpdates(LocationRequest.create());
    LostClientManager.shared().addPendingIntent(client, locationRequest, pendingIntent);

    otherClient.connect();
    delegate.requestLocationUpdates(LocationRequest.create());
    LostClientManager.shared().addPendingIntent(otherClient, locationRequest, pendingIntent);

    delegate.removeLocationUpdates();
    LostClientManager.shared().removePendingIntent(client, pendingIntent);

    assertThat(clientManager.getPendingIntents().get(client)).isEmpty();
    assertThat(clientManager.getPendingIntents().get(otherClient).size()).isEqualTo(1);
  }

  @Test public void removeLocationUpdates_shouldModifyOnlyClientLocationListeners() {
    TestLocationCallback callback = new TestLocationCallback();

    client.connect();
    delegate.requestLocationUpdates(LocationRequest.create());
    LostClientManager.shared().addLocationCallback(client, LocationRequest.create(), callback,
        Looper.myLooper());

    otherClient.connect();
    delegate.requestLocationUpdates(LocationRequest.create());
    LostClientManager.shared().addLocationCallback(otherClient, LocationRequest.create(), callback,
        Looper.myLooper());

    LostClientManager.shared().removeLocationCallback(client, callback);
    delegate.removeLocationUpdates();

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
    delegate.reportLocation(location);

    assertThat(listener.getAllLocations()).contains(location);
    assertThat(otherListener.getAllLocations()).isEmpty();
  }

  @Test public void reportLocation_shouldNotifyPendingIntents() {
    Intent intent = new Intent(application, TestService.class);
    PendingIntent pendingIntent = PendingIntent.getService(application, 0, intent, 0);

    client.connect();
    delegate.requestLocationUpdates(LocationRequest.create());
    LostClientManager.shared().addPendingIntent(client, LocationRequest.create(), pendingIntent);

    PendingIntent otherPendingIntent = PendingIntent.getService(application, 0, intent, 0);
    otherClient.connect();
    delegate.requestLocationUpdates(LocationRequest.create());
    LostClientManager.shared().addPendingIntent(otherClient, LocationRequest.create(),
        otherPendingIntent);
    LostClientManager.shared().removePendingIntent(otherClient, otherPendingIntent);
    delegate.removeLocationUpdates();

    Location location = new Location("test");
    delegate.reportLocation(location);

    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNotNull();
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNull();
  }

  @Test public void reportProviderEnabled_shouldNotifyAvailabilityChanged() throws Exception {
    TestFusedLocationProviderCallback callback = new TestFusedLocationProviderCallback();
    delegate.init(callback);
    delegate.reportProviderEnabled(GPS_PROVIDER);
    assertThat(callback.locationAvailability).isNotNull();
  }

  @Test public void reportLocation_shouldNotifyBothListeners() {
    TestLocationListener listener = new TestLocationListener();
    client.connect();
    delegate.requestLocationUpdates(LocationRequest.create());
    LostClientManager.shared().addListener(client, LocationRequest.create(), listener);

    TestLocationListener otherListener = new TestLocationListener();
    otherClient.connect();
    LocationRequest otherRequest = LocationRequest.create();
    delegate.requestLocationUpdates(otherRequest);
    LostClientManager.shared().addListener(otherClient, otherRequest, otherListener);

    Location location = new Location("test");
    delegate.reportLocation(location);
    assertThat(listener.getAllLocations()).contains(location);
    assertThat(otherListener.getAllLocations()).contains(location);
  }

  @Test public void reportLocation_listener_shouldRespectFastestInterval() {
    TestLocationListener listener = new TestLocationListener();
    client.connect();
    LocationRequest request = LocationRequest.create();
    request.setFastestInterval(1000);
    delegate.requestLocationUpdates(request);
    LostClientManager.shared().addListener(client, request, listener);

    TestLocationListener otherListener = new TestLocationListener();
    otherClient.connect();
    LocationRequest otherRequest = LocationRequest.create();
    otherRequest.setFastestInterval(0);
    delegate.requestLocationUpdates(otherRequest);
    LostClientManager.shared().addListener(otherClient, otherRequest, otherListener);

    delegate.reportLocation(new Location("test"));
    listener.getAllLocations().clear();
    otherListener.getAllLocations().clear();

    Location location = new Location("test");
    delegate.reportLocation(location);
    assertThat(listener.getAllLocations()).isEmpty();
    assertThat(otherListener.getAllLocations().size()).isEqualTo(1);
  }

  @Test public void reportLocation_listener_shouldRespectSmallestDisplacement() {
    TestLocationListener listener = new TestLocationListener();
    client.connect();
    LocationRequest request = LocationRequest.create();
    request.setSmallestDisplacement(10);
    delegate.requestLocationUpdates(request);
    LostClientManager.shared().addListener(client, request, listener);

    TestLocationListener otherListener = new TestLocationListener();
    otherClient.connect();
    LocationRequest otherRequest = LocationRequest.create();
    otherRequest.setFastestInterval(0);
    delegate.requestLocationUpdates(otherRequest);
    LostClientManager.shared().addListener(otherClient, otherRequest, otherListener);

    delegate.reportLocation(new Location("test"));
    listener.getAllLocations().clear();
    otherListener.getAllLocations().clear();

    Location location = new Location("test");
    delegate.reportLocation(location);
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
    delegate.requestLocationUpdates(request);
    LostClientManager.shared().addListener(client, request, listener);

    TestLocationListener otherListener = new TestLocationListener();
    otherClient.connect();
    LocationRequest otherRequest = LocationRequest.create();
    otherRequest.setFastestInterval(0);
    delegate.requestLocationUpdates(otherRequest);
    LostClientManager.shared().addListener(otherClient, otherRequest, otherListener);

    delegate.reportLocation(new Location("test"));
    listener.getAllLocations().clear();
    otherListener.getAllLocations().clear();

    Location location = new Location("test");
    location.setLatitude(70.0);
    location.setLongitude(40.0);
    delegate.reportLocation(location);
    assertThat(listener.getAllLocations()).isEmpty();
    assertThat(otherListener.getAllLocations().size()).isEqualTo(1);

    delegate.reportLocation(new Location("test"));
    assertThat(listener.getAllLocations()).isEmpty();
    assertThat(otherListener.getAllLocations().size()).isEqualTo(2);

    Thread.sleep(1000);
    delegate.reportLocation(location);
    assertThat(listener.getAllLocations().size()).isEqualTo(1);
    assertThat(otherListener.getAllLocations().size()).isEqualTo(3);
  }

  @Test public void reportLocation_shouldNotifyBothPendingIntents() {
    Intent intent = new Intent(application, TestService.class);
    PendingIntent pendingIntent = PendingIntent.getService(application, 0, intent, 0);
    client.connect();
    delegate.requestLocationUpdates(LocationRequest.create());
    LostClientManager.shared().addPendingIntent(client, LocationRequest.create(), pendingIntent);

    Intent otherIntent = new Intent(application, TestService.class);
    PendingIntent otherPendingIntent = PendingIntent.getService(application, 0, otherIntent, 0);
    otherClient.connect();
    LocationRequest otherRequest = LocationRequest.create();
    delegate.requestLocationUpdates(otherRequest);
    LostClientManager.shared().addPendingIntent(otherClient, LocationRequest.create(),
        otherPendingIntent);

    Location location = new Location("test");
    delegate.reportLocation(location);
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNotNull();
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNotNull();
  }

  @Test public void reportLocation_pendingIntent_shouldRespectFastestInterval() {
    Intent intent = new Intent(application, TestService.class);
    PendingIntent pendingIntent = PendingIntent.getService(application, 0, intent, 0);
    client.connect();
    LocationRequest request = LocationRequest.create();
    request.setFastestInterval(1000);
    delegate.requestLocationUpdates(request);
    LostClientManager.shared().addPendingIntent(client, request, pendingIntent);

    Intent otherIntent = new Intent(application, TestService.class);
    PendingIntent otherPendingIntent = PendingIntent.getService(application, 0, otherIntent, 0);
    otherClient.connect();
    LocationRequest otherRequest = LocationRequest.create();
    otherRequest.setFastestInterval(0);
    delegate.requestLocationUpdates(otherRequest);
    LostClientManager.shared().addPendingIntent(otherClient, otherRequest, otherPendingIntent);

    delegate.reportLocation(new Location("test"));
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNotNull();
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNotNull();

    Location location = new Location("test");
    delegate.reportLocation(location);
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNotNull();
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNull();
  }

  @Test public void reportLocation_pendingIntent_shouldRespectSmallestDisplacement() {
    Intent intent = new Intent(application, TestService.class);
    PendingIntent pendingIntent = PendingIntent.getService(application, 0, intent, 0);
    client.connect();
    LocationRequest request = LocationRequest.create();
    request.setFastestInterval(1000);
    delegate.requestLocationUpdates(request);
    LostClientManager.shared().addPendingIntent(client, request, pendingIntent);

    Intent otherIntent = new Intent(application, TestService.class);
    PendingIntent otherPendingIntent = PendingIntent.getService(application, 0, otherIntent, 0);
    otherClient.connect();
    LocationRequest otherRequest = LocationRequest.create();
    otherRequest.setFastestInterval(0);
    delegate.requestLocationUpdates(otherRequest);
    LostClientManager.shared().addPendingIntent(otherClient, otherRequest, otherPendingIntent);

    delegate.reportLocation(new Location("test"));
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNotNull();
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNotNull();

    Location location = new Location("test");
    delegate.reportLocation(location);
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
    delegate.requestLocationUpdates(request);
    LostClientManager.shared().addPendingIntent(client, request, pendingIntent);

    Intent otherIntent = new Intent(application, TestService.class);
    PendingIntent otherPendingIntent = PendingIntent.getService(application, 0, otherIntent, 0);
    otherClient.connect();
    LocationRequest otherRequest = LocationRequest.create();
    otherRequest.setFastestInterval(0);
    delegate.requestLocationUpdates(otherRequest);
    LostClientManager.shared().addPendingIntent(otherClient, otherRequest, otherPendingIntent);

    delegate.reportLocation(new Location("test"));
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNotNull();
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNotNull();

    Location location = new Location("test");
    location.setLatitude(70.0);
    location.setLongitude(40.0);
    delegate.reportLocation(location);
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNotNull();
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNull();

    delegate.reportLocation(new Location("test"));
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNotNull();
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNull();

    Thread.sleep(1000);
    delegate.reportLocation(location);
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNotNull();
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNotNull();
  }

  @Test public void reportLocation_shouldNotifyBothCallbacks() {
    TestLocationCallback callback = new TestLocationCallback();
    client.connect();
    delegate.requestLocationUpdates(LocationRequest.create());
    LostClientManager.shared().addLocationCallback(client, LocationRequest.create(), callback,
        Looper.myLooper());

    TestLocationCallback otherCallback = new TestLocationCallback();
    otherClient.connect();
    LocationRequest otherRequest = LocationRequest.create();
    delegate.requestLocationUpdates(otherRequest);
    LostClientManager.shared().addLocationCallback(otherClient, LocationRequest.create(),
        otherCallback, Looper.myLooper());

    Location location = new Location("test");
    delegate.reportLocation(location);
    assertThat(callback.getResult()).isNotNull();
    assertThat(otherCallback.getResult()).isNotNull();
  }

  @Test public void reportLocation_callback_shouldRespectFastestInterval() {
    TestLocationCallback callback = new TestLocationCallback();
    client.connect();
    LocationRequest request = LocationRequest.create();
    request.setFastestInterval(1000);
    delegate.requestLocationUpdates(request);
    LostClientManager.shared().addLocationCallback(client, request, callback, Looper.myLooper());

    TestLocationCallback otherCallback = new TestLocationCallback();
    otherClient.connect();
    LocationRequest otherRequest = LocationRequest.create();
    otherRequest.setFastestInterval(0);
    delegate.requestLocationUpdates(otherRequest);
    LostClientManager.shared().addLocationCallback(otherClient, otherRequest, otherCallback,
        Looper.myLooper());

    delegate.reportLocation(new Location("test"));
    callback.setResult(null);
    otherCallback.setResult(null);

    Location location = new Location("test");
    delegate.reportLocation(location);
    assertThat(callback.getResult()).isNull();
    assertThat(otherCallback.getResult()).isNotNull();
  }

  @Test public void reportLocation_callback_shouldRespectSmallestDisplacement() {
    TestLocationCallback callback = new TestLocationCallback();
    client.connect();
    LocationRequest request = LocationRequest.create();
    request.setSmallestDisplacement(10);
    delegate.requestLocationUpdates(request);
    LostClientManager.shared().addLocationCallback(client, request, callback, Looper.myLooper());

    TestLocationCallback otherCallback = new TestLocationCallback();
    otherClient.connect();
    LocationRequest otherRequest = LocationRequest.create();
    otherRequest.setFastestInterval(0);
    delegate.requestLocationUpdates(otherRequest);
    LostClientManager.shared().addLocationCallback(otherClient, otherRequest, otherCallback,
        Looper.myLooper());

    delegate.reportLocation(new Location("test"));
    callback.setResult(null);
    otherCallback.setResult(null);

    Location location = new Location("test");
    delegate.reportLocation(location);
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
    delegate.requestLocationUpdates(request);
    LostClientManager.shared().addLocationCallback(client, request, callback, Looper.myLooper());

    TestLocationCallback otherCallback = new TestLocationCallback();
    otherClient.connect();
    LocationRequest otherRequest = LocationRequest.create();
    otherRequest.setFastestInterval(0);
    delegate.requestLocationUpdates(otherRequest);
    LostClientManager.shared().addLocationCallback(otherClient, otherRequest, otherCallback,
        Looper.myLooper());

    delegate.reportLocation(new Location("test"));
    callback.setResult(null);
    otherCallback.setResult(null);

    Location location = new Location("test");
    location.setLatitude(70.0);
    location.setLongitude(40.0);
    delegate.reportLocation(location);
    assertThat(callback.getResult()).isNull();
    assertThat(otherCallback.getResult()).isNotNull();

    delegate.reportLocation(new Location("test"));
    assertThat(callback.getResult()).isNull();
    assertThat(otherCallback.getResult()).isNotNull();

    Thread.sleep(1000);
    delegate.reportLocation(location);
    assertThat(callback.getResult()).isNotNull();
    assertThat(otherCallback.getResult()).isNotNull();
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

  public class TestFusedLocationProviderCallback implements IFusedLocationProviderCallback {
    private Location location;
    private LocationAvailability locationAvailability;

    @Override public void onLocationChanged(Location location) throws RemoteException {
      this.location = location;
    }

    @Override public void onLocationAvailabilityChanged(LocationAvailability locationAvailability)
        throws RemoteException {
      this.locationAvailability = locationAvailability;
    }

    @Override public IBinder asBinder() {
      return null;
    }
  }
}
