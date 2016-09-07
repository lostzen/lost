package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.FusedLocationProviderApi;
import com.mapzen.android.lost.api.LocationAvailability;
import com.mapzen.android.lost.api.LocationCallback;
import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LostApiClient;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of the {@link FusedLocationProviderApi}.
 */
public class FusedLocationProviderApiImpl
    implements FusedLocationProviderApi {

  private static final String TAG = FusedLocationProviderApiImpl.class.getSimpleName();

  private final Context context;
  private FusedLocationProviderService service;
  private boolean connecting;

  private final ServiceConnection serviceConnection = new ServiceConnection() {
    @Override public void onServiceConnected(ComponentName name, IBinder binder) {
      FusedLocationProviderService.FusedLocationProviderBinder fusedBinder =
          (FusedLocationProviderService.FusedLocationProviderBinder) binder;
      if (fusedBinder != null) {
        service = fusedBinder.getService();
      }

      if (!connectionCallbacks.isEmpty()) {
        for (LostApiClient.ConnectionCallbacks callbacks : connectionCallbacks) {
          callbacks.onConnected();
        }
      }
      connecting = false;
      Log.d(TAG, "[onServiceConnected]");
    }

    @Override public void onServiceDisconnected(ComponentName name) {
      if (!connectionCallbacks.isEmpty()) {
        for (LostApiClient.ConnectionCallbacks callbacks : connectionCallbacks) {
          callbacks.onConnectionSuspended();
        }
      }
      connecting = false;
      Log.d(TAG, "[onServiceDisconnected]");
    }
  };

  Set<LostApiClient.ConnectionCallbacks> connectionCallbacks;

  public FusedLocationProviderApiImpl(Context context) {
    this.context = context;
    connectionCallbacks = new HashSet<>();
  }

  public boolean isConnecting() {
    return connecting;
  }

  public void connect(LostApiClient.ConnectionCallbacks callbacks) {
    connecting = true;

    Intent intent = new Intent(context, FusedLocationProviderService.class);
    context.startService(intent);

    if (callbacks != null) {
      connectionCallbacks.add(callbacks);
    }
    intent = new Intent(context, FusedLocationProviderService.class);
    context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
  }

  public void disconnect(LostApiClient client, boolean stopService) {
    if (service != null) {
      service.disconnect(client);
    }

    if (stopService) {
      context.unbindService(serviceConnection);

      Intent intent = new Intent(context, FusedLocationProviderService.class);
      context.stopService(intent);
    }
  }

  @Override public Location getLastLocation(LostApiClient client) {
    return service.getLastLocation(client);
  }

  @Override public LocationAvailability getLocationAvailability(LostApiClient client) {
    return service.getLocationAvailability(client);
  }

  @Override public void requestLocationUpdates(LostApiClient client, LocationRequest request,
      LocationListener listener) {
    service.requestLocationUpdates(client, request, listener);
  }

  @Override public void requestLocationUpdates(LostApiClient client, LocationRequest request,
      LocationListener listener, Looper looper) {
    throw new RuntimeException("Sorry, not yet implemented");
  }

  @Override public void requestLocationUpdates(LostApiClient client, LocationRequest request,
      LocationCallback callback, Looper looper) {
    service.requestLocationUpdates(client, request, callback, looper);
  }

  @Override
  public void requestLocationUpdates(LostApiClient client, LocationRequest request,
      PendingIntent callbackIntent) {
    service.requestLocationUpdates(client, request, callbackIntent);
  }

  @Override public void removeLocationUpdates(LostApiClient client, LocationListener listener) {
    service.removeLocationUpdates(client, listener);
  }

  @Override public void removeLocationUpdates(LostApiClient client,
      PendingIntent callbackIntent) {
    service.removeLocationUpdates(client, callbackIntent);
  }

  @Override public void removeLocationUpdates(LostApiClient client, LocationCallback callback) {
    service.removeLocationUpdates(client, callback);
  }

  @Override public void setMockMode(LostApiClient client, boolean isMockMode) {
    service.setMockMode(client, isMockMode);
  }

  @Override public void setMockLocation(LostApiClient client, Location mockLocation) {
    service.setMockLocation(client, mockLocation);
  }

  @Override public void setMockTrace(LostApiClient client, File file) {
    service.setMockTrace(client, file);
  }

  @Override public boolean isProviderEnabled(LostApiClient client, String provider) {
    return service.isProviderEnabled(client, provider);
  }

  public Map<LostApiClient, Set<LocationListener>> getLocationListeners() {
    return service.getLocationListeners();
  }

  public FusedLocationProviderService getService() {
    return service;
  }
}
