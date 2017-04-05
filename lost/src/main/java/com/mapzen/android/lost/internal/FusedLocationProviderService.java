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
import android.os.Looper;
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

  public Location getLastLocation(LostApiClient client) {
    return serviceImpl.getLastLocation(client);
  }

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  public LocationAvailability getLocationAvailability(LostApiClient client) {
    return serviceImpl.getLocationAvailability(client);
  }

  public PendingResult<Status> requestLocationUpdates(LostApiClient client, LocationRequest request,
      LocationListener listener) {
    return serviceImpl.requestLocationUpdates(client, request, listener);
  }

  public PendingResult<Status> requestLocationUpdates(LostApiClient client, LocationRequest request,
      PendingIntent callbackIntent) {
    return serviceImpl.requestLocationUpdates(client, request, callbackIntent);
  }

  public PendingResult<Status> requestLocationUpdates(LostApiClient client, LocationRequest request,
      LocationCallback callback, Looper looper) {
    return serviceImpl.requestLocationUpdates(client, request, callback, looper);
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

  public PendingResult<Status> setMockMode(LostApiClient client, boolean isMockMode) {
    return serviceImpl.setMockMode(client, isMockMode);
  }

  public PendingResult<Status> setMockLocation(LostApiClient client, Location mockLocation) {
    return serviceImpl.setMockLocation(client, mockLocation);
  }

  public PendingResult<Status> setMockTrace(LostApiClient client, File file) {
    return serviceImpl.setMockTrace(client, file);
  }

  public boolean isProviderEnabled(LostApiClient client, String provider) {
    return serviceImpl.isProviderEnabled(client, provider);
  }

  public Map<LostApiClient, Set<LocationListener>> getLocationListeners() {
    return serviceImpl.getLocationListeners();
  }
}
