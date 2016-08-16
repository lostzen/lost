package com.mapzen.android.lost.internal;

public class TestClock implements Clock {
  private long currentTimeInMillis;

  @Override public long getCurrentTimeInMillis() {
    return currentTimeInMillis;
  }

  public void setCurrentTimeInMillis(long currentTimeInMillis) {
    this.currentTimeInMillis = currentTimeInMillis;
  }
}
