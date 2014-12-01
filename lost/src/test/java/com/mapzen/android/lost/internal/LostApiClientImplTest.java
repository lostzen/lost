package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LocationServices;
import com.mapzen.android.lost.api.LostApiClient;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.location.Location;
import android.location.LocationManager;

import static android.content.Context.LOCATION_SERVICE;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.application;
import static org.robolectric.Robolectric.shadowOf;

@Config(manifest=Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class LostApiClientImplTest {
    private LostApiClient client;

    @Before
    public void setUp() throws Exception {
        client = new LostApiClient.Builder(application).build();
    }

    @After
    public void tearDown() throws Exception {
        client.disconnect();
    }

    @Test
    public void connect_shouldCreateFusedLocationProviderApiImpl() throws Exception {
        client.connect();
        assertThat(LocationServices.FusedLocationApi)
                .isInstanceOf(FusedLocationProviderApiImpl.class);
    }

    @Test
    public void connect_shouldCreateGeofencingApiImpl() throws Exception {
        client.connect();
        assertThat(LocationServices.GeofencingApi)
                .isInstanceOf(GeofencingApiImpl.class);
    }

    @Test
    public void disconnect_shouldRemoveFusedLocationProviderApiImpl() throws Exception {
        client.connect();
        client.disconnect();
        assertThat(LocationServices.FusedLocationApi).isNull();
    }

    @Test
    public void disconnect_shouldRemoveGeofencingApiImpl() throws Exception {
        client.connect();
        client.disconnect();
        assertThat(LocationServices.GeofencingApi).isNull();
    }

    @Test
    public void disconnect_shouldUnregisterLocationUpdateListeners() throws Exception {
        client.connect();
        LocationServices.FusedLocationApi.requestLocationUpdates(LocationRequest.create(),
                new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                    }
                });

        client.disconnect();
        LocationManager lm = (LocationManager) application.getSystemService(LOCATION_SERVICE);
        assertThat(shadowOf(lm).getRequestLocationUpdateListeners()).isEmpty();
    }

    @Test
    public void isConnected_shouldReturnFalseBeforeConnected() throws Exception {
        assertThat(client.isConnected()).isFalse();
    }

    @Test
    public void isConnected_shouldReturnTrueAfterConnected() throws Exception {
        client.connect();
        assertThat(client.isConnected()).isTrue();
    }

    @Test
    public void isConnected_shouldReturnFalseAfterDisconnected() throws Exception {
        client.connect();
        client.disconnect();
        assertThat(client.isConnected()).isFalse();
    }
}
