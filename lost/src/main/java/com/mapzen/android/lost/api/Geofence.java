package com.mapzen.android.lost.api;

import com.mapzen.android.lost.internal.ParcelableGeofence;

public interface Geofence {

  int GEOFENCE_TRANSITION_ENTER = 1;
  int GEOFENCE_TRANSITION_EXIT = 2;
  int GEOFENCE_TRANSITION_DWELL = 4;
  long NEVER_EXPIRE = -1L;

  String getRequestId();

  final class Builder {
    private String requestId;
    private double latitude;
    private double longitude;
    private float radius;
    private long durationMillis;

    public Geofence build() {
      return new ParcelableGeofence(requestId, latitude, longitude, radius, durationMillis);
    }

    public Geofence.Builder setCircularRegion(double latitude, double longitude, float radius) {
      this.latitude = latitude;
      this.longitude = longitude;
      this.radius = radius;
      return this;
    }

    public Geofence.Builder setExpirationDuration(long durationMillis) {
      this.durationMillis = durationMillis;
      return this;
    }

    public Geofence.Builder setLoiteringDelay(int loiteringDelayMs) {
      throw new RuntimeException("Sorry, not yet implemented");
    }

    public Geofence.Builder setNotificationResponsiveness(int notificationResponsivenessMs) {
      throw new RuntimeException("Sorry, not yet implemented");
    }

    public Geofence.Builder setRequestId(String requestId) {
      this.requestId = requestId;
      return this;
    }

    public Geofence.Builder setTransitionTypes(int transitionTypes) {
      throw new RuntimeException("Sorry, not yet implemented");
    }
  }
}
