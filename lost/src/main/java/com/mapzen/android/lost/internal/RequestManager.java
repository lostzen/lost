package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationCallback;
import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LostApiClient;

import android.app.PendingIntent;

import java.util.Set;

/**
 * Keeps track of which {@link LocationListener}s, {@link PendingIntent}s, and
 * {@link LocationCallback}s have made which {@link LocationRequest}s so that requests can be
 * properly removed from the underlying {@link LocationEngine}.
 */
interface RequestManager {
  void requestLocationUpdates(LostApiClient client, LocationRequest request,
      LocationListener listener);

  void requestLocationUpdates(LostApiClient client, LocationRequest request,
      LocationCallback callback);

  void requestLocationUpdates(LostApiClient client, LocationRequest request,
      PendingIntent callbackIntent);

  Set<LocationRequest> removeLocationUpdates(LostApiClient client, LocationListener listener);

  Set<LocationRequest> removeLocationUpdates(LostApiClient client, PendingIntent callbackIntent);

  Set<LocationRequest> removeLocationUpdates(LostApiClient client, LocationCallback callback);
}
