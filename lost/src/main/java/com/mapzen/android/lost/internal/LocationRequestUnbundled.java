package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Set of location requests used to synthesize parameters for location updates.
 */
public class LocationRequestUnbundled {

  private List<LocationRequest> requests = new ArrayList<>();
  private long fastestInterval = Long.MAX_VALUE;

  public void addRequest(LocationRequest request) {
    if (request.getFastestInterval() < fastestInterval) {
      fastestInterval = request.getFastestInterval();
    }
    requests.add(request);
  }

  public void removeAllRequests() {
    fastestInterval = Long.MAX_VALUE;
    requests.clear();
  }

  public List<LocationRequest> getRequests() {
    return requests;
  }

  public long getFastestInterval() {
    return fastestInterval;
  }
}
