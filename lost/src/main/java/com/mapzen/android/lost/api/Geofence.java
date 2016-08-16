package com.mapzen.android.lost.api;

public interface Geofence {

  int GEOFENCE_TRANSITION_ENTER = 0x00000001;
  int GEOFENCE_TRANSITION_EXIT = 0x00000002;
  int GEOFENCE_TRANSITION_DWELL = 0x00000004;
  long NEVER_EXPIRE = 0xffffffffffffffffL;

  String getRequestId();

  final class Builder {
    public Geofence build() {
      throw new RuntimeException("Sorry, not yet implemented");
    }

    public Geofence.Builder setCircularRegion(double latitude, double longitude, float radius) {
      throw new RuntimeException("Sorry, not yet implemented");
    }

    public Geofence.Builder setExpirationDuration(long durationMillis) {
      throw new RuntimeException("Sorry, not yet implemented");
    }

    public Geofence.Builder setLoiteringDelay(int loiteringDelayMs) {
      throw new RuntimeException("Sorry, not yet implemented");
    }

    public Geofence.Builder setNotificationResponsiveness(int notificationResponsivenessMs) {
      throw new RuntimeException("Sorry, not yet implemented");
    }

    public Geofence.Builder setRequestId(String requestId) {
      throw new RuntimeException("Sorry, not yet implemented");
    }

    public Geofence.Builder setTransitionTypes(int transitionTypes) {
      throw new RuntimeException("Sorry, not yet implemented");
    }
  }
}
