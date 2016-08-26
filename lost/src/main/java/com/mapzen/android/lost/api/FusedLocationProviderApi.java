package com.mapzen.android.lost.api;

import android.app.PendingIntent;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.RequiresPermission;

import java.io.File;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public interface FusedLocationProviderApi {

  /**
   * @deprecated Use {@link LocationResult#hasResult(android.content.Intent)} and
   * {@link LocationResult#extractResult(android.content.Intent)}.
   */
  @Deprecated String KEY_LOCATION_CHANGED = "com.mapzen.android.lost.LOCATION";

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  Location getLastLocation(LostApiClient apiClient);

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  LocationAvailability getLocationAvailability(LostApiClient apiClient);

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  void requestLocationUpdates(LostApiClient apiClient, LocationRequest request,
      LocationListener listener);

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  void requestLocationUpdates(LostApiClient apiClient, LocationRequest request,
      LocationListener listener, Looper looper);

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  void requestLocationUpdates(LostApiClient apiClient, LocationRequest request,
      LocationCallback callback, Looper looper);

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  void requestLocationUpdates(LostApiClient apiClient, LocationRequest request,
      PendingIntent callbackIntent);

  void removeLocationUpdates(LostApiClient apiClient, LocationListener listener);

  void removeLocationUpdates(LostApiClient apiClient, PendingIntent callbackIntent);

  void removeLocationUpdates(LostApiClient apiClient, LocationCallback callback);

  void setMockMode(LostApiClient apiClient, boolean isMockMode);

  void setMockLocation(LostApiClient apiClient, Location mockLocation);

  void setMockTrace(LostApiClient apiClient, final File file);

  /**
   * @deprecated Use {@link SettingsApi#checkLocationSettings(LostApiClient,
   * LocationSettingsRequest)}.
   */
  @Deprecated boolean isProviderEnabled(LostApiClient apiClient, String provider);
}
