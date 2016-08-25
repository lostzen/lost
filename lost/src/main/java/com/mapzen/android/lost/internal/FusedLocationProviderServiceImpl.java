package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationAvailability;
import com.mapzen.android.lost.api.LocationCallback;
import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LocationResult;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
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
  private Map<LocationCallback, LocationRequest> callbackToRequest;
  private Map<LocationCallback, Looper> callbackToLooper;

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

  public Location getLastLocation() {
    return locationEngine.getLastLocation();
  }

  public LocationAvailability getLocationAvailability() {
    return createLocationAvailability();
  }

  public void requestLocationUpdates(LocationRequest request, LocationListener listener) {
    listenerToRequest.put(listener, request);
    locationEngine.setRequest(request);
  }

  public void requestLocationUpdates(LocationRequest request, PendingIntent callbackIntent) {
    intentToRequest.put(callbackIntent, request);
    locationEngine.setRequest(request);
  }

  public void requestLocationUpdates(LocationRequest request, LocationCallback callback,
      Looper looper) {
    callbackToRequest.put(callback, request);
    callbackToLooper.put(callback, looper);
    locationEngine.setRequest(request);
  }

  public void removeLocationUpdates(LocationListener listener) {
    listenerToRequest.remove(listener);
    checkAllListenersPendingIntentsAndCallbacks();
  }

  public void removeLocationUpdates(PendingIntent callbackIntent) {
    intentToRequest.remove(callbackIntent);
    checkAllListenersPendingIntentsAndCallbacks();
  }

  public void removeLocationUpdates(LocationCallback callback) {
    callbackToRequest.remove(callback);
    callbackToLooper.remove(callback);
    checkAllListenersPendingIntentsAndCallbacks();
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

    LocationAvailability availability = createLocationAvailability();
    ArrayList<Location> locations = new ArrayList<>();
    locations.add(location);
    final LocationResult result = LocationResult.create(locations);

    for (PendingIntent intent : intentToRequest.keySet()) {
      try {
        Intent toSend = new Intent().putExtra(KEY_LOCATION_CHANGED, location);
        toSend.putExtra(LocationAvailability.EXTRA_LOCATION_AVAILABILITY, availability);
        toSend.putExtra(LocationResult.EXTRA_LOCATION_RESULT, result);
        intent.send(context, 0, toSend);
      } catch (PendingIntent.CanceledException e) {
        Log.e(TAG, "Unable to send pending intent: " + intent);
      }
    }

    for (final LocationCallback callback : callbackToRequest.keySet()) {
      Looper looper = callbackToLooper.get(callback);
      Handler handler = new Handler(looper);
      handler.post(new Runnable() {
          @Override public void run() {
              callback.onLocationResult(result);
            }
        });
    }
  }

  public void reportProviderDisabled(String provider) {
    for (LocationListener listener : listenerToRequest.keySet()) {
      listener.onProviderDisabled(provider);
    }
    notifyLocationAvailabilityChanged();
  }

  public void reportProviderEnabled(String provider) {
    for (LocationListener listener : listenerToRequest.keySet()) {
      listener.onProviderEnabled(provider);
    }
    notifyLocationAvailabilityChanged();
  }

  public Map<LocationListener, LocationRequest> getListeners() {
    return listenerToRequest;
  }

  public Map<PendingIntent, LocationRequest> getPendingIntents() {
    return intentToRequest;
  }

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

  private void notifyLocationAvailabilityChanged() {
    final LocationAvailability availability = createLocationAvailability();
    for (final LocationCallback callback : callbackToRequest.keySet()) {
      Looper looper = callbackToLooper.get(callback);
      Handler handler = new Handler(looper);
      handler.post(new Runnable() {
        @Override public void run() {
          callback.onLocationAvailability(availability);
        }
      });
    }
  }
}
