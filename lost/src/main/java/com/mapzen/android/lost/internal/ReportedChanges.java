package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationRequest;

import android.location.Location;

import java.util.Map;

/**
 * Represents changes to reported locations and times at which locations were reported for
 * different modes of notification (listeners, pending intents, and callbacks). After all types of
 * callbacks are invoked, these changes will be committed so that the next time a location is
 * reported, the {@link FusedLocationProviderServiceDelegate} can properly determine if it should be
 * reported to listeners, pending intents, and callbacks.
 */
public class ReportedChanges {

  private Map<LocationRequest, Long> updatedRequestToReportedTime;
  private Map<LocationRequest, Location> updatedRequestToReportedLocation;

  public ReportedChanges(Map<LocationRequest, Long> timeChanges,
      Map<LocationRequest, Location> locationChanges) {
    updatedRequestToReportedTime = timeChanges;
    updatedRequestToReportedLocation = locationChanges;
  }

  public Map<LocationRequest, Long> timeChanges() {
    return updatedRequestToReportedTime;
  }

  public Map<LocationRequest, Location> locationChanges() {
    return updatedRequestToReportedLocation;
  }

  public void putAll(ReportedChanges changes) {
    updatedRequestToReportedTime.putAll(changes.timeChanges());
    updatedRequestToReportedLocation.putAll(changes.locationChanges());
  }
}
