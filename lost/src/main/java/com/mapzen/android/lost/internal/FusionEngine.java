package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationRequest;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static android.location.LocationManager.PASSIVE_PROVIDER;

/**
 * Location engine that fuses GPS and network locations.
 */
public class FusionEngine extends LocationEngine implements LocationListener {
  private static final String TAG = FusionEngine.class.getSimpleName();

  /** Location updates more than 60 seconds old are considered stale. */
  public static final long RECENT_UPDATE_THRESHOLD_IN_MILLIS = 60 * 1000;
  public static final long RECENT_UPDATE_THRESHOLD_IN_NANOS =
      RECENT_UPDATE_THRESHOLD_IN_MILLIS * 1000000;

  private final LocationManager locationManager;

  private Location gpsLocation;
  private Location networkLocation;

  static Clock clock = new SystemClock();

  public FusionEngine(Context context, Callback callback) {
    super(context, callback);
    locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
  }

  @Override public Location getLastLocation() {
    final List<String> providers = locationManager.getAllProviders();
    final long minTime = clock.getCurrentTimeInMillis() - RECENT_UPDATE_THRESHOLD_IN_MILLIS;

    Location bestLocation = null;
    float bestAccuracy = Float.MAX_VALUE;
    long bestTime = Long.MIN_VALUE;

    for (String provider : providers) {
      try {
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
      } catch (SecurityException e) {
        Log.e(TAG, "Permissions not granted for provider: " + provider, e);
      }
    }

    return bestLocation;
  }

  @Override public boolean isProviderEnabled(String provider) {
    return locationManager.isProviderEnabled(provider);
  }

  @Override protected void enable() {
    long networkInterval = Long.MAX_VALUE;
    long gpsInterval = Long.MAX_VALUE;
    long passiveInterval = Long.MAX_VALUE;
    for (LocationRequest request : getRequest().getRequests()) {
      switch (request.getPriority()) {
        case LocationRequest.PRIORITY_HIGH_ACCURACY:
          if (request.getInterval() < gpsInterval) {
            gpsInterval = request.getInterval();
          }
          if (request.getInterval() < networkInterval) {
            networkInterval = request.getInterval();
          }
          break;
        case LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY:
        case LocationRequest.PRIORITY_LOW_POWER:
          if (request.getInterval() < networkInterval) {
            networkInterval = request.getInterval();
          }
          break;
        case LocationRequest.PRIORITY_NO_POWER:
          if (request.getInterval() < passiveInterval) {
            passiveInterval = request.getInterval();
          }
          break;
        default:
          break;
      }
    }

    boolean checkGps = false;
    if (gpsInterval < Long.MAX_VALUE) {
      enableGps(gpsInterval);
      checkGps = true;
    }
    if (networkInterval < Long.MAX_VALUE) {
      enableNetwork(networkInterval);
      if (checkGps) {
        Location lastGps = locationManager.getLastKnownLocation(GPS_PROVIDER);
        Location lastNetwork = locationManager.getLastKnownLocation(NETWORK_PROVIDER);
        if (lastGps != null && lastNetwork != null) {
          boolean useGps = isBetterThan(lastGps, lastNetwork);
          if (useGps) {
            checkLastKnownGps();
          } else {
            checkLastKnownNetwork();
          }
        } else if (lastGps != null) {
          checkLastKnownGps();
        } else {
          checkLastKnownNetwork();
        }
      } else {
        checkLastKnownNetwork();
      }
    }
    if (passiveInterval < Long.MAX_VALUE) {
      enablePassive(passiveInterval);
      checkLastKnownPassive();
    }
  }

  @Override protected void disable() throws SecurityException {
    if (locationManager != null) {
      locationManager.removeUpdates(this);
    }
  }

  private void enableGps(long interval) throws SecurityException {
    try {
      locationManager.requestLocationUpdates(GPS_PROVIDER, interval, 0, this, getLooper());
    } catch (IllegalArgumentException e) {
      Log.e(TAG, "Unable to register for GPS updates.", e);
    }
  }

  private void enableNetwork(long interval) throws SecurityException {
    try {
      locationManager.requestLocationUpdates(NETWORK_PROVIDER, interval, 0, this, getLooper());
    } catch (IllegalArgumentException e) {
      Log.e(TAG, "Unable to register for network updates.", e);
    }
  }

  private void enablePassive(long interval) throws SecurityException {
    try {
      locationManager.requestLocationUpdates(PASSIVE_PROVIDER, interval, 0, this, getLooper());
    } catch (IllegalArgumentException e) {
      Log.e(TAG, "Unable to register for passive updates.", e);
    }
  }

  private void checkLastKnownGps() {
    checkLastKnownAndNotify(GPS_PROVIDER);
  }

  private void checkLastKnownNetwork() {
    checkLastKnownAndNotify(NETWORK_PROVIDER);
  }

  private void checkLastKnownPassive() {
    checkLastKnownAndNotify(PASSIVE_PROVIDER);
  }

  private void checkLastKnownAndNotify(String provider) {
    Location location = locationManager.getLastKnownLocation(provider);
    if (location != null) {
      onLocationChanged(location);
    }
  }

  @Override public void onLocationChanged(Location location) {
    if (GPS_PROVIDER.equals(location.getProvider())) {
      gpsLocation = location;
      if (getCallback() != null && isBetterThan(gpsLocation, networkLocation)) {
        getCallback().reportLocation(location);
      }
    } else if (NETWORK_PROVIDER.equals(location.getProvider())) {
      networkLocation = location;
      if (getCallback() != null && isBetterThan(networkLocation, gpsLocation)) {
        getCallback().reportLocation(location);
      }
    }
  }

  @Override public void onStatusChanged(String provider, int status, Bundle extras) {
  }

  @Override public void onProviderEnabled(String provider) {
    final Callback callback = getCallback();
    if (callback != null) {
      callback.reportProviderEnabled(provider);
    }
  }

  @Override public void onProviderDisabled(String provider) {
    final Callback callback = getCallback();
    if (callback != null) {
      callback.reportProviderDisabled(provider);
    }
  }

  public static boolean isBetterThan(Location locationA, Location locationB) {
    if (locationA == null) {
      return false;
    }

    if (locationB == null) {
      return true;
    }

    if (SystemClock.getTimeInNanos(locationA)
        > SystemClock.getTimeInNanos(locationB) + RECENT_UPDATE_THRESHOLD_IN_NANOS) {
      return true;
    }

    if (!locationA.hasAccuracy()) {
      return false;
    }

    if (!locationB.hasAccuracy()) {
      return true;
    }

    return locationA.getAccuracy() < locationB.getAccuracy();
  }
}
