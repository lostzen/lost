package com.mapzen.android.lost.api;

import java.util.ArrayList;
import java.util.List;

public class GeofencingRequest {
  private List<Geofence> geofences;

  private GeofencingRequest(List<Geofence> geofences) {
    this.geofences = geofences;
  }

  public List<Geofence> getGeofences() {
    return geofences;
  }

  public static final class Builder {
    private List<Geofence> geofences = new ArrayList<>();

    public GeofencingRequest build() {
      if (geofences.isEmpty()) {
        throw new IllegalArgumentException("No geofence has been added to this request.");
      }
      return new GeofencingRequest(geofences);
    }

    public GeofencingRequest.Builder addGeofence(Geofence geofence) {
      if (geofence == null) {
        throw new IllegalArgumentException("Geofence cannot be null");
      }
      geofences.add(geofence);
      return this;
    }
  }
}
