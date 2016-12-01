package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationSettingsRequest;
import com.mapzen.android.lost.api.LocationSettingsResult;
import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.android.lost.api.PendingResult;
import com.mapzen.android.lost.api.SettingsApi;

import android.content.Context;

public class SettingsApiImpl implements SettingsApi {

  private Context context;

  public void connect(Context context) {
    this.context = context;
  }

  public boolean isConnected() {
    return context != null;
  }

  public void disconnect() {
    context = null;
  }

  @Override
  public PendingResult<LocationSettingsResult> checkLocationSettings(LostApiClient client,
      LocationSettingsRequest request) {
    PendingIntentGenerator generator = new PendingIntentGenerator(context);
    return new LocationSettingsResultRequest(context, generator, request);
  }
}
