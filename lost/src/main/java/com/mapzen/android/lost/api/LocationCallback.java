package com.mapzen.android.lost.api;

/**
 * Used for receiving notifications from the FusedLocationProviderApi when the device location has
 * changed or can no longer be determined. The methods are called if the LocationCallback has been
 * registered with the location client using the
 * {@link FusedLocationProviderApi#requestLocationUpdates(LocationRequest, LocationCallback,
 * android.os.Looper) method.
 */
public interface LocationCallback {
  /**
   * Called when there is a change in the availability of location data.
   * @param locationAvailability
   */
  void onLocationAvailability(LocationAvailability locationAvailability);

  /**
   * Called when device location information is available.
   * @param result
   */
  void onLocationResult(LocationResult result);

}
