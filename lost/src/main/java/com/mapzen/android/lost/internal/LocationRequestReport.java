package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationRequest;

import android.location.Location;

/**
 * Wrapper object used to store info about when a {@link Location} was last reported for a given
 * {@link LocationRequest}. Used by {@link LostClientManager} to determine if a new location should
 * should be reported to a given client listener.
 */
class LocationRequestReport {

  Location location;
  final LocationRequest locationRequest;

  LocationRequestReport(LocationRequest request) {
    this.locationRequest = request;
  }

}
