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

  private boolean mockMode;
  private LocationEngine locationEngine;
  private Map<LostApiClient, Map<LocationListener, LocationRequest>> listenerToRequest;
  private Map<LostApiClient, Map<PendingIntent, LocationRequest>> intentToRequest;
  private Map<LostApiClient, Map<LocationCallback, LocationRequest>> callbackToRequest;
  private Map<LostApiClient, Map<LocationCallback, Looper>> callbackToLooper;

  public FusedLocationProviderServiceImpl(Context context) {
    this.context = context;
    locationEngine = new FusionEngine(context, this);
    listenerToRequest = new HashMap<>();
    intentToRequest = new HashMap<>();
    callbackToRequest = new HashMap<>();
    callbackToLooper = new HashMap<>();
  }

  public void shutdown() {
    listenerToRequest.clear();
    intentToRequest.clear();
    callbackToRequest.clear();
    callbackToLooper.clear();
    locationEngine.setRequest(null);
  }

  public Location getLastLocation(LostApiClient apiClient) {
    return locationEngine.getLastLocation();
  }

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  public LocationAvailability getLocationAvailability(LostApiClient apiClient) {
    return createLocationAvailability();
  }

  public void requestLocationUpdates(LostApiClient apiClient, LocationRequest request,
      LocationListener listener) {
    Map<LocationListener, LocationRequest> map = listenerToRequest.get(apiClient);
    if (map == null) {
      map = new HashMap<>();
    }
    map.put(listener, request);
    listenerToRequest.put(apiClient, map);
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
    if (mockMode != isMockMode) {
      toggleMockMode();
    }
  }

  public void setMockLocation(LostApiClient apiClient, Location mockLocation) {
    if (mockMode) {
      ((MockEngine) locationEngine).setLocation(mockLocation);
    }
  }

  public void setMockTrace(LostApiClient apiClient, File file) {
    if (mockMode) {
      ((MockEngine) locationEngine).setTrace(file);
    }
  }

  public boolean isProviderEnabled(LostApiClient apiClient, String provider) {
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

    LocationAvailability availability = createLocationAvailability();
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

  /**
   * Checks if any listeners or pending intents are still registered for location updates. If not,
   * then shutdown the location engine.
   */
  private void checkAllListenersPendingIntentsAndCallbacks() {
    if (listenerToRequest.isEmpty() && intentToRequest.isEmpty() && callbackToRequest.isEmpty()) {
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
  private LocationAvailability createLocationAvailability() {
    LocationManager locationManager = (LocationManager) context.getSystemService(
        Context.LOCATION_SERVICE);
    boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    boolean gpsLocationExists = locationManager.getLastKnownLocation(
        LocationManager.GPS_PROVIDER) != null;
    boolean networkLocationExists = locationManager.getLastKnownLocation(
        LocationManager.NETWORK_PROVIDER) != null;
    return new LocationAvailability((gpsEnabled && gpsLocationExists)
        || (networkEnabled && networkLocationExists));
  }

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  private void notifyLocationAvailabilityChanged() {
    final LocationAvailability availability = createLocationAvailability();
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

  public void disconnect(LostApiClient client) {
    listenerToRequest.remove(client);
    intentToRequest.remove(client);
    callbackToRequest.remove(client);
    callbackToLooper.remove(client);
  }
}
