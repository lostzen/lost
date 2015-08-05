package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.Geofence;

public class GeofenceImpl implements Geofence {
    private double latitude;
    private double longitude;
    private float radius;
    private long durationMillis;
    private int loiteringDelayMs;
    private int notificationResponsivenessMs;
    private String requestId;
    private int transitionTypes;

    public GeofenceImpl(double latitude, double longitude, float radius, long durationMillis, int loiteringDelayMs, int notificationResponsivenessMs, String requestId, int transitionTypes) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.durationMillis = durationMillis;
        this.loiteringDelayMs = loiteringDelayMs;
        this.notificationResponsivenessMs = notificationResponsivenessMs;
        this.requestId = requestId;
        this.transitionTypes = transitionTypes;
    }

    public String getRequestId() {
        return this.requestId;
    }
}
