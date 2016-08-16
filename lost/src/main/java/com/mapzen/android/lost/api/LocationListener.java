package com.mapzen.android.lost.api;

import android.location.Location;

public interface LocationListener {
  void onLocationChanged(Location location);

  void onProviderDisabled(String provider);

  void onProviderEnabled(String provider);
}
