package com.mapzen.android.lost.api;

public class LocationSettingsResult implements Result {

  private final Status status;
  private final LocationSettingsStates locationSettingsStates;

  public LocationSettingsResult(Status status, LocationSettingsStates states) {
    this.status = status;
    this.locationSettingsStates = states;
  }

  public LocationSettingsStates getLocationSettingsStates() {
    return this.locationSettingsStates;
  }

  public Status getStatus() {
    return this.status;
  }
}
