package com.mapzen.android.lost.api;

import com.mapzen.android.lost.internal.FusedLocationProviderApiImpl;
import com.mapzen.android.lost.internal.GeofencingApiImpl;

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
