package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.Geofence;
import com.mapzen.android.lost.api.GeofencingApi;
import com.mapzen.android.lost.api.GeofencingRequest;
import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.android.lost.api.PendingResult;
import com.mapzen.android.lost.api.Status;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.support.annotation.RequiresPermission;

import java.util.HashMap;
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * Implementation of the {@link GeofencingApi}.
 */
public class GeofencingApiImpl implements GeofencingApi {

  private Context context;
  private LocationManager locationManager;
  private final HashMap<String, PendingIntent> pendingIntentMap;
  private Intent internalIntent;
  private IntentFactory intentFactory;

  private IdGenerator idGenerator;
  private HashMap<Integer, PendingIntent> idToPendingIntent = new HashMap<>();
  private HashMap<Integer, Geofence> idToGeofence = new HashMap<>();

  public GeofencingApiImpl(IntentFactory factory, IdGenerator generator) {
    intentFactory = factory;
    idGenerator = generator;
    pendingIntentMap = new HashMap<>();
  }

  public void connect(Context context) {
    this.context = context;
    locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
  }

  public boolean isConnected() {
    return locationManager != null;
  }

  public void disconnect() {
    locationManager = null;
  }

  @Override
  public PendingResult<Status> addGeofences(LostApiClient client,
      GeofencingRequest geofencingRequest, PendingIntent pendingIntent) throws SecurityException {
    List<Geofence> geofences = geofencingRequest.getGeofences();
    addGeofences(client, geofences, pendingIntent);
    return new SimplePendingResult(true);
  }

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  @Override public PendingResult<Status> addGeofences(LostApiClient client,
      List<Geofence> geofences, PendingIntent pendingIntent) throws SecurityException {
    for (Geofence geofence : geofences) {
      addGeofence(client, geofence, pendingIntent);
    }
    return new SimplePendingResult(true);
  }

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  private PendingResult<Status> addGeofence(LostApiClient client, Geofence geofence,
      PendingIntent pendingIntent) throws SecurityException {

    int pendingIntentId = idGenerator.generateId();
    internalIntent = intentFactory.createIntent(context);
    internalIntent.addCategory(String.valueOf(pendingIntentId));
    ParcelableGeofence pGeofence = (ParcelableGeofence) geofence;

    idToPendingIntent.put(pendingIntentId, pendingIntent);
    idToGeofence.put(pendingIntentId, pGeofence);

    PendingIntent internalPendingIntent = intentFactory.createPendingIntent(context,
        pendingIntentId, internalIntent);

    String requestId = String.valueOf(pGeofence.hashCode());
    locationManager.addProximityAlert(
            pGeofence.getLatitude(),
            pGeofence.getLongitude(),
            pGeofence.getRadius(),
            pGeofence.getDuration(),
            internalPendingIntent);
    if (pGeofence.getRequestId() != null && !pGeofence.getRequestId().isEmpty()) {
      requestId = pGeofence.getRequestId();
    }
    pendingIntentMap.put(requestId, pendingIntent);
    return new SimplePendingResult(true);
  }

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  @Override public PendingResult<Status> removeGeofences(LostApiClient client,
      List<String> geofenceRequestIds) {
    boolean hasResult = false;
    for (String geofenceRequestId : geofenceRequestIds) {
      if (pendingIntentMap.containsKey(geofenceRequestId)) {
        hasResult = true;
      }
      removeGeofences(client, geofenceRequestId);
    }
    return new SimplePendingResult(hasResult);
  }

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  private void removeGeofences(LostApiClient client, String geofenceRequestId)
      throws SecurityException {
    PendingIntent pendingIntent = pendingIntentMap.get(geofenceRequestId);
    removeGeofences(client, pendingIntent);
  }

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  @Override public PendingResult<Status> removeGeofences(LostApiClient client,
      PendingIntent pendingIntent) throws SecurityException {
    boolean hasResult = false;
    if (pendingIntentMap.values().contains(pendingIntent)) {
      hasResult = true;
    }
    locationManager.removeProximityAlert(pendingIntent);
    pendingIntentMap.values().remove(pendingIntent);
    return new SimplePendingResult(hasResult);
  }

  public PendingIntent pendingIntentForIntentId(int intentId) {
    return idToPendingIntent.get(intentId);
  }

  public Geofence geofenceForIntentId(int intentId) {
    return idToGeofence.get(intentId);
  }

}
