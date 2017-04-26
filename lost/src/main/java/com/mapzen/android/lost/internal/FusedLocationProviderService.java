package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationAvailability;
import com.mapzen.android.lost.api.LocationRequest;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

/**
 * Service which runs the fused location provider in the background.
 */
public class FusedLocationProviderService extends Service {

  private FusedLocationProviderServiceDelegate delegate;

  private final IFusedLocationProviderService.Stub binder =
      new IFusedLocationProviderService.Stub() {
        @Override public void init(IFusedLocationProviderCallback callback) throws RemoteException {
          delegate.init(callback);
        }

        @Override public Location getLastLocation() throws RemoteException {
          return delegate.getLastLocation();
        }

        @Override public LocationAvailability getLocationAvailability() throws RemoteException {
          return delegate.getLocationAvailability();
        }

        @Override public void requestLocationUpdates(LocationRequest request)
            throws RemoteException {
          delegate.requestLocationUpdates(request);
        }

        @Override public void removeLocationUpdates() throws RemoteException {
          delegate.removeLocationUpdates();
        }

        @Override public void setMockMode(boolean isMockMode) throws RemoteException {
          delegate.setMockMode(isMockMode);
        }

        @Override public void setMockLocation(Location mockLocation) throws RemoteException {
          delegate.setMockLocation(mockLocation);
        }

        @Override public void setMockTrace(String path, String filename) throws RemoteException {
          delegate.setMockTrace(path, filename);
        }
      };

  @Nullable @Override public IBinder onBind(Intent intent) {
    return binder;
  }

  @Override public void onCreate() {
    super.onCreate();
    delegate = new FusedLocationProviderServiceDelegate(this);
  }
}
