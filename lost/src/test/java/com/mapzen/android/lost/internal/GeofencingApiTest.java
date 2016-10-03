package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.Geofence;
import com.mapzen.android.lost.api.GeofencingRequest;
import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.android.lost.api.PendingResult;
import com.mapzen.android.lost.api.Status;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import android.app.PendingIntent;
import android.content.Context;
import android.location.LocationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
  LostApiClient client;
  IntentFactory intentFactory;

  @Before public void setUp() throws Exception {
    context = mock(Context.class);
    locationManager = mock(LocationManager.class);
    when(context.getSystemService(LOCATION_SERVICE)).thenReturn(locationManager);
    intentFactory = new TestIntentFactory();
    geofencingApi = new GeofencingApiImpl(intentFactory);
    geofencingApi.connect(context);
    client = new LostApiClient.Builder(context).build();
  }

  @Test public void shouldNotBeNull() throws Exception {
    assertThat(geofencingApi).isNotNull();
  }

  @Test public void addGeofences_shouldAddProximityAlert() throws Exception {
    Geofence geofence = new Geofence.Builder().setRequestId("test_id")
        .setCircularRegion(1, 2, 3)
        .setExpirationDuration(NEVER_EXPIRE)
        .build();
    GeofencingRequest request = new GeofencingRequest.Builder().addGeofence(geofence).build();
    PendingIntent intent = Mockito.mock(PendingIntent.class);
    geofencingApi.addGeofences(client, request, intent);
    PendingIntent pendingIntent = intentFactory.createPendingIntent(context, 123, null);
    Mockito.verify(locationManager, times(1)).addProximityAlert(1, 2, 3, NEVER_EXPIRE,
        pendingIntent);
  }

  //as suggested here: https://github.com/mapzen/LOST/pull/88#discussion_r77384417
  @Test public void addGeofencesArray_shouldAddProximityAlert() {
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
    PendingIntent pendingIntent = intentFactory.createPendingIntent(context, 123, null);
    Mockito.verify(locationManager, times(1)).addProximityAlert(1, 2, 3, NEVER_EXPIRE,
        pendingIntent);
    Mockito.verify(locationManager, times(1)).addProximityAlert(4, 5, 6, NEVER_EXPIRE,
        pendingIntent);
  }

  @Test public void removeGeofence_pendingIntent_shouldRemoveProximityAlert() {
    PendingIntent intent = Mockito.mock(PendingIntent.class);
    geofencingApi.removeGeofences(client, intent);
    Mockito.verify(locationManager, times(1)).removeProximityAlert(intent);
  }

  @Test public void removeGeofences_ListOfString_shouldRemoveProximityAlert() {
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

  @Test public void addGeofences_request_shouldReturnPendingResult() {
    Geofence geofence = new Geofence.Builder()
        .setRequestId("test_id")
        .setCircularRegion(1, 2, 3)
        .setExpirationDuration(NEVER_EXPIRE)
        .build();
    GeofencingRequest request = new GeofencingRequest.Builder().addGeofence(geofence).build();
    PendingIntent pendingIntent = mock(PendingIntent.class);
    PendingResult result = geofencingApi.addGeofences(client, request, pendingIntent);

    assertThat(result.await().getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
    assertThat(result.await(1000, TimeUnit.MILLISECONDS).getStatus().getStatusCode()).isEqualTo(
        Status.SUCCESS);
    assertThat(result.isCanceled()).isFalse();
    TestResultCallback callback = new TestResultCallback();
    result.setResultCallback(callback);
    assertThat(callback.getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
    TestResultCallback otherCallback = new TestResultCallback();
    result.setResultCallback(otherCallback, 1000, TimeUnit.MILLISECONDS);
    assertThat(otherCallback.getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
  }

  @Test public void addGeofences_geofences_shouldReturnPendingResult() {
    Geofence geofence = new Geofence.Builder()
        .setRequestId("test_id")
        .setCircularRegion(1, 2, 3)
        .setExpirationDuration(NEVER_EXPIRE)
        .build();
    List<Geofence> geofences = new ArrayList<>();
    geofences.add(geofence);
    PendingIntent pendingIntent = mock(PendingIntent.class);
    PendingResult result = geofencingApi.addGeofences(client, geofences, pendingIntent);

    assertThat(result.await().getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
    assertThat(result.await(1000, TimeUnit.MILLISECONDS).getStatus().getStatusCode()).isEqualTo(
        Status.SUCCESS);
    assertThat(result.isCanceled()).isFalse();
    TestResultCallback callback = new TestResultCallback();
    result.setResultCallback(callback);
    assertThat(callback.getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
    TestResultCallback otherCallback = new TestResultCallback();
    result.setResultCallback(otherCallback, 1000, TimeUnit.MILLISECONDS);
    assertThat(otherCallback.getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
  }

  @Test public void removeGeofence_geofenceRequestIds_shouldReturnPendingResult() {
    Geofence geofence = new Geofence.Builder()
        .setRequestId("test_id")
        .setCircularRegion(1, 2, 3)
        .setExpirationDuration(NEVER_EXPIRE)
        .build();
    List<Geofence> geofences = new ArrayList<>();
    geofences.add(geofence);
    PendingIntent pendingIntent = mock(PendingIntent.class);
    geofencingApi.addGeofences(client, geofences, pendingIntent);
    List<String> ids = new ArrayList<>();
    ids.add("test_id");
    PendingResult result = geofencingApi.removeGeofences(client, ids);

    assertThat(result.await().getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
    assertThat(result.await(1000, TimeUnit.MILLISECONDS).getStatus().getStatusCode()).isEqualTo(
        Status.SUCCESS);
    assertThat(result.isCanceled()).isFalse();
    TestResultCallback callback = new TestResultCallback();
    result.setResultCallback(callback);
    assertThat(callback.getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
    TestResultCallback otherCallback = new TestResultCallback();
    result.setResultCallback(otherCallback, 1000, TimeUnit.MILLISECONDS);
    assertThat(otherCallback.getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
  }

  @Test public void removeGeofence_pendingIntent_shouldReturnPendingResult() {
    Geofence geofence = new Geofence.Builder()
        .setRequestId("test_id")
        .setCircularRegion(1, 2, 3)
        .setExpirationDuration(NEVER_EXPIRE)
        .build();
    List<Geofence> geofences = new ArrayList<>();
    geofences.add(geofence);
    PendingIntent pendingIntent = mock(PendingIntent.class);
    geofencingApi.addGeofences(client, geofences, pendingIntent);
    PendingResult result = geofencingApi.removeGeofences(client, pendingIntent);

    assertThat(result.await().getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
    assertThat(result.await(1000, TimeUnit.MILLISECONDS).getStatus().getStatusCode()).isEqualTo(
        Status.SUCCESS);
    assertThat(result.isCanceled()).isFalse();
    TestResultCallback callback = new TestResultCallback();
    result.setResultCallback(callback);
    assertThat(callback.getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
    TestResultCallback otherCallback = new TestResultCallback();
    result.setResultCallback(otherCallback, 1000, TimeUnit.MILLISECONDS);
    assertThat(otherCallback.getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
  }

  @Test public void removeNoGeofence_pendingIntent_shouldReturnPendingResult() {
    PendingIntent pendingIntent = mock(PendingIntent.class);
    PendingResult result = geofencingApi.removeGeofences(client, pendingIntent);

    assertThat(result.await().getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
    assertThat(result.await(1000, TimeUnit.MILLISECONDS).getStatus().getStatusCode()).isEqualTo(
        Status.SUCCESS);
    assertThat(result.isCanceled()).isFalse();
    TestResultCallback callback = new TestResultCallback();
    result.setResultCallback(callback);
    assertThat(callback.getStatus()).isNull();
    TestResultCallback otherCallback = new TestResultCallback();
    result.setResultCallback(otherCallback, 1000, TimeUnit.MILLISECONDS);
    assertThat(otherCallback.getStatus()).isNull();
  }

  @Test public void removeNoGeofence_requestIds_shouldReturnPendingResult() {
    List<String> ids = new ArrayList<>();
    ids.add("test_id");
    PendingResult result = geofencingApi.removeGeofences(client, ids);

    assertThat(result.await().getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
    assertThat(result.await(1000, TimeUnit.MILLISECONDS).getStatus().getStatusCode()).isEqualTo(
        Status.SUCCESS);
    assertThat(result.isCanceled()).isFalse();
    TestResultCallback callback = new TestResultCallback();
    result.setResultCallback(callback);
    assertThat(callback.getStatus()).isNull();
    TestResultCallback otherCallback = new TestResultCallback();
    result.setResultCallback(otherCallback, 1000, TimeUnit.MILLISECONDS);
    assertThat(otherCallback.getStatus()).isNull();
  }

}
