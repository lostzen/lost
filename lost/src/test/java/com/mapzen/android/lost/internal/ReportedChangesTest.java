package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationRequest;

import org.junit.Before;
import org.junit.Test;

import android.location.Location;

import java.util.HashMap;
import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class ReportedChangesTest {

  Map<LocationRequest, Long> timeChanges = new HashMap<>();
  Map<LocationRequest, Location> locationChanges = new HashMap<>();
  ReportedChanges changes;

  @Before public void setup() {
    changes = new ReportedChanges(timeChanges, locationChanges);
  }

  @Test public void putAll_shouldUpdateTimeChanges() {
    Map<LocationRequest, Long> otherTimeChanges = new HashMap<>();
    LocationRequest locRequest = LocationRequest.create(new TestPidReader());
    otherTimeChanges.put(locRequest, 1234L);
    Map<LocationRequest, Location> otherLocationChanges = new HashMap<>();
    ReportedChanges otherChanges = new ReportedChanges(otherTimeChanges, otherLocationChanges);
    changes.putAll(otherChanges);
    assertThat(changes.timeChanges().get(locRequest)).isEqualTo(1234L);
  }

  @Test public void putAll_shouldUpdateLocationChanges() {
    Map<LocationRequest, Long> otherTimeChanges = new HashMap<>();
    Map<LocationRequest, Location> otherLocationChanges = new HashMap<>();
    LocationRequest locRequest = LocationRequest.create(new TestPidReader());
    Location loc = mock(Location.class);
    otherLocationChanges.put(locRequest, loc);
    ReportedChanges otherChanges = new ReportedChanges(otherTimeChanges, otherLocationChanges);
    changes.putAll(otherChanges);
    assertThat(changes.locationChanges().get(locRequest)).isEqualTo(loc);
  }

  @Test public void clearAll_shouldClearTimeChanges() {
    Map<LocationRequest, Long> otherTimeChanges = new HashMap<>();
    LocationRequest locRequest = LocationRequest.create(new TestPidReader());
    otherTimeChanges.put(locRequest, 1234L);
    Map<LocationRequest, Location> otherLocationChanges = new HashMap<>();
    Location loc = mock(Location.class);
    otherLocationChanges.put(locRequest, loc);
    ReportedChanges otherChanges = new ReportedChanges(otherTimeChanges, otherLocationChanges);
    changes.putAll(otherChanges);
    changes.clearAll();
    assertThat(changes.timeChanges()).isEmpty();
  }

  @Test public void clearAll_shouldClearLocationChanges() {
    Map<LocationRequest, Long> otherTimeChanges = new HashMap<>();
    LocationRequest locRequest = LocationRequest.create(new TestPidReader());
    otherTimeChanges.put(locRequest, 1234L);
    Map<LocationRequest, Location> otherLocationChanges = new HashMap<>();
    Location loc = mock(Location.class);
    otherLocationChanges.put(locRequest, loc);
    ReportedChanges otherChanges = new ReportedChanges(otherTimeChanges, otherLocationChanges);
    changes.putAll(otherChanges);
    changes.clearAll();
    assertThat(changes.locationChanges()).isEmpty();
  }

}
