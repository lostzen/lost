package com.mapzen.android.lost;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLocationManager;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static android.location.LocationManager.PASSIVE_PROVIDER;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.application;
import static org.robolectric.Robolectric.shadowOf;

@Config(manifest=Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class LocationClientTest {
    private LocationClient locationClient;
    private LocationManager locationManager;
    private ShadowLocationManager shadowLocationManager;
    private TestConnectionCallbacks connectionCallbacks;

    @Before
    public void setUp() throws Exception {
        connectionCallbacks = new TestConnectionCallbacks();
        locationClient = new LocationClient(application, connectionCallbacks);
        locationManager = (LocationManager) application.getSystemService(Context.LOCATION_SERVICE);
        shadowLocationManager = shadowOf(locationManager);
        locationClient.connect();
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(locationClient).isNotNull();
    }

    @Test
    public void connect_shouldCallOnConnected() throws Exception {
        assertThat(connectionCallbacks.connected).isTrue();
    }

    @Test
    public void disconnect_shouldCallOnDisconnected() throws Exception {
        locationClient.disconnect();
        assertThat(connectionCallbacks.connected).isFalse();
    }

    @Test(expected = IllegalStateException.class)
    public void getLastLocation_shouldThrowExceptionIfNotConnected() throws Exception {
        locationClient = new LocationClient(application, connectionCallbacks);
        locationClient.getLastLocation();
    }

    @Test
    public void getLastLocation_shouldReturnNullIfNoLocationAvailable() throws Exception {
        assertThat(locationClient.getLastLocation()).isNull();
    }

    @Test
    public void getLastLocation_shouldReturnGpsLocationIfOnlyProvider() throws Exception {
        Location location = new Location(GPS_PROVIDER);
        shadowLocationManager.setLastKnownLocation(GPS_PROVIDER, location);
        assertThat(locationClient.getLastLocation()).isEqualTo(location);
    }

    @Test
    public void getLastLocation_shouldReturnNetworkLocationIfOnlyProvider() throws Exception {
        Location location = new Location(NETWORK_PROVIDER);
        shadowLocationManager.setLastKnownLocation(NETWORK_PROVIDER, location);
        assertThat(locationClient.getLastLocation()).isEqualTo(location);
    }

    @Test
    public void getLastLocation_shouldReturnPassiveLocationIfOnlyProvider() throws Exception {
        Location location = new Location(PASSIVE_PROVIDER);
        shadowLocationManager.setLastKnownLocation(PASSIVE_PROVIDER, location);
        assertThat(locationClient.getLastLocation()).isEqualTo(location);
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

        assertThat(locationClient.getLastLocation()).isEqualTo(passiveLocation);
    }

    @Test(expected = IllegalStateException.class)
    public void requestLocationUpdates_shouldThrowExceptionIfNotConnected() throws Exception {
        locationClient = new LocationClient(application, connectionCallbacks);
        locationClient.requestLocationUpdates(LocationRequest.create(), new TestLocationListener());
    }

    @Test
    public void requestLocationUpdates_shouldRegisterGpsAndNetworkListener() throws Exception {
        LocationListener listener = new TestLocationListener();
        locationClient.requestLocationUpdates(LocationRequest.create(), listener);
        assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).hasSize(2);
    }

    @Test
    public void requestLocationUpdates_shouldNotifyOnLocationChangedGps() throws Exception {
        TestLocationListener listener = new TestLocationListener();
        locationClient.requestLocationUpdates(LocationRequest.create(), listener);
        Location location = new Location(GPS_PROVIDER);
        shadowLocationManager.simulateLocation(location);
        assertThat(listener.location).isEqualTo(location);
    }

    @Test
    public void requestLocationUpdates_shouldNotifyOnLocationChangedNetwork() throws Exception {
        TestLocationListener listener = new TestLocationListener();
        locationClient.requestLocationUpdates(LocationRequest.create(), listener);
        Location location = new Location(NETWORK_PROVIDER);
        shadowLocationManager.simulateLocation(location);
        assertThat(listener.location).isEqualTo(location);
    }

    @Test
    public void requestLocationUpdates_shouldNotNotifyIfDoesNotExceedCriteriaGps()
            throws Exception {
        TestLocationListener listener = new TestLocationListener();
        LocationRequest request = LocationRequest.create();
        request.setFastestInterval(5000);
        request.setSmallestDisplacement(200000);
        locationClient.requestLocationUpdates(request, listener);

        final long time = System.currentTimeMillis();
        Location location1 = getTestLocation(GPS_PROVIDER, 0, 0, time);
        Location location2 = getTestLocation(GPS_PROVIDER, 1, 1, time + 1000);

        shadowLocationManager.simulateLocation(location1);
        shadowLocationManager.simulateLocation(location2);
        assertThat(listener.location).isEqualTo(location1);
    }

    @Test
    public void requestLocationUpdates_shouldNotNotifyIfDoesNotExceedCriteriaNetwork()
            throws Exception {
        TestLocationListener listener = new TestLocationListener();
        LocationRequest request = LocationRequest.create();
        request.setFastestInterval(5000);
        request.setSmallestDisplacement(200000);
        locationClient.requestLocationUpdates(request, listener);

        final long time = System.currentTimeMillis();
        Location location1 = getTestLocation(NETWORK_PROVIDER, 0, 0, time);
        Location location2 = getTestLocation(NETWORK_PROVIDER, 1, 1, time + 1000);

        shadowLocationManager.simulateLocation(location1);
        shadowLocationManager.simulateLocation(location2);
        assertThat(listener.location).isEqualTo(location1);
    }

    @Test
    public void requestLocationUpdates_shouldIgnoreNetworkWhenGpsIsMoreAccurate() throws Exception {
        TestLocationListener listener = new TestLocationListener();
        LocationRequest request = LocationRequest.create();
        request.setFastestInterval(0);
        request.setSmallestDisplacement(0);
        locationClient.requestLocationUpdates(request, listener);

        final long time = System.currentTimeMillis();
        Location gpsLocation = getTestLocation(GPS_PROVIDER, 0, 0, time);
        Location networkLocation = getTestLocation(NETWORK_PROVIDER, 0, 0, time + 1);

        gpsLocation.setAccuracy(10);
        networkLocation.setAccuracy(20);

        shadowLocationManager.simulateLocation(gpsLocation);
        shadowLocationManager.simulateLocation(networkLocation);

        assertThat(listener.location).isEqualTo(gpsLocation);
    }

    @Test
    public void requestLocationUpdates_shouldIgnoreGpsWhenNetworkIsMoreAccurate() throws Exception {
        TestLocationListener listener = new TestLocationListener();
        LocationRequest request = LocationRequest.create();
        request.setFastestInterval(0);
        request.setSmallestDisplacement(0);
        locationClient.requestLocationUpdates(request, listener);

        final long time = System.currentTimeMillis();
        Location networkLocation = getTestLocation(NETWORK_PROVIDER, 0, 0, time);
        Location gpsLocation = getTestLocation(GPS_PROVIDER, 0, 0, time + 1);

        networkLocation.setAccuracy(10);
        gpsLocation.setAccuracy(20);

        shadowLocationManager.simulateLocation(networkLocation);
        shadowLocationManager.simulateLocation(gpsLocation);

        assertThat(listener.location).isEqualTo(networkLocation);
    }

    @Test
    public void removeLocationUpdates_shouldUnregisterAllListeners() throws Exception {
        TestLocationListener listener = new TestLocationListener();
        LocationRequest request = LocationRequest.create();
        locationClient.requestLocationUpdates(request, listener);
        locationClient.removeLocationUpdates(listener);
        assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).isEmpty();
    }

    @Test
    public void disconnect_shouldUnregisterAllListeners() throws Exception {
        TestLocationListener listener = new TestLocationListener();
        LocationRequest request = LocationRequest.create();
        locationClient.requestLocationUpdates(request, listener);
        locationClient.disconnect();
        assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).isEmpty();
    }

    @Test
    public void isConnected_shouldReturnTrueWhenConnected() throws Exception {
        locationClient.connect();
        assertThat(locationClient.isConnected()).isTrue();
    }

    @Test
    public void isConnected_shouldReturnFalseWhenDisconnected() throws Exception {
        locationClient.disconnect();
        assertThat(locationClient.isConnected()).isFalse();
    }

    private static Location getTestLocation(String provider, float lat, float lng, long time) {
        Location location = new Location(provider);
        location.setLatitude(lat);
        location.setLongitude(lng);
        location.setTime(time);
        return location;
    }

    class TestConnectionCallbacks implements LocationClient.ConnectionCallbacks {
        private boolean connected = false;

        @Override
        public void onConnected(Bundle connectionHint) {
            connected = true;
        }

        @Override
        public void onDisconnected() {
            connected = false;
        }
    }

    class TestLocationListener implements LocationListener {
        private Location location;

        @Override
        public void onLocationChanged(Location location) {
            this.location = location;
        }
    }
}
