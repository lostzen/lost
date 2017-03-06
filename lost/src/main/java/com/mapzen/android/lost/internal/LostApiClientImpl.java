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
  private final ClientManager clientManager;

  public LostApiClientImpl(Context context, ConnectionCallbacks callbacks,
      ClientManager clientManager) {
    this.context = context;
    this.connectionCallbacks = callbacks;
    this.clientManager = clientManager;
  }

  @Override public void connect() {
    clientManager.addClient(this);

    GeofencingApiImpl geofencingApi = getGeofencingImpl();
    if (!geofencingApi.isConnected()) {
      geofencingApi.connect(context);
    }

    SettingsApiImpl settingsApi = getSettingsApiImpl();
    if (!settingsApi.isConnected()) {
      settingsApi.connect(context);
    }

    FusedLocationProviderApiImpl fusedApi = getFusedLocationProviderApiImpl();
    if (fusedApi.isConnected()) {
      if (connectionCallbacks != null) {
        connectionCallbacks.onConnected();
        fusedApi.addConnectionCallbacks(connectionCallbacks);
      }
    } else if (fusedApi.isConnecting()) {
      if (connectionCallbacks != null) {
        fusedApi.addConnectionCallbacks(connectionCallbacks);
      }
    } else {
      fusedApi.connect(context, connectionCallbacks);
    }
  }

  @Override public void disconnect() {
    getFusedLocationProviderApiImpl().removeConnectionCallbacks(connectionCallbacks);

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
        && getFusedLocationProviderApiImpl().isConnected() && clientManager.containsClient(this);
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
