package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationAvailability;
import com.mapzen.android.lost.api.LocationResult;

import android.content.Context;
import android.location.Location;
import android.os.RemoteException;

import java.util.ArrayList;

public class FusedLocationServiceCallbackManager {

  void onLocationChanged(Context context, Location location, LostClientManager clientManager,
      IFusedLocationProviderService service) {
    if (service == null) {
      throw new IllegalStateException("Location update received after client was "
          + "disconnected. Did you forget to unregister location updates before "
          + "disconnecting?");
    }

    ReportedChanges changes = clientManager.reportLocationChanged(location);

    LocationAvailability availability;
    try {
      availability = service.getLocationAvailability();
    } catch (RemoteException e) {
      throw new RuntimeException(e);
    }

    ArrayList<Location> locations = new ArrayList<>();
    locations.add(location);
    final LocationResult result = LocationResult.create(locations);
    ReportedChanges pendingIntentChanges = clientManager.sendPendingIntent(
        context, location, availability, result);

    ReportedChanges callbackChanges = clientManager.reportLocationResult(location, result);

    changes.putAll(pendingIntentChanges);
    changes.putAll(callbackChanges);
    clientManager.updateReportedValues(changes);
  }

  void onLocationAvailabilityChanged(LocationAvailability locationAvailability,
      LostClientManager clientManager) {
    clientManager.notifyLocationAvailability(locationAvailability);
  }
}
