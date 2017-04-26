package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationAvailability;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LocationResult;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.os.RemoteException;
import android.support.annotation.RequiresPermission;

import java.io.File;
import java.util.ArrayList;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class FusedLocationProviderServiceDelegate implements LocationEngine.Callback {

  private Context context;

  private boolean mockMode;
  private LocationEngine locationEngine;

  private ClientManager clientManager;
  private IFusedLocationProviderCallback callback;

  public FusedLocationProviderServiceDelegate(Context context, ClientManager manager) {
    this.context = context;
    this.clientManager = manager;
    locationEngine = new FusionEngine(context, this);
  }

  public void init(IFusedLocationProviderCallback callback) {
    this.callback = callback;
  }

  public Location getLastLocation() {
    return locationEngine.getLastLocation();
  }

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  public LocationAvailability getLocationAvailability() {
    return locationEngine.createLocationAvailability();
  }

  public void requestLocationUpdates(LocationRequest request) {
    locationEngine.setRequest(request);
  }

  public void removeLocationUpdates() {
    locationEngine.setRequest(null);
  }

  public void setMockMode(boolean isMockMode) {
    if (mockMode != isMockMode) {
      toggleMockMode();
    }
  }

  public void setMockLocation(Location mockLocation) {
    if (mockMode) {
      ((MockEngine) locationEngine).setLocation(mockLocation);
    }
  }

  public void setMockTrace(String path, String filename) {
    if (mockMode) {
      ((MockEngine) locationEngine).setTrace(new File(path, filename));
    }
  }

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  public void reportLocation(Location location) {
    // Notify remote AIDL callback
    if (callback != null) {
      try {
        callback.onLocationChanged(location);
      } catch (RemoteException e) {
        throw new RuntimeException(e);
      }
    }

    ReportedChanges changes = clientManager.reportLocationChanged(location);

    LocationAvailability availability = locationEngine.createLocationAvailability();
    ArrayList<Location> locations = new ArrayList<>();
    locations.add(location);
    final LocationResult result = LocationResult.create(locations);
    ReportedChanges pendingIntentChanges = clientManager.sendPendingIntent(
        context, location, availability, result);

    ReportedChanges callbackChanges = clientManager.reportLocationResult(location, result);

    changes.putAll(pendingIntentChanges);
    changes.putAll(callbackChanges);
    clientManager.updateReportedValues(changes);
  }

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  public void reportProviderDisabled(String provider) {
    notifyLocationAvailabilityChanged();
  }

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  public void reportProviderEnabled(String provider) {
    notifyLocationAvailabilityChanged();
    LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    manager.requestSingleUpdate(provider, new android.location.LocationListener() {
      @Override
      public void onLocationChanged(Location location) {
        notifyLocationAvailabilityChanged();
      }

      @Override
      public void onStatusChanged(String s, int i, Bundle bundle) {

      }

      @Override
      public void onProviderEnabled(String s) {

      }

      @Override
      public void onProviderDisabled(String s) {

      }
    }, Looper.myLooper());
  }

  private void toggleMockMode() {
    mockMode = !mockMode;
    locationEngine.setRequest(null);
    if (mockMode) {
      locationEngine = new MockEngine(context, this, new GpxTraceThreadFactory());
    } else {
      locationEngine = new FusionEngine(context, this);
    }
  }

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  private void notifyLocationAvailabilityChanged() {
    final LocationAvailability availability = locationEngine.createLocationAvailability();
    if (callback != null) {
      try {
        callback.onLocationAvailabilityChanged(availability);
      } catch (RemoteException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
