package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.FusedLocationProviderApi;
import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of the {@link FusedLocationProviderApi}.
 */
public class FusedLocationProviderApiImpl
    implements FusedLocationProviderApi, LocationEngine.Callback {

  private static final String TAG = FusedLocationProviderApiImpl.class.getSimpleName();

  private final Context context;
  private boolean mockMode;
  private LocationEngine locationEngine;
  private Map<LocationListener, LocationRequest> listenerToRequest;
  private Map<PendingIntent, LocationRequest> intentToRequest;

  public FusedLocationProviderApiImpl(Context context) {
    this.context = context;
    locationEngine = new FusionEngine(context, this);
    listenerToRequest = new HashMap<>();
    intentToRequest = new HashMap<>();
  }

  @Override public Location getLastLocation() {
    return locationEngine.getLastLocation();
  }

  @Override public void requestLocationUpdates(LocationRequest request, LocationListener listener) {
    listenerToRequest.put(listener, request);
    locationEngine.setRequest(request);
  }

  @Override public void requestLocationUpdates(LocationRequest request, LocationListener listener,
      Looper looper) {
    throw new RuntimeException("Sorry, not yet implemented");
  }

  @Override
  public void requestLocationUpdates(LocationRequest request, PendingIntent callbackIntent) {
    intentToRequest.put(callbackIntent, request);
    locationEngine.setRequest(request);
  }

  @Override public void removeLocationUpdates(LocationListener listener) {
    listenerToRequest.remove(listener);
    checkAllListenersAndPendingIntents();
  }

  @Override public void removeLocationUpdates(PendingIntent callbackIntent) {
    intentToRequest.remove(callbackIntent);
    checkAllListenersAndPendingIntents();
  }

  /**
   * Checks if any listeners or pending intents are still registered for location updates. If not,
   * then shutdown the location engine.
   */
  private void checkAllListenersAndPendingIntents() {
    if (listenerToRequest.isEmpty() && intentToRequest.isEmpty()) {
      locationEngine.setRequest(null);
    }
  }

  @Override public void setMockMode(boolean isMockMode) {
    if (mockMode != isMockMode) {
      toggleMockMode();
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

  @Override public void setMockLocation(Location mockLocation) {
    if (mockMode) {
      ((MockEngine) locationEngine).setLocation(mockLocation);
    }
  }

  @Override public void setMockTrace(File file) {
    if (mockMode) {
      ((MockEngine) locationEngine).setTrace(file);
    }
  }

  @Override public boolean isProviderEnabled(String provider) {
    return locationEngine.isProviderEnabled(provider);
  }

  @Override public void reportLocation(Location location) {
    for (LocationListener listener : listenerToRequest.keySet()) {
      listener.onLocationChanged(location);
    }

    for (PendingIntent intent : intentToRequest.keySet()) {
      try {
        intent.send(context, 0, new Intent().putExtra(KEY_LOCATION_CHANGED, location));
      } catch (PendingIntent.CanceledException e) {
        Log.e(TAG, "Unable to send pending intent: " + intent);
      }
    }
  }

  @Override public void reportProviderDisabled(String provider) {
    for (LocationListener listener : listenerToRequest.keySet()) {
      listener.onProviderDisabled(provider);
    }
  }

  @Override public void reportProviderEnabled(String provider) {
    for (LocationListener listener : listenerToRequest.keySet()) {
      listener.onProviderEnabled(provider);
    }
  }

  public void shutdown() {
    listenerToRequest.clear();
    locationEngine.setRequest(null);
  }

  public Map<LocationListener, LocationRequest> getListeners() {
    return listenerToRequest;
  }
}
