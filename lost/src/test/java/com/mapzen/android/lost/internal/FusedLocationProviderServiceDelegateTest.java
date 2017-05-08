package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.BaseRobolectricTest;
import com.mapzen.android.lost.api.LocationAvailability;
import com.mapzen.android.lost.api.LocationRequest;
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
import org.robolectric.shadows.ShadowEnvironment;
import org.robolectric.shadows.ShadowLooper;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static android.content.Context.LOCATION_SERVICE;
import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static com.mapzen.android.lost.api.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
import static com.mapzen.android.lost.api.LocationRequest.PRIORITY_HIGH_ACCURACY;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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

  @Before public void setUp() throws Exception {
    client = new LostApiClient.Builder(application).build();
    otherClient = new LostApiClient.Builder(application).build();
    delegate = new FusedLocationProviderServiceDelegate(application);
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
    TestFusedLocationProviderCallback callback = new TestFusedLocationProviderCallback();
    delegate.init(callback);
    delegate.requestLocationUpdates(LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY));

    Location location = new Location(GPS_PROVIDER);
    shadowLocationManager.simulateLocation(location);
    assertThat(callback.location).isEqualTo(location);
  }

  @Test public void requestLocationUpdates_shouldNotifyOnLocationChangedNetwork() throws Exception {
    TestFusedLocationProviderCallback callback = new TestFusedLocationProviderCallback();
    delegate.init(callback);
    delegate.requestLocationUpdates(LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY));

    Location location = new Location(NETWORK_PROVIDER);
    shadowLocationManager.simulateLocation(location);
    assertThat(callback.location).isEqualTo(location);
  }

  @Test public void requestLocationUpdates_shouldPreferGpsIfMoreAccurate() throws Exception {
    TestFusedLocationProviderCallback callback = new TestFusedLocationProviderCallback();
    delegate.init(callback);
    delegate.requestLocationUpdates(LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY));

    Location gpsLocation = getTestLocation(GPS_PROVIDER, 0, 0, 0);
    Location networkLocation = getTestLocation(NETWORK_PROVIDER, 0, 0, 0);

    gpsLocation.setAccuracy(10);
    networkLocation.setAccuracy(20);
    shadowLocationManager.simulateLocation(gpsLocation);
    shadowLocationManager.simulateLocation(networkLocation);
    assertThat(callback.location).isEqualTo(gpsLocation);
  }

  @Test public void requestLocationUpdates_shouldPreferNetworkIfMoreAccurate() throws Exception {
    TestFusedLocationProviderCallback callback = new TestFusedLocationProviderCallback();
    delegate.init(callback);
    delegate.requestLocationUpdates(LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY));

    Location gpsLocation = getTestLocation(GPS_PROVIDER, 0, 0, 0);
    Location networkLocation = getTestLocation(NETWORK_PROVIDER, 0, 0, 0);

    gpsLocation.setAccuracy(20);
    networkLocation.setAccuracy(10);
    shadowLocationManager.simulateLocation(networkLocation);
    shadowLocationManager.simulateLocation(gpsLocation);
    assertThat(callback.location).isEqualTo(networkLocation);
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

  @Test public void setMockLocation_shouldCallbackOnce() throws Exception {
    IFusedLocationProviderCallback callback = mock(IFusedLocationProviderCallback.class);
    delegate.init(callback);
    Location mockLocation = new Location("mock");
    delegate.setMockMode(true);
    TestLocationListener listener = new TestLocationListener();
    LocationRequest request = LocationRequest.create();
    delegate.requestLocationUpdates(request);
    LostClientManager.shared().addListener(client, request, listener);
    delegate.setMockLocation(mockLocation);
    verify(callback, times(1)).onLocationChanged(mockLocation);
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

  @Test public void requestLocationUpdates_shouldRegisterGpsAndNetworkListenerViaPendingIntent()
      throws Exception {
    delegate.requestLocationUpdates(LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY));
    assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).hasSize(2);
  }

  @Test public void removeLocationUpdates_shouldUnregisterAllPendingIntentListeners()
      throws Exception {
    LocationRequest locationRequest =
        LocationRequest.create().setPriority(PRIORITY_BALANCED_POWER_ACCURACY);
    delegate.requestLocationUpdates(locationRequest);
    delegate.removeLocationUpdates();
    assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).isEmpty();
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

  @Test public void reportProviderEnabled_shouldNotifyAvailabilityChanged() throws Exception {
    TestFusedLocationProviderCallback callback = new TestFusedLocationProviderCallback();
    delegate.init(callback);
    delegate.reportProviderEnabled(GPS_PROVIDER);
    assertThat(callback.locationAvailability).isNotNull();
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
