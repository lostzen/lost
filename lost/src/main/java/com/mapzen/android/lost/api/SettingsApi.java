package com.mapzen.android.lost.api;

import android.support.annotation.RequiresPermission;

import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;

public interface SettingsApi {

  /**
   * Checks if the relevant system settings are enabled on the device to carry out the desired
   * location request.
   *
   * @param apiClient Client which does not need to be connected at time of request.
   * @param request Location requirements.
   * @return a PendingResult which can be used to check status of request and optionally resolve
   * resolutions.
   */
  @RequiresPermission(anyOf = {BLUETOOTH, BLUETOOTH_ADMIN})
  PendingResult<LocationSettingsResult> checkLocationSettings(LostApiClient apiClient,
      LocationSettingsRequest request);
}
