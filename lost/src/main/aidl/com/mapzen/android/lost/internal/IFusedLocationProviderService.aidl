package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationAvailability;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.internal.IFusedLocationProviderCallback;

interface IFusedLocationProviderService {

  void add(in IFusedLocationProviderCallback callback);

  void remove(in IFusedLocationProviderCallback callback);

  Location getLastLocation();

  LocationAvailability getLocationAvailability();

  void requestLocationUpdates(in LocationRequest request);

  void removeLocationUpdates(in List<LocationRequest> requests);

  void setMockMode(boolean isMockMode);

  void setMockLocation(in Location mockLocation);

  void setMockTrace(String path, String filename);
}
