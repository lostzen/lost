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
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of the {@link FusedLocationProviderApi}.
 */
public class FusedLocationProviderApiImpl extends ApiImpl
    implements FusedLocationProviderApi, EventCallbacks, ServiceConnection {

  private Context context;
  private FusedLocationServiceConnectionManager serviceConnectionManager;
  private FusedLocationServiceCallbackManager serviceCallbackManager;
  private RequestManager requestManager;
  private boolean isBound;

  IFusedLocationProviderService service;

  private IFusedLocationProviderCallback.Stub remoteCallback
      = new IFusedLocationProviderCallback.Stub() {
    public void onLocationChanged(final Location location) throws RemoteException {
      new Handler(Looper.getMainLooper()).post(new Runnable() {
        @Override public void run() {
          final LostClientManager clientManager = LostClientManager.shared();
          serviceCallbackManager.onLocationChanged(context, location, clientManager, service);
        }
      });
    }

    @Override public void onLocationAvailabilityChanged(LocationAvailability locationAvailability)
        throws RemoteException {
      LostClientManager clientManager = LostClientManager.shared();
      serviceCallbackManager.onLocationAvailabilityChanged(locationAvailability, clientManager);
    }
  };

  @Override public void onConnect(Context context) {
    this.context = context;
    final Intent intent = new Intent(context, FusedLocationProviderService.class);
    context.bindService(intent, this, Context.BIND_AUTO_CREATE);
  }

  @Override public void onServiceConnected(IBinder binder) {
    service = IFusedLocationProviderService.Stub.asInterface(binder);
    isBound = true;
    registerRemoteCallback();
  }

  @Override public void onDisconnect() {
    if (isBound) {
      unregisterRemoteCallback();
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

  public FusedLocationProviderApiImpl(FusedLocationServiceConnectionManager connectionManager,
      FusedLocationServiceCallbackManager callbackManager, RequestManager requestManager) {
    serviceConnectionManager = connectionManager;
    serviceCallbackManager = callbackManager;
    this.requestManager = requestManager;
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
    try {
      return service.getLastLocation();
    } catch (RemoteException e) {
      throw new RuntimeException(e);
    }
  }

  @Override public LocationAvailability getLocationAvailability(LostApiClient client) {
    throwIfNotConnected(client);
    try {
      return service.getLocationAvailability();
    } catch (RemoteException e) {
      throw new RuntimeException(e);
    }
  }

  @Override public PendingResult<Status> requestLocationUpdates(LostApiClient client,
      LocationRequest request, LocationListener listener) {
    throwIfNotConnected(client);
    requestManager.requestLocationUpdates(client, request, listener);
    LostClientManager.shared().addListener(client, request, listener);
    requestLocationUpdatesInternal(request);
    return new SimplePendingResult(true);
  }

  @Override public PendingResult<Status> requestLocationUpdates(LostApiClient client,
      LocationRequest request, LocationListener listener, Looper looper) {
    throw new RuntimeException("Sorry, not yet implemented");
  }

  @Override public PendingResult<Status> requestLocationUpdates(LostApiClient client,
      LocationRequest request, LocationCallback callback, Looper looper) {
    throwIfNotConnected(client);
    requestManager.requestLocationUpdates(client, request, callback);
    LostClientManager.shared().addLocationCallback(client, request, callback, looper);
    requestLocationUpdatesInternal(request);
    return new SimplePendingResult(true);
  }

  @Override public PendingResult<Status> requestLocationUpdates(LostApiClient client,
      LocationRequest request, PendingIntent callbackIntent) {
    throwIfNotConnected(client);
    requestManager.requestLocationUpdates(client, request, callbackIntent);
    LostClientManager.shared().addPendingIntent(client, request, callbackIntent);
    requestLocationUpdatesInternal(request);
    return new SimplePendingResult(true);
  }

  /**
   * Forwards location update request to the {@link FusedLocationProviderService} to initiate
   * and/or update request params.
   */
  private void requestLocationUpdatesInternal(LocationRequest request) {
    try {
      service.requestLocationUpdates(request);
    } catch (RemoteException e) {
      throw new RuntimeException(e);
    }
  }

  private void removeLocationUpdatesInternal(List<LocationRequest> requests) {
    if (requests == null) {
      return;
    }
    try {
      service.removeLocationUpdates(requests);
    } catch (RemoteException e) {
      throw new RuntimeException(e);
    }
  }

  @Override public PendingResult<Status> removeLocationUpdates(LostApiClient client,
      LocationListener listener) {
    throwIfNotConnected(client);
    List<LocationRequest> requests = requestManager.removeLocationUpdates(client, listener);
    removeLocationUpdatesInternal(requests);
    boolean hasResult = LostClientManager.shared().removeListener(client, listener);
    checkAllListenersPendingIntentsAndCallbacks();
    return new SimplePendingResult(hasResult);
  }

  @Override public PendingResult<Status> removeLocationUpdates(LostApiClient client,
      PendingIntent callbackIntent) {
    throwIfNotConnected(client);
    List<LocationRequest> requests = requestManager.removeLocationUpdates(client, callbackIntent);
    removeLocationUpdatesInternal(requests);
    boolean hasResult = LostClientManager.shared().removePendingIntent(client, callbackIntent);
    checkAllListenersPendingIntentsAndCallbacks();
    return new SimplePendingResult(hasResult);
  }

  @Override public PendingResult<Status> removeLocationUpdates(LostApiClient client,
      LocationCallback callback) {
    throwIfNotConnected(client);
    List<LocationRequest> requests = requestManager.removeLocationUpdates(client, callback);
    removeLocationUpdatesInternal(requests);
    boolean hasResult = LostClientManager.shared().removeLocationCallback(client, callback);
    checkAllListenersPendingIntentsAndCallbacks();
    return new SimplePendingResult(hasResult);
  }

  /**
   * Checks if any listeners or pending intents are still registered for location updates. If not,
   * then shutdown the location engines.
   */
  private void checkAllListenersPendingIntentsAndCallbacks() {
    //TODO: potentially remove hasNoListeners method, not needed anymore
    //if (LostClientManager.shared().hasNoListeners()) {
    //  try {
    //    service.removeAllLocationUpdates();
    //  } catch (RemoteException e) {
    //    throw new RuntimeException(e);
    //  }
    //}
  }

  @Override public PendingResult<Status> setMockMode(LostApiClient client, boolean isMockMode) {
    throwIfNotConnected(client);
    try {
      service.setMockMode(isMockMode);
    } catch (RemoteException e) {
      throw new RuntimeException(e);
    }
    return new SimplePendingResult(true);
  }

  @Override public PendingResult<Status> setMockLocation(LostApiClient client,
      Location mockLocation) {
    throwIfNotConnected(client);
    try {
      service.setMockLocation(mockLocation);
    } catch (RemoteException e) {
      throw new RuntimeException(e);
    }
    return new SimplePendingResult(true);
  }

  @Override public PendingResult<Status> setMockTrace(LostApiClient client, String path,
      String filename) {
    throwIfNotConnected(client);
    try {
      service.setMockTrace(path, filename);
    } catch (RemoteException e) {
      throw new RuntimeException(e);
    }
    return new SimplePendingResult(true);
  }

  public Map<LostApiClient, Set<LocationListener>> getLocationListeners() {
    return LostClientManager.shared().getLocationListeners();
  }

  void removeConnectionCallbacks(LostApiClient.ConnectionCallbacks callbacks) {
    serviceConnectionManager.removeCallbacks(callbacks);
  }

  FusedLocationServiceConnectionManager getServiceConnectionManager() {
    return serviceConnectionManager;
  }

  void registerRemoteCallback() {
    if (service != null) {
      try {
        service.init(remoteCallback);
      } catch (RemoteException e) {
        throw new RuntimeException(e);
      }
    }
  }

  void unregisterRemoteCallback() {
    if (service != null) {
      try {
        service.init(null);
      } catch (RemoteException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
