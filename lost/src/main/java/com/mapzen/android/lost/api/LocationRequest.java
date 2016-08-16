package com.mapzen.android.lost.api;

public final class LocationRequest {
  public static final int PRIORITY_HIGH_ACCURACY = 0x00000064;
  public static final int PRIORITY_BALANCED_POWER_ACCURACY = 0x00000066;
  public static final int PRIORITY_LOW_POWER = 0x00000068;
  public static final int PRIORITY_NO_POWER = 0x00000069;

  static final long DEFAULT_INTERVAL_IN_MS = 3600000;
  static final long DEFAULT_FASTEST_INTERVAL_IN_MS = 600000;
  static final float DEFAULT_SMALLEST_DISPLACEMENT_IN_METERS = 0.0f;

  private long interval = DEFAULT_INTERVAL_IN_MS;
  private long fastestInterval = DEFAULT_FASTEST_INTERVAL_IN_MS;
  private float smallestDisplacement = DEFAULT_SMALLEST_DISPLACEMENT_IN_METERS;
  private int priority = PRIORITY_BALANCED_POWER_ACCURACY;

  private LocationRequest() {
  }

  public static LocationRequest create() {
    return new LocationRequest();
  }

  public long getInterval() {
    return interval;
  }

  public LocationRequest setInterval(long millis) {
    interval = millis;

    if (interval < fastestInterval) {
      fastestInterval = interval;
    }

    return this;
  }

  public long getFastestInterval() {
    return fastestInterval;
  }

  public LocationRequest setFastestInterval(long millis) {
    fastestInterval = millis;
    return this;
  }

  public float getSmallestDisplacement() {
    return smallestDisplacement;
  }

  public LocationRequest setSmallestDisplacement(float meters) {
    smallestDisplacement = meters;
    return this;
  }

  public int getPriority() {
    return priority;
  }

  public LocationRequest setPriority(int priority) {
    if (priority != PRIORITY_HIGH_ACCURACY
        && priority != PRIORITY_BALANCED_POWER_ACCURACY
        && priority != PRIORITY_LOW_POWER
        && priority != PRIORITY_NO_POWER) {
      throw new IllegalArgumentException("Invalid priority: " + priority);
    }

    this.priority = priority;
    return this;
  }
}
