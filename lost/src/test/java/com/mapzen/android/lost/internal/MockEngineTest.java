package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.lost.BuildConfig;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowEnvironment;
import org.robolectric.shadows.ShadowLooper;

import android.location.Location;
import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.RuntimeEnvironment.application;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, manifest = Config.NONE)
public class MockEngineTest {
  private MockEngine mockEngine;
  private TestCallback callback;

  @Before public void setUp() throws Exception {
    callback = new TestCallback();
    mockEngine = new MockEngine(application, callback);
  }

  @Test public void shouldNotBeNull() throws Exception {
    assertThat(mockEngine).isNotNull();
  }

  @Test public void setLocation_shouldReportLocation() throws Exception {
    Location mockLocation = new Location("mock");
    mockEngine.setLocation(mockLocation);
    assertThat(callback.lastLocation).isEqualTo(mockLocation);
  }

  @Ignore("Intermittently failing. Find a better way to test without Thread.sleep(100)")
  @Test public void setTrace_shouldReportEachLocation() throws Exception {
    mockEngine.setTrace(getTestGpxTrace());
    mockEngine.setRequest(LocationRequest.create().setFastestInterval(0));
    Thread.sleep(100);
    ShadowLooper.runUiThreadTasks();
    assertThat(callback.locations).hasSize(3);
    assertThat(callback.locations.get(0).getLatitude()).isEqualTo(0.0);
    assertThat(callback.locations.get(0).getLongitude()).isEqualTo(0.1);
    assertThat(callback.locations.get(1).getLatitude()).isEqualTo(1.0);
    assertThat(callback.locations.get(1).getLongitude()).isEqualTo(1.1);
    assertThat(callback.locations.get(2).getLatitude()).isEqualTo(2.0);
    assertThat(callback.locations.get(2).getLongitude()).isEqualTo(2.1);
  }

  @Ignore("Intermittently failing. Find a better way to test without Thread.sleep(100)")
  @Test public void setTrace_shouldReportSpeed() throws Exception {
    mockEngine.setTrace(getTestGpxTrace());
    mockEngine.setRequest(LocationRequest.create().setFastestInterval(0));
    Thread.sleep(100);
    ShadowLooper.runUiThreadTasks();
    assertThat(callback.locations.get(0).getSpeed()).isEqualTo(10f);
    assertThat(callback.locations.get(1).getSpeed()).isEqualTo(20f);
    assertThat(callback.locations.get(2).getSpeed()).isEqualTo(30f);
  }

  @Test public void setTrace_shouldCalculateBearing() throws Exception {
    mockEngine.setTrace(getTestGpxTrace());
    mockEngine.setRequest(LocationRequest.create().setFastestInterval(0));
    Thread.sleep(100);
    ShadowLooper.runUiThreadTasks();
    assertThat(callback.locations.get(0).getBearing()).isEqualTo(0.0f);
    assertThat(callback.locations.get(1).getBearing()).isEqualTo(
        callback.locations.get(0).bearingTo(callback.locations.get(1)));
    assertThat(callback.locations.get(2).getBearing()).isEqualTo(
        callback.locations.get(1).bearingTo(callback.locations.get(2)));
  }

  @Test public void setTrace_shouldSetHasBearing() throws Exception {
    mockEngine.setTrace(getTestGpxTrace());
    mockEngine.setRequest(LocationRequest.create().setFastestInterval(0));
    Thread.sleep(100);
    ShadowLooper.runUiThreadTasks();
    assertThat(callback.locations.get(0).hasBearing()).isFalse();
    assertThat(callback.locations.get(1).hasBearing()).isTrue();
    assertThat(callback.locations.get(2).hasBearing()).isTrue();
  }

  @Test public void setTrace_shouldRespectFastestInterval() throws Exception {
    mockEngine.setTrace(getTestGpxTrace());
    mockEngine.setRequest(LocationRequest.create().setFastestInterval(100));
    Thread.sleep(100);
    ShadowLooper.runUiThreadTasks();
    assertThat(callback.locations).hasSize(1);
    Thread.sleep(100);
    ShadowLooper.runUiThreadTasks();
    assertThat(callback.locations).hasSize(2);
    Thread.sleep(100);
    ShadowLooper.runUiThreadTasks();
    assertThat(callback.locations).hasSize(3);
  }

  @Test @Ignore("Intermittently failing. Find a better way to test without Thread.sleep(100)")
  public void setTrace_shouldNotRequireSpeed() throws Exception {
    mockEngine.setTrace(getTestGpxTrace());
    mockEngine.setRequest(LocationRequest.create().setFastestInterval(0));
    Thread.sleep(100);
    ShadowLooper.runUiThreadTasks();
    assertThat(callback.locations.get(0).hasSpeed()).isTrue();
    mockEngine.disable();
    callback.reset();
    Thread.sleep(100);
    mockEngine.setTrace(getNoSpeedGpxTrace());
    mockEngine.setRequest(LocationRequest.create().setFastestInterval(0));
    Thread.sleep(100);
    ShadowLooper.runUiThreadTasks();
    assertThat(callback.locations.get(0).hasSpeed()).isFalse();
  }

  @Test @Ignore("Intermittently failing. Find a better way to test without Thread.sleep(100)")
  public void disable_shouldCancelTraceReplay() throws Exception {
    mockEngine.setTrace(getTestGpxTrace());
    mockEngine.setRequest(LocationRequest.create().setFastestInterval(100));
    Thread.sleep(100);
    ShadowLooper.runUiThreadTasks();
    assertThat(callback.locations).hasSize(1);
    mockEngine.disable();
    Thread.sleep(100);
    ShadowLooper.runUiThreadTasks();
    assertThat(callback.locations).hasSize(1);
  }

  public static File getGpxFile(String filename) throws IOException {
    String contents = Files.toString(new File("src/test/resources/" + filename), Charsets.UTF_8);
    ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);
    File directory = Environment.getExternalStorageDirectory();
    File file = new File(directory, filename);
    FileWriter fileWriter = new FileWriter(file, false);
    fileWriter.write(contents);
    fileWriter.close();
    return file;
  }

  public static File getTestGpxTrace() throws IOException {
    return getGpxFile("lost.gpx");
  }

  public static File getNoSpeedGpxTrace() throws IOException {
    return getGpxFile("lost-no-speed.gpx");
  }

  class TestCallback implements LocationEngine.Callback {
    private Location lastLocation;
    private ArrayList<Location> locations = new ArrayList<>();

    @Override public void reportLocation(Location location) {
      lastLocation = location;
      locations.add(location);
    }

    @Override public void reportProviderDisabled(String provider) {
    }

    @Override public void reportProviderEnabled(String provider) {
    }

    public void reset() {
      lastLocation = null;
      locations.clear();
    }
  }
}
