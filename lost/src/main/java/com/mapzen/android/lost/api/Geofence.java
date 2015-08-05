package com.mapzen.android.lost.api;

import com.mapzen.android.lost.internal.GeofenceImpl;

public interface Geofence {

    int GEOFENCE_TRANSITION_ENTER = 0x00000001;
    int GEOFENCE_TRANSITION_EXIT = 0x00000002;
    int GEOFENCE_TRANSITION_DWELL = 0x00000004;
    long NEVER_EXPIRE = 0xffffffffffffffffl;

    String getRequestId();

    final class Builder {
        private GeofenceImpl builtGeofence;

        private double latitude;
        private double longitude;
        private float radius;
        private long durationMillis;
        private int loiteringDelayMs;
        private int notificationResponsivenessMs;
        private String requestId;
        private int transitionTypes;

        public Geofence build () {
            builtGeofence = new GeofenceImpl(latitude, longitude, radius, durationMillis, loiteringDelayMs, notificationResponsivenessMs, requestId, transitionTypes);
            return builtGeofence;
        }

        public Geofence.Builder setCircularRegion(double latitude, double longitude, float radius) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.radius = radius;
            return this;
        }

        public Geofence.Builder setExpirationDuration (long durationMillis) {
            this.durationMillis = durationMillis;
            return this;
        }

        public Geofence.Builder setLoiteringDelay (int loiteringDelayMs) {
            this.loiteringDelayMs = loiteringDelayMs;
            return this;
        }

        public Geofence.Builder setNotificationResponsiveness (int notificationResponsivenessMs) {
            this.notificationResponsivenessMs = notificationResponsivenessMs;
            return this;
        }

        public Geofence.Builder setRequestId (String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Geofence.Builder setTransitionTypes (int transitionTypes) {
            this.transitionTypes = transitionTypes;
            return this;
        }
    }
}