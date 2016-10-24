package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationAvailability;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LocationResult;
import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.lost.BuildConfig;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Looper;

import java.util.ArrayList;

import static android.location.LocationManager.GPS_PROVIDER;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.robolectric.RuntimeEnvironment.application;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, manifest = Config.NONE)
public class LostClientManagerTest {

  ClientManager manager = LostClientManager.shared();
  Context context = mock(Context.class);
  LostApiClient client = new LostApiClient.Builder(context).build();

  @After public void tearDown() {
    manager.shutdown();
  }

  @Test public void shouldHaveZeroClientCount() {
    assertThat(manager.numberOfClients()).isEqualTo(0);
  }

  @Test public void addClient_shouldIncreaseClientCount() {
    manager.addClient(client);
    assertThat(manager.numberOfClients()).isEqualTo(1);
    manager.removeClient(client);
  }

  @Test public void removeClient_shouldDecreaseClientCount() {
    manager.addClient(client);
    LostApiClient anotherClient = new LostApiClient.Builder(context).build();
    manager.addClient(anotherClient);
    assertThat(manager.numberOfClients()).isEqualTo(2);
    manager.removeClient(client);
    assertThat(manager.numberOfClients()).isEqualTo(1);
    manager.removeClient(anotherClient);
  }

  @Test public void addListener_shouldAddListenerForClient() {
    LocationRequest request = LocationRequest.create();
    TestLocationListener listener = new TestLocationListener();
    manager.addListener(client, request, listener);
    assertThat(manager.getLocationListeners().get(client)).contains(listener);
  }

  @Test public void addPendingIntent_shouldAddPendingIntentForClient() {
    LocationRequest request = LocationRequest.create();
    PendingIntent pendingIntent = mock(PendingIntent.class);
    manager.addPendingIntent(client, request, pendingIntent);
    assertThat(manager.getPendingIntents().get(client)).contains(pendingIntent);
  }

  @Test public void addLocationCallback_shouldAddLocationCallbackForClient() {
    LocationRequest request = LocationRequest.create();
    TestLocationCallback callback = new TestLocationCallback();
    Looper looper = mock(Looper.class);
    manager.addLocationCallback(client, request, callback, looper);
    assertThat(manager.getLocationCallbacks().get(client)).contains(callback);
  }

  @Test public void removeListener_shouldRemoveListenerForClient() {
    LocationRequest request = LocationRequest.create();
    TestLocationListener listener = new TestLocationListener();
    manager.addListener(client, request, listener);
    manager.removeListener(client, listener);
    assertThat(manager.getLocationListeners().get(client)).isNull();
  }

  @Test public void removePendingIntent_shouldRemovePendingIntentForClient() {
    LocationRequest request = LocationRequest.create();
    PendingIntent pendingIntent = mock(PendingIntent.class);
    manager.addPendingIntent(client, request, pendingIntent);
    manager.removePendingIntent(client, pendingIntent);
    assertThat(manager.getPendingIntents().get(client)).isNull();
  }

  @Test public void removeLocationCallback_shouldRemoveLocationCallbackForClient() {
    LocationRequest request = LocationRequest.create();
    TestLocationCallback callback = new TestLocationCallback();
    Looper looper = mock(Looper.class);
    manager.addLocationCallback(client, request, callback, looper);
    manager.removeLocationCallback(client, callback);
    assertThat(manager.getLocationCallbacks().get(client)).isNull();
  }

  @Test public void reportLocationChanged_shouldNotifyListener() {
    LocationRequest request = LocationRequest.create();
    TestLocationListener listener = new TestLocationListener();
    manager.addListener(client, request, listener);
    Location location = new Location("test");
    manager.reportLocationChanged(location);
    assertThat(listener.getAllLocations()).contains(location);
  }

  @Test public void sendPendingIntent_shouldFireIntent() {
    LocationRequest request = LocationRequest.create();
    Intent intent = new Intent(application, FusedLocationProviderServiceImplTest.TestService.class);
    PendingIntent pendingIntent = PendingIntent.getService(application, 0, intent, 0);
    manager.addPendingIntent(client, request, pendingIntent);
    Location location = new Location("test");
    LocationAvailability availability = new LocationAvailability(true);
    ArrayList<Location> locations = new ArrayList<>();
    locations.add(location);
    LocationResult result = LocationResult.create(locations);
    manager.sendPendingIntent(application, location, availability, result);
    Intent nextStartedService = ShadowApplication.getInstance().getNextStartedService();
    assertThat(nextStartedService).isNotNull();
    assertThat(nextStartedService.getParcelableExtra(
        LocationAvailability.EXTRA_LOCATION_AVAILABILITY)).isNotNull();
    assertThat(nextStartedService.getParcelableExtra(
        LocationResult.EXTRA_LOCATION_RESULT)).isNotNull();
  }

  @Test public void reportLocationResult_shouldNotifyCallback() {
    LocationRequest request = LocationRequest.create();
    TestLocationCallback callback = new TestLocationCallback();
    Looper looper = Looper.myLooper();
    manager.addLocationCallback(client, request, callback, looper);
    Location location = new Location("test");
    ArrayList<Location> locations = new ArrayList<>();
    locations.add(location);
    LocationResult result = LocationResult.create(locations);
    manager.reportLocationResult(result);
    assertThat(callback.getResult()).isEqualTo(result);
  }

  @Test public void reportProviderEnabled_shouldNotifyListeners() {
    LocationRequest request = LocationRequest.create();
    TestLocationListener listener = new TestLocationListener();
    manager.addListener(client, request, listener);
    manager.reportProviderEnabled(GPS_PROVIDER);
    assertThat(listener.getIsGpsEnabled()).isTrue();
  }

  @Test public void reportProviderDisabled_shouldNotifyListeners() {
    LocationRequest request = LocationRequest.create();
    TestLocationListener listener = new TestLocationListener();
    manager.addListener(client, request, listener);
    manager.reportProviderDisabled(GPS_PROVIDER);
    assertThat(listener.getIsGpsEnabled()).isFalse();
  }

  @Test public void notifyLocationAvailability_shouldNotifyCallback() {
    LocationRequest request = LocationRequest.create();
    TestLocationCallback callback = new TestLocationCallback();
    Looper looper = Looper.myLooper();
    manager.addLocationCallback(client, request, callback, looper);
    LocationAvailability availability = new LocationAvailability(true);
    manager.notifyLocationAvailability(availability);
    assertThat(callback.getAvailability()).isEqualTo(availability);
  }

  @Test public void hasNoListeners_shouldReturnTrue() {
    assertThat(manager.hasNoListeners()).isTrue();
  }

  @Test public void hasNoListeners_shouldReturnFalseWhenListenerAdded() {
    LocationRequest request = LocationRequest.create();
    TestLocationListener listener = new TestLocationListener();
    manager.addListener(client, request, listener);
    assertThat(manager.hasNoListeners()).isFalse();
  }

  @Test public void hasNoListeners_shouldReturnFalseWhenPendingIntentAdded() {
    LocationRequest request = LocationRequest.create();
    PendingIntent pendingIntent = mock(PendingIntent.class);
    manager.addPendingIntent(client, request, pendingIntent);
    assertThat(manager.hasNoListeners()).isFalse();
  }

  @Test public void hasNoListeners_shouldReturnFalseWhenCallbackAdded() {
    LocationRequest request = LocationRequest.create();
    TestLocationCallback callback = new TestLocationCallback();
    Looper looper = mock(Looper.class);
    manager.addLocationCallback(client, request, callback, looper);
    assertThat(manager.hasNoListeners()).isFalse();
  }

  @Test public void removeClient_shouldRemoveListenersForClient() {
    LocationRequest request = LocationRequest.create();
    TestLocationListener listener = new TestLocationListener();
    manager.addListener(client, request, listener);
    manager.removeClient(client);
    assertThat(manager.getLocationListeners().get(client)).isNull();
  }

  @Test public void removeClient_shouldRemovePendingIntentsForClient() {
    LocationRequest request = LocationRequest.create();
    PendingIntent pendingIntent = mock(PendingIntent.class);
    manager.addPendingIntent(client, request, pendingIntent);
    manager.removeClient(client);
    assertThat(manager.getPendingIntents().get(client)).isNull();
  }

  @Test public void removeClient_shouldRemoveCallbacksForClient() {
    LocationRequest request = LocationRequest.create();
    TestLocationCallback callback = new TestLocationCallback();
    Looper looper = mock(Looper.class);
    manager.addLocationCallback(client, request, callback, looper);
    manager.removeClient(client);
    assertThat(manager.getLocationCallbacks().get(client)).isNull();
  }

  @Test public void shutdown_shouldClearAllMaps() {
    LocationRequest request = LocationRequest.create();
    TestLocationListener listener = new TestLocationListener();
    manager.addListener(client, request, listener);

    PendingIntent pendingIntent = mock(PendingIntent.class);
    manager.addPendingIntent(client, request, pendingIntent);

    TestLocationCallback callback = new TestLocationCallback();
    Looper looper = mock(Looper.class);
    manager.addLocationCallback(client, request, callback, looper);

    manager.shutdown();
    assertThat(manager.getLocationListeners()).isEmpty();
    assertThat(manager.getPendingIntents()).isEmpty();
    assertThat(manager.getLocationCallbacks()).isEmpty();
  }

}
