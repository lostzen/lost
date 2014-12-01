package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.FusedLocationProviderApi;
import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.PendingIntent;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

/**
 * Implementation of the {@link FusedLocationProviderApi}.
 */
public class FusedLocationProviderApiImpl implements FusedLocationProviderApi {
    public static final String TAG = FusedLocationProviderApiImpl.class.getSimpleName();

    /** Location updates more than 60 seconds old are considered stale. */
    public static final int RECENT_UPDATE_THRESHOLD_IN_MILLIS = 60 * 1000;

    // Name of the mock location provider.
    public static final String MOCK_PROVIDER = "mock";

    private final LocationManager locationManager;
    private final Context context;
    private LocationListener locationListener;
    private float gpsAccuracy = Float.MAX_VALUE;
    private float networkAccuracy = Float.MAX_VALUE;
    private long fastestInterval;
    private float smallestDisplacement;
    private boolean mockMode;
    private Location mockLocation;

    // GPX tags
    public static final String TAG_TRACK_POINT = "trkpt";
    public static final String TAG_SPEED = "speed";
    public static final String TAG_LAT = "lat";
    public static final String TAG_LNG = "lon";

    private GpsListener gpsListener;
    private NetworkListener networkListener;

    Clock clock = new SystemClock();

    public FusedLocationProviderApiImpl(Context context) {
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        createGpsAndNetworkListeners();
    }

    @Override
    public Location getLastLocation() {
        if (mockMode) {
            return mockLocation;
        }

        final List<String> providers = locationManager.getAllProviders();
        final long minTime = clock.getCurrentTimeInMillis() - RECENT_UPDATE_THRESHOLD_IN_MILLIS;

        Location bestLocation = null;
        float bestAccuracy = Float.MAX_VALUE;
        long bestTime = Long.MIN_VALUE;

        for (String provider : providers) {
            final Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                final float accuracy = location.getAccuracy();
                final long time = location.getTime();
                if (time > minTime && accuracy < bestAccuracy) {
                    bestLocation = location;
                    bestAccuracy = accuracy;
                    bestTime = time;
                } else if (time < minTime && bestAccuracy == Float.MAX_VALUE && time > bestTime) {
                    bestLocation = location;
                    bestTime = time;
                }
            }
        }

        return bestLocation;
    }

    @Override
    public void removeLocationUpdates(LocationListener listener) {
        locationManager.removeUpdates(gpsListener);
        locationManager.removeUpdates(networkListener);
    }

    @Override
    public void removeLocationUpdates(PendingIntent callbackIntent) {
        throw new RuntimeException("Sorry, not yet implemented");
    }

    @Override
    public void requestLocationUpdates(LocationRequest request, LocationListener listener,
            Looper looper) {
        throw new RuntimeException("Sorry, not yet implemented");
    }

    @Override
    public void requestLocationUpdates(LocationRequest request, LocationListener listener) {
        this.locationListener = listener;
        this.fastestInterval = request.getFastestInterval();
        this.smallestDisplacement = request.getSmallestDisplacement();

        if (!mockMode) {
            connectGpsListener();
            connectNetworkListener();
        }
    }

    @Override
    public void requestLocationUpdates(LocationRequest request, PendingIntent callbackIntent) {
        throw new RuntimeException("Sorry, not yet implemented");
    }

    @Override
    public void setMockLocation(Location mockLocation) {
        this.mockLocation = mockLocation;
        if (locationListener != null) {
            locationListener.onLocationChanged(mockLocation);
        }
    }

    @Override
    public void setMockMode(boolean isMockMode) {
        if (mockMode == isMockMode) {
            return;
        }

        mockMode = isMockMode;

        if (isMockMode) {
            removeLocationUpdates(locationListener);
        } else {
            createGpsAndNetworkListeners();
            connectGpsListener();
            connectNetworkListener();
        }
    }

    @Override
    public void setMockTrace(final File file) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                final XPath xPath = XPathFactory.newInstance().newXPath();
                final String expression = "//" + TAG_TRACK_POINT;
                final String speedExpression = "//" + TAG_SPEED;

                NodeList nodeList = null;
                NodeList speedList = null;
                try {
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document document = builder.parse(file);
                    nodeList = (NodeList) xPath.compile(expression)
                            .evaluate(document, XPathConstants.NODESET);
                    speedList = (NodeList) xPath.compile(speedExpression)
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

                parse(nodeList, speedList);
            }
        }).start();
    }

    private void parse(NodeList nodeList, NodeList speedList) {
        if (nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                String lat = node.getAttributes().getNamedItem(TAG_LAT).getNodeValue();
                String lng = node.getAttributes().getNamedItem(TAG_LNG).getNodeValue();

                final Location location = new Location(MOCK_PROVIDER);
                location.setLatitude(Double.parseDouble(lat));
                location.setLongitude(Double.parseDouble(lng));
                location.setTime(System.currentTimeMillis());
                location.setSpeed(Float.parseFloat(speedList.item(i).getFirstChild()
                        .getNodeValue()));

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

    private void createGpsAndNetworkListeners() {
        gpsListener = new GpsListener();
        networkListener = new NetworkListener();
    }

    private void connectGpsListener() {
        try {
            locationManager.requestLocationUpdates(GPS_PROVIDER, fastestInterval,
                    smallestDisplacement, gpsListener);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Unable to register for GPS updates.", e);
        }
    }

    private void connectNetworkListener() {
        try {
            locationManager.requestLocationUpdates(NETWORK_PROVIDER, fastestInterval,
                    smallestDisplacement, networkListener);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Unable to register for network updates.", e);
        }
    }

    private class GpsListener implements android.location.LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            gpsAccuracy = location.getAccuracy();
            if (locationListener != null && gpsAccuracy <= networkAccuracy) {
                locationListener.onLocationChanged(location);
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {
        }
    }

    private class NetworkListener implements android.location.LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            networkAccuracy = location.getAccuracy();
            if (locationListener != null && networkAccuracy <= gpsAccuracy) {
                locationListener.onLocationChanged(location);
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {
        }
    }
}
