package com.mapzen.android.lost.internal;

import android.location.Location;

public class TestClock implements Clock {
  long currentTimeInNanos;
  long elapsedTimeInNanos;

  @Override public long getSystemElapsedTimeInNanos() {
    return currentTimeInNanos;
  }

  @Override public long getElapsedTimeInNanos(Location location) {
    return elapsedTimeInNanos;
  }
}
