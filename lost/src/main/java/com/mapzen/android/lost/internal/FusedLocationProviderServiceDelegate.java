package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationAvailability;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LocationResult;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
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

  public FusedLocationProviderServiceDelegate(Context context, ClientManager manager) {
    this.context = context;
    this.clientManager = manager;
    locationEngine = new FusionEngine(context, this);
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
    checkAllListenersPendingIntentsAndCallbacks();
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

  public void setMockTrace(File file) {
    if (mockMode) {
      ((MockEngine) locationEngine).setTrace(file);
    }
  }

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  public void reportLocation(Location location) {
    ReportedChanges changes = clientManager.reportLocationChanged(location);

    LocationAvailability availability = locationEngine.createLocationAvailability();
    ArrayList<Location> locations = new ArrayList<>();
    locations.add(location);
    final LocationResult result = LocationResult.create(locations);
    ReportedChanges pendingIntentChanges = clientManager.sendPendingIntent(
        context, location, availability, result);

    ReportedChanges callbackChanges = clientManager.reportLocationResult(
        location, result);

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

  /**
   * Checks if any listeners or pending intents are still registered for location updates. If not,
   * then shutdown the location engines.
   */
  private void checkAllListenersPendingIntentsAndCallbacks() {
    if (clientManager.hasNoListeners()) {
      locationEngine.setRequest(null);
    }
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
    clientManager.notifyLocationAvailability(availability);
  }
}
