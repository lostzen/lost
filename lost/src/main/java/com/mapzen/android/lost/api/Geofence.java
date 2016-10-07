package com.mapzen.android.lost.api;

import com.mapzen.android.lost.internal.ParcelableGeofence;

/**
 * Represents a circular graphical region which can be used to monitor when a user enters/exits
 * a designated area.
 */
public interface Geofence {

  int GEOFENCE_TRANSITION_ENTER = 1;
  int GEOFENCE_TRANSITION_EXIT = 2;
  int GEOFENCE_TRANSITION_DWELL = 4;
  long NEVER_EXPIRE = -1L;
  int LOITERING_DELAY_NONE = -1;

  String getRequestId();

  /**
   * Builder class for {@link Geofence}.
   */
  final class Builder {
    private String requestId;
    private double latitude;
    private double longitude;
    private float radius;
    private long durationMillis = NEVER_EXPIRE;
    private int transitionTypes;
    private int loiteringDelayMs = LOITERING_DELAY_NONE;

    /**
     * Construct and return a new {@link Geofence} object from the {@link Builder}'s properties.
     * @return new {@link Geofence} object.
     */
    public Geofence build() {
      return new ParcelableGeofence(requestId, latitude, longitude, radius, durationMillis,
          transitionTypes, loiteringDelayMs);
    }

    /**
     * Sets the latitude, longitude, and radius for the {@link Builder}.
     * @param latitude in degrees.
     * @param longitude in degrees.
     * @param radius in meters.
     * @return the {@link Builder} object.
     */
    public Geofence.Builder setCircularRegion(double latitude, double longitude, float radius) {
      this.latitude = latitude;
      this.longitude = longitude;
      this.radius = radius;
      return this;
    }

    /**
     * Sets the duration in millis for the {@link Builder}.
     * @param durationMillis duration in milliseconds.
     * @return the {@link Builder} object.
     */
    public Geofence.Builder setExpirationDuration(long durationMillis) {
      this.durationMillis = durationMillis;
      return this;
    }

    /**
     * Sets the loitering delay in millis for the {@link Builder}. If transition type is set to
     * dwell then this value is used, otherwise it is ignored.
     * @param loiteringDelayMs duration in milliseconds.
     * @return the {@link Builder} object.
     */
    public Geofence.Builder setLoiteringDelay(int loiteringDelayMs) {
      this.loiteringDelayMs = loiteringDelayMs;
      return this;
    }

    /**
     * Not yet implemented
     */
    public Geofence.Builder setNotificationResponsiveness(int notificationResponsivenessMs) {
      throw new RuntimeException("Sorry, not yet implemented");
    }

    /**
     * Sets the request id for the {@link Builder}.
     * @param requestId id to be used for geofence.
     * @return the {@link Builder} object.
     */
    public Geofence.Builder setRequestId(String requestId) {
      this.requestId = requestId;
      return this;
    }

    /**
     * Sets the transition type for the {@link Builder}.
     * @param transitionTypes types to be used for geofence.
     * @return the {@link Builder} object.
     */
    public Geofence.Builder setTransitionTypes(int transitionTypes) {
      this.transitionTypes = transitionTypes;
      return this;
    }
  }
}
