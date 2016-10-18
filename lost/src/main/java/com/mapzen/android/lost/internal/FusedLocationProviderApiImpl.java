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
import java.util.Map;
import java.util.Set;

/**
 * Implementation of the {@link FusedLocationProviderApi}.
 */
public class FusedLocationProviderApiImpl
    implements FusedLocationProviderApi {

  private Context context;
  private FusedLocationProviderService service;
  private FusedLocationServiceConnectionManager serviceConnectionManager;

  private FusedLocationServiceConnectionManager.EventCallbacks eventCallbacks =
      new FusedLocationServiceConnectionManager.EventCallbacks() {
    @Override public void onConnect(Context context) {
      FusedLocationProviderApiImpl.this.context = context;

      Intent intent = new Intent(context, FusedLocationProviderService.class);
      context.startService(intent);

      intent = new Intent(context, FusedLocationProviderService.class);
      context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override public void onServiceConnected(IBinder binder) {
      FusedLocationProviderService.FusedLocationProviderBinder fusedBinder =
          (FusedLocationProviderService.FusedLocationProviderBinder) binder;
      if (fusedBinder != null) {
        service = fusedBinder.getService();
      }
    }

    @Override public void onDisconnect(LostApiClient client, boolean stopService,
        boolean disconnectService) {
      if (disconnectService) {
        service.disconnect(client);
      }
      if (stopService) {
        context.unbindService(serviceConnection);
        Intent intent = new Intent(context, FusedLocationProviderService.class);
        context.stopService(intent);
        service = null;
      }
    }
  };

  private final ServiceConnection serviceConnection = new ServiceConnection() {
    @Override public void onServiceConnected(ComponentName name, IBinder binder) {
      serviceConnectionManager.onServiceConnected(binder);
    }

    @Override public void onServiceDisconnected(ComponentName name) {
      serviceConnectionManager.onServiceDisconnected();
    }
  };

  public FusedLocationProviderApiImpl() {
    serviceConnectionManager = new FusedLocationServiceConnectionManager(eventCallbacks);
  }

  public boolean isConnecting() {
    return serviceConnectionManager.isConnecting();
  }

  public void addConnectionCallbacks(LostApiClient.ConnectionCallbacks callbacks) {
    serviceConnectionManager.addCallbacks(callbacks);
  }

  public void connect(Context context, LostApiClient.ConnectionCallbacks callbacks) {
    serviceConnectionManager.connect(context, callbacks);
  }

  public void disconnect() {
    disconnect(null, true);
  }

  public void disconnect(LostApiClient client, boolean stopService) {
    serviceConnectionManager.disconnect(client, stopService);
  }

  public boolean isConnected() {
    return serviceConnectionManager.isConnected();
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
