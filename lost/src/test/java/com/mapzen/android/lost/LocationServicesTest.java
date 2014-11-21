package com.mapzen.android.lost;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class LocationServicesTest {
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
