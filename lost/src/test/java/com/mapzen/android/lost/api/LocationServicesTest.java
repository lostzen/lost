package com.mapzen.android.lost.api;

import com.mapzen.android.lost.internal.FusedLocationProviderApiImpl;
import com.mapzen.android.lost.internal.GeofencingApiImpl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.fest.assertions.api.Assertions.assertThat;

@Config(manifest=Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class LocationServicesTest {
    private LostApiClient client;

    @Before
    public void setUp() throws Exception {
        client = new LostApiClient.Builder(Robolectric.application).build();
        client.connect();
    }

    @After
    public void tearDown() throws Exception {
        client.disconnect();
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
