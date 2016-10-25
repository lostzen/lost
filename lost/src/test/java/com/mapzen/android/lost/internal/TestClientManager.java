package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationAvailability;
import com.mapzen.android.lost.api.LocationCallback;
import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LocationResult;
import com.mapzen.android.lost.api.LostApiClient;

import android.app.PendingIntent;
import android.content.Context;
import android.location.Location;
import android.os.Looper;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TestClientManager implements ClientManager {

  private HashSet<LostApiClient> clients = new HashSet<>();

  @Override public void addClient(LostApiClient client) {
    clients.add(client);
  }

  @Override public void removeClient(LostApiClient client) {
    clients.remove(client);
  }

  @Override public boolean containsClient(LostApiClient client) {
    return clients.contains(client);
  }

  @Override public int numberOfClients() {
    return clients.size();
  }

  @Override public void addListener(LostApiClient client, LocationRequest request,
      LocationListener listener) {

  }

  @Override public void addPendingIntent(LostApiClient client, LocationRequest request,
      PendingIntent callbackIntent) {

  }

  @Override public void addLocationCallback(LostApiClient client, LocationRequest request,
      LocationCallback callback, Looper looper) {

  }

  @Override public boolean removeListener(LostApiClient client, LocationListener listener) {
    return false;
  }

  @Override public boolean removePendingIntent(LostApiClient client, PendingIntent callbackIntent) {
    return false;
  }

  @Override public boolean removeLocationCallback(LostApiClient client, LocationCallback callback) {
    return false;
  }

  @Override public void reportLocationChanged(Location location) {

  }

  @Override public void sendPendingIntent(Context context, Location location,
      LocationAvailability availability, LocationResult result) {

  }

  @Override public void reportLocationResult(LocationResult result) {

  }

  @Override public void reportProviderEnabled(String provider) {

  }

  @Override public void reportProviderDisabled(String provider) {

  }

  @Override public void notifyLocationAvailability(LocationAvailability availability) {

  }

  @Override public boolean hasNoListeners() {
    return false;
  }

  @Override public void shutdown() {

  }

  @Override public Map<LostApiClient, Set<LocationListener>> getLocationListeners() {
    return null;
  }

  @Override public Map<LostApiClient, Set<PendingIntent>> getPendingIntents() {
    return null;
  }

  @Override public Map<LostApiClient, Set<LocationCallback>> getLocationCallbacks() {
    return null;
  }
}
