package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationAvailability;
import com.mapzen.android.lost.api.LocationCallback;
import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LocationResult;
import com.mapzen.android.lost.api.LostApiClient;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresPermission;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static com.mapzen.android.lost.api.FusedLocationProviderApi.KEY_LOCATION_CHANGED;

public class FusedLocationProviderServiceImpl implements LocationEngine.Callback {

  private static final String TAG = FusedLocationProviderServiceImpl.class.getSimpleName();

  private Context context;

  private Map<LostApiClient, Map<LocationListener, LocationRequest>> listenerToRequest;
  private Map<LostApiClient, Map<PendingIntent, LocationRequest>> intentToRequest;
  private Map<LostApiClient, Map<LocationCallback, LocationRequest>> callbackToRequest;
  private Map<LostApiClient, Map<LocationCallback, Looper>> callbackToLooper;
  private Map<LostApiClient, LocationEngine> clientToLocationEngine;
  private Map<LostApiClient, Boolean> clientToMockMode;

  private final ClientManager clientManager = ClientManager.shared();

  private final ClientManagerListener clientManagerListener = new ClientManagerListener() {
    @Override public void onClientAdded(LostApiClient client) {
      LocationEngine locationEngine = new FusionEngine(context,
          FusedLocationProviderServiceImpl.this);
      clientToLocationEngine.put(client, locationEngine);
      clientToMockMode.put(client, false);
    }
  };

  public FusedLocationProviderServiceImpl(Context context) {
    this.context = context;
    clientManager.setListener(clientManagerListener);
    listenerToRequest = new HashMap<>();
    intentToRequest = new HashMap<>();
    callbackToRequest = new HashMap<>();
    callbackToLooper = new HashMap<>();
    clientToLocationEngine = new HashMap<>();
    clientToMockMode = new HashMap<>();
  }

  public void shutdown() {
    for (LocationEngine engine : clientToLocationEngine.values()) {
      engine.setRequest(null);
    }
    listenerToRequest.clear();
    intentToRequest.clear();
    callbackToRequest.clear();
    callbackToLooper.clear();
    clientToLocationEngine.clear();
    clientToMockMode.clear();
  }

  public Location getLastLocation(LostApiClient apiClient) {
    LocationEngine locationEngine = clientToLocationEngine.get(apiClient);
    return locationEngine.getLastLocation();
  }

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  public LocationAvailability getLocationAvailability(LostApiClient apiClient) {
    LocationEngine locationEngine = clientToLocationEngine.get(apiClient);
    return locationEngine.createLocationAvailability();
  }

  public void requestLocationUpdates(LostApiClient apiClient, LocationRequest request,
      LocationListener listener) {
    Map<LocationListener, LocationRequest> map = listenerToRequest.get(apiClient);
    if (map == null) {
      map = new HashMap<>();
    }
    map.put(listener, request);
    listenerToRequest.put(apiClient, map);

    LocationEngine locationEngine = clientToLocationEngine.get(apiClient);
    locationEngine.setRequest(request);
  }

  public void requestLocationUpdates(LostApiClient apiClient, LocationRequest request,
      PendingIntent callbackIntent) {
    Map<PendingIntent, LocationRequest> map = intentToRequest.get(apiClient);
    if (map == null) {
      map = new HashMap<>();
    }
    map.put(callbackIntent, request);
    intentToRequest.put(apiClient, map);

    LocationEngine locationEngine = clientToLocationEngine.get(apiClient);
    locationEngine.setRequest(request);
  }

  public void requestLocationUpdates(LostApiClient apiClient, LocationRequest request,
      LocationCallback callback, Looper looper) {
    Map<LocationCallback, LocationRequest> callbackMap = callbackToRequest.get(apiClient);
    if (callbackMap == null) {
      callbackMap = new HashMap<>();
    }
    callbackMap.put(callback, request);
    callbackToRequest.put(apiClient, callbackMap);

    Map<LocationCallback, Looper> looperMap = callbackToLooper.get(apiClient);
    if (looperMap == null) {
      looperMap = new HashMap<>();
    }
    looperMap.put(callback, looper);
    callbackToLooper.put(apiClient, looperMap);

    LocationEngine locationEngine = clientToLocationEngine.get(apiClient);
    locationEngine.setRequest(request);
  }

  public void removeLocationUpdates(LostApiClient apiClient, LocationListener listener) {
    Map<LocationListener, LocationRequest> map = listenerToRequest.get(apiClient);
    if (map != null) {
      map.remove(listener);
    }
    if (map.isEmpty()) {
      listenerToRequest.remove(apiClient);
    }
    checkAllListenersPendingIntentsAndCallbacks();
  }

  public void removeLocationUpdates(LostApiClient apiClient, PendingIntent callbackIntent) {
    Map<PendingIntent, LocationRequest> map = intentToRequest.get(apiClient);
    if (map != null) {
      map.remove(callbackIntent);
    }
    if (map.isEmpty()) {
      intentToRequest.remove(apiClient);
    }
    checkAllListenersPendingIntentsAndCallbacks();
  }

  public void removeLocationUpdates(LostApiClient apiClient, LocationCallback callback) {
    Map<LocationCallback, LocationRequest> callbackMap = callbackToRequest.get(apiClient);
    if (callbackMap != null) {
      callbackMap.remove(callback);
    }
    if (callbackMap.isEmpty()) {
      callbackToRequest.remove(apiClient);
    }
    Map<LocationCallback, Looper> looperMap = callbackToLooper.get(apiClient);
    if (looperMap != null) {
      looperMap.remove(callback);
    }
    if (looperMap.isEmpty()) {
      callbackToLooper.remove(apiClient);
    }
    checkAllListenersPendingIntentsAndCallbacks();
  }

  public void setMockMode(LostApiClient apiClient, boolean isMockMode) {
    boolean mockMode = isMockMode(apiClient);
    if (mockMode != isMockMode) {
      toggleMockMode(apiClient, isMockMode);
    }
  }

  public void setMockLocation(LostApiClient apiClient, Location mockLocation) {
    if (isMockMode(apiClient)) {
      LocationEngine locationEngine = clientToLocationEngine.get(apiClient);
      ((MockEngine) locationEngine).setLocation(mockLocation);
    }
  }

  public void setMockTrace(LostApiClient apiClient, File file) {
    if (isMockMode(apiClient)) {
      LocationEngine locationEngine = clientToLocationEngine.get(apiClient);
      ((MockEngine) locationEngine).setTrace(file);
    }
  }

  public boolean isProviderEnabled(LostApiClient apiClient, String provider) {
    LocationEngine locationEngine = clientToLocationEngine.get(apiClient);
    return locationEngine.isProviderEnabled(provider);
  }

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  public void reportLocation(Location location) {
    for (LostApiClient client : listenerToRequest.keySet()) {
      if (listenerToRequest.get(client) != null) {
        for (LocationListener listener : listenerToRequest.get(client).keySet()) {
          listener.onLocationChanged(location);
        }
      }
    }

    LocationEngine locationEngine = clientToLocationEngine.values().iterator().next();
    LocationAvailability availability = locationEngine.createLocationAvailability();
    ArrayList<Location> locations = new ArrayList<>();
    locations.add(location);
    final LocationResult result = LocationResult.create(locations);

    for (LostApiClient client : intentToRequest.keySet()) {
      if (intentToRequest.get(client) != null) {
        for (PendingIntent intent : intentToRequest.get(client).keySet()) {
          try {
            Intent toSend = new Intent().putExtra(KEY_LOCATION_CHANGED, location);
            toSend.putExtra(LocationAvailability.EXTRA_LOCATION_AVAILABILITY, availability);
            toSend.putExtra(LocationResult.EXTRA_LOCATION_RESULT, result);
            intent.send(context, 0, toSend);
          } catch (PendingIntent.CanceledException e) {
            Log.e(TAG, "Unable to send pending intent: " + intent);
          }
        }
      }
    }

    for (LostApiClient client : callbackToRequest.keySet()) {
      if (callbackToRequest.get(client) != null) {
        for (final LocationCallback callback : callbackToRequest.get(client).keySet()) {
          Looper looper = callbackToLooper.get(client).get(callback);
          Handler handler = new Handler(looper);
          handler.post(new Runnable() {
            @Override public void run() {
              callback.onLocationResult(result);
            }
          });
        }
      }
    }
  }

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  public void reportProviderDisabled(String provider) {
    for (LostApiClient client : listenerToRequest.keySet()) {
      if (listenerToRequest.get(client) != null) {
        for (LocationListener listener : listenerToRequest.get(client).keySet()) {
          listener.onProviderDisabled(provider);
        }
      }
    }
    notifyLocationAvailabilityChanged();
  }

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  public void reportProviderEnabled(String provider) {
    for (LostApiClient client : listenerToRequest.keySet()) {
      if (listenerToRequest.get(client) != null) {
        for (LocationListener listener : listenerToRequest.get(client).keySet()) {
          listener.onProviderEnabled(provider);
        }
      }
    }
    notifyLocationAvailabilityChanged();
  }

  public Map<LostApiClient, Map<LocationListener, LocationRequest>> getListeners() {
    return listenerToRequest;
  }

  public Map<LostApiClient, Map<PendingIntent, LocationRequest>> getPendingIntents() {
    return intentToRequest;
  }

  public Map<LostApiClient, Map<LocationCallback, Looper>> getLocationListeners() {
    return callbackToLooper;
  }

  public void disconnect(LostApiClient client) {
    listenerToRequest.remove(client);
    intentToRequest.remove(client);
    callbackToRequest.remove(client);
    callbackToLooper.remove(client);
  }

  /**
   * Checks if any listeners or pending intents are still registered for location updates. If not,
   * then shutdown the location engines.
   */
  private void checkAllListenersPendingIntentsAndCallbacks() {
    if (listenerToRequest.isEmpty() && intentToRequest.isEmpty() && callbackToRequest.isEmpty()) {
      for (LocationEngine locationEngine : clientToLocationEngine.values()) {
        locationEngine.setRequest(null);
      }
    }
  }

  private void toggleMockMode(LostApiClient apiClient, boolean isMockMode) {
    clientToMockMode.put(apiClient, isMockMode);

    LocationEngine locationEngine = clientToLocationEngine.get(apiClient);
    locationEngine.setRequest(null);

    if (isMockMode) {
      locationEngine = new MockEngine(context, this);
    } else {
      locationEngine = new FusionEngine(context, this);
    }
    clientToLocationEngine.put(apiClient, locationEngine);
  }

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  private void notifyLocationAvailabilityChanged() {
    LocationEngine locationEngine = clientToLocationEngine.values().iterator().next();
    final LocationAvailability availability = locationEngine.createLocationAvailability();
    for (LostApiClient client : callbackToRequest.keySet()) {
      if (callbackToRequest.get(client) != null) {
        for (final LocationCallback callback : callbackToRequest.get(client).keySet()) {
          Looper looper = callbackToLooper.get(client).get(callback);
          Handler handler = new Handler(looper);
          handler.post(new Runnable() {
            @Override public void run() {
              callback.onLocationAvailability(availability);
            }
          });
        }
      }
    }
  }

  private boolean isMockMode(LostApiClient apiClient) {
    if (!clientToMockMode.containsKey(apiClient)) {
      return false;
    }
    return clientToMockMode.get(apiClient);
  }
}
