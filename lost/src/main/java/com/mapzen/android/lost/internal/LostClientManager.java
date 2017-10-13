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
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.mapzen.android.lost.api.FusedLocationProviderApi.KEY_LOCATION_CHANGED;
import static com.mapzen.android.lost.internal.Clock.MS_TO_NS;

/**
 * Used by {@link LostApiClientImpl} to manage connected clients and by
 * {@link FusedLocationProviderApiImpl} to manage client's {@link LocationListener}s,
 * {@link PendingIntent}s, and {@link LocationCallback}s.
 */
public class LostClientManager implements ClientManager {
  private static final String TAG = ClientManager.class.getSimpleName();

  private static LostClientManager instance;
  private final Clock clock;
  private final HandlerFactory handlerFactory;

  private HashMap<LostApiClient, LostClientWrapper> clients;
  private Map<LocationListener, LocationRequestReport> listenerToReports;
  private Map<PendingIntent, LocationRequestReport> intentToReports;
  private Map<LocationCallback, LocationRequestReport> callbackToReports;

  LostClientManager(Clock clock, HandlerFactory handlerFactory) {
    this.clock = clock;
    this.handlerFactory = handlerFactory;

    clients = new HashMap<>();
    listenerToReports = new HashMap<>();
    intentToReports = new HashMap<>();
    callbackToReports = new HashMap<>();
  }

  public static LostClientManager shared() {
    if (instance == null) {
      instance = new LostClientManager(new SystemClock(), new AndroidHandlerFactory());
    }
    return instance;
  }

  @Override public void addClient(LostApiClient client) {
    clients.put(client, new LostClientWrapper(client));
  }

  @Override public void removeClient(LostApiClient client) {
    clients.remove(client);
  }

  @Override public boolean containsClient(LostApiClient client) {
    return clients.containsKey(client);
  }

  @Override public int numberOfClients() {
    return clients.size();
  }

  @Override public void addListener(LostApiClient client, LocationRequest request,
      LocationListener listener) {
    throwIfClientNotAdded(client);
    clients.get(client).locationListeners().add(listener);
    listenerToReports.put(listener, new LocationRequestReport(request));
  }

  @Override public void addPendingIntent(LostApiClient client, LocationRequest request,
      PendingIntent callbackIntent) {
    throwIfClientNotAdded(client);
    clients.get(client).pendingIntents().add(callbackIntent);
    intentToReports.put(callbackIntent, new LocationRequestReport(request));
  }

  @Override public void addLocationCallback(LostApiClient client, LocationRequest request,
      LocationCallback callback, Looper looper) {
    throwIfClientNotAdded(client);
    clients.get(client).locationCallbacks().add(callback);
    clients.get(client).looperMap().put(callback, looper);
    callbackToReports.put(callback, new LocationRequestReport(request));
  }

  private void throwIfClientNotAdded(LostApiClient client) {
    if (clients.get(client) == null) {
      throw new IllegalArgumentException("Client must be added before requesting location updates");
    }
  }

  @Override public boolean removeListener(LostApiClient client, LocationListener listener) {
    final Set<LocationListener> listeners = clients.get(client).locationListeners();
    boolean removed = false;

    if (listeners.contains(listener)) {
      listeners.remove(listener);
      removed = true;
    }

    listenerToReports.remove(listener);
    return removed;
  }

  @Override public boolean removePendingIntent(LostApiClient client, PendingIntent callbackIntent) {
    final Set<PendingIntent> pendingIntents = clients.get(client).pendingIntents();
    boolean removed = false;

    if (pendingIntents.contains(callbackIntent)) {
      pendingIntents.remove(callbackIntent);
      removed = true;
    }

    intentToReports.remove(callbackIntent);
    return removed;
  }

  @Override public boolean removeLocationCallback(LostApiClient client, LocationCallback callback) {
    final Set<LocationCallback> callbacks = clients.get(client).locationCallbacks();
    boolean removed = false;

    if (callbacks.contains(callback)) {
      callbacks.remove(callback);
      removed = true;
    }

    clients.get(client).looperMap().remove(callback);
    callbackToReports.remove(callback);
    return removed;
  }

  /**
   * Reports location changed for all listeners respecting their corresponding
   * {@link LocationRequest#getFastestInterval()} and
   * {@link LocationRequest#getSmallestDisplacement()} values. Returns a map of the updated
   * last reported times so that {@code LostClientManager#requestToLastReportedTime} after
   * all reporting (including pending intents and location callbacks) has been done.
   * @param location
   * @return
   */
  @Override public void reportLocationChanged(final Location location) {
    iterateAndNotify(location, getLocationListeners(), listenerToReports,
        new Notifier<LocationListener>() {
          @Override public void notify(LostApiClient client, LocationListener listener) {
            listener.onLocationChanged(location);
          }
        });
  }

  /**
   * Fires intent for all pending intents respecting their corresponding
   * {@link LocationRequest#getFastestInterval()} and
   * {@link LocationRequest#getSmallestDisplacement()} values. Returns a map of the updated
   * last reported times so that {@code LostClientManager#requestToLastReportedTime} after
   * all reporting (including pending intents and location callbacks) has been done.
   * @param location
   * @return
   */
  @Override public void sendPendingIntent(final Context context,
      final Location location, final LocationAvailability availability,
      final LocationResult result) {
    iterateAndNotify(location,
        getPendingIntents(), intentToReports, new Notifier<PendingIntent>() {
          @Override public void notify(LostApiClient client, PendingIntent pendingIntent) {
            fireIntent(context, pendingIntent, location, availability, result);
          }
        });
  }

  @Override public void reportLocationResult(Location location,
      final LocationResult result) {
    iterateAndNotify(location,
        getLocationCallbacks(), callbackToReports, new Notifier<LocationCallback>() {
          @Override public void notify(LostApiClient client, LocationCallback callback) {
            notifyCallback(client, callback, result);
          }
        });
  }

  @Override public void notifyLocationAvailability(final LocationAvailability availability) {
    for (LostClientWrapper wrapper : clients.values()) {
      for (LocationCallback callback : wrapper.locationCallbacks()) {
        notifyAvailability(wrapper.client(), callback, availability);
      }
    }
  }

  @Override public boolean hasNoListeners() {
    for (LostClientWrapper wrapper : clients.values()) {
      if (!wrapper.locationListeners().isEmpty()
          || !wrapper.pendingIntents().isEmpty()
          || !wrapper.locationCallbacks().isEmpty()) {
        return false;
      }
    }

    return true;
  }

  @VisibleForTesting void clearClients() {
    clients.clear();
  }

  @Override public Map<LostApiClient, Set<LocationListener>> getLocationListeners() {
    final Map<LostApiClient, Set<LocationListener>> clientToListeners = new HashMap<>();
    for (LostApiClient client : clients.keySet()) {
      clientToListeners.put(client, clients.get(client).locationListeners());
    }

    return clientToListeners;
  }

  @Override public Map<LostApiClient, Set<PendingIntent>> getPendingIntents() {
    final Map<LostApiClient, Set<PendingIntent>> clientToPendingIntents = new HashMap<>();
    for (LostApiClient client : clients.keySet()) {
      clientToPendingIntents.put(client, clients.get(client).pendingIntents());
    }

    return clientToPendingIntents;
  }

  @Override public Map<LostApiClient, Set<LocationCallback>> getLocationCallbacks() {
    final Map<LostApiClient, Set<LocationCallback>> clientToLocationCallbacks = new HashMap<>();
    for (LostApiClient client : clients.keySet()) {
      clientToLocationCallbacks.put(client, clients.get(client).locationCallbacks());
    }
    return clientToLocationCallbacks;
  }

  private void fireIntent(Context context, PendingIntent intent, Location location,
      LocationAvailability availability, LocationResult result) {
    try {
      Intent toSend = new Intent().putExtra(KEY_LOCATION_CHANGED, location);
      toSend.putExtra(LocationAvailability.EXTRA_LOCATION_AVAILABILITY, availability);
      toSend.putExtra(LocationResult.EXTRA_LOCATION_RESULT, result);
      intent.send(context, 0, toSend);
    } catch (PendingIntent.CanceledException e) {
      Log.e(TAG, "Unable to send pending intent: " + intent);
    }
  }

  private void notifyCallback(LostApiClient client, final LocationCallback callback,
      final LocationResult result) {
    final Looper looper = clients.get(client).looperMap().get(callback);
    handlerFactory.run(looper, new Runnable() {
      @Override public void run() {
        callback.onLocationResult(result);
      }
    });
  }

  private void notifyAvailability(LostApiClient client, final LocationCallback callback,
      final LocationAvailability availability) {
    final Looper looper = clients.get(client).looperMap().get(callback);
    final Handler handler = new Handler(looper);
    handler.post(new Runnable() {
      @Override public void run() {
        callback.onLocationAvailability(availability);
      }
    });
  }

  private <T> void iterateAndNotify(Location location,
      Map<LostApiClient, Set<T>> clientToObj, Map<T, LocationRequestReport> objToLocationRequest,
      Notifier notifier) {
    for (LostApiClient client : clientToObj.keySet()) {
      if (clientToObj.get(client) != null) {
        for (final T obj : clientToObj.get(client)) {
          LocationRequestReport report = objToLocationRequest.get(obj);
          LocationRequest request = report.locationRequest;
          Location lastReportedLocation = report.location;
          if (lastReportedLocation == null) {
            report.location = location;
            notifier.notify(client, obj);
          } else {
            long lastReportedTime = clock.getElapsedTimeInNanos(lastReportedLocation);
            long locationTime = clock.getElapsedTimeInNanos(location);
            long intervalSinceLastReport = (locationTime - lastReportedTime) / MS_TO_NS;
            long fastestInterval = request.getFastestInterval();
            float smallestDisplacement = request.getSmallestDisplacement();
            float displacementSinceLastReport = location.distanceTo(lastReportedLocation);
            if (intervalSinceLastReport >= fastestInterval &&
                displacementSinceLastReport >= smallestDisplacement) {
              report.location = location;
              notifier.notify(client, obj);
            }
          }
        }
      }
    }
  }

  interface Notifier<T> {
    void notify(LostApiClient client, T obj);
  }
}
