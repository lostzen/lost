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

  @Override public void onDestroy() {
    super.onDestroy();
    serviceImpl.shutdown();
  }

  public Location getLastLocation(LostApiClient client) {
    return serviceImpl.getLastLocation(client);
  }

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  public LocationAvailability getLocationAvailability(LostApiClient client) {
    return serviceImpl.getLocationAvailability(client);
  }

  public void requestLocationUpdates(LostApiClient client, LocationRequest request,
      LocationListener listener) {
    serviceImpl.requestLocationUpdates(client, request, listener);
  }

  public void requestLocationUpdates(LostApiClient client, LocationRequest request,
      PendingIntent callbackIntent) {
    serviceImpl.requestLocationUpdates(client, request, callbackIntent);
  }

  public void requestLocationUpdates(LostApiClient client, LocationRequest request,
      LocationCallback callback, Looper looper) {
    serviceImpl.requestLocationUpdates(client, request, callback, looper);
  }

  public void removeLocationUpdates(LostApiClient client, LocationListener listener) {
    serviceImpl.removeLocationUpdates(client, listener);
  }

  public void removeLocationUpdates(LostApiClient client, PendingIntent callbackIntent) {
    serviceImpl.removeLocationUpdates(client, callbackIntent);
  }

  public void removeLocationUpdates(LostApiClient client, LocationCallback callback) {
    serviceImpl.removeLocationUpdates(client, callback);
  }

  public void setMockMode(LostApiClient client, boolean isMockMode) {
    serviceImpl.setMockMode(client, isMockMode);
  }

  public void setMockLocation(LostApiClient client, Location mockLocation) {
    serviceImpl.setMockLocation(client, mockLocation);
  }

  public void setMockTrace(LostApiClient client, File file) {
    serviceImpl.setMockTrace(client, file);
  }

  public boolean isProviderEnabled(LostApiClient client, String provider) {
    return serviceImpl.isProviderEnabled(client, provider);
  }

  public Map<LostApiClient, Set<LocationListener>> getLocationListeners() {
    return serviceImpl.getLocationListeners();
  }

  public void disconnect(LostApiClient client) {
    serviceImpl.disconnect(client);
  }
}
