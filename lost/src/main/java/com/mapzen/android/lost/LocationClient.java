package com.mapzen.android.lost;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;

public class LocationClient {
    public static final String TAG = LocationClient.class.getSimpleName();

    // GPX tags
    public static final String TAG_TRACK_POINT = "trkpt";
    public static final String TAG_LAT = "lat";
    public static final String TAG_LNG = "lon";

    // Name of the mock location provider.
    public static final String MOCK_PROVIDER = "mock";

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
        if (mockMode == isMockMode) {
            return;
        }
        mockMode = isMockMode;

        if (locationManager == null) {
            return;
        }

        if (mockMode) {
            removeLocationUpdates(locationListener);
        } else {
            initGpsListener(fastestInterval, smallestDisplacement);
            initNetworkListener(fastestInterval, smallestDisplacement);
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
                final File file = new File(Environment.getExternalStorageDirectory(), filename);
                final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                final XPath xPath = XPathFactory.newInstance().newXPath();
                final String expression = "//" + TAG_TRACK_POINT;

                NodeList nodeList = null;
                try {
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document document = builder.parse(file);
                    nodeList = (NodeList) xPath.compile(expression)
                            .evaluate(document, XPathConstants.NODESET);
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XPathExpressionException e) {
                    e.printStackTrace();
                }

                parse(nodeList);
            }
        }).start();
    }

    private void parse(NodeList nodeList) {
        if (nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                String lat = node.getAttributes().getNamedItem(TAG_LAT).getNodeValue();
                String lng = node.getAttributes().getNamedItem(TAG_LNG).getNodeValue();

                final Location location = new Location(MOCK_PROVIDER);
                location.setLatitude(Double.parseDouble(lat));
                location.setLongitude(Double.parseDouble(lng));
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
