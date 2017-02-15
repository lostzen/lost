package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.BaseRobolectricTest;
import com.mapzen.lost.BuildConfig;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowEnvironment;

import android.location.Location;
import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.RuntimeEnvironment.application;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, manifest = Config.NONE)
public class TraceThreadTest extends BaseRobolectricTest {
  private TestMockEngine mockEngine;
  private TestSleepFactory sleepFactory;
  private TraceThread traceThread;

  @Before public void setUp() throws Exception {
    mockEngine = new TestMockEngine();
    sleepFactory = new TestSleepFactory();
    traceThread = new TraceThread(application, getTestGpxTrace(), mockEngine, sleepFactory);
  }

  @Test public void shouldNotBeNull() throws Exception {
    assertThat(traceThread).isNotNull();
  }

  @Test public void shouldReportEachLocation() throws Exception {
    traceThread.run();
    assertThat(mockEngine.locations).hasSize(3);
    assertThat(mockEngine.locations.get(0).getLatitude()).isEqualTo(0.0);
    assertThat(mockEngine.locations.get(0).getLongitude()).isEqualTo(0.1);
    assertThat(mockEngine.locations.get(1).getLatitude()).isEqualTo(1.0);
    assertThat(mockEngine.locations.get(1).getLongitude()).isEqualTo(1.1);
    assertThat(mockEngine.locations.get(2).getLatitude()).isEqualTo(2.0);
    assertThat(mockEngine.locations.get(2).getLongitude()).isEqualTo(2.1);
  }

  @Test public void shouldReportSpeed() throws Exception {
    traceThread.run();
    assertThat(mockEngine.locations.get(0).getSpeed()).isEqualTo(10f);
    assertThat(mockEngine.locations.get(1).getSpeed()).isEqualTo(20f);
    assertThat(mockEngine.locations.get(2).getSpeed()).isEqualTo(30f);
  }

  @Test public void shouldCalculateBearing() throws Exception {
    traceThread.run();
    assertThat(mockEngine.locations.get(0).getBearing()).isEqualTo(0.0f);
    assertThat(mockEngine.locations.get(1).getBearing())
        .isEqualTo(mockEngine.locations.get(0).bearingTo(mockEngine.locations.get(1)));
    assertThat(mockEngine.locations.get(2).getBearing())
        .isEqualTo(mockEngine.locations.get(1).bearingTo(mockEngine.locations.get(2)));
  }

  @Test public void shouldSetHasBearing() throws Exception {
    traceThread.run();
    assertThat(mockEngine.locations.get(0).hasBearing()).isFalse();
    assertThat(mockEngine.locations.get(1).hasBearing()).isTrue();
    assertThat(mockEngine.locations.get(2).hasBearing()).isTrue();
  }

  @Test public void shouldRespectFastestInterval() throws Exception {
    mockEngine.setRequest(new TestLocationRequestUnbundled(100));
    traceThread.run();
    assertThat(sleepFactory.sleepTimeInMillis).isEqualTo(300);
  }

  @Test public void shouldReportSpeedIfAvailable() throws Exception {
    traceThread = new TraceThread(application, getTestGpxTrace(), mockEngine, sleepFactory);
    traceThread.run();
    assertThat(mockEngine.locations.get(0).hasSpeed()).isTrue();
  }

  @Test public void shouldNotReportSpeedIfNotAvailable() throws Exception {
    traceThread = new TraceThread(application, getNoSpeedGpxTrace(), mockEngine, sleepFactory);
    traceThread.run();
    assertThat(mockEngine.locations.get(0).hasSpeed()).isFalse();
  }

  public static File getTestGpxTrace() throws IOException {
    return getGpxFile("lost.gpx");
  }

  public static File getNoSpeedGpxTrace() throws IOException {
    return getGpxFile("lost-no-speed.gpx");
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

  class TestMockEngine extends MockEngine {
    private ArrayList<Location> locations = new ArrayList<>();
    private LocationRequestUnbundled request = new TestLocationRequestUnbundled(0);

    public TestMockEngine() {
      super(application, new FusionEngine.Callback() {
        @Override public void reportLocation(Location location) {
        }

        @Override public void reportProviderDisabled(String provider) {
        }

        @Override public void reportProviderEnabled(String provider) {
        }
      }, new MockEngineTest.TestTraceThreadFactory());
    }

    @Override public void setLocation(Location location) {
      this.locations.add(location);
    }

    @Override protected LocationRequestUnbundled getRequest() {
      return request;
    }

    public void setRequest(LocationRequestUnbundled request) {
      this.request = request;
    }
  }

  class TestLocationRequestUnbundled extends LocationRequestUnbundled {
    private long fastestInterval;

    TestLocationRequestUnbundled(long fastestInterval) {
      this.fastestInterval = fastestInterval;
    }

    @Override public long getFastestInterval() {
      return fastestInterval;
    }
  }

  class TestSleepFactory implements SleepFactory {
    private long sleepTimeInMillis;

    @Override public void sleep(long millis) {
      sleepTimeInMillis += millis;
    }
  }
}
