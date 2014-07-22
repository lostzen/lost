package com.mapzen.android.lost;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.util.Xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;

public class LocationClient {
    public static final String TAG = LocationClient.class.getSimpleName();

    // GPX Tags
    public static final String TAG_GPX = "gpx";
    public static final String TAG_TRACK_POINT = "trkpt";
    public static final String TAG_LATITUDE = "lat";
    public static final String TAG_LONGITUDE = "lon";

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
    private Location mockLocation;

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

        if (mockMode) {
            return mockLocation;
        }

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

    public void setMockLocation(Location mockLocation) {
        this.mockLocation = mockLocation;

        if (locationListener != null) {
            locationListener.onLocationChanged(mockLocation);
        }
    }

    public void setMockTrace(final String filename) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file = new File(Environment.getExternalStorageDirectory(), filename);
                FileInputStream in = null;
                try {
                    in = new FileInputStream(file);
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    parser.setInput(in, null);
                    parser.nextTag();
                    parse(parser);
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "Unable to find gpx trace file: " + filename, e);
                } catch (XmlPullParserException e) {
                    Log.e(TAG, "Error parsing gpx trace file: " + filename, e);
                } catch (IOException e) {
                    Log.e(TAG, "Error parsing gpx trace file: " + filename, e);
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            Log.e(TAG, "Error closing gpx trace file: " + filename, e);
                        }
                    }
                }
            }
        }).start();
    }

    private void parse(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, TAG_GPX);
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            if (TAG_TRACK_POINT.equals(parser.getName())) {
                final Location location = new Location(TAG_GPX);
                for (int i = 0; i < parser.getAttributeCount(); i++) {
                    final String name = parser.getAttributeName(i);
                    final String value = parser.getAttributeValue(i);
                    if (TAG_LATITUDE.equals(name)) {
                        location.setLatitude(Double.parseDouble(value));
                    } else if (TAG_LONGITUDE.equals(name)) {
                        location.setLongitude(Double.parseDouble(value));
                    }
                }

                location.setTime(System.currentTimeMillis());

                new Handler(context.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        locationListener.onLocationChanged(location);
                    }
                });

                try {
                    Thread.sleep(fastestInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static interface ConnectionCallbacks {
        public void onConnected(Bundle connectionHint);
        public void onDisconnected();
    }
}
