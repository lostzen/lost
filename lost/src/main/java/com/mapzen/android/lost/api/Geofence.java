package com.mapzen.android.lost.api;

public interface Geofence {

    public static final int GEOFENCE_TRANSITION_ENTER = 0x00000001;
    public static final int GEOFENCE_TRANSITION_EXIT = 0x00000002;
    public static final int GEOFENCE_TRANSITION_DWELL = 0x00000004;
    public static final long NEVER_EXPIRE = 0xffffffffffffffffl;

    String getRequestId();

    public static final class Builder {
        public Geofence build () {
            throw new RuntimeException("Sorry, not yet implemented");
        }

        public Geofence.Builder setCircularRegion (double latitude, double longitude,
                float radius) {
            throw new RuntimeException("Sorry, not yet implemented");
        }

        public Geofence.Builder setExpirationDuration (long durationMillis) {
            throw new RuntimeException("Sorry, not yet implemented");
        }

        public Geofence.Builder setLoiteringDelay (int loiteringDelayMs) {
            throw new RuntimeException("Sorry, not yet implemented");
        }

        public Geofence.Builder setNotificationResponsiveness (int notificationResponsivenessMs) {
            throw new RuntimeException("Sorry, not yet implemented");
        }

        public Geofence.Builder setRequestId (String requestId) {
            throw new RuntimeException("Sorry, not yet implemented");
        }

        public Geofence.Builder setTransitionTypes (int transitionTypes) {
            throw new RuntimeException("Sorry, not yet implemented");
        }
    }
}
