package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.FusedLocationProviderApi;
import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;

import android.app.PendingIntent;
import android.content.Context;
import android.location.Location;
import android.os.Looper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Implementation of the {@link FusedLocationProviderApi}.
 */
public class FusedLocationProviderApiImpl implements
        FusedLocationProviderApi, LocationEngine.Callback {

    private final Context context;
    private HashMap<LocationRequest, LocationEngine> locationEngines;
    private HashMap<LocationEngine, List<LocationListener>> engineListeners;
    private LocationEngine lastLocationEngine;
    private boolean mockMode;
    private File mockTraceFile;
    private Location mockLocation;

    public FusedLocationProviderApiImpl(Context context) {
        this.context = context;
        locationEngines = new HashMap<>();
        engineListeners = new HashMap<>();
        lastLocationEngine = new FusionEngine(context, null);
    }

    @Override
    public Location getLastLocation() {
        return lastLocationEngine.getLastLocation();
    }

    @Override
    public void requestLocationUpdates(LocationRequest request, LocationListener listener) {
        LocationEngine engine = locationEngineForRequest(request);
        addListenerForEngine(engine, listener);
        engine.setRequest(request);
    }

    @Override
    public void requestLocationUpdates(LocationRequest request, LocationListener listener,
            Looper looper) {
        throw new RuntimeException("Sorry, not yet implemented");
    }

    @Override
    public void requestLocationUpdates(LocationRequest request, PendingIntent callbackIntent) {
        throw new RuntimeException("Sorry, not yet implemented");
    }

    @Override
    public void removeLocationUpdates(LocationListener listener) {
        LocationEngine engine = removeListenerFromEngine(listener);
        if (engine != null) {
            checkShutdownEngine(engine);
        }
    }

    @Override
    public void removeLocationUpdates(PendingIntent callbackIntent) {
        throw new RuntimeException("Sorry, not yet implemented");
    }

    @Override
    public void setMockMode(boolean isMockMode) {
        if (mockMode != isMockMode) {
            toggleMockMode();
        }
    }

    private void toggleMockMode() {
        mockMode = !mockMode;
        shutdownAllEngines();
        if (mockMode) {
            lastLocationEngine = new MockEngine(context, null);
        } else {
            lastLocationEngine = new FusionEngine(context, null);
        }
    }

    @Override
    public void setMockLocation(Location mockLocation) {
        this.mockLocation = mockLocation;
        if (mockMode) {
            ((MockEngine) lastLocationEngine).setLocation(mockLocation);
            for (LocationEngine engine : locationEngines.values()) {
                ((MockEngine) engine).setLocation(mockLocation);
            }
        }
    }

    @Override
    public void setMockTrace(File file) {
        this.mockTraceFile = file;
        if (mockMode) {
            for (LocationEngine engine : locationEngines.values()) {
                ((MockEngine) engine).setTrace(file);
            }
        }
    }

    @Override
    public void reportLocation(LocationEngine engine, Location location) {
        for (LocationListener listener : engineListeners.get(engine)) {
            listener.onLocationChanged(location);
        }
    }

    public void shutdown() {
        shutdownAllEngines();
    }

    /**
     * First checks for an existing {@link LocationEngine} given a request. Creates a new one if
     * none exist.
     * @param request
     * @return
     */
    private LocationEngine locationEngineForRequest(LocationRequest request) {
        LocationEngine existing = locationEngines.get(request);
        if (existing == null) {
            if (mockMode) {
                existing = new MockEngine(context, this);
                MockEngine existingMock = (MockEngine) existing;
                existingMock.setTrace(mockTraceFile);
                if (mockLocation != null) {
                    existingMock.setLocation(mockLocation);
                }
            } else {
                existing = new FusionEngine(context, this);
            }
            locationEngines.put(request, existing);
        }
        return existing;
    }

    private void checkShutdownEngine(LocationEngine engine) {
        if (engineListeners.get(engine) == null) {
            engine.setRequest(null);
        }
    }

    private void addListenerForEngine(LocationEngine engine, LocationListener listener) {
        List existing = engineListeners.get(engine);
        if (existing == null) {
            existing = new ArrayList();
            engineListeners.put(engine, existing);
        }
        existing.add(listener);
    }

    /**
     * Removes {@link LocationListener} from map of LocationEngine/LocationListeners and
     * returns the {@link LocationEngine} that the listner was removed from
     * @param listener
     * @return
     */
    private LocationEngine removeListenerFromEngine(LocationListener listener) {
        for (LocationEngine engine : engineListeners.keySet()) {
            List<LocationListener> listeners = engineListeners.get(engine);
            if (listeners.contains(listener)) {
                listeners.remove(listener);
                if (listeners.isEmpty()) {
                    engineListeners.remove(engine);
                }
                return engine;
            }
        }
        return null;
    }

    private void shutdownAllEngines() {
        for (LocationEngine engine : locationEngines.values()) {
            engineListeners.remove(engine);
            engine.setRequest(null);
        }
        locationEngines.clear();
    }
}
