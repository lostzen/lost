package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.FusedLocationProviderApi;
import com.mapzen.android.lost.api.LocationAvailability;
import com.mapzen.android.lost.api.LocationCallback;
import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.android.lost.api.PendingResult;
import com.mapzen.android.lost.api.Status;
import com.mapzen.android.lost.internal.FusedLocationServiceConnectionManager.EventCallbacks;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;

import java.io.File;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of the {@link FusedLocationProviderApi}.
 */
public class FusedLocationProviderApiImpl
    implements FusedLocationProviderApi, EventCallbacks, ServiceConnection {

  private Context context;
  private FusedLocationProviderService service;
  private FusedLocationServiceConnectionManager serviceConnectionManager;
  private boolean isBound;

  @Override public void onConnect(Context context) {
    this.context = context;
    final Intent intent = new Intent(context, FusedLocationProviderService.class);
    context.bindService(intent, this, Context.BIND_AUTO_CREATE);
  }

  @Override public void onServiceConnected(IBinder binder) {
    FusedLocationProviderService.FusedLocationProviderBinder fusedBinder =
        (FusedLocationProviderService.FusedLocationProviderBinder) binder;
    if (fusedBinder != null) {
      service = fusedBinder.getService();
      isBound = true;
    }
  }

  @Override public void onDisconnect() {
    if (isBound) {
      context.unbindService(this);
      isBound = false;
    }

    service = null;
  }

  @Override public void onServiceConnected(ComponentName name, IBinder binder) {
    serviceConnectionManager.onServiceConnected(binder);
    isBound = true;
  }

  @Override public void onServiceDisconnected(ComponentName name) {
    serviceConnectionManager.onServiceDisconnected();
    isBound = false;
  }

  public FusedLocationProviderApiImpl(FusedLocationServiceConnectionManager connectionManager) {
    serviceConnectionManager = connectionManager;
    serviceConnectionManager.setEventCallbacks(this);
  }

  public boolean isConnecting() {
    return serviceConnectionManager.isConnecting();
  }

  public void addConnectionCallbacks(LostApiClient.ConnectionCallbacks callbacks) {
    serviceConnectionManager.addCallbacks(callbacks);
  }

  public void connect(Context context, LostApiClient.ConnectionCallbacks callbacks) {
    serviceConnectionManager.connect(context, callbacks);
  }

  public void disconnect() {
    serviceConnectionManager.disconnect();
  }

  public boolean isConnected() {
    return serviceConnectionManager.isConnected();
  }

  @Override public Location getLastLocation(LostApiClient client) {
    throwIfNotConnected(client);
    return service.getLastLocation();
  }

  @Override public LocationAvailability getLocationAvailability(LostApiClient client) {
    throwIfNotConnected(client);
    return service.getLocationAvailability();
  }

  @Override public PendingResult<Status> requestLocationUpdates(LostApiClient client,
      LocationRequest request, LocationListener listener) {
    throwIfNotConnected(client);
    LostClientManager.shared().addListener(client, request, listener);
    service.requestLocationUpdates(request);
    return new SimplePendingResult(true);
  }

  @Override public PendingResult<Status> requestLocationUpdates(LostApiClient client,
      LocationRequest request, LocationListener listener, Looper looper) {
    throw new RuntimeException("Sorry, not yet implemented");
  }

  @Override public PendingResult<Status> requestLocationUpdates(LostApiClient client,
      LocationRequest request, LocationCallback callback, Looper looper) {
    throwIfNotConnected(client);
    LostClientManager.shared().addLocationCallback(client, request, callback, looper);
    service.requestLocationUpdates(request);
    return new SimplePendingResult(true);
  }

  @Override public PendingResult<Status> requestLocationUpdates(LostApiClient client,
      LocationRequest request, PendingIntent callbackIntent) {
    throwIfNotConnected(client);
    LostClientManager.shared().addPendingIntent(client, request, callbackIntent);
    service.requestLocationUpdates(request);
    return new SimplePendingResult(true);
  }

  @Override public PendingResult<Status> removeLocationUpdates(LostApiClient client,
      LocationListener listener) {
    throwIfNotConnected(client);
    boolean hasResult = LostClientManager.shared().removeListener(client, listener);
    service.removeLocationUpdates();
    return new SimplePendingResult(hasResult);
  }

  @Override public PendingResult<Status> removeLocationUpdates(LostApiClient client,
      PendingIntent callbackIntent) {
    throwIfNotConnected(client);
    boolean hasResult = LostClientManager.shared().removePendingIntent(client, callbackIntent);
    service.removeLocationUpdates();
    return new SimplePendingResult(hasResult);
  }

  @Override public PendingResult<Status> removeLocationUpdates(LostApiClient client,
      LocationCallback callback) {
    throwIfNotConnected(client);
    boolean hasResult = LostClientManager.shared().removeLocationCallback(client, callback);
    service.removeLocationUpdates();
    return new SimplePendingResult(hasResult);
  }

  @Override public PendingResult<Status> setMockMode(LostApiClient client, boolean isMockMode) {
    throwIfNotConnected(client);
    service.setMockMode(isMockMode);
    return new SimplePendingResult(true);
  }

  @Override public PendingResult<Status> setMockLocation(LostApiClient client,
      Location mockLocation) {
    throwIfNotConnected(client);
    service.setMockLocation(mockLocation);
    return new SimplePendingResult(true);
  }

  @Override public PendingResult<Status> setMockTrace(LostApiClient client, File file) {
    throwIfNotConnected(client);
    service.setMockTrace(file);
    return new SimplePendingResult(true);
  }

  public Map<LostApiClient, Set<LocationListener>> getLocationListeners() {
    return LostClientManager.shared().getLocationListeners();
  }

  public FusedLocationProviderService getService() {
    return service;
  }

  void removeConnectionCallbacks(LostApiClient.ConnectionCallbacks callbacks) {
    serviceConnectionManager.removeCallbacks(callbacks);
  }

  FusedLocationServiceConnectionManager getServiceConnectionManager() {
    return serviceConnectionManager;
  }

  private void throwIfNotConnected(LostApiClient client) {
    if (!client.isConnected()) {
      throw new IllegalStateException("LostApiClient is not connected.");
    }
  }
}
