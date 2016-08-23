package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationAvailability;
import com.mapzen.android.lost.api.LocationCallback;
import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.util.Map;

/**
 * Service which runs the fused location provider in the background.
 */
public class FusedLocationProviderService extends Service {

  private static final String TAG = FusedLocationProviderService.class.getSimpleName();

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
    serviceImpl = new FusedLocationProviderServiceImpl(this);
    Log.d(TAG, "[onCreate]");
  }

  @Override public void onDestroy() {
    super.onDestroy();
    serviceImpl.shutdown();
    Log.d(TAG, "[onDestroy]");
  }

  public Location getLastLocation() {
    return serviceImpl.getLastLocation();
  }

  public LocationAvailability getLocationAvailability() {
    return serviceImpl.getLocationAvailability();
  }

  public void requestLocationUpdates(LocationRequest request, LocationListener listener) {
    serviceImpl.requestLocationUpdates(request, listener);
  }

  public void requestLocationUpdates(LocationRequest request, PendingIntent callbackIntent) {
    serviceImpl.requestLocationUpdates(request, callbackIntent);
  }

  public void requestLocationUpdates(LocationRequest request, LocationCallback callback,
      Looper looper) {
    serviceImpl.requestLocationUpdates(request, callback, looper);
  }

  public void removeLocationUpdates(LocationListener listener) {
    serviceImpl.removeLocationUpdates(listener);
  }

  public void removeLocationUpdates(PendingIntent callbackIntent) {
    serviceImpl.removeLocationUpdates(callbackIntent);
  }

  public void removeLocationUpdates(LocationCallback callback) {
    serviceImpl.removeLocationUpdates(callback);
  }

  public void setMockMode(boolean isMockMode) {
    serviceImpl.setMockMode(isMockMode);
  }

  public void setMockLocation(Location mockLocation) {
    serviceImpl.setMockLocation(mockLocation);
  }

  public void setMockTrace(File file) {
    serviceImpl.setMockTrace(file);
  }

  public boolean isProviderEnabled(String provider) {
    return serviceImpl.isProviderEnabled(provider);
  }

  public Map<LocationListener, LocationRequest> getListeners() {
    return serviceImpl.getListeners();
  }
}
