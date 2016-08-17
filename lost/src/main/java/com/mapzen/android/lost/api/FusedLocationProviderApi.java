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
  Location getLastLocation();

  void removeLocationUpdates(LocationListener listener);

  void removeLocationUpdates(PendingIntent callbackIntent);

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  void requestLocationUpdates(LocationRequest request, LocationListener listener, Looper looper);

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  void requestLocationUpdates(LocationRequest request, LocationListener listener);

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  void requestLocationUpdates(LocationRequest request, PendingIntent callbackIntent);

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  void setMockLocation(Location mockLocation);

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  void setMockMode(boolean isMockMode);

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  void setMockTrace(final File file);

  /**
   * @deprecated Use {@link SettingsApi#checkLocationSettings(LostApiClient,
   * LocationSettingsRequest)}.
   */
  @Deprecated boolean isProviderEnabled(String provider);
}
