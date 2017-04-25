package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.BaseRobolectricTest;
import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LocationServices;
import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.lost.BuildConfig;

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
import static org.mockito.Mockito.mock;
import static org.robolectric.RuntimeEnvironment.application;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, manifest = Config.NONE)
public class LostApiClientImplTest extends BaseRobolectricTest {
  private LostApiClient client;
  private TestConnectionCallbacks callbacks;

  @Before public void setUp() throws Exception {
    LostClientManager.shared().clearClients();
    callbacks = new TestConnectionCallbacks();
    client = new LostApiClientImpl(application, callbacks, LostClientManager.shared());
  }

  @After public void tearDown() throws Exception {
    client.disconnect();
    ((FusedLocationProviderApiImpl) LocationServices.FusedLocationApi)
        .getServiceConnectionManager()
        .getConnectionCallbacks()
        .clear();
  }

  @Test public void connect_shouldConnectFusedLocationProviderApiImpl() throws Exception {
    client.connect();
    FusedLocationProviderApiImpl fusedApi =
        (FusedLocationProviderApiImpl) LocationServices.FusedLocationApi;
    assertThat(fusedApi.isConnected()).isTrue();
  }

  @Test public void connect_shouldConnectGeofencingApiImpl() throws Exception {
    client.connect();
    GeofencingApiImpl geofencingApi = (GeofencingApiImpl) LocationServices.GeofencingApi;
    assertThat(geofencingApi.isConnected()).isTrue();
  }

  @Test public void connect_shouldConnectSettingsApiImpl() throws Exception {
    client.connect();
    SettingsApiImpl settingsApi = (SettingsApiImpl) LocationServices.SettingsApi;
    assertThat(settingsApi.isConnected()).isTrue();
  }

  @Test public void connect_shouldBeConnectedWhenConnectionCallbackInvoked() throws Exception {
    callbacks.setLostClient(client);
    client.connect();
    assertThat(callbacks.isClientConnectedOnConnect()).isTrue();
  }

  @Test public void connect_multipleClients_shouldBeConnectedWhenConnectionCallbackInvoked()
      throws Exception {
    client.connect();
    TestConnectionCallbacks callbacks = new TestConnectionCallbacks();
    LostApiClient anotherClient =
        new LostApiClientImpl(application, callbacks, new LostClientManager());
    callbacks.setLostClient(anotherClient);
    anotherClient.connect();
    assertThat(callbacks.isClientConnectedOnConnect()).isTrue();
  }

  @Test public void connect_shouldAddConnectionCallbacks() throws Exception {
    // Connect first Lost client with connection callbacks.
    new LostApiClientImpl(application, new LostApiClient.ConnectionCallbacks() {
      @Override public void onConnected() {
        // Connect second Lost client with new connection callbacks once the service has connected.
        new LostApiClientImpl(application,  new TestConnectionCallbacks(),
            new LostClientManager()).connect();
      }

      @Override public void onConnectionSuspended() {
      }
    }, new LostClientManager()).connect();

    FusedLocationProviderApiImpl api =
        (FusedLocationProviderApiImpl) LocationServices.FusedLocationApi;

    // Verify both sets of connection callbacks have been stored in the service connection manager.
    assertThat(api.getServiceConnectionManager().getConnectionCallbacks()).hasSize(2);
  }

  @Test public void disconnect_shouldNotRemoveFusedLocationProviderApiImpl() throws Exception {
    client.connect();
    client.disconnect();
    assertThat(LocationServices.FusedLocationApi).isNotNull();
  }

  @Test public void disconnect_shouldNotRemoveGeofencingApiImpl() throws Exception {
    client.connect();
    client.disconnect();
    assertThat(LocationServices.GeofencingApi).isNotNull();
  }

  @Test public void disconnect_shouldNotRemoveSettingsApiImpl() throws Exception {
    client.connect();
    client.disconnect();
    assertThat(LocationServices.SettingsApi).isNotNull();
  }

  @Test public void disconnect_shouldDisconnectFusedLocationProviderApiImpl() throws Exception {
    client.connect();
    client.disconnect();
    FusedLocationProviderApiImpl fusedApi =
        (FusedLocationProviderApiImpl) LocationServices.FusedLocationApi;
    assertThat(fusedApi.isConnected()).isFalse();
  }

  @Test public void disconnect_shouldDisconnectGeofencingApiImpl() throws Exception {
    client.connect();
    client.disconnect();
    GeofencingApiImpl geofencingApi = (GeofencingApiImpl) LocationServices.GeofencingApi;
    assertThat(geofencingApi.isConnected()).isFalse();
  }

  @Test public void disconnect_shouldDisconnectSettingsApiImpl() throws Exception {
    client.connect();
    client.disconnect();
    SettingsApiImpl settingsApi = (SettingsApiImpl) LocationServices.SettingsApi;
    assertThat(settingsApi.isConnected()).isFalse();
  }

  @Test public void disconnect_shouldUnregisterLocationUpdateListeners() throws Exception {
    client.connect();
    ((FusedLocationProviderApiImpl) LocationServices.FusedLocationApi).service =
        mock(IFusedLocationProviderService.class);
    LocationServices.FusedLocationApi.requestLocationUpdates(client, LocationRequest.create(),
        new LocationListener() {
          @Override public void onLocationChanged(Location location) {
          }
        });

    client.disconnect();
    LocationManager lm = (LocationManager) application.getSystemService(LOCATION_SERVICE);
    assertThat(shadowOf(lm).getRequestLocationUpdateListeners()).isEmpty();
  }

  @Test public void disconnect_multipleClients_shouldNotRemoveFusedLocationProviderApiImpl()
      throws Exception {
    LostApiClient anotherClient =
        new LostApiClientImpl(application, callbacks, new LostClientManager());
    anotherClient.connect();
    client.connect();
    client.disconnect();
    assertThat(LocationServices.FusedLocationApi).isNotNull();
  }

  @Test public void disconnect_multipleClients_shouldNotRemoveGeofencingApiImpl() throws Exception {
    LostApiClient anotherClient =
        new LostApiClientImpl(application, callbacks, new LostClientManager());
    anotherClient.connect();
    client.connect();
    client.disconnect();
    assertThat(LocationServices.GeofencingApi).isNotNull();
  }

  @Test public void disconnect_multipleClients_shouldNotRemoveSettingsApiImpl() throws Exception {
    LostApiClient anotherClient =
        new LostApiClientImpl(application, callbacks, new LostClientManager());
    anotherClient.connect();
    client.connect();
    client.disconnect();
    assertThat(LocationServices.SettingsApi).isNotNull();
  }

  @Test public void disconnect_shouldRemoveConnectionCallbacks() {
    client.connect();
    client.disconnect();
    FusedLocationProviderApiImpl fusedLocationProviderApi =
        (FusedLocationProviderApiImpl) LocationServices.FusedLocationApi;
    FusedLocationServiceConnectionManager serviceConnectionManager =
        fusedLocationProviderApi.getServiceConnectionManager();
    assertThat(serviceConnectionManager.connectionCallbacks.size()).isEqualTo(0);
  }

  @Test public void isConnected_shouldReturnFalseBeforeConnected() throws Exception {
    assertThat(client.isConnected()).isFalse();
  }

  @Test public void isConnected_shouldReturnTrueAfterConnected() throws Exception {
    client.connect();
    assertThat(client.isConnected()).isTrue();
  }

  @Test public void isConnected_shouldReturnFalseAfterDisconnected() throws Exception {
    client.connect();
    client.disconnect();
    assertThat(client.isConnected()).isFalse();
  }

  @Test public void isConnected_multipleClients_shouldReturnFalseAfterDisconnected()
      throws Exception {
    LostApiClient anotherClient =
        new LostApiClientImpl(application, callbacks, new LostClientManager());
    anotherClient.connect();
    client.connect();
    client.disconnect();
    assertThat(client.isConnected()).isFalse();
  }
}
