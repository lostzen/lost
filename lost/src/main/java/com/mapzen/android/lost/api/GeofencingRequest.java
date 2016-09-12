package com.mapzen.android.lost.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to specify {@link Geofence}s to monitor when using the {@link GeofencingApi}.
 */
public class GeofencingRequest {
  private List<Geofence> geofences;

  private GeofencingRequest(List<Geofence> geofences) {
    this.geofences = geofences;
  }

  /**
   * Get the geofences that will be monitored.
   * @return List of geofences to monitor.
   */
  public List<Geofence> getGeofences() {
    return geofences;
  }

  /**
   * Builder for creating {@link GeofencingRequest}s.
   */
  public static final class Builder {
    private List<Geofence> geofences = new ArrayList<>();

    /**
     * Create and return a new {@link GeofencingRequest} object from the builder's properties.
     * @return newly created {@link GeofencingRequest}
     */
    public GeofencingRequest build() {
      if (geofences.isEmpty()) {
        throw new IllegalArgumentException("No geofence has been added to this request.");
      }
      return new GeofencingRequest(geofences);
    }

    /**
     * Add a {@link Geofence} to the list of geofences to be monitored.
     * @param geofence Geofence to monitor.
     * @return the {@link Builder} object.
     */
    public GeofencingRequest.Builder addGeofence(Geofence geofence) {
      if (geofence == null) {
        throw new IllegalArgumentException("Geofence cannot be null");
      }
      geofences.add(geofence);
      return this;
    }

    public GeofencingRequest.Builder addGeofences(List<Geofence> geofences) {
      if (geofences == null) {
        throw new IllegalArgumentException("Geofence cannot be null");
      }
      this.geofences.addAll(geofences);
      return this;
    }
  }
}
