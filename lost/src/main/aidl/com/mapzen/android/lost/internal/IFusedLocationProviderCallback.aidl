package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationAvailability;

interface IFusedLocationProviderCallback {

  void onLocationChanged(in Location location);

  void onLocationAvailabilityChanged(in LocationAvailability locationAvailability);
}
