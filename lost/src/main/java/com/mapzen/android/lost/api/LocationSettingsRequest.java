package com.mapzen.android.lost.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Specifies the types of location services the client is interested in using. This object is used
 * in conjunction with the {@link SettingsApi}.
 */
public class LocationSettingsRequest {
  private final List<LocationRequest> locationRequests;
  private final boolean needBle;

  LocationSettingsRequest(List<LocationRequest> locationRequests, boolean needBle) {
    this.locationRequests = locationRequests;
    this.needBle = needBle;
  }

  /**
   * Returns the list of {@link LocationRequest}s associated with this request.
   * @return List of {@link LocationRequest}s.
   */
  public List<LocationRequest> getLocationRequests() {
    return Collections.unmodifiableList(this.locationRequests);
  }

  /**
   * Returns whether or not this request needs Bluetooth Low Energy.
   * @return whether the request needs BLE or not.
   */
  public boolean getNeedBle() {
    return this.needBle;
  }

  /**
   * Builder class for constructing {@link LocationSettingsRequest}s.
   */
  public static final class Builder {
    private final ArrayList<LocationRequest> locationRequests = new ArrayList();
    private boolean needBle = false;

    public Builder() {
    }

    /**
     * Add a {@link LocationRequest} to the list of requests to check settings for.
     * @param request Request to add to existing list of requests
     * @return Return {@link Builder} object
     */
    public LocationSettingsRequest.Builder addLocationRequest(LocationRequest request) {
      this.locationRequests.add(request);
      return this;
    }

    /**
     * Add a collection of {@link LocationRequest}s to the list of requests to check settings for.
     * @param requests Requests to add to existing list of requests.
     * @return the {@link Builder} object
     */
    public LocationSettingsRequest.Builder addAllLocationRequests(
        Collection<LocationRequest> requests) {
      this.locationRequests.addAll(requests);
      return this;
    }

    /**
     * Set whether or not BLE is needed.
     * @param needBle whether or not BLE is needed.
     * @return the {@link Builder} object
     */
    public LocationSettingsRequest.Builder setNeedBle(boolean needBle) {
      this.needBle = needBle;
      return this;
    }

    /**
     * Create and return a new {@link LocationSettingsRequest} given the builder's properties.
     * @return newly created {@link LocationSettingsRequest}.
     */
    public LocationSettingsRequest build() {
      return new LocationSettingsRequest(this.locationRequests, this.needBle);
    }
  }
}
