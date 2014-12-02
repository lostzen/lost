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
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * Implementation of the {@link FusedLocationProviderApi}.
 */
public class FusedLocationProviderApiImpl implements FusedLocationProviderApi,
        FusionEngine.Callback {
    public static final String TAG = FusedLocationProviderApiImpl.class.getSimpleName();

    // Name of the mock location provider.
    public static final String MOCK_PROVIDER = "mock";

    private final Context context;
    private LocationListener locationListener;

    private long fastestInterval;

    private boolean mockMode;
    private Location mockLocation;

    // GPX tags
    public static final String TAG_TRACK_POINT = "trkpt";
    public static final String TAG_SPEED = "speed";
    public static final String TAG_LAT = "lat";
    public static final String TAG_LNG = "lon";

    private FusionEngine fusionEngine;

    public FusedLocationProviderApiImpl(Context context) {
        this.context = context;
        fusionEngine = new FusionEngine(context, this);
    }

    @Override
    public Location getLastLocation() {
        if (mockMode) {
            return mockLocation;
        }

        return fusionEngine.getLastLocation();
    }

    @Override
    public void removeLocationUpdates(LocationListener listener) {
        fusionEngine.setRequest(null);
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
        if (!mockMode) {
            fusionEngine.setRequest(request);
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
        this.mockMode = isMockMode;
        if (isMockMode) {
            fusionEngine.setRequest(null);
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

    @Override
    public void reportLocation(Location location) {
        if (locationListener != null) {
            locationListener.onLocationChanged(location);
        }
    }
}
