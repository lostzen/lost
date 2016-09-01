package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationAvailability;
import com.mapzen.android.lost.api.LocationCallback;
import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LostApiClient;

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

  @Override public void onDestroy() {
    super.onDestroy();
    serviceImpl.shutdown();
  }

  public Location getLastLocation(LostApiClient apiClient) {
    return serviceImpl.getLastLocation(apiClient);
  }

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  public LocationAvailability getLocationAvailability(LostApiClient apiClient) {
    return serviceImpl.getLocationAvailability(apiClient);
  }

  public void requestLocationUpdates(LostApiClient apiClient, LocationRequest request,
      LocationListener listener) {
    serviceImpl.requestLocationUpdates(apiClient, request, listener);
  }

  public void requestLocationUpdates(LostApiClient apiClient, LocationRequest request,
      PendingIntent callbackIntent) {
    serviceImpl.requestLocationUpdates(apiClient, request, callbackIntent);
  }

  public void requestLocationUpdates(LostApiClient apiClient, LocationRequest request,
      LocationCallback callback, Looper looper) {
    serviceImpl.requestLocationUpdates(apiClient, request, callback, looper);
  }

  public void removeLocationUpdates(LostApiClient apiClient, LocationListener listener) {
    serviceImpl.removeLocationUpdates(apiClient, listener);
  }

  public void removeLocationUpdates(LostApiClient apiClient, PendingIntent callbackIntent) {
    serviceImpl.removeLocationUpdates(apiClient, callbackIntent);
  }

  public void removeLocationUpdates(LostApiClient apiClient, LocationCallback callback) {
    serviceImpl.removeLocationUpdates(apiClient, callback);
  }

  public void setMockMode(LostApiClient apiClient, boolean isMockMode) {
    serviceImpl.setMockMode(apiClient, isMockMode);
  }

  public void setMockLocation(LostApiClient apiClient, Location mockLocation) {
    serviceImpl.setMockLocation(apiClient, mockLocation);
  }

  public void setMockTrace(LostApiClient apiClient, File file) {
    serviceImpl.setMockTrace(apiClient, file);
  }

  public boolean isProviderEnabled(LostApiClient apiClient, String provider) {
    return serviceImpl.isProviderEnabled(apiClient, provider);
  }

  public Map<LostApiClient, Map<LocationListener, LocationRequest>> getListeners() {
    return serviceImpl.getListeners();
  }

  public void disconnect(LostApiClient client) {
    serviceImpl.disconnect(client);
  }
}
