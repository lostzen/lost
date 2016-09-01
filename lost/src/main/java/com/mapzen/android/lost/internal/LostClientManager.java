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
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static com.mapzen.android.lost.api.FusedLocationProviderApi.KEY_LOCATION_CHANGED;

/**
 * Used by {@link LostApiClientImpl} to manage connected clients and by
 * {@link FusedLocationProviderServiceImpl} to manage client's {@link LocationListener}s,
 * {@link PendingIntent}s, and {@link LocationCallback}s.
 */
public class LostClientManager implements ClientManager {
  private static final String TAG = ClientManager.class.getSimpleName();

  private static LostClientManager instance;
  private HashSet<LostApiClient> clients;

  private Map<LostApiClient, Map<LocationListener, LocationRequest>> listenerToRequest;
  private Map<LostApiClient, Map<PendingIntent, LocationRequest>> intentToRequest;
  private Map<LostApiClient, Map<LocationCallback, LocationRequest>> callbackToRequest;
  private Map<LostApiClient, Map<LocationCallback, Looper>> callbackToLooper;


  private LostClientManager() {
    clients = new HashSet<>();

    listenerToRequest = new HashMap<>();
    intentToRequest = new HashMap<>();
    callbackToRequest = new HashMap<>();
    callbackToLooper = new HashMap<>();
  }

  public static LostClientManager shared() {
    if (instance == null) {
      instance = new LostClientManager();
    }
    return instance;
  }

  public void addClient(LostApiClient client) {
    clients.add(client);
  }

  public void removeClient(LostApiClient client) {
    clients.remove(client);
  }

  public int numberOfClients() {
    return clients.size();
  }

  public void addListener(LostApiClient client, LocationRequest request,
      LocationListener listener) {
    Map<LocationListener, LocationRequest> map = listenerToRequest.get(client);
    if (map == null) {
      map = new HashMap<>();
    }
    map.put(listener, request);
    listenerToRequest.put(client, map);
  }

  public void addPendingIntent(LostApiClient client, LocationRequest request,
      PendingIntent callbackIntent) {
    Map<PendingIntent, LocationRequest> map = intentToRequest.get(client);
    if (map == null) {
      map = new HashMap<>();
    }
    map.put(callbackIntent, request);
    intentToRequest.put(client, map);
  }

  public void addLocationCallback(LostApiClient client, LocationRequest request,
      LocationCallback callback, Looper looper) {
    Map<LocationCallback, LocationRequest> callbackMap = callbackToRequest.get(client);
    if (callbackMap == null) {
      callbackMap = new HashMap<>();
    }
    callbackMap.put(callback, request);
    callbackToRequest.put(client, callbackMap);

    Map<LocationCallback, Looper> looperMap = callbackToLooper.get(client);
    if (looperMap == null) {
      looperMap = new HashMap<>();
    }
    looperMap.put(callback, looper);
    callbackToLooper.put(client, looperMap);
  }

  public void removeListener(LostApiClient client, LocationListener listener) {
    Map<LocationListener, LocationRequest> map = listenerToRequest.get(client);
    if (map != null) {
      map.remove(listener);
    }
    if (map.isEmpty()) {
      listenerToRequest.remove(client);
    }
  }

  public void removePendingIntent(LostApiClient client, PendingIntent callbackIntent) {
    Map<PendingIntent, LocationRequest> map = intentToRequest.get(client);
    if (map != null) {
      map.remove(callbackIntent);
    }
    if (map.isEmpty()) {
      intentToRequest.remove(client);
    }
  }

  public void removeLocationCallback(LostApiClient client, LocationCallback callback) {
    Map<LocationCallback, LocationRequest> callbackMap = callbackToRequest.get(client);
    if (callbackMap != null) {
      callbackMap.remove(callback);
    }
    if (callbackMap.isEmpty()) {
      callbackToRequest.remove(client);
    }
    Map<LocationCallback, Looper> looperMap = callbackToLooper.get(client);
    if (looperMap != null) {
      looperMap.remove(callback);
    }
    if (looperMap.isEmpty()) {
      callbackToLooper.remove(client);
    }
  }

  public void reportLocationChanged(Location location) {
    for (LostApiClient client : listenerToRequest.keySet()) {
      if (listenerToRequest.get(client) != null) {
        for (LocationListener listener : listenerToRequest.get(client).keySet()) {
          listener.onLocationChanged(location);
        }
      }
    }
  }

  public void sendPendingIntent(Context context, Location location,
      LocationAvailability availability, LocationResult result) {
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
  }

  public void reportLocationResult(final LocationResult result) {
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

  public void reportProviderEnabled(String provider) {
    for (LostApiClient client : listenerToRequest.keySet()) {
      if (listenerToRequest.get(client) != null) {
        for (LocationListener listener : listenerToRequest.get(client).keySet()) {
          listener.onProviderEnabled(provider);
        }
      }
    }
  }

  public void reportProviderDisabled(String provider) {
    for (LostApiClient client : listenerToRequest.keySet()) {
      if (listenerToRequest.get(client) != null) {
        for (LocationListener listener : listenerToRequest.get(client).keySet()) {
          listener.onProviderDisabled(provider);
        }
      }
    }
  }

  public void notifyLocationAvailability(final LocationAvailability availability) {
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

  public boolean hasNoListeners() {
    return listenerToRequest.isEmpty() && intentToRequest.isEmpty() && callbackToRequest.isEmpty();
  }

  public void disconnect(LostApiClient client) {
    listenerToRequest.remove(client);
    intentToRequest.remove(client);
    callbackToRequest.remove(client);
    callbackToLooper.remove(client);
  }

  public void shutdown() {
    listenerToRequest.clear();
    intentToRequest.clear();
    callbackToRequest.clear();
    callbackToLooper.clear();
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
}
