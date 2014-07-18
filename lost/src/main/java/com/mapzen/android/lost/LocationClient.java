package com.mapzen.android.lost;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;

public class LocationClient {
    public static final String TAG = LocationClient.class.getSimpleName();

    private final Context context;
    private final ConnectionCallbacks connectionCallbacks;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private android.location.LocationListener gpsListener;
    private android.location.LocationListener networkListener;

    private float gpsAccuracy = Float.MAX_VALUE;
    private float networkAccuracy = Float.MAX_VALUE;
    private long fastestInterval;
    private float smallestDisplacement;
    private boolean mockMode;

    public LocationClient(Context context, ConnectionCallbacks connectionCallbacks) {
        this.context = context;
        this.connectionCallbacks = connectionCallbacks;
    }

    public void connect() {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        connectionCallbacks.onConnected(null);
    }

    public void disconnect() {
        removeLocationUpdates(locationListener);
        locationManager = null;
        connectionCallbacks.onDisconnected();
    }

    public Location getLastLocation() {
        throwIfNotConnected();
        final List<String> providers = locationManager.getAllProviders();
        Location bestLocation = null;
        for (String provider : providers) {
            final Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                if (bestLocation == null || location.getAccuracy() < bestLocation.getAccuracy()) {
                    bestLocation = location;
                }
            }
        }
        return bestLocation;
    }

    public void requestLocationUpdates(LocationRequest request, LocationListener locationListener) {
        throwIfNotConnected();
        this.locationListener = locationListener;

        fastestInterval = request.getFastestInterval();
        smallestDisplacement = request.getSmallestDisplacement();

        initGpsListener(fastestInterval, smallestDisplacement);
        initNetworkListener(fastestInterval, smallestDisplacement);
    }

    private void initGpsListener(long interval, float displacement) {
        createGpsListener();
        if (!mockMode) {
            connectGpsListener(interval, displacement);
        }
    }

    private void createGpsListener() {
        this.gpsListener = new android.location.LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                gpsAccuracy = location.getAccuracy();
                if (gpsAccuracy <= networkAccuracy) {
                    LocationClient.this.locationListener.onLocationChanged(location);
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };
    }

    private void connectGpsListener(long interval, float displacement) {
        if (gpsListener == null) {
            return;
        }

        try {
            locationManager.requestLocationUpdates(GPS_PROVIDER, interval, displacement,
                    gpsListener);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Unable to register for GPS updates.", e);
        }
    }

    private void initNetworkListener(long interval, float displacement) {
        createNetworkListener();
        if (!mockMode) {
            connectNetworkListener(interval, displacement);
        }
    }

    private void createNetworkListener() {
        this.networkListener = new android.location.LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                networkAccuracy = location.getAccuracy();
                if (networkAccuracy <= gpsAccuracy) {
                    LocationClient.this.locationListener.onLocationChanged(location);
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };
    }

    private void connectNetworkListener(long interval, float displacement) {
        if (networkListener == null) {
            return;
        }

        try {
            locationManager.requestLocationUpdates(NETWORK_PROVIDER, interval, displacement,
                    networkListener);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Unable to register for network updates.", e);
        }
    }

    private void throwIfNotConnected() {
        if (locationManager == null) {
            throw new IllegalStateException("Not connected. "
                    + "Call connect() and wait for onConnected() to be called.");
        }
    }

    public void removeLocationUpdates(LocationListener locationListener) {
        if (gpsListener != null) {
            locationManager.removeUpdates(gpsListener);
        }

        if (networkListener != null) {
            locationManager.removeUpdates(networkListener);
        }
    }

    public boolean isGPSEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public LocationManager getLocationManager() {
        return locationManager;
    }

    public boolean isConnected() {
        return locationManager != null;
    }

    public void setMockMode(boolean isMockMode) {
        mockMode = isMockMode;

        if (locationManager == null) {
            return;
        }

        if (mockMode) {
            removeLocationUpdates(locationListener);
        } else {
            connectGpsListener(fastestInterval, smallestDisplacement);
            connectNetworkListener(fastestInterval, smallestDisplacement);
        }
    }

    public static interface ConnectionCallbacks {
        public void onConnected(Bundle connectionHint);
        public void onDisconnected();
    }
}
