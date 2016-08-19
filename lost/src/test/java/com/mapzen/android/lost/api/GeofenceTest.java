package com.mapzen.android.lost.api;

import com.mapzen.android.lost.internal.ParcelableGeofence;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class GeofenceTest {

  @Test public void shouldNotBeNull() throws Exception {
    assertThat(new Geofence.Builder().build()).isNotNull();
  }

  @Test public void getRequestId_shouldReturnId() throws Exception {
    Geofence geofence = new Geofence.Builder().setRequestId("test_id").build();
    assertThat(geofence.getRequestId()).isEqualTo("test_id");
  }

  @Test public void setCircularRegion_shouldSetLatitude() throws Exception {
    ParcelableGeofence geofence = (ParcelableGeofence) new Geofence.Builder()
        .setCircularRegion(1, 0, 0)
        .build();
    assertThat(geofence.getLatitude()).isEqualTo(1);
  }

  @Test public void setCircularRegion_shouldSetLongitude() throws Exception {
    ParcelableGeofence geofence = (ParcelableGeofence) new Geofence.Builder()
        .setCircularRegion(0, 1, 0)
        .build();
    assertThat(geofence.getLongitude()).isEqualTo(1);
  }

  @Test public void setCircularRegion_shouldSetRadius() throws Exception {
    ParcelableGeofence geofence = (ParcelableGeofence) new Geofence.Builder()
        .setCircularRegion(0, 0, 1)
        .build();
    assertThat(geofence.getRadius()).isEqualTo(1);
  }
}
