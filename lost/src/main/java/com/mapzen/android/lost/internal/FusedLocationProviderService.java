package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationAvailability;
import com.mapzen.android.lost.api.LocationRequest;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;

import java.io.File;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * Service which runs the fused location provider in the background.
 */
public class FusedLocationProviderService extends Service {

  private FusedLocationProviderServiceDelegate serviceImpl;

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
    serviceImpl = new FusedLocationProviderServiceDelegate(this, LostClientManager.shared());
  }

  public Location getLastLocation() {
    return serviceImpl.getLastLocation();
  }

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  public LocationAvailability getLocationAvailability() {
    return serviceImpl.getLocationAvailability();
  }

  public void requestLocationUpdates(LocationRequest request) {
    serviceImpl.requestLocationUpdates(request);
  }

  public void removeLocationUpdates() {
    serviceImpl.removeLocationUpdates();
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
}
