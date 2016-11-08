package com.mapzen.android.lost.api;

import android.location.Location;
import android.os.Looper;

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

  /**
   * Called when a location provider is disabled. You will only receive updates for the priority
   * level set in the location request used to register for updates in
   * {@link FusedLocationProviderApi#requestLocationUpdates(LostApiClient, LocationRequest,
   * LocationListener)}. Ie. {@link LocationRequest#PRIORITY_HIGH_ACCURACY} will invoke this method
   * for gps and network changes but {@link LocationRequest#PRIORITY_BALANCED_POWER_ACCURACY} will
   * only invoke it for network changes. This method will be removed in the next major release, it
   * is recommended that you use {@link LocationAvailability} instead.
   * @param provider the disabled provider.
   */
  @Deprecated
  void onProviderDisabled(String provider);

  /**
   * Called when a location provider is enabled. You will only receive updates for the priority
   * level set in the location request used to register for updates in
   * {@link FusedLocationProviderApi#requestLocationUpdates(LostApiClient, LocationRequest,
   * LocationListener)}. Ie. {@link LocationRequest#PRIORITY_HIGH_ACCURACY} will invoke this method
   * for gps and network changes but {@link LocationRequest#PRIORITY_BALANCED_POWER_ACCURACY} will
   * only invoke it for network changes. This method will be removed in the next major release, it
   * is recommended that you use {@link LocationAvailability} instead.
   * @param provider the enabled provider.
   */
  @Deprecated
  void onProviderEnabled(String provider);
}
