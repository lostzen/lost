package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationAvailability;
import com.mapzen.android.lost.api.LocationRequest;

import android.location.Location;
import android.os.RemoteException;

public class TestServiceStub extends IFusedLocationProviderService.Stub {

  IFusedLocationProviderCallback callback;

  @Override public void init(IFusedLocationProviderCallback callback) throws RemoteException {
    this.callback = callback;
  }

  @Override public Location getLastLocation() throws RemoteException {
    return null;
  }

  @Override public LocationAvailability getLocationAvailability() throws RemoteException {
    return null;
  }

  @Override public void requestLocationUpdates(LocationRequest request) throws RemoteException {

  }

  @Override public void removeLocationUpdates() throws RemoteException {

  }

  @Override public void setMockMode(boolean isMockMode) throws RemoteException {

  }

  @Override public void setMockLocation(Location mockLocation) throws RemoteException {

  }

  @Override public void setMockTrace(String path, String filename) throws RemoteException {

  }
}
