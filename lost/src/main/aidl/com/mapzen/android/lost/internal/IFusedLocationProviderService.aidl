package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationAvailability;
import com.mapzen.android.lost.api.LocationRequest;

interface IFusedLocationProviderService {

  Location getLastLocation();

  LocationAvailability getLocationAvailability();

  void requestLocationUpdates(in LocationRequest request);

  void removeLocationUpdates();

  void setMockMode(boolean isMockMode);

  void setMockLocation(in Location mockLocation);

  void setMockTrace(String path, String filename);
}
