package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationAvailability;
import com.mapzen.android.lost.api.LocationRequest;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;
import android.support.annotation.RequiresPermission;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * Base class for {@link com.mapzen.android.lost.internal.FusionEngine} and
 * {@link com.mapzen.android.lost.internal.MockEngine} classes.
 */
public abstract class LocationEngine {
  private final Context context;
  private final Callback callback;
  private LocationRequestUnbundled request;

  public LocationEngine(Context context, Callback callback) {
    this.context = context;
    this.callback = callback;
    request = new LocationRequestUnbundled();
  }

  /**
   * Return most best recent location available.
   */
  public abstract Location getLastLocation();

  public abstract boolean isProviderEnabled(String provider);

  /**
   * Enables the engine on receiving a valid location request. Disables the engine on receiving a
   * {@code null} request.
   *
   * @param request Valid location request to enable or {@code null} to disable.
   */
  public void setRequest(LocationRequest request) {
    if (request != null) {
      this.request.addRequest(request);
      enable();
    } else {
      this.request.removeAllRequests();
      disable();
    }
  }

  /**
   * Subclass should perform all operations required to enable the engine. (ex. Register for
   * location updates.)
   */
  protected abstract void enable();

  /**
   * Subclass should perform all operations required to disable the engine. (ex. Remove location
   * updates.)
   */
  protected abstract void disable();

  protected Context getContext() {
    return context;
  }

  protected Looper getLooper() {
    return context.getMainLooper();
  }

  protected Callback getCallback() {
    return callback;
  }

  protected LocationRequestUnbundled getRequest() {
    return request;
  }

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  protected LocationAvailability createLocationAvailability() {
    LocationManager locationManager = (LocationManager) context.getSystemService(
        Context.LOCATION_SERVICE);
    boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    boolean gpsLocationExists = locationManager.getLastKnownLocation(
        LocationManager.GPS_PROVIDER) != null;
    boolean networkLocationExists = locationManager.getLastKnownLocation(
        LocationManager.NETWORK_PROVIDER) != null;
    return new LocationAvailability((gpsEnabled && gpsLocationExists)
        || (networkEnabled && networkLocationExists));
  }

  public interface Callback {
    void reportLocation(Location location);

    void reportProviderDisabled(String provider);

    void reportProviderEnabled(String provider);
  }
}
