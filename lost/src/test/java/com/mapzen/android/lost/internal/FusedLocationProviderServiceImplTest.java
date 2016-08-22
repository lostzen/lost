package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LocationServices;
import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.lost.BuildConfig;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import static org.mockito.Mockito.when;
import static org.robolectric.RuntimeEnvironment.application;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, manifest = Config.NONE)
public class FusedLocationProviderServiceImplTest {
  private FusedLocationProviderServiceImpl api;
  private LocationManager locationManager;
  private ShadowLocationManager shadowLocationManager;

  @Before public void setUp() throws Exception {
    mockService();
    api = new FusedLocationProviderServiceImpl(application);
    locationManager = (LocationManager) application.getSystemService(LOCATION_SERVICE);
    shadowLocationManager = shadowOf(locationManager);
  }

  private void mockService() {
    FusedLocationProviderService.FusedLocationProviderBinder stubBinder = mock(
        FusedLocationProviderService.FusedLocationProviderBinder.class);
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
    LocationListener listener = new TestLocationListener();
    api.requestLocationUpdates(LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY),
        listener);
    assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).hasSize(2);
  }

  @Test public void requestLocationUpdates_shouldNotifyOnLocationChangedGps() throws Exception {
    TestLocationListener listener = new TestLocationListener();
    api.requestLocationUpdates(LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY),
        listener);
    Location location = new Location(GPS_PROVIDER);
    shadowLocationManager.simulateLocation(location);
    assertThat(listener.getMostRecentLocation()).isEqualTo(location);
  }

  @Test public void requestLocationUpdates_shouldNotifyOnLocationChangedNetwork() throws Exception {
    TestLocationListener listener = new TestLocationListener();
    api.requestLocationUpdates(LocationRequest.create(), listener);
    Location location = new Location(NETWORK_PROVIDER);
    shadowLocationManager.simulateLocation(location);
    assertThat(listener.getMostRecentLocation()).isEqualTo(location);
  }

  @Test public void requestLocationUpdates_shouldNotNotifyIfLessThanFastestIntervalGps()
      throws Exception {
    TestLocationListener listener = new TestLocationListener();
    LocationRequest request = LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY);
    request.setFastestInterval(5000);
    api.requestLocationUpdates(request, listener);

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
    api.requestLocationUpdates(request, listener);

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
    api.requestLocationUpdates(request, listener);

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
    api.requestLocationUpdates(request, listener);

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
    api.requestLocationUpdates(request, listener);

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
    api.requestLocationUpdates(request, listener);

    final long time = System.currentTimeMillis();
    Location networkLocation = getTestLocation(NETWORK_PROVIDER, 0, 0, time);
    Location gpsLocation = getTestLocation(GPS_PROVIDER, 0, 0, time + 1);

    networkLocation.setAccuracy(10);
    gpsLocation.setAccuracy(20);
    shadowLocationManager.simulateLocation(networkLocation);
    shadowLocationManager.simulateLocation(gpsLocation);
    assertThat(listener.getMostRecentLocation()).isEqualTo(networkLocation);
  }

  @Test public void removeLocationUpdates_shouldUnregisterAllListeners() throws Exception {
    TestLocationListener listener = new TestLocationListener();
    LocationRequest request = LocationRequest.create();
    api.requestLocationUpdates(request, listener);
    api.removeLocationUpdates(listener);
    assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).isEmpty();
  }

  @Test public void setMockMode_shouldUnregisterAllListenersWhenTrue() throws Exception {
    TestLocationListener listener = new TestLocationListener();
    LocationRequest request = LocationRequest.create();
    api.requestLocationUpdates(request, listener);
    api.setMockMode(true);
    assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).isEmpty();
  }

  @Test public void setMockMode_shouldNotRegisterDuplicateListeners() throws Exception {
    TestLocationListener listener = new TestLocationListener();
    LocationRequest request = LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY);
    api.setMockMode(true);
    api.requestLocationUpdates(request, listener);
    api.setMockMode(false);
    api.requestLocationUpdates(request, listener);
    assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).hasSize(2);
  }

  @Test public void setMockMode_shouldToggleEngines() {
    TestLocationListener listener = new TestLocationListener();
    LocationRequest request = LocationRequest.create();
    api.requestLocationUpdates(request, listener);
    api.setMockMode(true);
    TestLocationListener listener2 = new TestLocationListener();
    LocationRequest request2 = LocationRequest.create();
    api.requestLocationUpdates(request2, listener2);
    assertThat(api.getListeners()).hasSize(2);
  }

  @Test public void requestLocationUpdates_shouldNotRegisterListenersWithMockModeOn()
      throws Exception {
    api.setMockMode(true);
    TestLocationListener listener = new TestLocationListener();
    LocationRequest request = LocationRequest.create();
    api.requestLocationUpdates(request, listener);
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
    api.requestLocationUpdates(request, listener);
    api.setMockLocation(mockLocation);
    assertThat(listener.getAllLocations()).hasSize(1);
    assertThat(listener.getMostRecentLocation()).isEqualTo(mockLocation);
  }

  @Test @Ignore("Intermittently failing. Find a better way to test without Thread.sleep(100)")
  public void setMockTrace_shouldInvokeListenerForEachLocation() throws Exception {
    api.setMockMode(true);
    api.setMockTrace(getTestGpxTrace());
    TestLocationListener listener = new TestLocationListener();
    LocationRequest request = LocationRequest.create();
    request.setFastestInterval(0);
    api.requestLocationUpdates(request, listener);
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

  @Test @Ignore("Intermittently failing. Find a better way to test without Thread.sleep(100)")
  public void setMockTrace_shouldBroadcastSpeedWithLocation() throws Exception {
    api.setMockMode(true);
    api.setMockTrace(getTestGpxTrace());
    TestLocationListener listener = new TestLocationListener();
    LocationRequest request = LocationRequest.create();
    request.setFastestInterval(0);
    api.requestLocationUpdates(request, listener);
    Thread.sleep(100);
    ShadowLooper.runUiThreadTasks();
    assertThat(listener.getAllLocations().get(0).getSpeed()).isEqualTo(10f);
    assertThat(listener.getAllLocations().get(1).getSpeed()).isEqualTo(20f);
    assertThat(listener.getAllLocations().get(2).getSpeed()).isEqualTo(30f);
  }

  @Test @Ignore("Intermittently failing. Find a better way to test without Thread.sleep(100)")
  public void setMockTrace_shouldRespectFastestInterval() throws Exception {
    api.setMockMode(true);
    api.setMockTrace(getTestGpxTrace());
    TestLocationListener listener = new TestLocationListener();
    LocationRequest request = LocationRequest.create();
    request.setInterval(100);
    api.requestLocationUpdates(request, listener);
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

  @Test public void isProviderEnabled_shouldReturnProviderStatus() throws Exception {
    shadowLocationManager.setProviderEnabled(LocationManager.GPS_PROVIDER, true);
    shadowLocationManager.setProviderEnabled(LocationManager.NETWORK_PROVIDER, true);
    assertThat(api.isProviderEnabled(LocationManager.GPS_PROVIDER)).isTrue();
    assertThat(api.isProviderEnabled(LocationManager.NETWORK_PROVIDER)).isTrue();

    shadowLocationManager.setProviderEnabled(LocationManager.GPS_PROVIDER, false);
    shadowLocationManager.setProviderEnabled(LocationManager.NETWORK_PROVIDER, false);
    assertThat(api.isProviderEnabled(LocationManager.GPS_PROVIDER)).isFalse();
    assertThat(api.isProviderEnabled(LocationManager.NETWORK_PROVIDER)).isFalse();
  }

  @Test public void onProviderDisabled_shouldReportWhenGpsIsDisabled() throws Exception {
    TestLocationListener listener = new TestLocationListener();
    LocationRequest request = LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY);
    api.requestLocationUpdates(request, listener);
    listener.isGpsEnabled = true;
    shadowLocationManager.setProviderEnabled(GPS_PROVIDER, false);
    assertThat(listener.isGpsEnabled).isFalse();
  }

  @Test public void onProviderDisabled_shouldReportWhenNetworkIsDisabled() throws Exception {
    TestLocationListener listener = new TestLocationListener();
    LocationRequest request = LocationRequest.create();
    api.requestLocationUpdates(request, listener);
    listener.isNetworkEnabled = true;
    shadowLocationManager.setProviderEnabled(NETWORK_PROVIDER, false);
    assertThat(listener.isNetworkEnabled).isFalse();
  }

  @Test public void onProviderEnabled_shouldReportWhenGpsIsEnabled() throws Exception {
    TestLocationListener listener = new TestLocationListener();
    LocationRequest request = LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY);
    api.requestLocationUpdates(request, listener);
    listener.isGpsEnabled = false;
    shadowLocationManager.setProviderEnabled(GPS_PROVIDER, true);
    assertThat(listener.isGpsEnabled).isTrue();
  }

  @Test public void onProviderEnabled_shouldReportWhenNetworkIsEnabled() throws Exception {
    TestLocationListener listener = new TestLocationListener();
    LocationRequest request = LocationRequest.create();
    api.requestLocationUpdates(request, listener);
    listener.isNetworkEnabled = false;
    shadowLocationManager.setProviderEnabled(NETWORK_PROVIDER, true);
    assertThat(listener.isNetworkEnabled).isTrue();
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

  class TestLocationListener implements LocationListener {
    private ArrayList<Location> locations = new ArrayList<>();
    private boolean isGpsEnabled = true;
    private boolean isNetworkEnabled = true;

    @Override public void onLocationChanged(Location location) {
      locations.add(location);
    }

    @Override public void onProviderDisabled(String provider) {
      switch (provider) {
        case GPS_PROVIDER:
          isGpsEnabled = false;
          break;
        case NETWORK_PROVIDER:
          isNetworkEnabled = false;
          break;
        default:
          break;
      }
    }

    @Override public void onProviderEnabled(String provider) {
      switch (provider) {
        case GPS_PROVIDER:
          isGpsEnabled = true;
          break;
        case NETWORK_PROVIDER:
          isNetworkEnabled = true;
          break;
        default:
          break;
      }
    }

    public List<Location> getAllLocations() {
      return locations;
    }

    public Location getMostRecentLocation() {
      return locations.get(locations.size() - 1);
    }
  }

  @Test public void requestLocationUpdates_shouldNotifyBothListeners() {
    LocationRequest request = LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY);
    TestLocationListener listener1 = new TestLocationListener();
    TestLocationListener listener2 = new TestLocationListener();
    api.requestLocationUpdates(request, listener1);
    api.requestLocationUpdates(request, listener2);
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
    api.requestLocationUpdates(request, listener1);
    api.requestLocationUpdates(request, listener2);
    api.removeLocationUpdates(listener2);
    Location location = new Location(GPS_PROVIDER);
    location.setLatitude(40.0);
    location.setLongitude(70.0);
    shadowLocationManager.simulateLocation(location);
    assertThat(listener1.getAllLocations()).contains(location);
    assertThat(listener2.getAllLocations()).doesNotContain(location);
  }

  @Test public void requestLocationUpdates_shouldRegisterGpsAndNetworkListenerViaPendingIntent()
      throws Exception {
    PendingIntent pendingIntent = PendingIntent.getService(application, 0, new Intent(), 0);
    api.requestLocationUpdates(LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY),
        pendingIntent);
    assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).hasSize(2);
  }

  @Test public void requestLocationUpdates_shouldNotifyOnLocationChangedGpsViaPendingIntent()
      throws Exception {
    Intent intent = new Intent(application, TestService.class);
    PendingIntent pendingIntent = PendingIntent.getService(application, 0, intent, 0);
    LocationRequest locationRequest = LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY);

    api.requestLocationUpdates(locationRequest, pendingIntent);
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

    api.requestLocationUpdates(locationRequest, pendingIntent);
    Location location = new Location(NETWORK_PROVIDER);
    shadowLocationManager.simulateLocation(location);

    Intent nextStartedService = ShadowApplication.getInstance().getNextStartedService();
    assertThat(nextStartedService).isNotNull();
    assertThat(nextStartedService.getParcelableExtra(KEY_LOCATION_CHANGED)).isEqualTo(location);
  }

  @Test public void removeLocationUpdates_shouldUnregisterAllPendingIntentListeners()
      throws Exception {
    Intent intent = new Intent(application, TestService.class);
    PendingIntent pendingIntent = PendingIntent.getService(application, 0, intent, 0);
    LocationRequest locationRequest =
        LocationRequest.create().setPriority(PRIORITY_BALANCED_POWER_ACCURACY);
    api.requestLocationUpdates(locationRequest, pendingIntent);

    api.removeLocationUpdates(pendingIntent);
    assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).isEmpty();
  }

  @Test public void requestLocationUpdates_shouldNotNotifyRemovedPendingIntent() throws Exception {
    LocationRequest request = LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY);
    Intent intent = new Intent(application, TestService.class);
    PendingIntent pendingIntent1 = PendingIntent.getService(application, 0, intent, 0);
    PendingIntent pendingIntent2 = PendingIntent.getService(application, 0, intent, 0);
    api.requestLocationUpdates(request, pendingIntent1);
    clearShadowLocationListeners();
    api.requestLocationUpdates(request, pendingIntent2);

    api.removeLocationUpdates(pendingIntent2);
    Location location = new Location(GPS_PROVIDER);
    location.setLatitude(40.0);
    location.setLongitude(70.0);
    shadowLocationManager.simulateLocation(location);

    // Only one service should be started since the second pending intent request was removed.
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNotNull();
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNull();
  }

  @Test public void removeLocationUpdates_shouldNotKillEngineIfListenerStillActive()
      throws Exception {
    TestLocationListener listener = new TestLocationListener();
    api.requestLocationUpdates(LocationRequest.create(), listener);

    PendingIntent pendingIntent = PendingIntent.getService(application, 0, new Intent(), 0);
    api.requestLocationUpdates(LocationRequest.create(), pendingIntent);

    api.removeLocationUpdates(pendingIntent);
    assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).isNotEmpty();
  }

  @Test public void removeLocationUpdates_shouldNotKillEngineIfIntentStillActive()
      throws Exception {
    TestLocationListener listener = new TestLocationListener();
    api.requestLocationUpdates(LocationRequest.create(), listener);

    PendingIntent pendingIntent = PendingIntent.getService(application, 0, new Intent(), 0);
    api.requestLocationUpdates(LocationRequest.create(), pendingIntent);

    api.removeLocationUpdates(listener);
    assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).isNotEmpty();
  }

  @Test public void shutdown_shouldUnregisterLocationUpdateListeners() throws Exception {
    LostApiClient client = new LostApiClient.Builder(application).build();
    client.connect();
    LocationServices.FusedLocationApi.requestLocationUpdates(LocationRequest.create(),
        new TestLocationListener());

    api.shutdown();
    LocationManager lm = (LocationManager) application.getSystemService(LOCATION_SERVICE);
    assertThat(shadowOf(lm).getRequestLocationUpdateListeners()).isEmpty();
    client.disconnect();
  }

  @Test public void shutdown_shouldClearListeners() {
    LostApiClient client = new LostApiClient.Builder(application).build();
    client.connect();
    LocationServices.FusedLocationApi.requestLocationUpdates(LocationRequest.create(),
        new TestLocationListener());
    api.shutdown();
    assertThat(api.getListeners()).isEmpty();
    client.disconnect();
  }

  @Test public void shutdown_shouldClearPendingIntents() {
    LostApiClient client = new LostApiClient.Builder(application).build();
    client.connect();
    LocationServices.FusedLocationApi.requestLocationUpdates(LocationRequest.create(),
        mock(PendingIntent.class));
    api.shutdown();
    assertThat(api.getPendingIntents()).isEmpty();
    client.disconnect();
  }

  /**
   * Due to a bug in Robolectric that allows the same location listener to be registered twice,
   * we need to manually clear the `ShadowLocationManager` to prevent duplicate listeners.
   *
   * @see <a href="https://github.com/robolectric/robolectric/issues/2603">
   * ShadowLocationManager should not allow duplicate listeners</a>
   */
  private void clearShadowLocationListeners() {
    Map<String, List> map = ReflectionHelpers.getField(shadowLocationManager, "locationListeners");
    map.clear();
  }

  public class TestService extends IntentService {
    public TestService() {
      super("test");
    }

    @Override protected void onHandleIntent(Intent intent) {
    }
  }

}
