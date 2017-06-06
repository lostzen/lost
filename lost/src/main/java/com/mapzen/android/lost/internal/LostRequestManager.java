package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationCallback;
import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LostApiClient;

import android.app.PendingIntent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Manages requests for {@link ClientCallbackWrapper}s so that the system can properly remove
 * unused requests from the {@link LocationEngine}.
 */
public class LostRequestManager implements RequestManager {

  private static LostRequestManager instance;
  private Map<ClientCallbackWrapper, Set<LocationRequest>> clientCallbackToLocationRequests;

  LostRequestManager() {
    clientCallbackToLocationRequests = new HashMap<>();
  }

  public static LostRequestManager shared() {
    if (instance == null) {
      instance = new LostRequestManager();
    }
    return instance;
  }

  @Override public void requestLocationUpdates(LostApiClient client, LocationRequest request,
      LocationListener listener) {
    ClientCallbackWrapper wrapper = getWrapper(client, listener);
    registerRequest(wrapper, request);
  }

  @Override public void requestLocationUpdates(LostApiClient client, LocationRequest request,
      LocationCallback callback) {
    ClientCallbackWrapper wrapper = getWrapper(client, callback);
    registerRequest(wrapper, request);
  }

  @Override public void requestLocationUpdates(LostApiClient client, LocationRequest request,
      PendingIntent callbackIntent) {
    ClientCallbackWrapper wrapper = getWrapper(client, callbackIntent);
    registerRequest(wrapper, request);
  }

  @Override public Set<LocationRequest> removeLocationUpdates(LostApiClient client, LocationListener listener) {
    ClientCallbackWrapper wrapper = getWrapper(client, listener);
    return getRequestOnlyUsedBy(wrapper);
  }

  @Override public Set<LocationRequest> removeLocationUpdates(LostApiClient client, PendingIntent callbackIntent) {
    ClientCallbackWrapper wrapper = getWrapper(client, callbackIntent);
    return getRequestOnlyUsedBy(wrapper);
  }

  @Override public Set<LocationRequest> removeLocationUpdates(LostApiClient client, LocationCallback callback) {
    ClientCallbackWrapper wrapper = getWrapper(client, callback);
    return getRequestOnlyUsedBy(wrapper);
  }

  private <T> ClientCallbackWrapper getWrapper(LostApiClient client, T callback) {
    return new ClientCallbackWrapper(client, callback);
  }

  private void registerRequest(ClientCallbackWrapper wrapper, LocationRequest request) {
    Set<LocationRequest> requests = clientCallbackToLocationRequests.get(wrapper);
    if (requests == null) {
      requests = new HashSet<>();
      clientCallbackToLocationRequests.put(wrapper, requests);
    }
    requests.add(request);
  }

  private Set<LocationRequest> getRequestOnlyUsedBy(ClientCallbackWrapper wrapper) {
    Set<LocationRequest> requestsToRemove = clientCallbackToLocationRequests.get(wrapper);
    if (requestsToRemove == null) {
      return requestsToRemove;
    }
    clientCallbackToLocationRequests.remove(wrapper);
    for (ClientCallbackWrapper w : clientCallbackToLocationRequests.keySet()) {
      Set<LocationRequest> requests = clientCallbackToLocationRequests.get(w);
      requestsToRemove.removeAll(requests);
    }
    return requestsToRemove;
  }

  Map<ClientCallbackWrapper, Set<LocationRequest>> getClientCallbackMap() {
    return clientCallbackToLocationRequests;
  }
}
