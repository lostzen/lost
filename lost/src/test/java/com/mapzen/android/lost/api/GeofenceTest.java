package com.mapzen.android.lost.api;

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
}
