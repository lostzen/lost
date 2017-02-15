package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.BaseRobolectricTest;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.lost.BuildConfig;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowEnvironment;

import android.content.Context;
import android.location.Location;
import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.RuntimeEnvironment.application;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, manifest = Config.NONE)
public class MockEngineTest extends BaseRobolectricTest {
  private MockEngine mockEngine;
  private TestCallback callback;
  private TestTraceThreadFactory traceThreadFactory;

  @Before public void setUp() throws Exception {
    callback = new TestCallback();
    traceThreadFactory = new TestTraceThreadFactory();
    mockEngine = new MockEngine(application, callback, traceThreadFactory);
  }

  @Test public void shouldNotBeNull() throws Exception {
    assertThat(mockEngine).isNotNull();
  }

  @Test public void setLocation_shouldReportLocation() throws Exception {
    Location mockLocation = new Location("mock");
    mockEngine.setLocation(mockLocation);
    assertThat(callback.lastLocation).isEqualTo(mockLocation);
  }

  @Test public void disable_shouldCancelTraceReplay() throws Exception {
    mockEngine.setTrace(getTestGpxTrace());
    mockEngine.setRequest(LocationRequest.create().setFastestInterval(100));
    mockEngine.disable();
    assertThat(traceThreadFactory.traceThread.isCanceled()).isTrue();
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

    @Override public void reportLocation(Location location) {
      lastLocation = location;
    }

    @Override public void reportProviderDisabled(String provider) {
    }

    @Override public void reportProviderEnabled(String provider) {
    }
  }

  static class TestTraceThreadFactory implements TraceThreadFactory {
    private TestTraceThread traceThread;

    @Override public TraceThread createTraceThread(Context context, File traceFile,
        MockEngine engine, SleepFactory sleepFactory) {
      traceThread = new TestTraceThread(context, traceFile, engine, sleepFactory);
      return traceThread;
    }
  }

  static class TestTraceThread extends TraceThread {
    TestTraceThread(Context context, File traceFile, MockEngine engine, SleepFactory sleepFactory) {
      super(context, traceFile, engine, sleepFactory);
    }
  }
}
