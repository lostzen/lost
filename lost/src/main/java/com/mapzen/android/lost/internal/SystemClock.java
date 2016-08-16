package com.mapzen.android.lost.internal;

import android.location.Location;
import android.os.Build;

public class SystemClock implements Clock {
  public static final long MS_TO_NS = 1000000;

  @Override public long getCurrentTimeInMillis() {
    return System.currentTimeMillis();
  }

  public static long getTimeInNanos(Location location) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      return location.getElapsedRealtimeNanos();
    }

    return location.getTime() * MS_TO_NS;
  }
}
