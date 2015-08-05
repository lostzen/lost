package com.mapzen.android.lost.internal;

import android.app.PendingIntent;

import com.mapzen.android.lost.api.Geofence;
import com.mapzen.android.lost.api.GeofencingApi;
import com.mapzen.android.lost.api.GeofencingRequest;

import java.util.Hashtable;
import java.util.List;

/**
 * Implementation of the {@link GeofencingApi}.
 */


public class GeofencingApiImpl implements GeofencingApi {

    private Hashtable<String, Geofence> idToFence;
    private Hashtable<String, PendingIntent> idToIntent;
    private Hashtable<PendingIntent, Geofence> intentToFence;

    public GeofencingApiImpl() {
        idToFence = new Hashtable<String, Geofence>();
        idToIntent = new Hashtable<String, PendingIntent>();
        intentToFence = new Hashtable<PendingIntent, Geofence>();
    }

    @Deprecated
    @Override
    public void addGeofences(List<Geofence> geofences, PendingIntent pendingIntent) {
        for (Geofence curGeofence : geofences) {
            idToFence.put(curGeofence.getRequestId(), curGeofence);
            idToIntent.put(curGeofence.getRequestId(), pendingIntent);
            intentToFence.put(pendingIntent, curGeofence);
        }
    }

    @Override
    public void addGeofences(GeofencingRequest geofencingRequest, PendingIntent pendingIntent) {
        throw new RuntimeException("Sorry, not yet implemented");
    }

    @Override
    public void removeGeofences(List<String> geofenceRequestIds) {
        for (String curRequestId : geofenceRequestIds) {
            idToFence.remove(curRequestId);
            PendingIntent pendingIntent = idToIntent.remove(curRequestId);
            intentToFence.remove(pendingIntent);
        }
    }

    @Override
    public void removeGeofences(PendingIntent pendingIntent) {
        Geofence geofence = intentToFence.remove(pendingIntent);
        String requestId = geofence.getRequestId();
        idToIntent.remove(requestId);
        idToFence.remove(requestId);
    }
}