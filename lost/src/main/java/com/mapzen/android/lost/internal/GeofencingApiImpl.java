package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.Geofence;
import com.mapzen.android.lost.api.GeofencingApi;
import com.mapzen.android.lost.api.GeofencingRequest;
import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.android.lost.api.PendingResult;
import com.mapzen.android.lost.api.Status;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.support.annotation.RequiresPermission;

import java.util.HashMap;
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.app.AlarmManager.RTC_WAKEUP;
import static com.mapzen.android.lost.api.Geofence.LOITERING_DELAY_NONE;

/**
 * Implementation of the {@link GeofencingApi}.
 */
public class GeofencingApiImpl extends ApiImpl implements GeofencingApi {

  private Context context;
  private LocationManager locationManager;
  private final HashMap<String, PendingIntent> pendingIntentMap;
  private Intent internalIntent;
  private IntentFactory geofencingServiceIntentFactory;
  private IntentFactory dwellServiceIntentFactory;

  private IdGenerator idGenerator;
  private HashMap<Integer, PendingIntent> idToPendingIntent = new HashMap<>();
  private HashMap<Integer, Geofence> idToGeofence = new HashMap<>();

  private HashMap<Geofence, PendingIntent> enteredFences = new HashMap<>();

  public GeofencingApiImpl(IntentFactory geofenceFactory, IntentFactory dwellFactory,
      IdGenerator generator) {
    geofencingServiceIntentFactory = geofenceFactory;
    dwellServiceIntentFactory = dwellFactory;
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
    throwIfNotConnected(client);
    List<Geofence> geofences = geofencingRequest.getGeofences();
    addGeofences(client, geofences, pendingIntent);
    return new SimplePendingResult(true);
  }

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  @Override public PendingResult<Status> addGeofences(LostApiClient client,
      List<Geofence> geofences, PendingIntent pendingIntent) throws SecurityException {
    throwIfNotConnected(client);
    for (Geofence geofence : geofences) {
      addGeofence(client, geofence, pendingIntent);
    }
    return new SimplePendingResult(true);
  }

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  private PendingResult<Status> addGeofence(LostApiClient client, Geofence geofence,
      PendingIntent pendingIntent) throws SecurityException {
    checkGeofence(geofence);
    int pendingIntentId = idGenerator.generateId();
    internalIntent = geofencingServiceIntentFactory.createIntent(context);
    internalIntent.addCategory(String.valueOf(pendingIntentId));
    ParcelableGeofence pGeofence = (ParcelableGeofence) geofence;

    idToPendingIntent.put(pendingIntentId, pendingIntent);
    idToGeofence.put(pendingIntentId, pGeofence);

    PendingIntent internalPendingIntent = geofencingServiceIntentFactory.createPendingIntent(
        context, pendingIntentId, internalIntent);

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
    throwIfNotConnected(client);
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
    throwIfNotConnected(client);
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

  public void geofenceEntered(Geofence geofence, int pendingIntentId) {
    ParcelableGeofence parcelableGeofence = (ParcelableGeofence) geofence;
    long loiterDelay = parcelableGeofence.getLoiteringDelayMs();

    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

    Intent intent = dwellServiceIntentFactory.createIntent(context);
    intent.addCategory(String.valueOf(pendingIntentId));
    PendingIntent pendingIntent = dwellServiceIntentFactory.createPendingIntent(context,
        pendingIntentId, intent);
    alarmManager.set(RTC_WAKEUP, System.currentTimeMillis() + loiterDelay, pendingIntent);

    enteredFences.put(geofence, pendingIntent);
  }

  public void geofenceExited(Geofence geofence) {
    PendingIntent pendingIntent = enteredFences.get(geofence);
    if (pendingIntent != null) {
      AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
      alarmManager.cancel(pendingIntent);
      enteredFences.remove(geofence);
    }
  }

  private void checkGeofence(Geofence geofence) {
    ParcelableGeofence pGeofence = (ParcelableGeofence) geofence;
    if ((pGeofence.getTransitionTypes() & Geofence.GEOFENCE_TRANSITION_DWELL) != 0) {
      if (pGeofence.getLoiteringDelayMs() == LOITERING_DELAY_NONE) {
        throw new IllegalStateException("Dwell transition type requested without loitering delay. "
            + "Please set a loitering delay for this geofence.");
      }
    }
  }
}
