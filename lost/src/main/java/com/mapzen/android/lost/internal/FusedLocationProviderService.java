package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationAvailability;
import com.mapzen.android.lost.api.LocationCallback;
import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.android.lost.api.PendingResult;
import com.mapzen.android.lost.api.Status;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;

import java.io.File;
import java.util.Map;
import java.util.Set;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * Service which runs the fused location provider in the background.
 */
public class FusedLocationProviderService extends Service {

  private FusedLocationProviderServiceImpl serviceImpl;

  private final IBinder binder = new FusedLocationProviderBinder();

  public class FusedLocationProviderBinder extends Binder {

    public FusedLocationProviderService getService() {
      return FusedLocationProviderService.this;
    }
  }

  @Nullable @Override public IBinder onBind(Intent intent) {
    return binder;
  }

  @Override public void onCreate() {
    super.onCreate();
    serviceImpl = new FusedLocationProviderServiceImpl(this, LostClientManager.shared());
  }

  public Location getLastLocation() {
    return serviceImpl.getLastLocation();
  }

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  public LocationAvailability getLocationAvailability() {
    return serviceImpl.getLocationAvailability();
  }

  public PendingResult<Status> requestLocationUpdates(LocationRequest request) {
    return serviceImpl.requestLocationUpdates(request);
  }

  public PendingResult<Status> removeLocationUpdates(LostApiClient client,
      LocationListener listener) {
    return serviceImpl.removeLocationUpdates(client, listener);
  }

  public PendingResult<Status> removeLocationUpdates(LostApiClient client,
      PendingIntent callbackIntent) {
    return serviceImpl.removeLocationUpdates(client, callbackIntent);
  }

  public PendingResult<Status> removeLocationUpdates(LostApiClient client,
      LocationCallback callback) {
    return serviceImpl.removeLocationUpdates(client, callback);
  }

  public PendingResult<Status> setMockMode(boolean isMockMode) {
    return serviceImpl.setMockMode(isMockMode);
  }

  public PendingResult<Status> setMockLocation(Location mockLocation) {
    return serviceImpl.setMockLocation(mockLocation);
  }

  public PendingResult<Status> setMockTrace(File file) {
    return serviceImpl.setMockTrace(file);
  }

  public boolean isProviderEnabled(LostApiClient client, String provider) {
    return serviceImpl.isProviderEnabled(client, provider);
  }

  public Map<LostApiClient, Set<LocationListener>> getLocationListeners() {
    return serviceImpl.getLocationListeners();
  }
}
