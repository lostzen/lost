package com.mapzen.android.lost.api;

public class LocationSettingsStates {
  private final boolean gpsUsable;
  private final boolean networkUsable;
  private final boolean bleUsable;
  private final boolean gpsPresent;
  private final boolean networkPresent;
  private final boolean blePresent;

  public LocationSettingsStates(boolean gpsUsable, boolean networkUsable, boolean bleUsable,
      boolean gpsPresent, boolean networkPresent, boolean blePresent) {
    this.gpsUsable = gpsUsable;
    this.networkUsable = networkUsable;
    this.bleUsable = bleUsable;
    this.gpsPresent = gpsPresent;
    this.networkPresent = networkPresent;
    this.blePresent = blePresent;
  }

  public boolean isGpsUsable() {
    return this.gpsUsable;
  }

  public boolean isGpsPresent() {
    return this.gpsPresent;
  }

  public boolean isNetworkLocationUsable() {
    return this.networkUsable;
  }

  public boolean isNetworkLocationPresent() {
    return this.networkPresent;
  }

  public boolean isLocationUsable() {
    return this.gpsUsable || this.networkUsable;
  }

  public boolean isLocationPresent() {
    return this.gpsPresent || this.networkPresent;
  }

  public boolean isBleUsable() {
    return this.bleUsable;
  }

  public boolean isBlePresent() {
    return this.blePresent;
  }
}
