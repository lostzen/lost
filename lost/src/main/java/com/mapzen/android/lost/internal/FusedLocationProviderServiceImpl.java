package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationAvailability;
import com.mapzen.android.lost.api.LocationCallback;
import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LocationResult;
import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.android.lost.api.PendingResult;
import com.mapzen.android.lost.api.Status;

import android.app.PendingIntent;
import android.content.Context;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.RequiresPermission;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class FusedLocationProviderServiceImpl implements LocationEngine.Callback {

  private Context context;

  private boolean mockMode;
  private LocationEngine locationEngine;

  private ClientManager clientManager;

  public FusedLocationProviderServiceImpl(Context context, ClientManager manager) {
    this.context = context;
    this.clientManager = manager;
    locationEngine = new FusionEngine(context, this);
  }

  public void shutdown() {
    locationEngine.setRequest(null);
    clientManager.shutdown();
  }

  public Location getLastLocation(LostApiClient client) {
    return locationEngine.getLastLocation();
  }

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  public LocationAvailability getLocationAvailability(LostApiClient client) {
    return locationEngine.createLocationAvailability();
  }

  public PendingResult<Status> requestLocationUpdates(LostApiClient client, LocationRequest request,
      LocationListener listener) {
    clientManager.addListener(client, request, listener);
    locationEngine.setRequest(request);
    return new FusedLocationPendingResult();
  }

  public PendingResult<Status> requestLocationUpdates(LostApiClient client, LocationRequest request,
      PendingIntent callbackIntent) {
    clientManager.addPendingIntent(client, request, callbackIntent);
    locationEngine.setRequest(request);
    return new FusedLocationPendingResult();
  }

  public PendingResult<Status> requestLocationUpdates(LostApiClient client, LocationRequest request,
      LocationCallback callback, Looper looper) {
    clientManager.addLocationCallback(client, request, callback, looper);
    locationEngine.setRequest(request);
    return new FusedLocationPendingResult();
  }

  public PendingResult<Status> removeLocationUpdates(LostApiClient client,
      LocationListener listener) {
    clientManager.removeListener(client, listener);
    checkAllListenersPendingIntentsAndCallbacks();
    return new FusedLocationPendingResult();
  }

  public PendingResult<Status> removeLocationUpdates(LostApiClient client,
      PendingIntent callbackIntent) {
    clientManager.removePendingIntent(client, callbackIntent);
    checkAllListenersPendingIntentsAndCallbacks();
    return new FusedLocationPendingResult();
  }

  public PendingResult<Status> removeLocationUpdates(LostApiClient client,
      LocationCallback callback) {
    clientManager.removeLocationCallback(client, callback);
    checkAllListenersPendingIntentsAndCallbacks();
    return new FusedLocationPendingResult();
  }

  public PendingResult<Status> setMockMode(LostApiClient client, boolean isMockMode) {
    if (mockMode != isMockMode) {
      toggleMockMode();
    }
    return new FusedLocationPendingResult();
  }

  public PendingResult<Status> setMockLocation(LostApiClient client, Location mockLocation) {
    if (mockMode) {
      ((MockEngine) locationEngine).setLocation(mockLocation);
    }
    return new FusedLocationPendingResult();
  }

  public PendingResult<Status> setMockTrace(LostApiClient client, File file) {
    if (mockMode) {
      ((MockEngine) locationEngine).setTrace(file);
    }
    return new FusedLocationPendingResult();
  }

  public boolean isProviderEnabled(LostApiClient client, String provider) {
    return locationEngine.isProviderEnabled(provider);
  }

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  public void reportLocation(Location location) {
    clientManager.reportLocationChanged(location);

    LocationAvailability availability = locationEngine.createLocationAvailability();
    ArrayList<Location> locations = new ArrayList<>();
    locations.add(location);
    final LocationResult result = LocationResult.create(locations);
    clientManager.sendPendingIntent(context, location, availability, result);


    clientManager.reportLocationResult(result);
  }

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  public void reportProviderDisabled(String provider) {
    clientManager.reportProviderDisabled(provider);
    notifyLocationAvailabilityChanged();
  }

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  public void reportProviderEnabled(String provider) {
    clientManager.reportProviderEnabled(provider);
    notifyLocationAvailabilityChanged();
  }

  public Map<LostApiClient, Set<LocationListener>> getLocationListeners() {
    return clientManager.getLocationListeners();
  }

  public Map<LostApiClient, Set<PendingIntent>> getPendingIntents() {
    return clientManager.getPendingIntents();
  }

  public Map<LostApiClient, Set<LocationCallback>> getLocationCallbacks() {
    return clientManager.getLocationCallbacks();
  }

  public void disconnect(LostApiClient client) {
    clientManager.disconnect(client);
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
      locationEngine = new MockEngine(context, this);
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
