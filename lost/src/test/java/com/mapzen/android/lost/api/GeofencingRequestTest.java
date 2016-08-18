package com.mapzen.android.lost.api;

import com.mapzen.android.lost.internal.ParcelableGeofence;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class GeofencingRequestTest {

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectRequestWithNoGeofences() throws Exception {
    new GeofencingRequest.Builder().build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void addGeofence_shouldRejectNullGeofence() throws Exception {
    new GeofencingRequest.Builder().addGeofence(null);
  }

  @Test public void getGeofences_shouldReturnSingleGeofence() throws Exception {
    Geofence geofence = new ParcelableGeofence("test_id");
    GeofencingRequest request = new GeofencingRequest.Builder().addGeofence(geofence).build();
    assertThat(request.getGeofences().get(0).getRequestId()).isEqualTo("test_id");
  }

  @Test public void getGeofences_shouldReturnMultipleGeofences() throws Exception {
    Geofence geofence1 = new ParcelableGeofence("test_id_1");
    Geofence geofence2 = new ParcelableGeofence("test_id_2");
    GeofencingRequest request = new GeofencingRequest.Builder()
        .addGeofence(geofence1)
        .addGeofence(geofence2)
        .build();
    assertThat(request.getGeofences().get(0).getRequestId()).isEqualTo("test_id_1");
    assertThat(request.getGeofences().get(1).getRequestId()).isEqualTo("test_id_2");
  }
}
