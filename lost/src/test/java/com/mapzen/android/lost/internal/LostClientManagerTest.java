package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.BaseRobolectricTest;
import com.mapzen.android.lost.api.LocationAvailability;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LocationResult;
import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.lost.BuildConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.os.Looper;

import java.util.ArrayList;

import static android.location.LocationManager.GPS_PROVIDER;
import static com.mapzen.android.lost.api.LocationAvailability.EXTRA_LOCATION_AVAILABILITY;
import static com.mapzen.android.lost.api.LocationResult.EXTRA_LOCATION_RESULT;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.robolectric.RuntimeEnvironment.application;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, manifest = Config.NONE)
public class LostClientManagerTest extends BaseRobolectricTest {

  LostClientManager manager = new LostClientManager();
  LostApiClient client;

  @Before public void setup() throws Exception {
    client = new LostApiClient.Builder(application).build();
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
    LostApiClient anotherClient = new LostApiClient.Builder(application).build();
    manager.addClient(anotherClient);
    assertThat(manager.numberOfClients()).isEqualTo(2);
    manager.removeClient(client);
    assertThat(manager.numberOfClients()).isEqualTo(1);
    manager.removeClient(anotherClient);
  }

  @Test(expected = IllegalArgumentException.class)
  public void addListener_shouldThrowExceptionIfClientWasNotAdded() throws Exception {
    manager.addListener(client, LocationRequest.create(), new TestLocationListener());
  }

  @Test(expected = IllegalArgumentException.class)
  public void addPendingIntent_shouldThrowExceptionIfClientWasNotAdded() throws Exception {
    manager.addPendingIntent(client, LocationRequest.create(), mock(PendingIntent.class));
  }

  @Test(expected = IllegalArgumentException.class)
  public void addLocationCallback_shouldThrowExceptionIfClientWasNotAdded() throws Exception {
    manager.addLocationCallback(client, LocationRequest.create(), new TestLocationCallback(),
        mock(Looper.class));
  }

  @Test public void addListener_shouldAddListenerForClient() {
    manager.addClient(client);
    LocationRequest request = LocationRequest.create();
    TestLocationListener listener = new TestLocationListener();
    manager.addListener(client, request, listener);
    assertThat(manager.getLocationListeners().get(client)).contains(listener);
  }

  @Test public void addPendingIntent_shouldAddPendingIntentForClient() {
    manager.addClient(client);
    LocationRequest request = LocationRequest.create();
    PendingIntent pendingIntent = mock(PendingIntent.class);
    manager.addPendingIntent(client, request, pendingIntent);
    assertThat(manager.getPendingIntents().get(client)).contains(pendingIntent);
  }

  @Test public void addLocationCallback_shouldAddLocationCallbackForClient() {
    manager.addClient(client);
    LocationRequest request = LocationRequest.create();
    TestLocationCallback callback = new TestLocationCallback();
    Looper looper = mock(Looper.class);
    manager.addLocationCallback(client, request, callback, looper);
    assertThat(manager.getLocationCallbacks().get(client)).contains(callback);
  }

  @Test public void removeListener_shouldRemoveListenerForClient() {
    manager.addClient(client);
    LocationRequest request = LocationRequest.create();
    TestLocationListener listener = new TestLocationListener();
    manager.addListener(client, request, listener);
    manager.removeListener(client, listener);
    assertThat(manager.getLocationListeners().get(client)).isEmpty();
  }

  @Test public void removePendingIntent_shouldRemovePendingIntentForClient() {
    manager.addClient(client);
    LocationRequest request = LocationRequest.create();
    PendingIntent pendingIntent = mock(PendingIntent.class);
    manager.addPendingIntent(client, request, pendingIntent);
    manager.removePendingIntent(client, pendingIntent);
    assertThat(manager.getPendingIntents().get(client)).isEmpty();
  }

  @Test public void removeLocationCallback_shouldRemoveLocationCallbackForClient() {
    manager.addClient(client);
    LocationRequest request = LocationRequest.create();
    TestLocationCallback callback = new TestLocationCallback();
    Looper looper = mock(Looper.class);
    manager.addLocationCallback(client, request, callback, looper);
    manager.removeLocationCallback(client, callback);
    assertThat(manager.getLocationCallbacks().get(client)).isEmpty();
  }

  @Test public void reportLocationChanged_shouldNotifyListener() {
    manager.addClient(client);
    LocationRequest request = LocationRequest.create();
    TestLocationListener listener = new TestLocationListener();
    manager.addListener(client, request, listener);
    Location location = new Location("test");
    manager.reportLocationChanged(location);
    assertThat(listener.getAllLocations()).contains(location);
  }

  @Test public void sendPendingIntent_shouldFireIntent() {
    manager.addClient(client);
    LocationRequest request = LocationRequest.create();
    Intent intent = new Intent(application,
        FusedLocationProviderServiceDelegateTest.TestService.class);
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
        EXTRA_LOCATION_AVAILABILITY)).isNotNull();
    assertThat(nextStartedService.getParcelableExtra(
        EXTRA_LOCATION_RESULT)).isNotNull();
  }

  @Test public void reportLocationResult_shouldNotifyCallback() {
    manager.addClient(client);
    LocationRequest request = LocationRequest.create();
    TestLocationCallback callback = new TestLocationCallback();
    Looper looper = Looper.myLooper();
    manager.addLocationCallback(client, request, callback, looper);
    Location location = new Location("test");
    ArrayList<Location> locations = new ArrayList<>();
    locations.add(location);
    LocationResult result = LocationResult.create(locations);
    manager.reportLocationResult(location, result);
    assertThat(callback.getResult()).isEqualTo(result);
  }

  @Test public void notifyLocationAvailability_shouldNotifyCallback() {
    manager.addClient(client);
    LocationRequest request = LocationRequest.create();
    TestLocationCallback callback = new TestLocationCallback();
    Looper looper = Looper.myLooper();
    manager.addLocationCallback(client, request, callback, looper);
    LocationAvailability availability = new LocationAvailability(true);
    manager.notifyLocationAvailability(availability);
    assertThat(callback.getAvailability()).isEqualTo(availability);
  }

  @Test public void hasNoListeners_shouldReturnTrue() {
    manager.addClient(client);
    assertThat(manager.hasNoListeners()).isTrue();
  }

  @Test public void hasNoListeners_shouldReturnFalseWhenListenerAdded() {
    manager.addClient(client);
    LocationRequest request = LocationRequest.create();
    TestLocationListener listener = new TestLocationListener();
    manager.addListener(client, request, listener);
    assertThat(manager.hasNoListeners()).isFalse();
  }

  @Test public void hasNoListeners_shouldReturnFalseWhenPendingIntentAdded() {
    manager.addClient(client);
    LocationRequest request = LocationRequest.create();
    PendingIntent pendingIntent = mock(PendingIntent.class);
    manager.addPendingIntent(client, request, pendingIntent);
    assertThat(manager.hasNoListeners()).isFalse();
  }

  @Test public void hasNoListeners_shouldReturnFalseWhenCallbackAdded() {
    manager.addClient(client);
    LocationRequest request = LocationRequest.create();
    TestLocationCallback callback = new TestLocationCallback();
    Looper looper = mock(Looper.class);
    manager.addLocationCallback(client, request, callback, looper);
    assertThat(manager.hasNoListeners()).isFalse();
  }

  @Test public void removeClient_shouldRemoveListenersForClient() {
    manager.addClient(client);
    LocationRequest request = LocationRequest.create();
    TestLocationListener listener = new TestLocationListener();
    manager.addListener(client, request, listener);
    manager.removeClient(client);
    assertThat(manager.getLocationListeners().get(client)).isNull();
  }

  @Test public void removeClient_shouldRemovePendingIntentsForClient() {
    manager.addClient(client);
    LocationRequest request = LocationRequest.create();
    PendingIntent pendingIntent = mock(PendingIntent.class);
    manager.addPendingIntent(client, request, pendingIntent);
    manager.removeClient(client);
    assertThat(manager.getPendingIntents().get(client)).isNull();
  }

  @Test public void removeClient_shouldRemoveCallbacksForClient() {
    manager.addClient(client);
    LocationRequest request = LocationRequest.create();
    TestLocationCallback callback = new TestLocationCallback();
    Looper looper = mock(Looper.class);
    manager.addLocationCallback(client, request, callback, looper);
    manager.removeClient(client);
    assertThat(manager.getLocationCallbacks().get(client)).isNull();
  }

  @Test public void reportLocationChanged_shouldNotNotifyIfLessThanFastestInterval()
      throws Exception {
    final TestLocationListener listener = new TestLocationListener();
    final LocationRequest request = LocationRequest.create()
        .setFastestInterval(2000)
        .setSmallestDisplacement(0);
    manager.addClient(client);
    manager.addListener(client, request, listener);

    Location location1 = getTestLocation("test_provider", 0, 0, 0);
    Location location2 = getTestLocation("test_provider", 1, 1, 1000);

    ReportedChanges reportedChanges = manager.reportLocationChanged(location1);
    manager.updateReportedValues(reportedChanges);
    manager.reportLocationChanged(location2);
    assertThat(listener.getMostRecentLocation()).isEqualTo(location1);
  }

  @Test public void reportLocationChanged_shouldNotNotifyIfLessThanSmallestDisplacement()
      throws Exception {
    final TestLocationListener listener = new TestLocationListener();
    final LocationRequest locationRequest = LocationRequest.create()
        .setFastestInterval(0)
        .setSmallestDisplacement(1000000);
    manager.addClient(client);
    manager.addListener(client, locationRequest, listener);

    Location location1 = getTestLocation("test_provider", 0, 0, 0);
    Location location2 = getTestLocation("test_provider", 1, 1, 1);

    ReportedChanges reportedChanges = manager.reportLocationChanged(location1);
    manager.updateReportedValues(reportedChanges);
    manager.reportLocationChanged(location2);
    assertThat(listener.getMostRecentLocation()).isEqualTo(location1);
  }

  @Test public void sendPendingIntent_shouldNotifyPendingIntentWithLocationAvailability() {
    Intent i = new Intent(application, FusedLocationProviderServiceDelegateTest.TestService.class);
    PendingIntent pendingIntent = PendingIntent.getService(application, 0, i, 0);
    Location location = new Location("test");
    ArrayList<Location> locations = new ArrayList<>();
    locations.add(location);
    LocationResult result = LocationResult.create(locations);

    manager.addClient(client);
    manager.addPendingIntent(client, LocationRequest.create(), pendingIntent);
    manager.sendPendingIntent(application, location, new LocationAvailability(true), result);

    Intent next = ShadowApplication.getInstance().getNextStartedService();
    assertThat(next).isNotNull();
    assertThat(next.getParcelableExtra(EXTRA_LOCATION_AVAILABILITY)).isNotNull();
    assertThat(next.getParcelableExtra(EXTRA_LOCATION_RESULT)).isNotNull();
  }

  @Test public void reportLocationResult_shouldNotifyLocationCallback() {
    TestLocationCallback callback = new TestLocationCallback();
    Location location = new Location("test");
    ArrayList<Location> locations = new ArrayList<>();
    locations.add(location);
    LocationResult result = LocationResult.create(locations);

    manager.addClient(client);
    manager.addLocationCallback(client, LocationRequest.create(), callback, Looper.myLooper());
    manager.reportLocationResult(location, result);

    assertThat(callback.getResult().getLastLocation()).isEqualTo(location);
  }

  @Test public void sendPendingIntent_shouldNotNotifyRemovedPendingIntent() throws Exception {
    Intent i1 = new Intent(application, FusedLocationProviderServiceDelegateTest.TestService.class);
    Intent i2 = new Intent(application, FusedLocationProviderServiceDelegateTest.TestService.class);
    PendingIntent pendingIntent = PendingIntent.getService(application, 0, i1, 0);
    PendingIntent otherPendingIntent = PendingIntent.getService(application, 0, i2, 0);
    Location location = new Location("test");
    ArrayList<Location> locations = new ArrayList<>();
    locations.add(location);
    LocationResult result = LocationResult.create(locations);

    manager.addClient(client);
    manager.addPendingIntent(client, LocationRequest.create(), pendingIntent);
    manager.addPendingIntent(client, LocationRequest.create(), otherPendingIntent);
    manager.removePendingIntent(client, otherPendingIntent);
    manager.sendPendingIntent(application, location, new LocationAvailability(true), result);

    // Only one service should be started since the second pending intent request was removed.
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNotNull();
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNull();
  }

  @Test public void reportLocationChanged_shouldNotifyBothListeners() {
    TestLocationListener listener1 = new TestLocationListener();
    TestLocationListener listener2 = new TestLocationListener();
    manager.addClient(client);
    manager.addListener(client, LocationRequest.create(), listener1);
    manager.addListener(client, LocationRequest.create(), listener2);

    Location location = new Location("test");
    manager.reportLocationChanged(location);
    assertThat(listener1.getAllLocations()).contains(location);
    assertThat(listener2.getAllLocations()).contains(location);
  }

  @Test public void sendPendingIntent_shouldNotifyBothPendingIntents() throws Exception {
    Intent i1 = new Intent(application, FusedLocationProviderServiceDelegateTest.TestService.class);
    Intent i2 = new Intent(application, FusedLocationProviderServiceDelegateTest.TestService.class);
    PendingIntent pendingIntent = PendingIntent.getService(application, 0, i1, 0);
    PendingIntent otherPendingIntent = PendingIntent.getService(application, 0, i2, 0);
    Location location = new Location("test");
    ArrayList<Location> locations = new ArrayList<>();
    locations.add(location);
    LocationResult result = LocationResult.create(locations);

    manager.addClient(client);
    manager.addPendingIntent(client, LocationRequest.create(), pendingIntent);
    manager.addPendingIntent(client, LocationRequest.create(), otherPendingIntent);
    manager.sendPendingIntent(application, location, new LocationAvailability(true), result);

    // Two services should be started.
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNotNull();
    assertThat(ShadowApplication.getInstance().getNextStartedService()).isNotNull();
  }

  @Test public void reportLocationChanged_shouldNotifyBothCallbacks() {
    TestLocationCallback callback1 = new TestLocationCallback();
    TestLocationCallback callback2 = new TestLocationCallback();
    manager.addClient(client);
    manager.addLocationCallback(client, LocationRequest.create(), callback1, Looper.myLooper());
    manager.addLocationCallback(client, LocationRequest.create(), callback2, Looper.myLooper());

    Location location = new Location("test");
    ArrayList<Location> locations = new ArrayList<>();
    locations.add(location);
    LocationResult locationResult = LocationResult.create(locations);
    manager.reportLocationResult(location, locationResult);
    assertThat(callback1.getResult()).isEqualTo(locationResult);
    assertThat(callback2.getResult()).isEqualTo(locationResult);
  }

  @Test public void reportLocationChanged_shouldNotNotifyRemovedListener() {
    LocationRequest request = LocationRequest.create();
    TestLocationListener listener1 = new TestLocationListener();
    TestLocationListener listener2 = new TestLocationListener();
    manager.addClient(client);
    manager.addListener(client, request, listener1);
    manager.addListener(client, request, listener2);
    manager.removeListener(client, listener2);
    Location location = new Location(GPS_PROVIDER);
    manager.reportLocationChanged(location);
    assertThat(listener1.getAllLocations()).contains(location);
    assertThat(listener2.getAllLocations()).doesNotContain(location);
  }

  private static Location getTestLocation(String provider, float lat, float lng, long time) {
    Location location = new Location(provider);
    location.setLatitude(lat);
    location.setLongitude(lng);
    location.setTime(time);
    return location;
  }
}
