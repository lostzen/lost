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

class TraceThread extends Thread {
  // GPX tags
  private static final String TAG_TRACK_POINT = "trkpt";
  private static final String TAG_SPEED = "speed";
  private static final String TAG_LAT = "lat";
  private static final String TAG_LNG = "lon";

  private boolean canceled;
  private Location previous;

  private final Context context;
  private final File traceFile;
  private final MockEngine engine;
  private final SleepFactory sleepFactory;

  TraceThread(Context context, File traceFile, MockEngine engine, SleepFactory sleepFactory) {
    this.context = context;
    this.traceFile = traceFile;
    this.engine = engine;
    this.sleepFactory = sleepFactory;
  }

  public void cancel() {
    canceled = true;
    interrupt();
  }

  public boolean isCanceled() {
    return canceled;
  }

  @Override public void run() {
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    final XPath xPath = XPathFactory.newInstance().newXPath();
    final String expression = "//" + TAG_TRACK_POINT;
    final String speedExpression = "//" + TAG_SPEED;

    NodeList nodeList = null;
    NodeList speedList = null;
    try {
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse(traceFile);
      nodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
      speedList =
          (NodeList) xPath.compile(speedExpression).evaluate(document, XPathConstants.NODESET);
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

  private void parse(NodeList nodeList, NodeList speedList) {
    if (nodeList != null) {
      for (int i = 0; i < nodeList.getLength(); i++) {
        postMockLocation(nodeToLocation(nodeList, speedList, i));
        sleepFastestInterval();
      }
    }
  }

  private Location nodeToLocation(NodeList nodeList, NodeList speedList, int i) {
    final Node node = nodeList.item(i);
    String lat = node.getAttributes().getNamedItem(TAG_LAT).getNodeValue();
    String lng = node.getAttributes().getNamedItem(TAG_LNG).getNodeValue();

    final Location location = new Location(MockEngine.MOCK_PROVIDER);
    location.setLatitude(Double.parseDouble(lat));
    location.setLongitude(Double.parseDouble(lng));
    location.setTime(System.currentTimeMillis());
    if (speedList.item(i) != null && speedList.item(i).getFirstChild() != null) {
      location.setSpeed(Float.parseFloat(speedList.item(i).getFirstChild().getNodeValue()));
    }

    if (previous != null) {
      location.setBearing(previous.bearingTo(location));
    }

    previous = location;
    return location;
  }

  private void sleepFastestInterval() {
    final LocationRequestUnbundled request = engine.getRequest();
    if (request != null) {
      sleepFactory.sleep(request.getFastestInterval());
    }
  }

  private void postMockLocation(final Location mockLocation) {
    new Handler(context.getMainLooper()).post(new Runnable() {
      @Override public void run() {
        if (!canceled) {
          engine.setLocation(mockLocation);
        }
      }
    });
  }
}
