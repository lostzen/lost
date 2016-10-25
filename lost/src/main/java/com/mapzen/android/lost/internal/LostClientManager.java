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
import java.util.Set;

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

  private Map<LostApiClient, Set<LocationListener>> clientToListeners;
  private Map<LostApiClient, Set<PendingIntent>> clientToPendingIntents;
  private Map<LostApiClient, Set<LocationCallback>> clientToLocationCallbacks;
  private Map<LostApiClient, Map<LocationCallback, Looper>> clientCallbackToLoopers;


  private LostClientManager() {
    clients = new HashSet<>();

    clientToListeners = new HashMap<>();
    clientToPendingIntents = new HashMap<>();
    clientToLocationCallbacks = new HashMap<>();
    clientCallbackToLoopers = new HashMap<>();
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
    clientToListeners.remove(client);
    clientToPendingIntents.remove(client);
    clientToLocationCallbacks.remove(client);
    clientCallbackToLoopers.remove(client);
  }

  public boolean containsClient(LostApiClient client) {
    return clients.contains(client);
  }

  public int numberOfClients() {
    return clients.size();
  }

  public void addListener(LostApiClient client, LocationRequest request,
      LocationListener listener) {
    Set<LocationListener> listeners = clientToListeners.get(client);
    if (listeners == null) {
      listeners = new HashSet<>();
    }
    listeners.add(listener);
    clientToListeners.put(client, listeners);
  }

  public void addPendingIntent(LostApiClient client, LocationRequest request,
      PendingIntent callbackIntent) {
    Set<PendingIntent> intents = clientToPendingIntents.get(client);
    if (intents == null) {
      intents = new HashSet<>();
    }
    intents.add(callbackIntent);
    clientToPendingIntents.put(client, intents);
  }

  public void addLocationCallback(LostApiClient client, LocationRequest request,
      LocationCallback callback, Looper looper) {
    Set<LocationCallback> callbacks = clientToLocationCallbacks.get(client);
    if (callbacks == null) {
      callbacks = new HashSet<>();
    }
    callbacks.add(callback);
    clientToLocationCallbacks.put(client, callbacks);

    Map<LocationCallback, Looper> looperMap = clientCallbackToLoopers.get(client);
    if (looperMap == null) {
      looperMap = new HashMap<>();
    }
    looperMap.put(callback, looper);
    clientCallbackToLoopers.put(client, looperMap);
  }

  public boolean removeListener(LostApiClient client, LocationListener listener) {
    boolean removedListener = false;
    if (clientToListeners.get(client) != null) {
      removedListener = clientToListeners.get(client).contains(listener);
    }
    Set<LocationListener> listeners = clientToListeners.get(client);
    if (listeners != null) {
      listeners.remove(listener);
      if (listeners.isEmpty()) {
        clientToListeners.remove(client);
      }
    }

    return removedListener;
  }

  public boolean removePendingIntent(LostApiClient client, PendingIntent callbackIntent) {
    boolean removedPendingIntent = false;
    if (clientToPendingIntents.get(client) != null) {
      removedPendingIntent = clientToPendingIntents.get(client).contains(callbackIntent);
    }
    Set<PendingIntent> intents = clientToPendingIntents.get(client);
    if (intents != null) {
      intents.remove(callbackIntent);
      if (intents.isEmpty()) {
        clientToPendingIntents.remove(client);
      }
    }
    return removedPendingIntent;
  }

  public boolean removeLocationCallback(LostApiClient client, LocationCallback callback) {
    boolean removedCallback = false;
    if (clientToLocationCallbacks.get(client) != null) {
      removedCallback = clientToLocationCallbacks.get(client).contains(callback);
    }
    Set<LocationCallback> callbacks = clientToLocationCallbacks.get(client);
    if (callbacks != null) {
      callbacks.remove(callback);
      if (callbacks.isEmpty()) {
        clientToLocationCallbacks.remove(client);
      }
    }

    Map<LocationCallback, Looper> looperMap = clientCallbackToLoopers.get(client);
    if (looperMap != null) {
      looperMap.remove(callback);
      if (looperMap.isEmpty()) {
        clientCallbackToLoopers.remove(client);
      }
    }
    return removedCallback;
  }

  public void reportLocationChanged(Location location) {
    for (LostApiClient client : clientToListeners.keySet()) {
      if (clientToListeners.get(client) != null) {
        for (LocationListener listener : clientToListeners.get(client)) {
          listener.onLocationChanged(location);
        }
      }
    }
  }

  public void sendPendingIntent(Context context, Location location,
      LocationAvailability availability, LocationResult result) {
    for (LostApiClient client : clientToPendingIntents.keySet()) {
      if (clientToPendingIntents.get(client) != null) {
        for (PendingIntent intent : clientToPendingIntents.get(client)) {
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
    for (LostApiClient client : clientToLocationCallbacks.keySet()) {
      if (clientToLocationCallbacks.get(client) != null) {
        for (final LocationCallback callback : clientToLocationCallbacks.get(client)) {
          Looper looper = clientCallbackToLoopers.get(client).get(callback);
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
    for (LostApiClient client : clientToListeners.keySet()) {
      if (clientToListeners.get(client) != null) {
        for (LocationListener listener : clientToListeners.get(client)) {
          listener.onProviderEnabled(provider);
        }
      }
    }
  }

  public void reportProviderDisabled(String provider) {
    for (LostApiClient client : clientToListeners.keySet()) {
      if (clientToListeners.get(client) != null) {
        for (LocationListener listener : clientToListeners.get(client)) {
          listener.onProviderDisabled(provider);
        }
      }
    }
  }

  public void notifyLocationAvailability(final LocationAvailability availability) {
    for (LostApiClient client : clientToLocationCallbacks.keySet()) {
      if (clientToLocationCallbacks.get(client) != null) {
        for (final LocationCallback callback : clientToLocationCallbacks.get(client)) {
          Looper looper = clientCallbackToLoopers.get(client).get(callback);
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
    return clientToListeners.isEmpty() && clientToPendingIntents.isEmpty() &&
        clientToLocationCallbacks.isEmpty();
  }

  public void shutdown() {
    clientToListeners.clear();
    clientToPendingIntents.clear();
    clientToLocationCallbacks.clear();
    clientCallbackToLoopers.clear();
  }

  public Map<LostApiClient, Set<LocationListener>> getLocationListeners() {
    return clientToListeners;
  }

  public Map<LostApiClient, Set<PendingIntent>> getPendingIntents() {
    return clientToPendingIntents;
  }

  public Map<LostApiClient, Set<LocationCallback>> getLocationCallbacks() {
    return clientToLocationCallbacks;
  }
}
