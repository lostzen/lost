package com.mapzen.android.lost.api;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

public class LocationSettingsRequestTest {

  List<LocationRequest> requests;
  LocationSettingsRequest request;

  @Before public void setup() {
    requests = new ArrayList<>();
    LocationRequest highAccuracy = LocationRequest.create().setPriority(
        LocationRequest.PRIORITY_HIGH_ACCURACY); //gps + wifi
    LocationRequest balancedPowerAccuracy = LocationRequest.create().setPriority(
        LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY); // wifi
    LocationRequest lowPower = LocationRequest.create().setPriority(
        LocationRequest.PRIORITY_LOW_POWER); // wifi
    LocationRequest noPower = LocationRequest.create().setPriority(
        LocationRequest.PRIORITY_NO_POWER); // gps or wifi or none
    requests.add(highAccuracy);
    requests.add(balancedPowerAccuracy);
    requests.add(lowPower);
    requests.add(noPower);

    request = new LocationSettingsRequest.Builder().addAllLocationRequests(requests)
        .setNeedBle(true)
        .build();
  }

  @Test public void shouldHaveRequests() {
    assertThat(request.getLocationRequests()).isEqualTo(requests);
  }

  @Test public void shouldNeedBle() {
    assertThat(request.getNeedBle()).isTrue();
  }
}
