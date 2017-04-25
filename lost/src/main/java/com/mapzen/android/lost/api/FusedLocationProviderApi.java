package com.mapzen.android.lost.api;

import android.app.PendingIntent;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.RequiresPermission;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public interface FusedLocationProviderApi {

  /**
   * @deprecated Use {@link LocationResult#hasResult(android.content.Intent)} and
   * {@link LocationResult#extractResult(android.content.Intent)}.
   */
  @Deprecated String KEY_LOCATION_CHANGED = "com.mapzen.android.lost.LOCATION";

  /**
   * Returns the best most recent location currently available.
   *
   * If a location is not available, which should happen very rarely, null will be returned.
   * The best accuracy available while respecting the location permissions will be returned.
   * @param client The client to return location for.
   * @return The best, most recent location available.
   * @throws IllegalStateException if the client is not connected at the time of this call.
   */
  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  Location getLastLocation(LostApiClient client);

  /**
   * Returns the availability of location data. Location is considered available if one of the
   * following conditions is met:
   *
   * GPS is enabled and the {@link android.location.LocationManager} has a last known GPS location
   * or
   * Network location is enabled and the {@link android.location.LocationManager} has a last known
   * Network location
   *
   * @param client The client to return availability for.
   * @return The availability of location data.
   * @throws IllegalStateException if the client is not connected at the time of this call.
   */
  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  LocationAvailability getLocationAvailability(LostApiClient client);

  /**
   * Request location updates to be called on the {@link LocationListener}.
   *
   * This method is suited for foreground use cases and callbacks for the {@link LocationListener}
   * will be made on the calling thread, which must already be a prepared looper thread. For
   * handling on a background thread it is recommended that you use the {@link PendingIntent}
   * version of this method {@link FusedLocationProviderApi#requestLocationUpdates(LostApiClient,
   * LocationRequest, PendingIntent)}.
   *
   * @param client Client to request updates for, must be connected when making this call.
   * @param request Specifies desired location accuracy, update interval, etc.
   * @param listener Listener to make calls on when location becomes available.
   * @return a {@link PendingResult} for the call to check whether call was successful.
   * @throws IllegalStateException if the client is not connected at the time of this call.
   */
  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  PendingResult<Status> requestLocationUpdates(LostApiClient client, LocationRequest request,
      LocationListener listener);

  /**
   * Request location updates to be called on the {@link LocationListener} on a given thread.
   *
   * This method is suited for foreground use cases and callbacks for the {@link LocationListener}
   * will be made on the given thread, which must already be a prepared looper thread. For
   * handling on a background thread it is recommended that you use the {@link PendingIntent}
   * version of this method {@link FusedLocationProviderApi#requestLocationUpdates(LostApiClient,
   * LocationRequest, PendingIntent)}.
   *
   * @param client Client to request updates for, must be connected when making this call.
   * @param request Specifies desired location accuracy, update interval, etc.
   * @param listener Listener to make calls on when location becomes available.
   * @param looper Looper to implement the listener callbacks on.
   * @return a {@link PendingResult} for the call to check whether call was successful.
   * @throws IllegalStateException if the client is not connected at the time of this call.
   */
  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  PendingResult<Status> requestLocationUpdates(LostApiClient client, LocationRequest request,
      LocationListener listener, Looper looper);

  /**
   * Request location updates to be called on the {@link LocationCallback} on a given thread.
   *
   * This method is suited for foreground use cases and callbacks for the {@link LocationCallback}
   * will be made on the given thread, which must already be a prepared looper thread. For
   * handling on a background thread it is recommended that you use the {@link PendingIntent}
   * version of this method {@link FusedLocationProviderApi#requestLocationUpdates(LostApiClient,
   * LocationRequest, PendingIntent)}.
   *
   * @param client Client to request updates for, must be connected when making this call.
   * @param request Specifies desired location accuracy, update interval, etc.
   * @param callback Callback to make calls on when location becomes available.
   * @param looper Looper to implement the listener callbacks on.
   * @return a {@link PendingResult} for the call to check whether call was successful.
   * @throws IllegalStateException if the client is not connected at the time of this call.
   */
  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  PendingResult<Status> requestLocationUpdates(LostApiClient client, LocationRequest request,
      LocationCallback callback, Looper looper);

  /**
   * Requests location updates with a callback on the specified PendingIntent.
   *
   * This method is suited for background use cases such as receiving location updates even when
   * the app has been killed by the system. In order to do so, use a PendingIntent for a started
   * service. For foreground use cases use the other ({@link LocationListener} &
   * {@link LocationCallback}) versions of this method.
   *
   * Both {@link LocationResult} and {@link LocationAvailability} are sent with the
   * {@link PendingIntent}. You can extract them using
   * {@link LocationResult#hasResult(android.content.Intent)},
   * {@link LocationResult#extractResult(android.content.Intent)},
   * {@link LocationAvailability#hasLocationAvailability(android.content.Intent)}, and
   * {@link LocationAvailability#extractLocationAvailability(android.content.Intent)}.
   *
   * @param client Client to request updates for, must be connected when making this call.
   * @param request Specifies desired location accuracy, update interval, etc.
   * @param callbackIntent Intent to be sent for each location update.
   * @return a {@link PendingResult} for the call to check whether call was successful.
   * @throws IllegalStateException if the client is not connected at the time of this call.
   */
  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  PendingResult<Status> requestLocationUpdates(LostApiClient client, LocationRequest request,
      PendingIntent callbackIntent);

  /**
   * Removes location updates for the {@link LocationListener}
   *
   * @param client Client which registered the listener. The client must be connected at the time
   * of this call.
   * @param listener Listener to remove updates for.
   * @return a {@link PendingResult} for the call to check whether call was successful.
   * @throws IllegalStateException if the client is not connected at the time of this call.
   */
  PendingResult<Status> removeLocationUpdates(LostApiClient client, LocationListener listener);

  /**
   * Removes location updates for the {@link PendingIntent}
   *
   * @param client Client which registered the pending intent. The client must be connected at the
   * time of this call.
   * @param callbackIntent Intent to remove updates for.
   * @return a {@link PendingResult} for the call to check whether call was successful.
   * @throws IllegalStateException if the client is not connected at the time of this call.
   */
  PendingResult<Status> removeLocationUpdates(LostApiClient client, PendingIntent callbackIntent);

  /**
   * Remove location updates for the {@link LocationCallback}
   *
   * @param client Client which registered the location callback. The client must be connected at
   * the time of this call.
   * @param callback Callback to remove updates for.
   * @return a {@link PendingResult} for the call to check whether call was successful.
   * @throws IllegalStateException if the client is not connected at the time of this call.
   */
  PendingResult<Status> removeLocationUpdates(LostApiClient client, LocationCallback callback);

  /**
   * Sets whether or not the location provider is in mock mode.
   *
   * This effects all {@link LostApiClient}s connected to the {@link FusedLocationProviderApi}.
   * Mock mode must be enabled before clients are able to call
   * {@link FusedLocationProviderApi#setMockLocation(LostApiClient, Location)} and
   * {@link FusedLocationProviderApi#setMockTrace(LostApiClient, String, String)}
   *
   * @param client Connected client.
   * @param isMockMode Whether mock mode should be enabled or not.
   * @return a {@link PendingResult} for the call to check whether call was successful.
   * @throws IllegalStateException if the client is not connected at the time of this call.
   */
  PendingResult<Status> setMockMode(LostApiClient client, boolean isMockMode);

  /**
   * Sets the mock location to be used for the location provider.
   *
   * Mock mode must be enabled before calling this method.
   *
   * @param client Connected client.
   * @param mockLocation Location to be set for the location provider.
   * @return a {@link PendingResult} for the call to check whether call was successful.
   * @throws IllegalStateException if the client is not connected at the time of this call.
   */
  PendingResult<Status> setMockLocation(LostApiClient client, Location mockLocation);

  /**
   * Sets a mock trace file to be used to report location for the location provider.
   *
   * Mock mode must be enabled before calling this method.
   *
   * @param client Connected client.
   * @param path storage directory containing GPX trace to be used to report location.
   * @param filename filename of GPX trace to be used to report location.
   * @return a {@link PendingResult} for the call to check whether call was successful.
   * @throws IllegalStateException if the client is not connected at the time of this call.
   */
  PendingResult<Status> setMockTrace(LostApiClient client, final String path,
      final String filename);
}
