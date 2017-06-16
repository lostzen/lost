package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationCallback;
import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LostApiClient;

import android.app.PendingIntent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages requests for {@link ClientCallbackWrapper}s so that the system can properly remove
 * unused requests from the {@link LocationEngine}.
 */
public class LostRequestManager implements RequestManager {

  private static LostRequestManager instance;
  private Map<ClientCallbackWrapper, List<LocationRequest>> clientCallbackToLocationRequests;

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

  @Override public List<LocationRequest> removeLocationUpdates(LostApiClient client,
      LocationListener listener) {
    ClientCallbackWrapper wrapper = getWrapper(client, listener);
    return getRequestOnlyUsedBy(wrapper);
  }

  @Override public List<LocationRequest> removeLocationUpdates(LostApiClient client,
      PendingIntent callbackIntent) {
    ClientCallbackWrapper wrapper = getWrapper(client, callbackIntent);
    return getRequestOnlyUsedBy(wrapper);
  }

  @Override public List<LocationRequest> removeLocationUpdates(LostApiClient client,
      LocationCallback callback) {
    ClientCallbackWrapper wrapper = getWrapper(client, callback);
    return getRequestOnlyUsedBy(wrapper);
  }

  private <T> ClientCallbackWrapper getWrapper(LostApiClient client, T callback) {
    return new ClientCallbackWrapper(client, callback);
  }

  private void registerRequest(ClientCallbackWrapper wrapper, LocationRequest request) {
    List<LocationRequest> requests = clientCallbackToLocationRequests.get(wrapper);
    if (requests == null) {
      requests = new ArrayList();
      clientCallbackToLocationRequests.put(wrapper, requests);
    }
    LocationRequest r = new LocationRequest(request);
    requests.add(r);
  }

  private List<LocationRequest> getRequestOnlyUsedBy(ClientCallbackWrapper wrapper) {
    List<LocationRequest> requestsToRemove = clientCallbackToLocationRequests.get(wrapper);
    if (requestsToRemove == null) {
      return null;
    }
    clientCallbackToLocationRequests.remove(wrapper);
    return requestsToRemove;
  }

  Map<ClientCallbackWrapper, List<LocationRequest>> getClientCallbackMap() {
    return clientCallbackToLocationRequests;
  }
}
