package com.mapzen.android.lost;

public class SystemClock implements Clock {
    @Override
    public long getCurrentTimeInMillis() {
        return System.currentTimeMillis();
    }
}
