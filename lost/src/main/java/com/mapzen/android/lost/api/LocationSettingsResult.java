package com.mapzen.android.lost.api;

/**
 * Result of checking settings via {@link SettingsApi#checkLocationSettings(LostApiClient,
 * LocationSettingsRequest)}. Indicates whether an {@link android.app.Activity} should be shown to
 * change the user's location settings.
 */
public class LocationSettingsResult implements Result {

  private final Status status;
  private final LocationSettingsStates locationSettingsStates;

  public LocationSettingsResult(Status status, LocationSettingsStates states) {
    this.status = status;
    this.locationSettingsStates = states;
  }

  /**
   * Returns the {@link LocationSettingsStates}.
   * @return the {@link LocationSettingsStates}.
   */
  public LocationSettingsStates getLocationSettingsStates() {
    return this.locationSettingsStates;
  }

  /**
   * Returns the {@link Status} of the result.
   * @return the {@link Status}.
   */
  public Status getStatus() {
    return this.status;
  }
}
