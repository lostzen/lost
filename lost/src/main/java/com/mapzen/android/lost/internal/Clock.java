package com.mapzen.android.lost.internal;

import android.location.Location;

public interface Clock {
  long MS_TO_NS = 1000000;

  long getSystemElapsedTimeInNanos();
  long getElapsedTimeInNanos(Location location);
}
