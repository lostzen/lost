package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.Geofence;
import com.mapzen.android.lost.api.GeofencingRequest;
import com.mapzen.android.lost.api.LostApiClient;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import android.app.PendingIntent;
import android.content.Context;
import android.location.LocationManager;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.LOCATION_SERVICE;
import static com.mapzen.android.lost.api.Geofence.NEVER_EXPIRE;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@SuppressWarnings("MissingPermission")
public class GeofencingApiTest {
  Context context;
  LocationManager locationManager;
  GeofencingApiImpl geofencingApi;

  @Before public void setUp() throws Exception {
    context = mock(Context.class);
    locationManager = mock(LocationManager.class);
    when(context.getSystemService(LOCATION_SERVICE)).thenReturn(locationManager);
    geofencingApi = new GeofencingApiImpl();
    geofencingApi.connect(context);
  }

  @Test public void shouldNotBeNull() throws Exception {
    assertThat(geofencingApi).isNotNull();
  }

  @Test public void addGeofences_shouldAddProximityAlert() throws Exception {
    LostApiClient client = new LostApiClient.Builder(context).build();
    Geofence geofence = new Geofence.Builder().setRequestId("test_id")
        .setCircularRegion(1, 2, 3)
        .setExpirationDuration(NEVER_EXPIRE)
        .build();
    GeofencingRequest request = new GeofencingRequest.Builder().addGeofence(geofence).build();
    PendingIntent intent = Mockito.mock(PendingIntent.class);
    geofencingApi.addGeofences(client, request, intent);
    Mockito.verify(locationManager, times(1)).addProximityAlert(1, 2, 3, NEVER_EXPIRE, intent);
  }

  //as suggested here: https://github.com/mapzen/LOST/pull/88#discussion_r77384417
  @Test public void addGeofencesArray_shouldAddProximityAlert() {
    LostApiClient client = new LostApiClient.Builder(context).build();

    Geofence geofence = new Geofence.Builder()
        .setRequestId("test_id")
        .setCircularRegion(1, 2, 3)
        .setExpirationDuration(NEVER_EXPIRE)
        .build();
    Geofence anotherGeofence = new Geofence.Builder()
        .setRequestId("test_id_1")
        .setCircularRegion(4, 5, 6)
        .setExpirationDuration(NEVER_EXPIRE)
        .build();
    ArrayList<Geofence> geofences = new ArrayList<>();
    geofences.add(geofence);
    geofences.add(anotherGeofence);
    PendingIntent intent = Mockito.mock(PendingIntent.class);
    geofencingApi.addGeofences(client, geofences, intent);
    Mockito.verify(locationManager, times(1)).addProximityAlert(1, 2, 3, NEVER_EXPIRE, intent);
    Mockito.verify(locationManager, times(1)).addProximityAlert(4, 5, 6, NEVER_EXPIRE, intent);
  }

  @Test public void removeGeofence_pendingIntent_shouldRemoveProximityAlert() {
    LostApiClient client = new LostApiClient.Builder(context).build();

    PendingIntent intent = Mockito.mock(PendingIntent.class);
    geofencingApi.removeGeofences(client, intent);
    Mockito.verify(locationManager, times(1)).removeProximityAlert(intent);
  }

  @Test public void removeGeofences_ListOfString_shouldRemoveProximityAlert() {
    LostApiClient client = new LostApiClient.Builder(context).build();

    Geofence geofence = new Geofence.Builder()
        .setRequestId("test_id")
        .setCircularRegion(1, 2, 3)
        .setExpirationDuration(NEVER_EXPIRE)
        .build();
    List<Geofence> geofenceList = new ArrayList<>();
    geofenceList.add(geofence);

    List<String> idList = new ArrayList<>();
    idList.add("test_id");

    PendingIntent intent = Mockito.mock(PendingIntent.class);

    geofencingApi.addGeofences(client, geofenceList, intent);
    geofencingApi.removeGeofences(client, idList);

    Mockito.verify(locationManager, times(1)).removeProximityAlert(intent);
  }

}
