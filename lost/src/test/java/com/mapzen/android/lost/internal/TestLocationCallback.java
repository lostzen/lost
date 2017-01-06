package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationAvailability;
import com.mapzen.android.lost.api.LocationCallback;
import com.mapzen.android.lost.api.LocationResult;

public class TestLocationCallback implements LocationCallback {

  private LocationAvailability availability;
  private LocationResult result;

  @Override public void onLocationAvailability(LocationAvailability locationAvailability) {
    availability = locationAvailability;
  }

  @Override public void onLocationResult(LocationResult locationResult) {
    result = locationResult;
  }

  public LocationAvailability getAvailability() {
    return availability;
  }

  public LocationResult getResult() {
    return result;
  }

  public void setResult(LocationResult result) {
    this.result = result;
  }
}
