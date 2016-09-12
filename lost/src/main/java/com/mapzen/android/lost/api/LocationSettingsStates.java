package com.mapzen.android.lost.api;

/**
 * Contains detailed information on all the current states of the location settings for a user's
 * device. This object is returned in the {@link LocationSettingsResult} when using the
 * {@link SettingsApi}.
 */
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

  /**
   * Is GPS location enabled on this device.
   * @return whether GPS location is enabled for the device.
   */
  public boolean isGpsUsable() {
    return this.gpsUsable;
  }

  /**
   * Is GPS location available to be received on this device.
   * @return whether GPS location is available for the device.
   */
  public boolean isGpsPresent() {
    return this.gpsPresent;
  }

  /**
   * Is Network location enabled on this device.
   * @return whether Network location is enabled for the device.
   */
  public boolean isNetworkLocationUsable() {
    return this.networkUsable;
  }

  /**
   * Is Network location available to be received on this device.
   * @return whether Network location is available for the device.
   */
  public boolean isNetworkLocationPresent() {
    return this.networkPresent;
  }

  /**
   * Is either GPS or Network location enabled on this device.
   * @return whether GPS or Network location is enabled for the device.
   */
  public boolean isLocationUsable() {
    return this.gpsUsable || this.networkUsable;
  }

  /**
   * Is either GPS or Network location available to be received on this device.
   * @return whether GPS or Network location is available for the device.
   */
  public boolean isLocationPresent() {
    return this.gpsPresent || this.networkPresent;
  }

  /**
   * Is BLE enabled on this device.
   * @return whether BLE is enabled on the device.
   */
  public boolean isBleUsable() {
    return this.bleUsable;
  }

  /**
   * Is BLE available on this device.
   * @return whether BLE is available on the device.
   */
  public boolean isBlePresent() {
    return this.blePresent;
  }
}
