package com.mapzen.android.lost.api;

import com.mapzen.android.lost.BaseRobolectricTest;
import com.mapzen.lost.BuildConfig;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.content.Intent;
import android.location.Location;

import java.util.ArrayList;

import static com.mapzen.android.lost.api.LocationResult.EXTRA_LOCATION_RESULT;
import static org.fest.assertions.api.Assertions.assertThat;

/**
 * {@link LocationResult} test class.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, manifest = Config.NONE)
public class LocationResultTest extends BaseRobolectricTest {

  @Test public void shouldNotBeNull() throws Exception {
    assertThat(LocationResult.create(null)).isNotNull();
  }

  @Test public void getLocations_shouldReturnEmptyListByDefault() throws Exception {
    assertThat(LocationResult.create(null).getLocations()).isEmpty();
  }

  @Test public void getLocations_shouldReturnLocationsIfNotEmpty() throws Exception {
    ArrayList<Location> locations = new ArrayList<>();
    locations.add(getTestLocation(0, 0));
    locations.add(getTestLocation(0, 0));
    locations.add(getTestLocation(0, 0));
    assertThat(LocationResult.create(locations).getLocations()).isEqualTo(locations);
  }

  @Test public void getLastLocation_shouldReturnNullIfLocationListIsEmpty() throws Exception {
    assertThat(LocationResult.create(null).getLastLocation()).isNull();
  }

  @Test public void getLastLocation_shouldReturnMostRecentLocationAvailable() throws Exception {
    ArrayList<Location> locations = new ArrayList<>();
    locations.add(getTestLocation(0, 0));
    locations.add(getTestLocation(1, 1));
    locations.add(getTestLocation(2, 2));
    Location expected = locations.get(2);
    assertThat(LocationResult.create(locations).getLastLocation()).isEqualTo(expected);
  }

  @Test public void hasResult_shouldReturnFalseIfIntentIsNull() throws Exception {
    assertThat(LocationResult.hasResult(null)).isFalse();
  }

  @Test public void hasResult_shouldReturnFalseIfIntentDoesNotHaveExtra() throws Exception {
    assertThat(LocationResult.hasResult(new Intent())).isFalse();
  }

  @Test public void hasResult_shouldReturnTrueIfIntentHasLocationExtra() throws Exception {
    LocationResult locationResult = LocationResult.create(null);
    Intent intent = new Intent().putExtra(EXTRA_LOCATION_RESULT, locationResult);
    assertThat(LocationResult.hasResult(intent)).isTrue();
  }

  @Test public void extractResult_shouldReturnNullIfIntentIsNull() throws Exception {
    assertThat(LocationResult.extractResult(null)).isNull();
  }

  @Test public void extractResult_shouldReturnNullIfIntentDoesNotHaveExtra() throws Exception {
    assertThat(LocationResult.extractResult(new Intent())).isNull();
  }

  @Test public void extractResult_shouldReturnLocationResultExtra() throws Exception {
    LocationResult locationResult = LocationResult.create(null);
    Intent intent = new Intent().putExtra(EXTRA_LOCATION_RESULT, locationResult);
    assertThat(LocationResult.extractResult(intent)).isEqualTo(locationResult);
  }

  @Test public void equals_shouldReturnFalseIfLocationListSizeDoesNotMatch() throws Exception {
    ArrayList<Location> locations1 = new ArrayList<>();
    locations1.add(getTestLocation(0, 0));
    LocationResult locationResult1 = LocationResult.create(locations1);

    ArrayList<Location> locations2 = new ArrayList<>();
    locations2.add(getTestLocation(0, 0));
    locations2.add(getTestLocation(0, 0));
    LocationResult locationResult2 = LocationResult.create(locations2);

    assertThat(locationResult1.equals(locationResult2)).isFalse();
  }

  @Test public void equals_shouldReturnFalseIfLocationTimestampsDoNotMatch() throws Exception {
    ArrayList<Location> locations1 = new ArrayList<>();
    locations1.add(getTestLocation(0, 0, 0));
    locations1.add(getTestLocation(0, 0, 1));
    LocationResult locationResult1 = LocationResult.create(locations1);

    ArrayList<Location> locations2 = new ArrayList<>();
    locations2.add(getTestLocation(0, 0, 2));
    locations2.add(getTestLocation(0, 0, 3));
    LocationResult locationResult2 = LocationResult.create(locations2);

    assertThat(locationResult1.equals(locationResult2)).isFalse();
  }

  @Test public void equals_shouldReturnTrueIfSizeAndTimestampsMatch() throws Exception {
    ArrayList<Location> locations1 = new ArrayList<>();
    locations1.add(getTestLocation(0, 0, 0));
    locations1.add(getTestLocation(0, 0, 1));
    LocationResult locationResult1 = LocationResult.create(locations1);

    ArrayList<Location> locations2 = new ArrayList<>();
    locations2.add(getTestLocation(0, 0, 0));
    locations2.add(getTestLocation(0, 0, 1));
    LocationResult locationResult2 = LocationResult.create(locations2);

    assertThat(locationResult1.equals(locationResult2)).isTrue();
  }

  // Helper methods

  static Location getTestLocation(double latitude, double longitude) {
    return getTestLocation(latitude, longitude, 0);
  }

  static Location getTestLocation(double latitude, double longitude, long time) {
    Location location = new Location("test");
    location.setLatitude(latitude);
    location.setLongitude(longitude);
    location.setTime(time);
    return location;
  }
}
