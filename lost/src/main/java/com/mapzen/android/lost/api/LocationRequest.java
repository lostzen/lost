package com.mapzen.android.lost.api;

import android.os.Parcel;
import android.os.Parcelable;

public final class LocationRequest implements Parcelable {
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

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeLong(this.interval);
    dest.writeLong(this.fastestInterval);
    dest.writeFloat(this.smallestDisplacement);
    dest.writeInt(this.priority);
  }

  protected LocationRequest(Parcel in) {
    this.interval = in.readLong();
    this.fastestInterval = in.readLong();
    this.smallestDisplacement = in.readFloat();
    this.priority = in.readInt();
  }

  public static final Parcelable.Creator<LocationRequest> CREATOR =
      new Parcelable.Creator<LocationRequest>() {
        @Override public LocationRequest createFromParcel(Parcel source) {
          return new LocationRequest(source);
        }

        @Override public LocationRequest[] newArray(int size) {
          return new LocationRequest[size];
        }
      };
}
