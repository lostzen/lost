package com.mapzen.android.lost.api;

import android.app.PendingIntent;
import android.support.annotation.RequiresPermission;

import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public interface GeofencingApi {

  /**
   * Extra passed in {@link android.content.Intent} that is fired when requesting proximity
   * updates. Value is an integer.
   */
  String EXTRA_TRANSITION = "com.mapzen.lost.extra.transition";

  /**
   * Extra passed in {@link android.content.Intent} that is fired when requesting proximity
   * updates. Value is array of {@link Geofence} objects.
   */
  String EXTRA_GEOFENCE_LIST = "com.mapzen.lost.extra.geofence_list";

  /**
   * Extra passed in {@link android.content.Intent} that is fired when requesting proximity
   * updates. Value is {@link android.location.Location} object.
   */
  String EXTRA_TRIGGERING_LOCATION = "com.mapzen.lost.extra.triggering_location";

  /**
   * Sets {@link PendingIntent} to be notified when the device enters or exits one of the specified
   * geofences.
   *
   * @param client Connected client to receive geofence updates for.
   * @param geofencingRequest Request containing geofences to receive updates for.
   * @param pendingIntent Intent to be notified when geofences are entered/exited.
   * @throws IllegalStateException if the client is not connected at the time of this call.
   */
  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  PendingResult<Status> addGeofences(LostApiClient client, GeofencingRequest geofencingRequest,
      PendingIntent pendingIntent);

  /**
   *
   * @deprecated Use {@link GeofencingApi#addGeofences(LostApiClient, GeofencingRequest,
   * PendingIntent)} instead.
   *
   * Sets {@link PendingIntent} to be notified when the device enters or exits one of the specified
   * geofences.
   *
   * @param client Connected client to receive geofence updates for.
   * @param geofences Geofences to receive updates for.
   * @param pendingIntent Intent to be notified when geofences are entered/exited.
   * @throws IllegalStateException if the client is not connected at the time of this call.
   */
  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  PendingResult<Status> addGeofences(LostApiClient client, List<Geofence> geofences,
      PendingIntent pendingIntent);

  /**
   * Removes geofences for a given list of request ids
   *
   * @param client Connected client to remove geofence updates for.
   * @param geofenceRequestIds Geofence ids to remove updates for.
   * @throws IllegalStateException if the client is not connected at the time of this call.
   */
  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  PendingResult<Status> removeGeofences(LostApiClient client, List<String> geofenceRequestIds);

  /**
   * Removes geofences for a given {@link PendingIntent}
   * @param client Connected client to remove geofence updates for.
   * @param pendingIntent Intent to remove updates for.
   * @throws IllegalStateException if the client is not connected at the time of this call.
   */
  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  PendingResult<Status> removeGeofences(LostApiClient client, PendingIntent pendingIntent);
}
