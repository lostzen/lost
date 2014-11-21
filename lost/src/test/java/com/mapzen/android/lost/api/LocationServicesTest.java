package com.mapzen.android.lost.api;

import com.mapzen.android.lost.internal.FusedLocationProviderApiImpl;
import com.mapzen.android.lost.internal.GeofencingApiImpl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class LocationServicesTest {
    @Before
    public void setUp() throws Exception {
        new LostApiClient.Builder(Robolectric.application).build().connect();
    }

    @Test
    public void shouldCreateFusedLocationProviderApiImpl() throws Exception {
        assertThat(LocationServices.FusedLocationApi)
                .isInstanceOf(FusedLocationProviderApiImpl.class);
    }

    @Test
    public void shouldCreateGeofencingApiImpl() throws Exception {
        assertThat(LocationServices.GeofencingApi)
                .isInstanceOf(GeofencingApiImpl.class);
    }
}
