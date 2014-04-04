package com.mapzen.android.ps.location;

public final class LocationRequest {
    static final long DEFAULT_INTERVAL_IN_MS = 3600000;
    static final long DEFAULT_FASTEST_INTERVAL_IN_MS = 600000;
    static final float DEFAULT_SMALLEST_DISPLACEMENT_IN_METERS = 0.0f;

    private long interval = DEFAULT_INTERVAL_IN_MS;
    private long fastestInterval = DEFAULT_FASTEST_INTERVAL_IN_MS;
    private float smallestDisplacement = DEFAULT_SMALLEST_DISPLACEMENT_IN_METERS;

    private LocationRequest() {
    }

    public static LocationRequest create() {
        return new LocationRequest();
    }

    public long getInterval() {
        return interval;
    }

    public LocationRequest setInterval(long millis) {
        interval = millis;

        if (interval < fastestInterval) {
            fastestInterval = interval;
        }

        return this;
    }

    public long getFastestInterval() {
        return fastestInterval;
    }

    public LocationRequest setFastestInterval(long millis) {
        fastestInterval = millis;
        return this;
    }

    public float getSmallestDisplacement() {
        return smallestDisplacement;
    }

    public LocationRequest setSmallestDisplacement(float meters) {
        smallestDisplacement = meters;
        return this;
    }
}
