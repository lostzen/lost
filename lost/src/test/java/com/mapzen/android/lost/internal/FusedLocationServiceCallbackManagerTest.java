package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationAvailability;
import com.mapzen.android.lost.api.LocationResult;

import org.junit.Test;

import android.content.Context;
import android.location.Location;
import android.os.RemoteException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FusedLocationServiceCallbackManagerTest {

  FusedLocationServiceCallbackManager callbackManager =
      new FusedLocationServiceCallbackManager();

  @Test public void onLocationChanged_shouldReportLocationChanged() {
    LostClientManager clientManager = mock(LostClientManager.class);
    Location location = mock(Location.class);
    callbackManager.onLocationChanged(mock(Context.class), location, clientManager,
        mock(IFusedLocationProviderService.class));
    verify(clientManager).reportLocationChanged(location);
  }

  @Test public void onLocationChanged_shouldSendPendingIntent() {
    LostClientManager clientManager = mock(LostClientManager.class);
    IFusedLocationProviderService service = mock(IFusedLocationProviderService.class);
    LocationAvailability locationAvailability = mock(LocationAvailability.class);
    try {
      when(service.getLocationAvailability()).thenReturn(locationAvailability);
    } catch (RemoteException e) {
      e.printStackTrace();
    }
    Context context = mock(Context.class);
    Location location = mock(Location.class);
    callbackManager.onLocationChanged(context, location, clientManager, service);
    verify(clientManager).sendPendingIntent(eq(context), eq(location), eq(locationAvailability),
        any(LocationResult.class));
  }

  @Test public void onLocationChanged_shouldReportLocationResult() {
    LostClientManager clientManager = mock(LostClientManager.class);
    IFusedLocationProviderService service = mock(IFusedLocationProviderService.class);
    Location location = mock(Location.class);
    callbackManager.onLocationChanged(mock(Context.class), location, clientManager, service);
    verify(clientManager).reportLocationResult(eq(location), any(LocationResult.class));
  }

  @Test public void onLocationAvailabilityChanged_shouldNotifyLocationAvailability() {
    LostClientManager clientManager = mock(LostClientManager.class);
    LocationAvailability locationAvailability = mock(LocationAvailability.class);
    callbackManager.onLocationAvailabilityChanged(locationAvailability, clientManager);
    verify(clientManager).notifyLocationAvailability(locationAvailability);
  }
}
