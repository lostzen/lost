package com.mapzen.android.ps.location;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static com.mapzen.android.ps.location.LocationRequest.DEFAULT_FASTEST_INTERVAL_IN_MS;
import static com.mapzen.android.ps.location.LocationRequest.DEFAULT_INTERVAL_IN_MS;
import static com.mapzen.android.ps.location.LocationRequest.DEFAULT_SMALLEST_DISPLACEMENT_IN_METERS;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class LocationRequestTest {
    private LocationRequest locationRequest;

    @Before
    public void setUp() throws Exception {
        locationRequest = LocationRequest.create();
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(locationRequest).isNotNull();
    }

    @Test
    public void create_shouldSetDefaultValues() throws Exception {
        assertThat(locationRequest.getInterval()).isEqualTo(DEFAULT_INTERVAL_IN_MS);
        assertThat(locationRequest.getFastestInterval()).isEqualTo(DEFAULT_FASTEST_INTERVAL_IN_MS);
        assertThat(locationRequest.getSmallestDisplacement())
                .isEqualTo(DEFAULT_SMALLEST_DISPLACEMENT_IN_METERS);
    }

    @Test
    public void shouldAllowMethodChaining() throws Exception {
        assertThat(locationRequest.setInterval(1000)).isSameAs(locationRequest);
        assertThat(locationRequest.setFastestInterval(1000)).isSameAs(locationRequest);
        assertThat(locationRequest.setSmallestDisplacement(10.0f)).isSameAs(locationRequest);
    }

    @Test
    public void setInterval_shouldOverrideDefaultValue() throws Exception {
        locationRequest.setInterval(5000);
        assertThat(locationRequest.getInterval()).isEqualTo(5000);
    }

    @Test
    public void setFastestInterval_shouldOverrideDefaultValue() throws Exception {
        locationRequest.setFastestInterval(2500);
        assertThat(locationRequest.getFastestInterval()).isEqualTo(2500);
    }

    @Test
    public void setInterval_shouldUpdateFastestIfLessThanFastest() throws Exception {
        locationRequest.setFastestInterval(10000);
        locationRequest.setInterval(5000);
        assertThat(locationRequest.getFastestInterval()).isEqualTo(5000);
    }

    @Test
    public void setSmallestDisplacement_shouldOverrideDefaultValue() throws Exception {
        locationRequest.setSmallestDisplacement(10.0f);
        assertThat(locationRequest.getSmallestDisplacement()).isEqualTo(10.0f);
    }
}
