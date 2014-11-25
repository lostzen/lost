package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowEnvironment;
import org.robolectric.shadows.ShadowLocationManager;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static android.location.LocationManager.PASSIVE_PROVIDER;
import static com.mapzen.android.lost.api.LocationClient.RECENT_UPDATE_THRESHOLD_IN_MILLIS;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.application;
import static org.robolectric.Robolectric.shadowOf;

@Config(manifest=Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class FusedLocationProviderApiImplTest {
    private FusedLocationProviderApiImpl api;
    private LocationManager locationManager;
    private ShadowLocationManager shadowLocationManager;

    @Before
    public void setUp() throws Exception {
        api = new FusedLocationProviderApiImpl(Robolectric.application);
        locationManager = (LocationManager) application.getSystemService(Context.LOCATION_SERVICE);
        shadowLocationManager = shadowOf(locationManager);
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(api).isNotNull();
    }

    @Test
    public void getLastLocation_shouldReturnNullIfNoLocationAvailable() throws Exception {
        assertThat(api.getLastLocation()).isNull();
    }

    @Test
    public void getLastLocation_shouldReturnGpsLocationIfOnlyProvider() throws Exception {
        Location location = new Location(GPS_PROVIDER);
        shadowLocationManager.setLastKnownLocation(GPS_PROVIDER, location);
        assertThat(api.getLastLocation()).isEqualTo(location);
    }

    @Test
    public void getLastLocation_shouldReturnNetworkLocationIfOnlyProvider() throws Exception {
        Location location = new Location(NETWORK_PROVIDER);
        shadowLocationManager.setLastKnownLocation(NETWORK_PROVIDER, location);
        assertThat(api.getLastLocation()).isEqualTo(location);
    }

    @Test
    public void getLastLocation_shouldReturnPassiveLocationIfOnlyProvider() throws Exception {
        Location location = new Location(PASSIVE_PROVIDER);
        shadowLocationManager.setLastKnownLocation(PASSIVE_PROVIDER, location);
        assertThat(api.getLastLocation()).isEqualTo(location);
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

        assertThat(api.getLastLocation()).isEqualTo(passiveLocation);
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

        assertThat(api.getLastLocation()).isEqualTo(gpsLocation);
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

        assertThat(api.getLastLocation()).isEqualTo(gpsLocation);
    }

    @Test
    public void requestLocationUpdates_shouldRegisterGpsAndNetworkListener() throws Exception {
        LocationListener listener = new TestLocationListener();
        api.requestLocationUpdates(LocationRequest.create(), listener);
        assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).hasSize(2);
    }

    @Test
    public void requestLocationUpdates_shouldNotifyOnLocationChangedGps() throws Exception {
        TestLocationListener listener = new TestLocationListener();
        api.requestLocationUpdates(LocationRequest.create(), listener);
        Location location = new Location(GPS_PROVIDER);
        shadowLocationManager.simulateLocation(location);
        assertThat(listener.getMostRecentLocation()).isEqualTo(location);
    }

    @Test
    public void requestLocationUpdates_shouldNotifyOnLocationChangedNetwork() throws Exception {
        TestLocationListener listener = new TestLocationListener();
        api.requestLocationUpdates(LocationRequest.create(), listener);
        Location location = new Location(NETWORK_PROVIDER);
        shadowLocationManager.simulateLocation(location);
        assertThat(listener.getMostRecentLocation()).isEqualTo(location);
    }

    @Test
    public void requestLocationUpdates_shouldNotNotifyIfLessThanFastestIntervalGps()
            throws Exception {
        TestLocationListener listener = new TestLocationListener();
        LocationRequest request = LocationRequest.create();
        request.setFastestInterval(5000);
        api.requestLocationUpdates(request, listener);

        final long time = System.currentTimeMillis();
        Location location1 = getTestLocation(GPS_PROVIDER, 0, 0, time);
        Location location2 = getTestLocation(GPS_PROVIDER, 1, 1, time + 1000);

        shadowLocationManager.simulateLocation(location1);
        shadowLocationManager.simulateLocation(location2);
        assertThat(listener.getMostRecentLocation()).isEqualTo(location1);
    }

    @Test
    public void requestLocationUpdates_shouldNotNotifyIfLessThanFastestIntervalNetwork()
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

    @Test
    public void requestLocationUpdates_shouldNotNotifyIfLessThanSmallestDisplacementGps()
            throws Exception {
        TestLocationListener listener = new TestLocationListener();
        LocationRequest request = LocationRequest.create();
        request.setSmallestDisplacement(200000);
        api.requestLocationUpdates(request, listener);

        final long time = System.currentTimeMillis();
        Location location1 = getTestLocation(GPS_PROVIDER, 0, 0, time);
        Location location2 = getTestLocation(GPS_PROVIDER, 1, 1, time + 1000);

        shadowLocationManager.simulateLocation(location1);
        shadowLocationManager.simulateLocation(location2);
        assertThat(listener.getMostRecentLocation()).isEqualTo(location1);
    }

    @Test
    public void requestLocationUpdates_shouldNotNotifyIfLessThanSmallestDisplacementNetwork()
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

    @Test
    public void requestLocationUpdates_shouldIgnoreNetworkWhenGpsIsMoreAccurate() throws Exception {
        TestLocationListener listener = new TestLocationListener();
        LocationRequest request = LocationRequest.create();
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

    @Test
    public void requestLocationUpdates_shouldIgnoreGpsWhenNetworkIsMoreAccurate() throws Exception {
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

    @Test
    public void removeLocationUpdates_shouldUnregisterAllListeners() throws Exception {
        TestLocationListener listener = new TestLocationListener();
        LocationRequest request = LocationRequest.create();
        api.requestLocationUpdates(request, listener);
        api.removeLocationUpdates(listener);
        assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).isEmpty();
    }

    @Test
    public void setMockMode_shouldUnregisterAllListenersWhenTrue() throws Exception {
        TestLocationListener listener = new TestLocationListener();
        LocationRequest request = LocationRequest.create();
        api.requestLocationUpdates(request, listener);
        api.setMockMode(true);
        assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).isEmpty();
    }

    @Test
    public void setMockMode_shouldRegisterListenersWhenFalse() throws Exception {
        TestLocationListener listener = new TestLocationListener();
        LocationRequest request = LocationRequest.create();
        api.requestLocationUpdates(request, listener);
        api.setMockMode(true);
        api.setMockMode(false);
        assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).isNotEmpty();
    }

    @Test
    public void setMockMode_shouldNotRegisterDuplicateListeners() throws Exception {
        TestLocationListener listener = new TestLocationListener();
        LocationRequest request = LocationRequest.create();
        api.requestLocationUpdates(request, listener);
        api.setMockMode(false);
        assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).hasSize(2);
    }

    @Test
    public void requestLocationUpdates_shouldNotRegisterListenersWithMockModeOn() throws Exception {
        api.setMockMode(true);
        TestLocationListener listener = new TestLocationListener();
        LocationRequest request = LocationRequest.create();
        api.requestLocationUpdates(request, listener);
        assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).isEmpty();
    }

    @Test
    public void setMockLocation_shouldReturnMockLastLocation() throws Exception {
        Location mockLocation = new Location("mock");
        api.setMockMode(true);
        api.setMockLocation(mockLocation);
        assertThat(api.getLastLocation()).isEqualTo(mockLocation);
    }

    @Test
    public void setMockLocation_shouldInvokeListenerOnce() throws Exception {
        Location mockLocation = new Location("mock");
        api.setMockMode(true);
        TestLocationListener listener = new TestLocationListener();
        LocationRequest request = LocationRequest.create();
        api.requestLocationUpdates(request, listener);
        api.setMockLocation(mockLocation);
        assertThat(listener.getAllLocations()).hasSize(1);
        assertThat(listener.getMostRecentLocation()).isEqualTo(mockLocation);
    }

    @Test
    public void setMockTrace_shouldInvokeListenerForEachLocation() throws Exception {
        File file = getTestGpxTrace();
        api.setMockMode(true);
        TestLocationListener listener = new TestLocationListener();
        LocationRequest request = LocationRequest.create();
        request.setFastestInterval(0);
        api.requestLocationUpdates(request, listener);
        api.setMockTracefile(file);
        Thread.sleep(1000);
        Robolectric.runUiThreadTasks();
        assertThat(listener.getAllLocations()).hasSize(3);
        assertThat(listener.getAllLocations().get(0).getLatitude()).isEqualTo(0.0);
        assertThat(listener.getAllLocations().get(0).getLongitude()).isEqualTo(0.1);
        assertThat(listener.getAllLocations().get(1).getLatitude()).isEqualTo(1.0);
        assertThat(listener.getAllLocations().get(1).getLongitude()).isEqualTo(1.1);
        assertThat(listener.getAllLocations().get(2).getLatitude()).isEqualTo(2.0);
        assertThat(listener.getAllLocations().get(2).getLongitude()).isEqualTo(2.1);
    }

    @Test
    public void setMockTrace_shouldBroadcastSpeedWithLocation() throws Exception {
        File file = getTestGpxTrace();
        api.setMockMode(true);
        TestLocationListener listener = new TestLocationListener();
        LocationRequest request = LocationRequest.create();
        request.setFastestInterval(0);
        api.requestLocationUpdates(request, listener);
        api.setMockTracefile(file);
        Thread.sleep(1000);
        Robolectric.runUiThreadTasks();
        assertThat(listener.getAllLocations().get(0).getSpeed()).isEqualTo(10f);
        assertThat(listener.getAllLocations().get(1).getSpeed()).isEqualTo(20f);
        assertThat(listener.getAllLocations().get(2).getSpeed()).isEqualTo(30f);
    }

    @Test
    public void setMockTrace_shouldRespectFastestInterval() throws Exception {
        File file = getTestGpxTrace();
        api.setMockMode(true);
        TestLocationListener listener = new TestLocationListener();
        LocationRequest request = LocationRequest.create();
        request.setInterval(1000);
        api.requestLocationUpdates(request, listener);
        api.setMockTracefile(file);
        Thread.sleep(1000);
        Robolectric.runUiThreadTasks();
        assertThat(listener.getAllLocations()).hasSize(1);
        Thread.sleep(1000);
        Robolectric.runUiThreadTasks();
        assertThat(listener.getAllLocations()).hasSize(2);
        Thread.sleep(1000);
        Robolectric.runUiThreadTasks();
        assertThat(listener.getAllLocations()).hasSize(3);
    }

    @Test
    public void setMockMode_shouldGenerateNewListeners() throws Exception {
        api.requestLocationUpdates(LocationRequest.create(), new TestLocationListener());

        android.location.LocationListener gpsListener =
                shadowLocationManager.getRequestLocationUpdateListeners().get(0);
        android.location.LocationListener networkListener =
                shadowLocationManager.getRequestLocationUpdateListeners().get(1);

        api.setMockMode(true);
        api.setMockMode(false);

        assertThat(shadowLocationManager.getRequestLocationUpdateListeners().get(0))
                .isNotSameAs(gpsListener);
        assertThat(shadowLocationManager.getRequestLocationUpdateListeners().get(1))
                .isNotSameAs(networkListener);
    }

    private void initTestClock(long time) {
        TestClock testClock = new TestClock();
        testClock.setCurrentTimeInMillis(time);
        api.clock = testClock;
    }

    private static Location getTestLocation(String provider, float lat, float lng, long time) {
        Location location = new Location(provider);
        location.setLatitude(lat);
        location.setLongitude(lng);
        location.setTime(time);
        return location;
    }

    private File getTestGpxTrace() throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get("src/test/resources/lost.gpx"));
        String contents = new String(encoded, "UTF-8");

        ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);
        File directory = Environment.getExternalStorageDirectory();
        File file = new File(directory, "lost.gpx");
        FileWriter fileWriter = new FileWriter(file, false);
        fileWriter.write(contents);
        fileWriter.close();
        return file;
    }

    class TestLocationListener implements LocationListener {
        private ArrayList<Location> locations = new ArrayList<Location>();

        @Override
        public void onLocationChanged(Location location) {
            locations.add(location);
        }

        public List<Location> getAllLocations() {
            return locations;
        }

        public Location getMostRecentLocation() {
            return locations.get(locations.size() - 1);
        }
    }
}
