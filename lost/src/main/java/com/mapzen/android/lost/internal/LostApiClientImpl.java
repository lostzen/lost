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
  private final ClientManager clientManager = LostClientManager.shared();

  public LostApiClientImpl(Context context, ConnectionCallbacks callbacks) {
    this.context = context;
    this.connectionCallbacks = callbacks;
  }

  @Override public void connect() {
    GeofencingApiImpl geofencingApi = getGeofencingImpl();
    if (!geofencingApi.isConnected()) {
      geofencingApi.connect(context);
    }
    SettingsApiImpl settingsApi = getSettingsApiImpl();
    if (!settingsApi.isConnected()) {
      settingsApi.connect(context);
    }
    FusedLocationProviderApiImpl fusedApi = getFusedLocationProviderApiImpl();
    if (!fusedApi.isConnected()) {
      fusedApi.connect(context, connectionCallbacks);
    } else if (connectionCallbacks != null) {
      if (fusedApi.isConnecting()) {
        fusedApi.connectionCallbacks.add(connectionCallbacks);
      } else {
        connectionCallbacks.onConnected();
      }
    }

    clientManager.addClient(this);
  }

  @Override public void disconnect() {
    clientManager.removeClient(this);
    if (clientManager.numberOfClients() > 0) {
      return;
    }

    getSettingsApiImpl().disconnect();
    getGeofencingImpl().disconnect();
    getFusedLocationProviderApiImpl().disconnect();
  }

  @Override public boolean isConnected() {
    return getGeofencingImpl().isConnected() && getSettingsApiImpl().isConnected()
        && getFusedLocationProviderApiImpl().isConnected();
  }

  private GeofencingApiImpl getGeofencingImpl() {
    return (GeofencingApiImpl) LocationServices.GeofencingApi;
  }

  private SettingsApiImpl getSettingsApiImpl() {
    return (SettingsApiImpl) LocationServices.SettingsApi;
  }

  private FusedLocationProviderApiImpl getFusedLocationProviderApiImpl() {
    return (FusedLocationProviderApiImpl) LocationServices.FusedLocationApi;
  }
}
