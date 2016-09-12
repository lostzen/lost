package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.FusedLocationProviderApi;
import com.mapzen.android.lost.api.LocationAvailability;
import com.mapzen.android.lost.api.LocationCallback;
import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.android.lost.api.PendingResult;
import com.mapzen.android.lost.api.Status;

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

  private Context context;
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


  public boolean isConnecting() {
    return connecting;
  }

  public FusedLocationProviderApiImpl() {
    connectionCallbacks = new HashSet<>();
  }

  public void connect(Context context, LostApiClient.ConnectionCallbacks callbacks) {
    this.context = context;

    Intent intent = new Intent(context, FusedLocationProviderService.class);
    context.startService(intent);

    if (callbacks != null) {
      connectionCallbacks.add(callbacks);
    }
    intent = new Intent(context, FusedLocationProviderService.class);
    context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
  }

  public void disconnect() {
    disconnect(null, true);
  }

  public void disconnect(LostApiClient client, boolean stopService) {
    if (service != null) {
      service.disconnect(client);
    }

    if (stopService) {
      context.unbindService(serviceConnection);

      Intent intent = new Intent(context, FusedLocationProviderService.class);
      context.stopService(intent);

      service = null;
    }
  }

  public boolean isConnected() {
    return service != null;
  }

  @Override public Location getLastLocation(LostApiClient client) {
    return service.getLastLocation(client);
  }

  @Override public LocationAvailability getLocationAvailability(LostApiClient client) {
    return service.getLocationAvailability(client);
  }

  @Override public PendingResult<Status> requestLocationUpdates(LostApiClient client,
      LocationRequest request, LocationListener listener) {
    return service.requestLocationUpdates(client, request, listener);
  }

  @Override public PendingResult<Status> requestLocationUpdates(LostApiClient client,
      LocationRequest request, LocationListener listener, Looper looper) {
    throw new RuntimeException("Sorry, not yet implemented");
  }

  @Override public PendingResult<Status> requestLocationUpdates(LostApiClient client,
      LocationRequest request, LocationCallback callback, Looper looper) {
    return service.requestLocationUpdates(client, request, callback, looper);
  }

  @Override
  public PendingResult<Status> requestLocationUpdates(LostApiClient client, LocationRequest request,
      PendingIntent callbackIntent) {
    return service.requestLocationUpdates(client, request, callbackIntent);
  }

  @Override public PendingResult<Status> removeLocationUpdates(LostApiClient client,
      LocationListener listener) {
    return service.removeLocationUpdates(client, listener);
  }

  @Override public PendingResult<Status> removeLocationUpdates(LostApiClient client,
      PendingIntent callbackIntent) {
    return service.removeLocationUpdates(client, callbackIntent);
  }

  @Override public PendingResult<Status> removeLocationUpdates(LostApiClient client,
      LocationCallback callback) {
    return service.removeLocationUpdates(client, callback);
  }

  @Override public PendingResult<Status> setMockMode(LostApiClient client, boolean isMockMode) {
    return service.setMockMode(client, isMockMode);
  }

  @Override public PendingResult<Status> setMockLocation(LostApiClient client,
      Location mockLocation) {
    return service.setMockLocation(client, mockLocation);
  }

  @Override public PendingResult<Status> setMockTrace(LostApiClient client, File file) {
    return service.setMockTrace(client, file);
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
