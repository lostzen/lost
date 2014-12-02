package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowEnvironment;

import android.location.Location;
import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.application;

@Config(manifest=Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class MockEngineTest {
    private MockEngine mockEngine;
    private TestCallback callback;

    @Before
    public void setUp() throws Exception {
        callback = new TestCallback();
        mockEngine = new MockEngine(application, callback);
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(mockEngine).isNotNull();
    }

    @Test
    public void setLocation_shouldReportLocation() throws Exception {
        Location mockLocation = new Location("mock");
        mockEngine.setLocation(mockLocation);
        assertThat(callback.lastLocation).isEqualTo(mockLocation);
    }

    @Test
    public void setTrace_shouldReportEachLocation() throws Exception {
        mockEngine.setTrace(getTestGpxTrace());
        mockEngine.setRequest(LocationRequest.create().setFastestInterval(0));
        Thread.sleep(1000);
        Robolectric.runUiThreadTasks();
        assertThat(callback.locations).hasSize(3);
        assertThat(callback.locations.get(0).getLatitude()).isEqualTo(0.0);
        assertThat(callback.locations.get(0).getLongitude()).isEqualTo(0.1);
        assertThat(callback.locations.get(1).getLatitude()).isEqualTo(1.0);
        assertThat(callback.locations.get(1).getLongitude()).isEqualTo(1.1);
        assertThat(callback.locations.get(2).getLatitude()).isEqualTo(2.0);
        assertThat(callback.locations.get(2).getLongitude()).isEqualTo(2.1);
    }

    @Test
    public void setTrace_shouldReportSpeed() throws Exception {
        mockEngine.setTrace(getTestGpxTrace());
        mockEngine.setRequest(LocationRequest.create().setFastestInterval(0));
        Thread.sleep(1000);
        Robolectric.runUiThreadTasks();
        assertThat(callback.locations.get(0).getSpeed()).isEqualTo(10f);
        assertThat(callback.locations.get(1).getSpeed()).isEqualTo(20f);
        assertThat(callback.locations.get(2).getSpeed()).isEqualTo(30f);
    }

    @Test
    public void setTrace_shouldRespectFastestInterval() throws Exception {
        mockEngine.setTrace(getTestGpxTrace());
        mockEngine.setRequest(LocationRequest.create().setFastestInterval(1000));
        Thread.sleep(1000);
        Robolectric.runUiThreadTasks();
        assertThat(callback.locations).hasSize(1);
        Thread.sleep(1000);
        Robolectric.runUiThreadTasks();
        assertThat(callback.locations).hasSize(2);
        Thread.sleep(1000);
        Robolectric.runUiThreadTasks();
        assertThat(callback.locations).hasSize(3);
    }

    @Test
    public void disable_shouldCancelTraceReplay() throws Exception {
        mockEngine.setTrace(getTestGpxTrace());
        mockEngine.setRequest(LocationRequest.create().setFastestInterval(1000));
        Thread.sleep(1000);
        Robolectric.runUiThreadTasks();
        assertThat(callback.locations).hasSize(1);
        mockEngine.disable();
        Thread.sleep(1000);
        Robolectric.runUiThreadTasks();
        assertThat(callback.locations).hasSize(1);
    }

    public static File getTestGpxTrace() throws IOException {
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

    class TestCallback implements LocationEngine.Callback {
        private Location lastLocation;
        private ArrayList<Location> locations = new ArrayList<Location>();

        @Override
        public void reportLocation(Location location) {
            lastLocation = location;
            locations.add(location);
        }
    }
}
