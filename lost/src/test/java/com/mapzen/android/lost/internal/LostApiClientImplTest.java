package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.FusedLocationProviderApi;
import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LocationServices;
import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.lost.BuildConfig;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import android.content.ComponentName;
import android.location.Location;
import android.location.LocationManager;

import static android.content.Context.LOCATION_SERVICE;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.RuntimeEnvironment.application;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, manifest = Config.NONE)
public class LostApiClientImplTest {
  private LostApiClient client;

  @Before public void setUp() {
    client = new LostApiClient.Builder(application).build();

    FusedLocationProviderService.FusedLocationProviderBinder stubBinder = mock(
        FusedLocationProviderService.FusedLocationProviderBinder.class);
    when(stubBinder.getService()).thenReturn(mock(FusedLocationProviderService.class));
    shadowOf(application).setComponentNameAndServiceForBindService(
        new ComponentName("com.mapzen.lost", "FusedLocationProviderService"), stubBinder);

  }

  @After public void tearDown() {
    client.disconnect();
  }

  @Test public void connect_shouldConnectFusedLocationProviderApiImpl() {
    client.connect();
    FusedLocationProviderApiImpl fusedApi =
        (FusedLocationProviderApiImpl) LocationServices.FusedLocationApi;
    assertThat(fusedApi.isConnected()).isTrue();
  }

  @Test public void connect_shouldConnectGeofencingApiImpl() {
    client.connect();
    GeofencingApiImpl geofencingApi = (GeofencingApiImpl) LocationServices.GeofencingApi;
    assertThat(geofencingApi.isConnected()).isTrue();
  }

  @Test public void connect_shouldConnectSettingsApiImpl() {
    client.connect();
    SettingsApiImpl settingsApi = (SettingsApiImpl) LocationServices.SettingsApi;
    assertThat(settingsApi.isConnected()).isTrue();
  }

  @Test public void disconnect_shouldNotRemoveFusedLocationProviderApiImpl() {
    client.connect();
    client.disconnect();
    assertThat(LocationServices.FusedLocationApi).isNotNull();
  }

  @Test public void disconnect_shouldNotRemoveGeofencingApiImpl() {
    client.connect();
    client.disconnect();
    assertThat(LocationServices.GeofencingApi).isNotNull();
  }

  @Test public void disconnect_shouldNotRemoveSettingsApiImpl() {
    client.connect();
    client.disconnect();
    assertThat(LocationServices.SettingsApi).isNotNull();
  }

  @Test public void disconnect_shouldDisconnectFusedLocationProviderApiImpl() {
    client.connect();
    client.disconnect();
    FusedLocationProviderApiImpl fusedApi =
        (FusedLocationProviderApiImpl) LocationServices.FusedLocationApi;
    assertThat(fusedApi.isConnected()).isFalse();
  }

  @Test public void disconnect_shouldDisconnectGeofencingApiImpl() {
    client.connect();
    client.disconnect();
    GeofencingApiImpl geofencingApi = (GeofencingApiImpl) LocationServices.GeofencingApi;
    assertThat(geofencingApi.isConnected()).isFalse();
  }

  @Test public void disconnect_shouldDisconnectSettingsApiImpl() {
    client.connect();
    client.disconnect();
    SettingsApiImpl settingsApi = (SettingsApiImpl) LocationServices.SettingsApi;
    assertThat(settingsApi.isConnected()).isFalse();
  }

  @Test public void disconnect_shouldUnregisterLocationUpdateListeners() {
    client.connect();
    LocationServices.FusedLocationApi.requestLocationUpdates(client, LocationRequest.create(),
        new LocationListener() {
          @Override public void onLocationChanged(Location location) {
          }

          @Override public void onProviderDisabled(String provider) {
          }

          @Override public void onProviderEnabled(String provider) {
          }
        });

    client.disconnect();
    LocationManager lm = (LocationManager) application.getSystemService(LOCATION_SERVICE);
    assertThat(shadowOf(lm).getRequestLocationUpdateListeners()).isEmpty();
  }

  @Test public void disconnect_multipleClients_shouldNotRemoveFusedLocationProviderApiImpl() {
    LostApiClient anotherClient = new LostApiClient.Builder(application).build();
    anotherClient.connect();
    client.connect();
    client.disconnect();
    assertThat(LocationServices.FusedLocationApi).isNotNull();
    anotherClient.disconnect();
  }

  @Test public void disconnect_multipleClients_shouldNotRemoveGeofencingApiImpl() {
    LostApiClient anotherClient = new LostApiClient.Builder(application).build();
    anotherClient.connect();
    client.connect();
    client.disconnect();
    assertThat(LocationServices.GeofencingApi).isNotNull();
    anotherClient.disconnect();
  }

  @Test public void disconnect_multipleClients_shouldNotRemoveSettingsApiImpl() {
    LostApiClient anotherClient = new LostApiClient.Builder(application).build();
    anotherClient.connect();
    client.connect();
    client.disconnect();
    assertThat(LocationServices.SettingsApi).isNotNull();
    anotherClient.disconnect();
  }


  @Test
  public void isConnected_shouldReturnFalseBeforeConnected() {
    assertThat(client.isConnected()).isFalse();
  }

  @Test public void isConnected_shouldReturnTrueAfterConnected() {
    client.connect();
    assertThat(client.isConnected()).isTrue();
  }

  @Test public void isConnected_shouldReturnFalseAfterDisconnected() {
    client.connect();
    client.disconnect();
    assertThat(client.isConnected()).isFalse();
  }
}
