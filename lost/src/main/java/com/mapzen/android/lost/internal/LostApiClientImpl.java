package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationServices;
import com.mapzen.android.lost.api.LostApiClient;

import android.content.Context;

/**
 * Implementation for {@link LostApiClient}. Constructs API implementations with {@link Context}.
 */
public class LostApiClientImpl implements LostApiClient {
  private final Context context;
  private final ConnectionCallbacks connectionCallbacks;

  public LostApiClientImpl(Context context, ConnectionCallbacks callbacks) {
    this.context = context;
    this.connectionCallbacks = callbacks;
  }

  @Override public void connect() {
    if (LocationServices.GeofencingApi == null) {
      LocationServices.GeofencingApi = new GeofencingApiImpl(context);
    }
    if (LocationServices.SettingsApi == null) {
      LocationServices.SettingsApi = new SettingsApiImpl(context);
    }
    if (LocationServices.FusedLocationApi == null) {
      FusedLocationProviderApiImpl fusedApi = new FusedLocationProviderApiImpl(context);
      fusedApi.connect(connectionCallbacks);
      LocationServices.FusedLocationApi = fusedApi;
    } else if (connectionCallbacks != null) {
      connectionCallbacks.onConnected();
    }
  }

  @Override public void disconnect() {
    if (LocationServices.FusedLocationApi != null
        && LocationServices.FusedLocationApi instanceof FusedLocationProviderApiImpl) {
      FusedLocationProviderApiImpl fusedProvider =
          (FusedLocationProviderApiImpl) LocationServices.FusedLocationApi;
      fusedProvider.disconnect();
    }
    LocationServices.FusedLocationApi = null;
    LocationServices.GeofencingApi = null;
    LocationServices.SettingsApi = null;
  }

  @Override public boolean isConnected() {
    return LocationServices.FusedLocationApi != null
        && LocationServices.GeofencingApi != null
        && LocationServices.SettingsApi != null;
  }
}
