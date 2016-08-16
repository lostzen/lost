package com.mapzen.android.lost.api;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class LocationSettingsStateTest {

  LocationSettingsStates states = new LocationSettingsStates(true, true, true, true, true, true);

  @Test public void shouldHaveGpsUsable() {
    assertThat(states.isGpsUsable()).isTrue();
  }

  @Test public void shouldHaveGpsPresent() {
    assertThat(states.isGpsPresent()).isTrue();
  }

  @Test public void shouldHaveNetworkUsable() {
    assertThat(states.isNetworkLocationUsable()).isTrue();
  }

  @Test public void shouldHaveNetworkPresent() {
    assertThat(states.isNetworkLocationPresent()).isTrue();
  }

  @Test public void shouldHaveLocationUsable() {
    assertThat(states.isLocationUsable()).isTrue();
  }

  @Test public void shouldHaveLocationPresent() {
    assertThat(states.isLocationPresent()).isTrue();
  }

  @Test public void shouldHaveBleUsable() {
    assertThat(states.isBleUsable()).isTrue();
  }

  @Test public void shouldHaveBlePresent() {
    assertThat(states.isBlePresent()).isTrue();
  }
}
