package com.mapzen.android.lost.api;

public interface SettingsApi {

  PendingResult<LocationSettingsResult> checkLocationSettings(LostApiClient apiClient,
      LocationSettingsRequest request);
}
