package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.Geofence;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableGeofence implements Geofence, Parcelable {
  private final String requestId;

  public ParcelableGeofence(String requestId) {
    this.requestId = requestId;
  }

  @Override public String getRequestId() {
    return requestId;
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
