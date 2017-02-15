package com.mapzen.android.lost.internal;

import com.mapzen.lost.BuildConfig;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.location.Location;

import static com.mapzen.android.lost.internal.SystemClock.MS_TO_NS;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, manifest = Config.NONE)
public class SystemClockTest {
  @Test @Config(sdk = 17) public void getTimeInNanos_shouldReturnElapsedRealtimeNanosForSdk17AndUp()
      throws Exception {
    final long nanos = 1000000;
    final Location location = new Location("mock");
    location.setElapsedRealtimeNanos(nanos);
    assertThat(SystemClock.getTimeInNanos(location)).isEqualTo(nanos);
  }

  @Test @Config(sdk = 16) public void getTimeInNanos_shouldUseUtcTimeInMillisForSdk16AndLower()
      throws Exception {
    final long millis = 1000;
    final Location location = new Location("mock");
    location.setTime(millis);
    assertThat(SystemClock.getTimeInNanos(location)).isEqualTo(millis * MS_TO_NS);
  }
}
