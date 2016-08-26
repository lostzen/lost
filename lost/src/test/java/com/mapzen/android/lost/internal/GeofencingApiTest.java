package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.Geofence;
import com.mapzen.android.lost.api.GeofencingApi;
import com.mapzen.android.lost.api.GeofencingRequest;
import com.mapzen.android.lost.api.LostApiClient;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import android.app.PendingIntent;
import android.content.Context;
import android.location.LocationManager;

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
  GeofencingApi geofencingApi;

  @Before public void setUp() throws Exception {
    context = mock(Context.class);
    locationManager = mock(LocationManager.class);
    when(context.getSystemService(LOCATION_SERVICE)).thenReturn(locationManager);
    geofencingApi = new GeofencingApiImpl(context);
  }

  @Test public void shouldNotBeNull() throws Exception {
    assertThat(geofencingApi).isNotNull();
  }

  @Test public void addGeofences_shouldAddProximityAlert() throws Exception {
    LostApiClient client = new LostApiClient.Builder(context).build();
    Geofence geofence = new Geofence.Builder()
        .setRequestId("test_id")
        .setCircularRegion(1, 2, 3)
        .setExpirationDuration(NEVER_EXPIRE)
        .build();
    GeofencingRequest request = new GeofencingRequest.Builder()
        .addGeofence(geofence)
        .build();
    PendingIntent intent = Mockito.mock(PendingIntent.class);
    geofencingApi.addGeofences(client, request, intent);
    Mockito.verify(locationManager, times(1)).addProximityAlert(1, 2, 3, NEVER_EXPIRE, intent);
  }
}
