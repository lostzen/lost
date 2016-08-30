package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.Geofence;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableGeofence implements Geofence, Parcelable {
  private final String requestId;

  private double latitude;
  private double longitude;
  private float radius;
  private long durationMillis;

  public ParcelableGeofence(String requestId, double latitude, double longitude, float radius,
      long durationMillis) {
    this.requestId = requestId;
    this.latitude = latitude;
    this.longitude = longitude;
    this.radius = radius;

    if (durationMillis < 0) {
      this.durationMillis = NEVER_EXPIRE;
    } else {
      this.durationMillis = durationMillis;
    }
  }

  @Override public String getRequestId() {
    return requestId;
  }

  public double getLatitude() {
    return latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public float getRadius() {
    return radius;
  }

  public long getDuration() {
    return durationMillis;
  }

  // Parcelable

  protected ParcelableGeofence(Parcel in) {
    requestId = in.readString();
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(requestId);
  }

  @Override public int describeContents() {
    return 0;
  }

  public static final Creator<ParcelableGeofence> CREATOR = new Creator<ParcelableGeofence>() {
    @Override public ParcelableGeofence createFromParcel(Parcel in) {
      return new ParcelableGeofence(in);
    }

    @Override public ParcelableGeofence[] newArray(int size) {
      return new ParcelableGeofence[size];
    }
  };
}
