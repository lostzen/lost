package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationSettingsRequest;
import com.mapzen.android.lost.api.LocationSettingsResult;
import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.android.lost.api.PendingResult;
import com.mapzen.android.lost.api.SettingsApi;

import android.content.Context;

public class SettingsApiImpl implements SettingsApi {

  private Context context;

  public SettingsApiImpl(Context context) {
    this.context = context;
  }

  @Override
  public PendingResult<LocationSettingsResult> checkLocationSettings(LostApiClient apiClient,
      LocationSettingsRequest request) {
    return new LocationSettingsResultRequest(context, request);
  }
}
