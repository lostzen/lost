package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.BaseRobolectricTest;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.lost.BuildConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLocationManager;

import android.annotation.TargetApi;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static android.location.LocationManager.PASSIVE_PROVIDER;
import static com.mapzen.android.lost.api.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
import static com.mapzen.android.lost.api.LocationRequest.PRIORITY_HIGH_ACCURACY;
import static com.mapzen.android.lost.api.LocationRequest.PRIORITY_LOW_POWER;
import static com.mapzen.android.lost.api.LocationRequest.PRIORITY_NO_POWER;
import static com.mapzen.android.lost.internal.FusionEngine.RECENT_UPDATE_THRESHOLD_IN_MILLIS;
import static com.mapzen.android.lost.internal.FusionEngine.RECENT_UPDATE_THRESHOLD_IN_NANOS;
import static com.mapzen.android.lost.internal.SystemClock.MS_TO_NS;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.RuntimeEnvironment.application;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, manifest = Config.NONE)
public class FusionEngineTest extends BaseRobolectricTest {
  private FusionEngine fusionEngine;
  private TestCallback callback;
  private LocationManager locationManager;
  private ShadowLocationManager shadowLocationManager;

  @Before public void setUp() throws Exception {
    callback = new TestCallback();
    fusionEngine = new FusionEngine(application, callback);
    locationManager = (LocationManager) application.getSystemService(Context.LOCATION_SERVICE);
    shadowLocationManager = shadowOf(locationManager);
  }

  @Test public void shouldNotBeNull() throws Exception {
    assertThat(fusionEngine).isNotNull();
  }

  @Test public void getLastLocation_shouldReturnNullIfNoLocationAvailable() throws Exception {
    assertThat(fusionEngine.getLastLocation()).isNull();
  }

  @Test public void getLastLocation_shouldReturnGpsLocationIfOnlyProvider() throws Exception {
    Location location = new Location(GPS_PROVIDER);
    shadowLocationManager.setLastKnownLocation(GPS_PROVIDER, location);
    assertThat(fusionEngine.getLastLocation()).isEqualTo(location);
  }

  @Test public void getLastLocation_shouldReturnNetworkLocationIfOnlyProvider() throws Exception {
    Location location = new Location(NETWORK_PROVIDER);
    shadowLocationManager.setLastKnownLocation(NETWORK_PROVIDER, location);
    assertThat(fusionEngine.getLastLocation()).isEqualTo(location);
  }

  @Test public void getLastLocation_shouldReturnPassiveLocationIfOnlyProvider() throws Exception {
    Location location = new Location(PASSIVE_PROVIDER);
    shadowLocationManager.setLastKnownLocation(PASSIVE_PROVIDER, location);
    assertThat(fusionEngine.getLastLocation()).isEqualTo(location);
  }

  @Test public void getLastLocation_shouldReturnMostAccurateResult() throws Exception {
    Location gpsLocation = new Location(GPS_PROVIDER);
    gpsLocation.setAccuracy(1000);
    shadowLocationManager.setLastKnownLocation(GPS_PROVIDER, gpsLocation);

    Location networkLocation = new Location(NETWORK_PROVIDER);
    networkLocation.setAccuracy(100);
    shadowLocationManager.setLastKnownLocation(NETWORK_PROVIDER, networkLocation);

    Location passiveLocation = new Location(PASSIVE_PROVIDER);
    passiveLocation.setAccuracy(10);
    shadowLocationManager.setLastKnownLocation(PASSIVE_PROVIDER, passiveLocation);

    assertThat(fusionEngine.getLastLocation()).isEqualTo(passiveLocation);
  }

  @Test public void getLastLocation_shouldIgnoreStaleLocations() throws Exception {
    long time = System.currentTimeMillis();
    initTestClock(time);

    Location gpsLocation = new Location(GPS_PROVIDER);
    gpsLocation.setAccuracy(100);
    gpsLocation.setTime(time);
    shadowLocationManager.setLastKnownLocation(GPS_PROVIDER, gpsLocation);

    Location networkLocation = new Location(NETWORK_PROVIDER);
    networkLocation.setAccuracy(100);
    networkLocation.setTime(time - (2 * RECENT_UPDATE_THRESHOLD_IN_MILLIS));
    shadowLocationManager.setLastKnownLocation(NETWORK_PROVIDER, networkLocation);

    assertThat(fusionEngine.getLastLocation()).isEqualTo(gpsLocation);
  }

  @Test public void getLastLocation_ifNoFreshLocationsShouldReturnMostRecent() throws Exception {
    long time = System.currentTimeMillis();
    initTestClock(time);

    Location gpsLocation = new Location(GPS_PROVIDER);
    gpsLocation.setAccuracy(100);
    gpsLocation.setTime(time - (2 * RECENT_UPDATE_THRESHOLD_IN_MILLIS));
    shadowLocationManager.setLastKnownLocation(GPS_PROVIDER, gpsLocation);

    Location networkLocation = new Location(NETWORK_PROVIDER);
    networkLocation.setAccuracy(100);
    networkLocation.setTime(time - (3 * RECENT_UPDATE_THRESHOLD_IN_MILLIS));
    shadowLocationManager.setLastKnownLocation(NETWORK_PROVIDER, networkLocation);

    assertThat(fusionEngine.getLastLocation()).isEqualTo(gpsLocation);
  }

  @SuppressWarnings("MissingPermission") @Test
  public void getLastLocation_shouldReturnNullIfNoLocationPermissionsGranted() throws Exception {
    Context mockContext = mock(Context.class);
    LocationManager mockLocatinManager = mock(LocationManager.class);
    List<String> providers = new ArrayList<>(3);
    providers.add(GPS_PROVIDER);
    providers.add(NETWORK_PROVIDER);
    providers.add(PASSIVE_PROVIDER);

    when(mockContext.getSystemService(Context.LOCATION_SERVICE)).thenReturn(mockLocatinManager);
    when(mockLocatinManager.getAllProviders()).thenReturn(providers);
    when(mockLocatinManager.getLastKnownLocation(GPS_PROVIDER)).thenThrow(new SecurityException());
    when(mockLocatinManager.getLastKnownLocation(NETWORK_PROVIDER)).thenThrow(
        new SecurityException());
    when(mockLocatinManager.getLastKnownLocation(PASSIVE_PROVIDER)).thenThrow(
        new SecurityException());

    final FusionEngine fusionEngine = new FusionEngine(mockContext, callback);
    assertThat(fusionEngine.getLastLocation()).isNull();
  }

  @Test public void setRequest_shouldRegisterGpsAndNetworkIfPriorityHighAccuracy()
      throws Exception {
    fusionEngine.setRequest(LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY));
    Collection<String> providers = shadowLocationManager.getProvidersForListener(fusionEngine);
    assertThat(providers).hasSize(2);
    assertThat(providers).contains(GPS_PROVIDER);
    assertThat(providers).contains(NETWORK_PROVIDER);
  }

  @Test public void setRequest_shouldRegisterNetworkOnlyIfPriorityBalanced() throws Exception {
    fusionEngine.setRequest(LocationRequest.create().setPriority(PRIORITY_BALANCED_POWER_ACCURACY));
    Collection<String> providers = shadowLocationManager.getProvidersForListener(fusionEngine);
    assertThat(providers).hasSize(1);
    assertThat(providers).contains(NETWORK_PROVIDER);
  }

  @Test public void setRequest_shouldRegisterNetworkOnlyIfPriorityLowPower() throws Exception {
    fusionEngine.setRequest(LocationRequest.create().setPriority(PRIORITY_LOW_POWER));
    Collection<String> providers = shadowLocationManager.getProvidersForListener(fusionEngine);
    assertThat(providers).hasSize(1);
    assertThat(providers).contains(NETWORK_PROVIDER);
  }

  @Test public void setRequest_shouldRegisterPassiveProviderOnlyNoPower() throws Exception {
    fusionEngine.setRequest(LocationRequest.create().setPriority(PRIORITY_NO_POWER));
    Collection<String> providers = shadowLocationManager.getProvidersForListener(fusionEngine);
    assertThat(providers).hasSize(1);
    assertThat(providers).contains(PASSIVE_PROVIDER);
  }

  @Test public void setRequest_shouldDisableLocationUpdatesForNullRequest() throws Exception {
    fusionEngine.setRequest(LocationRequest.create());
    fusionEngine.setRequest(null);
    assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).isEmpty();
  }

  @Test public void onLocationChanged_shouldReportGps() throws Exception {
    fusionEngine.setRequest(LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY));
    Location location = new Location(GPS_PROVIDER);
    shadowLocationManager.simulateLocation(location);
    assertThat(callback.location).isEqualTo(location);
  }

  @Test public void onLocationChanged_shouldReportNetwork() throws Exception {
    fusionEngine.setRequest(LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY));
    Location location = new Location(NETWORK_PROVIDER);
    shadowLocationManager.simulateLocation(location);
    assertThat(callback.location).isEqualTo(location);
  }

  @Test public void onLocationChanged_shouldNotReportLessThanFastestIntervalGps() throws Exception {
    LocationRequest request =
        LocationRequest.create().setFastestInterval(5000).setPriority(PRIORITY_HIGH_ACCURACY);
    fusionEngine.setRequest(request);

    final long time = System.currentTimeMillis();
    Location location1 = getTestLocation(GPS_PROVIDER, 0, 0, time);
    Location location2 = getTestLocation(GPS_PROVIDER, 1, 1, time + 1000);

    shadowLocationManager.simulateLocation(location1);
    shadowLocationManager.simulateLocation(location2);
    assertThat(callback.location).isEqualTo(location1);
  }

  @Test public void onLocationChanged_shouldNotReportLessThanFastestIntervalNetwork()
      throws Exception {
    LocationRequest request =
        LocationRequest.create().setFastestInterval(5000).setPriority(PRIORITY_HIGH_ACCURACY);
    fusionEngine.setRequest(request);

    final long time = System.currentTimeMillis();
    Location location1 = getTestLocation(NETWORK_PROVIDER, 0, 0, time);
    Location location2 = getTestLocation(NETWORK_PROVIDER, 1, 1, time + 1000);

    shadowLocationManager.simulateLocation(location1);
    shadowLocationManager.simulateLocation(location2);
    assertThat(callback.location).isEqualTo(location1);
  }

  @Test public void onLocationChanged_shouldNotReportLessThanMinDisplacementGps() throws Exception {
    LocationRequest request = LocationRequest.create()
        .setSmallestDisplacement(200000)
        .setPriority(PRIORITY_HIGH_ACCURACY);
    fusionEngine.setRequest(request);

    final long time = System.currentTimeMillis();
    Location location1 = getTestLocation(GPS_PROVIDER, 0, 0, time);
    Location location2 = getTestLocation(GPS_PROVIDER, 1, 1, time + 1000);

    shadowLocationManager.simulateLocation(location1);
    shadowLocationManager.simulateLocation(location2);
    assertThat(callback.location).isEqualTo(location1);
  }

  @Test public void onLocationChanged_shouldNotReportLessThanMinDisplacementNetwork()
      throws Exception {
    LocationRequest request = LocationRequest.create().setSmallestDisplacement(200000);
    fusionEngine.setRequest(request);

    final long time = System.currentTimeMillis();
    Location location1 = getTestLocation(NETWORK_PROVIDER, 0, 0, time);
    Location location2 = getTestLocation(NETWORK_PROVIDER, 1, 1, time + 1000);

    shadowLocationManager.simulateLocation(location1);
    shadowLocationManager.simulateLocation(location2);
    assertThat(callback.location).isEqualTo(location1);
  }

  @Test public void onLocationChanged_shouldIgnoreNetworkWhenGpsIsMoreAccurate() throws Exception {
    LocationRequest request =
        LocationRequest.create().setFastestInterval(0).setPriority(PRIORITY_HIGH_ACCURACY);
    fusionEngine.setRequest(request);

    final long time = System.currentTimeMillis();
    Location gpsLocation = getTestLocation(GPS_PROVIDER, 0, 0, time);
    Location networkLocation = getTestLocation(NETWORK_PROVIDER, 0, 0, time + 1);

    gpsLocation.setAccuracy(10);
    networkLocation.setAccuracy(20);
    shadowLocationManager.simulateLocation(gpsLocation);
    shadowLocationManager.simulateLocation(networkLocation);
    assertThat(callback.location).isEqualTo(gpsLocation);
  }

  @Test public void onLocationChanged_shouldIgnoreGpsWhenNetworkIsMoreAccurate() throws Exception {
    LocationRequest request = LocationRequest.create().setFastestInterval(0);
    fusionEngine.setRequest(request);

    final long time = System.currentTimeMillis();
    Location networkLocation = getTestLocation(NETWORK_PROVIDER, 0, 0, time + 1);
    Location gpsLocation = getTestLocation(GPS_PROVIDER, 0, 0, time);

    networkLocation.setAccuracy(10);
    gpsLocation.setAccuracy(20);
    shadowLocationManager.simulateLocation(networkLocation);
    shadowLocationManager.simulateLocation(gpsLocation);
    assertThat(callback.location).isEqualTo(networkLocation);
  }

  @Test public void onLocationChanged_gps_shouldReportLastKnownLocation() throws Exception {
    Location gpsLocation = new Location(GPS_PROVIDER);
    shadowLocationManager.setLastKnownLocation(GPS_PROVIDER, gpsLocation);

    LocationRequest request = LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY);
    fusionEngine.setRequest(request);

    assertThat(callback.location).isEqualTo(gpsLocation);
  }

  @Test public void onLocationChanged_network_shouldReportLastKnownLocation() throws Exception {
    Location networkLocation = new Location(NETWORK_PROVIDER);
    shadowLocationManager.setLastKnownLocation(NETWORK_PROVIDER, networkLocation);

    LocationRequest request = LocationRequest.create().setPriority(
        PRIORITY_BALANCED_POWER_ACCURACY);
    fusionEngine.setRequest(request);

    assertThat(callback.location).isEqualTo(networkLocation);
  }

  @Test public void onLocationChanged_gpsNetwork_shouldReportLastKnownLocation() throws Exception {
    Location gpsLocation = new Location(GPS_PROVIDER);
    shadowLocationManager.setLastKnownLocation(GPS_PROVIDER, gpsLocation);
    Location networkLocation = new Location(NETWORK_PROVIDER);
    shadowLocationManager.setLastKnownLocation(NETWORK_PROVIDER, networkLocation);

    LocationRequest request = LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY);
    fusionEngine.setRequest(request);

    assertThat(callback.location).isEqualTo(networkLocation);
  }

  @Test public void onLocationChanged_gpsNetwork_shouldReportOneLastKnownLocation() throws
      Exception {
    Location gpsLocation = new Location(GPS_PROVIDER);
    shadowLocationManager.setLastKnownLocation(GPS_PROVIDER, gpsLocation);
    Location networkLocation = new Location(NETWORK_PROVIDER);
    shadowLocationManager.setLastKnownLocation(NETWORK_PROVIDER, networkLocation);

    LocationRequest request = LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY);
    fusionEngine.setRequest(request);

    assertThat(callback.numLocationReports).isEqualTo(1);
  }


  @Test public void isBetterThan_shouldReturnFalseIfLocationAIsNull() throws Exception {
    Location locationA = null;
    Location locationB = new Location("test");
    //noinspection ConstantConditions
    assertThat(FusionEngine.isBetterThan(locationA, locationB)).isFalse();
  }

  @Test public void isBetterThan_shouldReturnTrueIfLocationBIsNull() throws Exception {
    Location locationA = new Location("test");
    Location locationB = null;
    //noinspection ConstantConditions
    assertThat(FusionEngine.isBetterThan(locationA, locationB)).isTrue();
  }

  @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1) @Test @Config(sdk = 17)
  public void isBetterThan_shouldReturnTrueIfLocationBIsStale_Api17() throws Exception {
    final long timeInNanos = System.currentTimeMillis() * MS_TO_NS;
    Location locationA = new Location("test");
    Location locationB = new Location("test");
    locationA.setElapsedRealtimeNanos(timeInNanos);
    locationB.setElapsedRealtimeNanos(timeInNanos - RECENT_UPDATE_THRESHOLD_IN_NANOS - 1);
    assertThat(FusionEngine.isBetterThan(locationA, locationB)).isTrue();
  }

  @Test @Config(sdk = 16) public void isBetterThan_shouldReturnTrueIfLocationBIsStale_Api16()
      throws Exception {
    final long timeInMillis = System.currentTimeMillis();
    Location locationA = new Location("test");
    Location locationB = new Location("test");
    locationA.setTime(timeInMillis);
    locationB.setTime(timeInMillis - RECENT_UPDATE_THRESHOLD_IN_MILLIS - 1);
    assertThat(FusionEngine.isBetterThan(locationA, locationB)).isTrue();
  }

  @Test public void isBetterThan_shouldReturnFalseIfLocationAHasNoAccuracy() throws Exception {
    Location locationA = new Location("test");
    Location locationB = new Location("test");
    locationA.removeAccuracy();
    locationB.setAccuracy(30.0f);
    assertThat(FusionEngine.isBetterThan(locationA, locationB)).isFalse();
  }

  @Test public void isBetterThan_shouldReturnTrueIfLocationBHasNoAccuracy() throws Exception {
    Location locationA = new Location("test");
    Location locationB = new Location("test");
    locationA.setAccuracy(30.0f);
    locationB.removeAccuracy();
    assertThat(FusionEngine.isBetterThan(locationA, locationB)).isTrue();
  }

  @Test public void isBetterThan_shouldReturnTrueIfLocationAIsMoreAccurate() throws Exception {
    Location locationA = new Location("test");
    Location locationB = new Location("test");
    locationA.setAccuracy(30.0f);
    locationB.setAccuracy(40.0f);
    assertThat(FusionEngine.isBetterThan(locationA, locationB)).isTrue();
  }

  @Test public void isBetterThan_shouldReturnFalseIfLocationBIsMoreAccurate() throws Exception {
    Location locationA = new Location("test");
    Location locationB = new Location("test");
    locationA.setAccuracy(40.0f);
    locationB.setAccuracy(30.0f);
    assertThat(FusionEngine.isBetterThan(locationA, locationB)).isFalse();
  }

  private static void initTestClock(long time) {
    TestClock testClock = new TestClock();
    testClock.setCurrentTimeInMillis(time);
    FusionEngine.clock = testClock;
  }

  private static Location getTestLocation(String provider, float lat, float lng, long time) {
    Location location = new Location(provider);
    location.setLatitude(lat);
    location.setLongitude(lng);
    location.setTime(time);
    return location;
  }

  class TestCallback implements LocationEngine.Callback {
    private Location location;
    private int numLocationReports = 0;

    @Override public void reportLocation(Location location) {
      this.location = location;
      numLocationReports++;
    }

    @Override public void reportProviderDisabled(String provider) {
    }

    @Override public void reportProviderEnabled(String provider) {
    }
  }
}
