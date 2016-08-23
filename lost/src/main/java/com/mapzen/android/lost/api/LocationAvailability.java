package com.mapzen.android.lost.api;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Status on the availability of location data.
 *
 * Delivered from LocationCallback registered via
 * {@link FusedLocationProviderApi#requestLocationUpdates(
 * LocationRequest,LocationCallback,android.os.Looper) or from a PendingIntent registered via
 * {@link FusedLocationProviderApi##requestLocationUpdates(LocationRequest,
 * android.app.PendingIntent). It is also available on demand via
 * {@link FusedLocationProviderApi#getLocationAvailability()}.
 */
public class LocationAvailability implements Parcelable {

  public static final String EXTRA_LOCATION_AVAILABILITY =
      "com.mapzen.android.lost.EXTRA_LOCATION_AVAILABILITY";

  boolean locationAvailable = false;

  public LocationAvailability(boolean available) {
    locationAvailable = available;
  }

  protected LocationAvailability(Parcel in) {
    locationAvailable = in.readByte() != 0;
  }

  public static final Creator<LocationAvailability> CREATOR = new Creator<LocationAvailability>() {
    @Override public LocationAvailability createFromParcel(Parcel in) {
      return new LocationAvailability(in);
    }

    @Override public LocationAvailability[] newArray(int size) {
      return new LocationAvailability[size];
    }
  };

  /**
   * Extracts the LocationAvailability from an Intent.
   * @param intent
   * @return
   */
  public static LocationAvailability extractLocationAvailability(Intent intent) {
    return hasLocationAvailability(intent) ? (LocationAvailability) intent.getExtras()
        .getParcelable(EXTRA_LOCATION_AVAILABILITY) : null;
  }

  /**
   * Returns true if an Intent contains a LocationAvailability.
   * @param intent
   * @return
   */
  public static boolean hasLocationAvailability(Intent intent) {
    return intent.hasExtra(EXTRA_LOCATION_AVAILABILITY);
  }

  /**
   * Returns true if the device location is known and reasonably up to date within the hints
   * requested by the active LocationRequests.
   * @return
   */
  public boolean isLocationAvailable() {
    return locationAvailable;
  }

  @Override public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    LocationAvailability that = (LocationAvailability) o;

    return locationAvailable == that.locationAvailable;
  }

  @Override public int hashCode() {
    return (locationAvailable ? 1 : 0);
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeByte((byte) (locationAvailable ? 1 : 0));
  }
}
