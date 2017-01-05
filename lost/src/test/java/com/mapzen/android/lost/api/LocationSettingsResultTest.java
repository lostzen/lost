package com.mapzen.android.lost.api;

import com.mapzen.android.lost.internal.TestSettingsDialogDisplayer;

import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class LocationSettingsResultTest {

  Status status;
  LocationSettingsStates states;
  LocationSettingsResult result;

  @Before public void setup() {
    status = new Status(Status.SUCCESS, new TestSettingsDialogDisplayer());
    states = new LocationSettingsStates(true, true, true, true, true, true);
    result = new LocationSettingsResult(status, states);
  }

  @Test public void shouldHaveStatus() {
    assertThat(result.getStatus()).isEqualTo(status);
  }

  @Test public void shouldHaveStates() {
    assertThat(result.getLocationSettingsStates()).isEqualTo(states);
  }
}
