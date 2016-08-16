package com.mapzen.android.lost.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class LocationSettingsRequest {
  private final List<LocationRequest> locationRequests;
  private final boolean needBle;

  LocationSettingsRequest(List<LocationRequest> locationRequests, boolean needBle) {
    this.locationRequests = locationRequests;
    this.needBle = needBle;
  }

  public List<LocationRequest> getLocationRequests() {
    return Collections.unmodifiableList(this.locationRequests);
  }

  public boolean getNeedBle() {
    return this.needBle;
  }

  public static final class Builder {
    private final ArrayList<LocationRequest> locationRequests = new ArrayList();
    private boolean needBle = false;

    public Builder() {
    }

    public LocationSettingsRequest.Builder addLocationRequest(LocationRequest request) {
      this.locationRequests.add(request);
      return this;
    }

    public LocationSettingsRequest.Builder addAllLocationRequests(
        Collection<LocationRequest> requests) {
      this.locationRequests.addAll(requests);
      return this;
    }

    public LocationSettingsRequest.Builder setNeedBle(boolean needBle) {
      this.needBle = needBle;
      return this;
    }

    public LocationSettingsRequest build() {
      return new LocationSettingsRequest(this.locationRequests, this.needBle);
    }
  }
}
