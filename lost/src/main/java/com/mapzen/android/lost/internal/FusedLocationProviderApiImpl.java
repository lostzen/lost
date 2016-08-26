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
import java.util.Map;

/**
 * Implementation of the {@link FusedLocationProviderApi}.
 */
public class FusedLocationProviderApiImpl
    implements FusedLocationProviderApi {

  private static final String TAG = FusedLocationProviderApiImpl.class.getSimpleName();

  private final Context context;
  private FusedLocationProviderService service;

  private final ServiceConnection serviceConnection = new ServiceConnection() {
    @Override public void onServiceConnected(ComponentName name, IBinder binder) {
      FusedLocationProviderService.FusedLocationProviderBinder fusedBinder =
          (FusedLocationProviderService.FusedLocationProviderBinder) binder;
      if (fusedBinder != null) {
        service = fusedBinder.getService();
      }

      if (connectionCallbacks != null) {
        connectionCallbacks.onConnected();
      }
      Log.d(TAG, "[onServiceConnected]");
    }

    @Override public void onServiceDisconnected(ComponentName name) {
      if (connectionCallbacks != null) {
        connectionCallbacks.onConnectionSuspended();
      }
      Log.d(TAG, "[onServiceDisconnected]");
    }
  };

  LostApiClient.ConnectionCallbacks connectionCallbacks;

  public FusedLocationProviderApiImpl(Context context) {
    this.context = context;
  }

  public void connect(LostApiClient.ConnectionCallbacks callbacks) {
    Intent intent = new Intent(context, FusedLocationProviderService.class);
    context.startService(intent);

    connectionCallbacks = callbacks;
    intent = new Intent(context, FusedLocationProviderService.class);
    context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
  }

  public void disconnect() {
    context.unbindService(serviceConnection);

    Intent intent = new Intent(context, FusedLocationProviderService.class);
    context.stopService(intent);
  }

  @Override public Location getLastLocation(LostApiClient apiClient) {
    return service.getLastLocation(apiClient);
  }

  @Override public LocationAvailability getLocationAvailability(LostApiClient apiClient) {
    return service.getLocationAvailability(apiClient);
  }

  @Override public void requestLocationUpdates(LostApiClient apiClient, LocationRequest request,
      LocationListener listener) {
    service.requestLocationUpdates(apiClient, request, listener);
  }

  @Override public void requestLocationUpdates(LostApiClient apiClient, LocationRequest request,
      LocationListener listener, Looper looper) {
    throw new RuntimeException("Sorry, not yet implemented");
  }

  @Override public void requestLocationUpdates(LostApiClient apiClient, LocationRequest request,
      LocationCallback callback, Looper looper) {
    service.requestLocationUpdates(apiClient, request, callback, looper);
  }

  @Override
  public void requestLocationUpdates(LostApiClient apiClient, LocationRequest request,
      PendingIntent callbackIntent) {
    service.requestLocationUpdates(apiClient, request, callbackIntent);
  }

  @Override public void removeLocationUpdates(LostApiClient apiClient, LocationListener listener) {
    service.removeLocationUpdates(apiClient, listener);
  }

  @Override public void removeLocationUpdates(LostApiClient apiClient, PendingIntent callbackIntent) {
    service.removeLocationUpdates(apiClient, callbackIntent);
  }

  @Override public void removeLocationUpdates(LostApiClient apiClient, LocationCallback callback) {
    service.removeLocationUpdates(apiClient, callback);
  }

  @Override public void setMockMode(LostApiClient apiClient, boolean isMockMode) {
    service.setMockMode(apiClient, isMockMode);
  }

  @Override public void setMockLocation(LostApiClient apiClient, Location mockLocation) {
    service.setMockLocation(apiClient, mockLocation);
  }

  @Override public void setMockTrace(LostApiClient apiClient, File file) {
    service.setMockTrace(apiClient, file);
  }

  @Override public boolean isProviderEnabled(LostApiClient apiClient, String provider) {
    return service.isProviderEnabled(apiClient, provider);
  }

  public Map<LocationListener, LocationRequest> getListeners() {
    return service.getListeners();
  }

  public FusedLocationProviderService getService() {
    return service;
  }
}
