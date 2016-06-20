package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.lost.BuildConfig;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowEnvironment;
import org.robolectric.shadows.ShadowLocationManager;
import org.robolectric.shadows.ShadowLooper;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static com.mapzen.android.lost.api.LocationRequest.PRIORITY_HIGH_ACCURACY;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.RuntimeEnvironment.application;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, manifest = Config.NONE)
public class FusedLocationProviderApiImplTest {
    private FusedLocationProviderApiImpl api;
    private LocationManager locationManager;
    private ShadowLocationManager shadowLocationManager;

    @Before
    public void setUp() throws Exception {
        api = new FusedLocationProviderApiImpl(application);
        locationManager = (LocationManager) application.getSystemService(Context.LOCATION_SERVICE);
        shadowLocationManager = shadowOf(locationManager);
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(api).isNotNull();
    }

    @Test
    public void getLastLocation_shouldReturnMostRecentLocation() throws Exception {
        Location location = new Location(GPS_PROVIDER);
        shadowLocationManager.setLastKnownLocation(GPS_PROVIDER, location);
        assertThat(api.getLastLocation()).isNotNull();
    }

    @Test
    public void requestLocationUpdates_shouldRegisterGpsAndNetworkListener() throws Exception {
        LocationListener listener = new TestLocationListener();
        api.requestLocationUpdates(LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY),
                listener);
        assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).hasSize(2);
    }

    @Test
    public void requestLocationUpdates_shouldNotifyOnLocationChangedGps() throws Exception {
        TestLocationListener listener = new TestLocationListener();
        api.requestLocationUpdates(LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY),
                listener);
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
    public void setMockMode_shouldNotRegisterDuplicateListeners() throws Exception {
        TestLocationListener listener = new TestLocationListener();
        LocationRequest request = LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY);
        api.setMockMode(true);
        api.requestLocationUpdates(request, listener);
        api.setMockMode(false);
        api.requestLocationUpdates(request, listener);
        assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).hasSize(2);
    }

    @Test
    public void setMockMode_shouldToggleEngines() {
        TestLocationListener listener = new TestLocationListener();
        LocationRequest request = LocationRequest.create();
        api.requestLocationUpdates(request, listener);
        api.setMockMode(true);
        api.requestLocationUpdates(request, listener);
        assertThat(api.getListeners()).hasSize(2);
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

    @Test
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

    @Test
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
}
