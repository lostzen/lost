package com.mapzen.android.lost.api;

import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.RequiresPermission;

import java.io.File;

public interface FusedLocationProviderApi {

  /**
   * @deprecated Use {@link LocationResult#hasResult(Intent)} and
   * {@link LocationResult#extractResult(Intent)}.
   */
  @Deprecated String KEY_LOCATION_CHANGED = "com.mapzen.android.lost.LOCATION";

  @RequiresPermission(anyOf = {
      "android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"
  })
  Location getLastLocation();

  void removeLocationUpdates(LocationListener listener);

  void removeLocationUpdates(PendingIntent callbackIntent);

  @RequiresPermission(anyOf = {
      "android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"
  })
  void requestLocationUpdates(LocationRequest request, LocationListener listener, Looper looper);

  @RequiresPermission(anyOf = {
      "android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"
  })
  void requestLocationUpdates(LocationRequest request, LocationListener listener);

  @RequiresPermission(anyOf = {
      "android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"
  })
  void requestLocationUpdates(LocationRequest request, PendingIntent callbackIntent);

  @RequiresPermission(anyOf = {
      "android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"
  })
  void setMockLocation(Location mockLocation);

  @RequiresPermission(anyOf = {
      "android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"
  })
  void setMockMode(boolean isMockMode);

  @RequiresPermission(anyOf = {
      "android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"
  })
  void setMockTrace(final File file);

  /**
   * @deprecated Use {@link SettingsApi#checkLocationSettings(LostApiClient,
   * LocationSettingsRequest)}.
   */
  @Deprecated boolean isProviderEnabled(String provider);
}
