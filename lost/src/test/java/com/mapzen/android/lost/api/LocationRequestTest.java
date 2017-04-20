package com.mapzen.android.lost.api;

import com.mapzen.android.lost.BaseRobolectricTest;
import com.mapzen.lost.BuildConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.os.Parcel;

import static com.mapzen.android.lost.api.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
import static com.mapzen.android.lost.api.LocationRequest.PRIORITY_HIGH_ACCURACY;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, manifest = Config.NONE)
public class LocationRequestTest extends BaseRobolectricTest {
  private LocationRequest locationRequest;

  @Before public void setUp() throws Exception {
    locationRequest = LocationRequest.create();
  }

  @Test public void shouldNotBeNull() throws Exception {
    assertThat(locationRequest).isNotNull();
  }

  @Test public void create_shouldSetDefaultValues() throws Exception {
    assertThat(locationRequest.getInterval()).isEqualTo(LocationRequest.DEFAULT_INTERVAL_IN_MS);
    assertThat(locationRequest.getFastestInterval()).isEqualTo(
        LocationRequest.DEFAULT_FASTEST_INTERVAL_IN_MS);
    assertThat(locationRequest.getSmallestDisplacement()).isEqualTo(
        LocationRequest.DEFAULT_SMALLEST_DISPLACEMENT_IN_METERS);
  }

  @Test public void shouldAllowMethodChaining() throws Exception {
    assertThat(locationRequest.setInterval(1000)).isSameAs(locationRequest);
    assertThat(locationRequest.setFastestInterval(1000)).isSameAs(locationRequest);
    assertThat(locationRequest.setSmallestDisplacement(10.0f)).isSameAs(locationRequest);
  }

  @Test public void setInterval_shouldOverrideDefaultValue() throws Exception {
    locationRequest.setInterval(5000);
    assertThat(locationRequest.getInterval()).isEqualTo(5000);
  }

  @Test public void setFastestInterval_shouldOverrideDefaultValue() throws Exception {
    locationRequest.setFastestInterval(2500);
    assertThat(locationRequest.getFastestInterval()).isEqualTo(2500);
  }

  @Test public void setInterval_shouldUpdateFastestIfLessThanFastest() throws Exception {
    locationRequest.setFastestInterval(10000);
    locationRequest.setInterval(5000);
    assertThat(locationRequest.getFastestInterval()).isEqualTo(5000);
  }

  @Test public void setSmallestDisplacement_shouldOverrideDefaultValue() throws Exception {
    locationRequest.setSmallestDisplacement(10.0f);
    assertThat(locationRequest.getSmallestDisplacement()).isEqualTo(10.0f);
  }

  @Test public void getPriority_shouldReturnDefaultValue() throws Exception {
    assertThat(locationRequest.getPriority()).isEqualTo(PRIORITY_BALANCED_POWER_ACCURACY);
  }

  @Test public void setPriority_shouldOverrideDefaultValue() throws Exception {
    locationRequest.setPriority(PRIORITY_HIGH_ACCURACY);
    assertThat(locationRequest.getPriority()).isEqualTo(PRIORITY_HIGH_ACCURACY);
  }

  @Test(expected = IllegalArgumentException.class)
  public void setPriority_shouldRejectInvalidValues() throws Exception {
    locationRequest.setPriority(-1);
  }

  @Test public void shouldBeParcelable() throws Exception {
    LocationRequest dehydrated = LocationRequest.create()
        .setPriority(PRIORITY_HIGH_ACCURACY)
        .setInterval(1000)
        .setFastestInterval(500)
        .setSmallestDisplacement(10);

    Parcel parcel = Parcel.obtain();
    dehydrated.writeToParcel(parcel, 0);
    parcel.setDataPosition(0);

    LocationRequest rehydrated = new LocationRequest(parcel);
    assertThat(rehydrated.getPriority()).isEqualTo(dehydrated.getPriority());
    assertThat(rehydrated.getInterval()).isEqualTo(dehydrated.getInterval());
    assertThat(rehydrated.getFastestInterval()).isEqualTo(dehydrated.getFastestInterval());
    assertThat(rehydrated.getSmallestDisplacement())
        .isEqualTo(dehydrated.getSmallestDisplacement());
  }
}
