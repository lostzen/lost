package com.mapzen.android.lost.internal;

import android.location.Location;
import android.os.Build;

public class SystemClock implements Clock {

  @Override public long getSystemElapsedTimeInNanos() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      return android.os.SystemClock.elapsedRealtimeNanos();
    }
    return android.os.SystemClock.elapsedRealtime() * MS_TO_NS;
  }

  @Override public long getElapsedTimeInNanos(Location location) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      return location.getElapsedRealtimeNanos();
    }

    return location.getTime() * MS_TO_NS;
  }
}
