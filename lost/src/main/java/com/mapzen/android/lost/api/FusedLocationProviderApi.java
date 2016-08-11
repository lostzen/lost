package com.mapzen.android.lost.api;

import android.app.PendingIntent;
import android.location.Location;
import android.os.Looper;

import java.io.File;

public interface FusedLocationProviderApi {

    String KEY_LOCATION_CHANGED = "com.lost.android.location.LOCATION";

    Location getLastLocation();

    void removeLocationUpdates(LocationListener listener);

    void removeLocationUpdates(PendingIntent callbackIntent);

    void requestLocationUpdates(LocationRequest request, LocationListener listener, Looper looper);

    void requestLocationUpdates(LocationRequest request, LocationListener listener);

    void requestLocationUpdates(LocationRequest request, PendingIntent callbackIntent);

    void setMockLocation(Location mockLocation);

    void setMockMode(boolean isMockMode);

    void setMockTrace(final File file);

    boolean isProviderEnabled(String provider);
}
