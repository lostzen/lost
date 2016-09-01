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

import java.util.Map;

/**
 * Used by {@link LostApiClientImpl} to manage connected clients and by
 * {@link FusedLocationProviderServiceImpl} to manage client's {@link LocationListener}s,
 * {@link PendingIntent}s, and {@link LocationCallback}s.
 */
public interface ClientManager {

  void addClient(LostApiClient client);
  void removeClient(LostApiClient client);
  int numberOfClients();
  void addListener(LostApiClient client, LocationRequest request, LocationListener listener);
  void addPendingIntent(LostApiClient client, LocationRequest request,
      PendingIntent callbackIntent);
  void addLocationCallback(LostApiClient client, LocationRequest request,
      LocationCallback callback, Looper looper);
  void removeListener(LostApiClient client, LocationListener listener);
  void removePendingIntent(LostApiClient client, PendingIntent callbackIntent);
  void removeLocationCallback(LostApiClient client, LocationCallback callback);
  void reportLocationChanged(Location location);
  void sendPendingIntent(Context context, Location location, LocationAvailability availability,
      LocationResult result);
  void reportLocationResult(final LocationResult result);
  void reportProviderEnabled(String provider);
  void reportProviderDisabled(String provider);
  void notifyLocationAvailability(final LocationAvailability availability);
  boolean hasNoListeners();
  void disconnect(LostApiClient client);
  void shutdown();
  Map<LostApiClient, Map<LocationListener, LocationRequest>> getListeners();
  Map<LostApiClient, Map<PendingIntent, LocationRequest>> getPendingIntents();
  Map<LostApiClient, Map<LocationCallback, Looper>> getLocationListeners();
}
