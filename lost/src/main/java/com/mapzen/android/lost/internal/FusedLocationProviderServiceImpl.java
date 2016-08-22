package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static com.mapzen.android.lost.api.FusedLocationProviderApi.KEY_LOCATION_CHANGED;

public class FusedLocationProviderServiceImpl implements LocationEngine.Callback {

  private static final String TAG = FusedLocationProviderServiceImpl.class.getSimpleName();

  private Context context;

  private boolean mockMode;
  private LocationEngine locationEngine;
  private Map<LocationListener, LocationRequest> listenerToRequest;
  private Map<PendingIntent, LocationRequest> intentToRequest;

  public FusedLocationProviderServiceImpl(Context context) {
    this.context = context;
    locationEngine = new FusionEngine(context, this);
    listenerToRequest = new HashMap<>();
    intentToRequest = new HashMap<>();
  }

  public void shutdown() {
    listenerToRequest.clear();
    intentToRequest.clear();
    locationEngine.setRequest(null);
  }

  public Location getLastLocation() {
    return locationEngine.getLastLocation();
  }

  public void requestLocationUpdates(LocationRequest request, LocationListener listener) {
    listenerToRequest.put(listener, request);
    locationEngine.setRequest(request);
  }

  public void requestLocationUpdates(LocationRequest request, PendingIntent callbackIntent) {
    intentToRequest.put(callbackIntent, request);
    locationEngine.setRequest(request);
  }

  public void removeLocationUpdates(LocationListener listener) {
    listenerToRequest.remove(listener);
    checkAllListenersAndPendingIntents();
  }

  public void removeLocationUpdates(PendingIntent callbackIntent) {
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

  public void setMockMode(boolean isMockMode) {
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

  public boolean isProviderEnabled(String provider) {
    return locationEngine.isProviderEnabled(provider);
  }

  public void reportLocation(Location location) {
    for (LocationListener listener : listenerToRequest.keySet()) {
      listener.onLocationChanged(location);
    }

    for (PendingIntent intent : intentToRequest.keySet()) {
      try {
        intent.send(context, 0, new Intent().putExtra(
            KEY_LOCATION_CHANGED, location));
      } catch (PendingIntent.CanceledException e) {
        Log.e(TAG, "Unable to send pending intent: " + intent);
      }
    }
  }

  public void reportProviderDisabled(String provider) {
    for (LocationListener listener : listenerToRequest.keySet()) {
      listener.onProviderDisabled(provider);
    }
  }

  public void reportProviderEnabled(String provider) {
    for (LocationListener listener : listenerToRequest.keySet()) {
      listener.onProviderEnabled(provider);
    }
  }

  public Map<LocationListener, LocationRequest> getListeners() {
    return listenerToRequest;
  }

  public Map<PendingIntent, LocationRequest> getPendingIntents() {
    return intentToRequest;
  }
}
