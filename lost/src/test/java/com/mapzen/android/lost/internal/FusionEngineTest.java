package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLocationManager;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static android.location.LocationManager.PASSIVE_PROVIDER;
import static com.mapzen.android.lost.api.LocationClient.RECENT_UPDATE_THRESHOLD_IN_MILLIS;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.application;
import static org.robolectric.Robolectric.shadowOf;

@Config(manifest=Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class FusionEngineTest {
    private FusionEngine fusionEngine;
    private TestCallback callback;
    private LocationManager locationManager;
    private ShadowLocationManager shadowLocationManager;

    @Before
    public void setUp() throws Exception {
        callback = new TestCallback();
        fusionEngine = new FusionEngine(application, callback);
        locationManager = (LocationManager) application.getSystemService(Context.LOCATION_SERVICE);
        shadowLocationManager = shadowOf(locationManager);
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(fusionEngine).isNotNull();
    }

    @Test
    public void getLastLocation_shouldReturnNullIfNoLocationAvailable() throws Exception {
        assertThat(fusionEngine.getLastLocation()).isNull();
    }

    @Test
    public void getLastLocation_shouldReturnGpsLocationIfOnlyProvider() throws Exception {
        Location location = new Location(GPS_PROVIDER);
        shadowLocationManager.setLastKnownLocation(GPS_PROVIDER, location);
        assertThat(fusionEngine.getLastLocation()).isEqualTo(location);
    }

    @Test
    public void getLastLocation_shouldReturnNetworkLocationIfOnlyProvider() throws Exception {
        Location location = new Location(NETWORK_PROVIDER);
        shadowLocationManager.setLastKnownLocation(NETWORK_PROVIDER, location);
        assertThat(fusionEngine.getLastLocation()).isEqualTo(location);
    }

    @Test
    public void getLastLocation_shouldReturnPassiveLocationIfOnlyProvider() throws Exception {
        Location location = new Location(PASSIVE_PROVIDER);
        shadowLocationManager.setLastKnownLocation(PASSIVE_PROVIDER, location);
        assertThat(fusionEngine.getLastLocation()).isEqualTo(location);
    }

    @Test
    public void getLastLocation_shouldReturnMostAccurateResult() throws Exception {
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

    @Test
    public void getLastLocation_shouldIgnoreStaleLocations() throws Exception {
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

    @Test
    public void getLastLocation_ifNoFreshLocationsShouldReturnMostRecent() throws Exception {
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

    @Test
    public void setRequest_shouldEnableLocationUpdatesForValidRequest() throws Exception {
        fusionEngine.setRequest(LocationRequest.create());
        assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).hasSize(2);
    }

    @Test
    public void setRequest_shouldDisableLocationUpdatesForNullRequest() throws Exception {
        fusionEngine.setRequest(LocationRequest.create());
        fusionEngine.setRequest(null);
        assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).isEmpty();
    }

    @Test
    public void onLocationChanged_shouldReportGps() throws Exception {
        fusionEngine.setRequest(LocationRequest.create());
        Location location = new Location(GPS_PROVIDER);
        shadowLocationManager.simulateLocation(location);
        assertThat(callback.location).isEqualTo(location);
    }

    @Test
    public void onLocationChanged_shouldReportNetwork() throws Exception {
        fusionEngine.setRequest(LocationRequest.create());
        Location location = new Location(NETWORK_PROVIDER);
        shadowLocationManager.simulateLocation(location);
        assertThat(callback.location).isEqualTo(location);
    }

    @Test
    public void onLocationChanged_shouldNotReportLessThanFastestIntervalGps() throws Exception {
        LocationRequest request = LocationRequest.create().setFastestInterval(5000);
        fusionEngine.setRequest(request);

        final long time = System.currentTimeMillis();
        Location location1 = getTestLocation(GPS_PROVIDER, 0, 0, time);
        Location location2 = getTestLocation(GPS_PROVIDER, 1, 1, time + 1000);

        shadowLocationManager.simulateLocation(location1);
        shadowLocationManager.simulateLocation(location2);
        assertThat(callback.location).isEqualTo(location1);
    }

    @Test
    public void onLocationChanged_shouldNotReportLessThanFastestIntervalNetwork() throws Exception {
        LocationRequest request = LocationRequest.create().setFastestInterval(5000);
        fusionEngine.setRequest(request);

        final long time = System.currentTimeMillis();
        Location location1 = getTestLocation(NETWORK_PROVIDER, 0, 0, time);
        Location location2 = getTestLocation(NETWORK_PROVIDER, 1, 1, time + 1000);

        shadowLocationManager.simulateLocation(location1);
        shadowLocationManager.simulateLocation(location2);
        assertThat(callback.location).isEqualTo(location1);
    }

    @Test
    public void onLocationChanged_shouldNotReportLessThanMinDisplacementGps() throws Exception {
        LocationRequest request = LocationRequest.create().setSmallestDisplacement(200000);
        fusionEngine.setRequest(request);

        final long time = System.currentTimeMillis();
        Location location1 = getTestLocation(GPS_PROVIDER, 0, 0, time);
        Location location2 = getTestLocation(GPS_PROVIDER, 1, 1, time + 1000);

        shadowLocationManager.simulateLocation(location1);
        shadowLocationManager.simulateLocation(location2);
        assertThat(callback.location).isEqualTo(location1);
    }

    @Test
    public void onLocationChanged_shouldNotReportLessThanMinDisplacementNetwork() throws Exception {
        LocationRequest request = LocationRequest.create().setSmallestDisplacement(200000);
        fusionEngine.setRequest(request);

        final long time = System.currentTimeMillis();
        Location location1 = getTestLocation(NETWORK_PROVIDER, 0, 0, time);
        Location location2 = getTestLocation(NETWORK_PROVIDER, 1, 1, time + 1000);

        shadowLocationManager.simulateLocation(location1);
        shadowLocationManager.simulateLocation(location2);
        assertThat(callback.location).isEqualTo(location1);
    }

    @Test
    public void onLocationChanged_shouldIgnoreNetworkWhenGpsIsMoreAccurate() throws Exception {
        LocationRequest request = LocationRequest.create().setFastestInterval(0);
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

    @Test
    public void onLocationChanged_shouldIgnoreGpsWhenNetworkIsMoreAccurate() throws Exception {
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

        @Override
        public void reportLocation(Location location) {
            this.location = location;
        }
    }
}
