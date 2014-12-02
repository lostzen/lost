package com.mapzen.android.lost.internal;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.Context;
import android.location.Location;
import android.os.Handler;

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
 * Mock implementation of LocationEngine that reports single locations and/or full GPX traces.
 */
public class MockEngine extends LocationEngine {
    public static final String MOCK_PROVIDER = "mock";

    // GPX tags
    public static final String TAG_TRACK_POINT = "trkpt";
    public static final String TAG_SPEED = "speed";
    public static final String TAG_LAT = "lat";
    public static final String TAG_LNG = "lon";

    private Location location;

    public MockEngine(Context context, FusionEngine.Callback callback) {
        super(context, callback);
    }

    public void setLocation(Location location) {
        this.location = location;
        if (getCallback() != null) {
            getCallback().reportLocation(location);
        }
    }

    @Override
    public Location getLastLocation() {
        return location;
    }

    @Override
    protected void enable() {
    }

    @Override
    protected void disable() {
    }

    /**
     * Set a GPX trace file to be replayed as mock locations.
     */
    public void setTrace(final File file) {
        if (getRequest() == null) {
            return;
        }

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

                new Handler(getContext().getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        setLocation(location);
                    }
                });

                try {
                    Thread.sleep(getRequest().getFastestInterval());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
