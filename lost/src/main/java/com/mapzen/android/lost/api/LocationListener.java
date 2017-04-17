package com.mapzen.android.lost.api;

import android.location.Location;

/**
 * Used for receiving notifications from the FusedLocationProviderApi when the location has changed.
 * The methods are called if the LocationListener has been registered with the location client
 * using the {@link FusedLocationProviderApi#requestLocationUpdates(LostApiClient, LocationRequest,
 * LocationListener)} or {@link FusedLocationProviderApi#requestLocationUpdates(LostApiClient,
 * LocationRequest, LocationListener, android.os.Looper)} methods.
 */
public interface LocationListener {
  /**
   * Called when the location has changed.
   * @param location the newest location.
   */
  void onLocationChanged(Location location);
}
