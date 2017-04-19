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
import java.util.Set;

/**
 * Used by {@link LostApiClientImpl} to manage connected clients and by
 * {@link FusedLocationProviderServiceDelegate} to manage client's {@link LocationListener}s,
 * {@link PendingIntent}s, and {@link LocationCallback}s.
 */
public interface ClientManager {

  void addClient(LostApiClient client);
  void removeClient(LostApiClient client);
  boolean containsClient(LostApiClient client);
  int numberOfClients();
  void addListener(LostApiClient client, LocationRequest request, LocationListener listener);
  void addPendingIntent(LostApiClient client, LocationRequest request,
      PendingIntent callbackIntent);
  void addLocationCallback(LostApiClient client, LocationRequest request,
      LocationCallback callback, Looper looper);
  boolean removeListener(LostApiClient client, LocationListener listener);
  boolean removePendingIntent(LostApiClient client, PendingIntent callbackIntent);
  boolean removeLocationCallback(LostApiClient client, LocationCallback callback);
  ReportedChanges reportLocationChanged(Location location);
  ReportedChanges sendPendingIntent(Context context, Location location,
      LocationAvailability availability, LocationResult result);
  ReportedChanges reportLocationResult(Location location, final LocationResult result);
  void updateReportedValues(ReportedChanges changes);
  void notifyLocationAvailability(final LocationAvailability availability);
  boolean hasNoListeners();
  Map<LostApiClient, Set<LocationListener>> getLocationListeners();
  Map<LostApiClient, Set<PendingIntent>> getPendingIntents();
  Map<LostApiClient, Set<LocationCallback>> getLocationCallbacks();
}
