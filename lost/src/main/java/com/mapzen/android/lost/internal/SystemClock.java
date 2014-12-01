package com.mapzen.android.lost.internal;

public class SystemClock implements Clock {
    @Override
    public long getCurrentTimeInMillis() {
        return System.currentTimeMillis();
    }
}
