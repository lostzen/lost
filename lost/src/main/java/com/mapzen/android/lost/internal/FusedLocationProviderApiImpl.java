package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.FusedLocationProviderApi;
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

import java.io.File;
import java.util.Map;

/**
 * Implementation of the {@link FusedLocationProviderApi}.
 */
public class FusedLocationProviderApiImpl
    implements FusedLocationProviderApi {

  private final Context context;
  private FusedLocationProviderService service;

  private final ServiceConnection serviceConnection = new ServiceConnection() {
    @Override public void onServiceConnected(ComponentName name, IBinder binder) {
      FusedLocationProviderService.FusedLocationProviderBinder fusedBinder =
          (FusedLocationProviderService.FusedLocationProviderBinder) binder;
      service = fusedBinder.getService();

      if (connectionCallbacks != null) {
        connectionCallbacks.onConnected();
      }
    }

    @Override public void onServiceDisconnected(ComponentName name) {
      if (connectionCallbacks != null) {
        connectionCallbacks.onConnectionSuspended();
      }
    }
  };

  LostApiClient.ConnectionCallbacks connectionCallbacks;

  public FusedLocationProviderApiImpl(Context context) {
    this.context = context;
  }

  public void connect(LostApiClient.ConnectionCallbacks callbacks) {
    connectionCallbacks = callbacks;
    Intent intent = new Intent(context, FusedLocationProviderService.class);
    context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
  }

  public void disconnect() {
    context.unbindService(serviceConnection);
  }

  @Override public Location getLastLocation() {
    return service.getLastLocation();
  }

  @Override public void requestLocationUpdates(LocationRequest request, LocationListener listener) {
    service.requestLocationUpdates(request, listener);
  }

  @Override public void requestLocationUpdates(LocationRequest request, LocationListener listener,
      Looper looper) {
    throw new RuntimeException("Sorry, not yet implemented");
  }

  @Override
  public void requestLocationUpdates(LocationRequest request, PendingIntent callbackIntent) {
    service.requestLocationUpdates(request, callbackIntent);
  }

  @Override public void removeLocationUpdates(LocationListener listener) {
    service.removeLocationUpdates(listener);
  }

  @Override public void removeLocationUpdates(PendingIntent callbackIntent) {
    service.removeLocationUpdates(callbackIntent);
  }

  @Override public void setMockMode(boolean isMockMode) {
    service.setMockMode(isMockMode);
  }

  @Override public void setMockLocation(Location mockLocation) {
    service.setMockLocation(mockLocation);
  }

  @Override public void setMockTrace(File file) {
    service.setMockTrace(file);
  }

  @Override public boolean isProviderEnabled(String provider) {
    return service.isProviderEnabled(provider);
  }

  public Map<LocationListener, LocationRequest> getListeners() {
    return service.getListeners();
  }
}
