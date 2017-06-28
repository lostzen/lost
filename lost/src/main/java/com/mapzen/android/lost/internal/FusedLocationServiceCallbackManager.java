package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationAvailability;
import com.mapzen.android.lost.api.LocationResult;

import android.content.Context;
import android.location.Location;
import android.os.RemoteException;

import java.util.ArrayList;

/**
 * Handles callbacks received in {@link FusedLocationProviderApiImpl} from
 * {@link FusedLocationProviderService}.
 */
public class FusedLocationServiceCallbackManager {

  /**
   * Called when a new location has been received. This method handles dispatching changes to all
   * {@link com.mapzen.android.lost.api.LocationListener}s, {@link android.app.PendingIntent}s, and
   * {@link com.mapzen.android.lost.api.LocationCallback}s which are registered.
   * @param context
   * @param location
   * @param clientManager
   * @param service
   */
  void onLocationChanged(Context context, Location location, LostClientManager clientManager,
      IFusedLocationProviderService service) {

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

  /**
   * Handles notifying all registered {@link LocationCallback}s that {@link LocationAvailability}
   * has changed.
   * @param locationAvailability
   * @param clientManager
   */
  void onLocationAvailabilityChanged(LocationAvailability locationAvailability,
      LostClientManager clientManager) {
    clientManager.notifyLocationAvailability(locationAvailability);
  }
}
