package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.BaseRobolectricTest;
import com.mapzen.android.lost.api.Geofence;
import com.mapzen.android.lost.api.GeofencingApi;
import com.mapzen.android.lost.api.GeofencingIntentSender;
import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.lost.BuildConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import java.util.ArrayList;

import static com.mapzen.android.lost.internal.GeofenceIntentHelper.EXTRA_ENTERING;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("MissingPermission")
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, manifest = Config.NONE)
public class GeofencingIntentSenderTest extends BaseRobolectricTest {

  GeofencingIntentSender intentSender;
  GeofencingApiImpl geofencingApi;
  Context context;
  int geofenceId = 123;
  LostApiClient client;

  @Before public void setup() {
    IdGenerator idGenerator = new TestIdGenerator();
    geofencingApi = new GeofencingApiImpl(new TestIntentFactory(), new TestIntentFactory(),
        idGenerator);
    intentSender = new GeofencingIntentSender(mock(Context.class), geofencingApi);
    context = mock(Context.class);
    when(context.getSystemService(Context.LOCATION_SERVICE)).thenReturn(
        mock(LocationManager.class));
    geofencingApi.connect(context);
    client = mock(LostApiClient.class);
    when(client.isConnected()).thenReturn(true);
  }

  @Test public void generateIntent_shouldHaveExtras() {
    int intentId = geofenceId;
    ParcelableGeofence geofence = new ParcelableGeofence("test", 40.0, 70.0, 50.0f, 1000,
        Geofence.GEOFENCE_TRANSITION_ENTER, 0);
    ArrayList<Geofence> allGeofences = new ArrayList<>();
    allGeofences.add(geofence);
    geofencingApi.addGeofences(client, allGeofences, null);

    Intent intent = new Intent("");
    Bundle extras = new Bundle();
    extras.putBoolean(EXTRA_ENTERING, true);
    intent.putExtras(extras);
    intent.addCategory(String.valueOf(intentId));

    Location location = new Location("");
    Intent generated = intentSender.generateIntent(intent, location);
    ArrayList geofences = (ArrayList) generated.getExtras().get(GeofencingApi.EXTRA_GEOFENCE_LIST);
    assertThat(geofences.get(0)).isEqualTo(geofence);
    assertThat(generated.getExtras().get(GeofencingApi.EXTRA_TRANSITION)).isEqualTo(
        Geofence.GEOFENCE_TRANSITION_ENTER);
    assertThat(generated.getExtras().get(GeofencingApi.EXTRA_TRIGGERING_LOCATION)).isEqualTo(
        location);
  }

  @Test public void shouldSendIntent_enter_shouldReturnTrueForEnter() {
    int intentId = geofenceId;

    Intent intent = new Intent("");
    Bundle extras = new Bundle();
    extras.putBoolean(EXTRA_ENTERING, true);
    intent.putExtras(extras);
    intent.addCategory(String.valueOf(intentId));

    ParcelableGeofence geofence = new ParcelableGeofence("test", 40.0, 70.0, 50.0f, 1000,
        Geofence.GEOFENCE_TRANSITION_ENTER, 0);
    ArrayList<Geofence> allGeofences = new ArrayList<>();
    allGeofences.add(geofence);
    geofencingApi.addGeofences(client, allGeofences, null);

    boolean shouldSendIntent = intentSender.shouldSendIntent(intent);
    assertThat(shouldSendIntent).isTrue();
  }

  @Test public void shouldSendIntent_exit_shouldReturnFalseForEnter() {
    int intentId = geofenceId;

    Intent intent = new Intent("");
    Bundle extras = new Bundle();
    extras.putBoolean(EXTRA_ENTERING, true);
    intent.putExtras(extras);
    intent.addCategory(String.valueOf(intentId));

    ParcelableGeofence geofence = new ParcelableGeofence("test", 40.0, 70.0, 50.0f, 1000,
        Geofence.GEOFENCE_TRANSITION_EXIT, 0);
    ArrayList<Geofence> allGeofences = new ArrayList<>();
    allGeofences.add(geofence);
    geofencingApi.addGeofences(client, allGeofences, null);

    boolean shouldSendIntent = intentSender.shouldSendIntent(intent);
    assertThat(shouldSendIntent).isFalse();
  }

  @Test public void shouldSendIntent_enterExit_shouldReturnTrueForEnter() {
    int intentId = geofenceId;

    Intent intent = new Intent("");
    Bundle extras = new Bundle();
    extras.putBoolean(EXTRA_ENTERING, true);
    intent.putExtras(extras);
    intent.addCategory(String.valueOf(intentId));

    ParcelableGeofence geofence = new ParcelableGeofence("test", 40.0, 70.0, 50.0f, 1000,
        Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT, 0);
    ArrayList<Geofence> allGeofences = new ArrayList<>();
    allGeofences.add(geofence);
    geofencingApi.addGeofences(client, allGeofences, null);

    boolean shouldSendIntent = intentSender.shouldSendIntent(intent);
    assertThat(shouldSendIntent).isTrue();
  }

  @Test public void shouldSendIntent_dwell_shouldReturnFalseForEnter() {
    int intentId = geofenceId;

    Intent intent = new Intent("");
    Bundle extras = new Bundle();
    extras.putBoolean(EXTRA_ENTERING, true);
    intent.putExtras(extras);
    intent.addCategory(String.valueOf(intentId));

    ParcelableGeofence geofence = new ParcelableGeofence("test", 40.0, 70.0, 50.0f, 1000,
        Geofence.GEOFENCE_TRANSITION_DWELL, 0);
    ArrayList<Geofence> allGeofences = new ArrayList<>();
    allGeofences.add(geofence);
    geofencingApi.addGeofences(client, allGeofences, null);

    boolean shouldSendIntent = intentSender.shouldSendIntent(intent);
    assertThat(shouldSendIntent).isFalse();
  }

  @Test public void shouldSendIntent_enter_shouldReturnFalseForExit() {
    int intentId = geofenceId;

    Intent intent = new Intent("");
    Bundle extras = new Bundle();
    extras.putBoolean(EXTRA_ENTERING, false);
    intent.putExtras(extras);
    intent.addCategory(String.valueOf(intentId));

    ParcelableGeofence geofence = new ParcelableGeofence("test", 40.0, 70.0, 50.0f, 1000,
        Geofence.GEOFENCE_TRANSITION_ENTER, 0);
    ArrayList<Geofence> allGeofences = new ArrayList<>();
    allGeofences.add(geofence);
    geofencingApi.addGeofences(client, allGeofences, null);

    boolean shouldSendIntent = intentSender.shouldSendIntent(intent);
    assertThat(shouldSendIntent).isFalse();
  }

  @Test public void shouldSendIntent_exit_shouldReturnTrueForExit() {
    int intentId = geofenceId;

    Intent intent = new Intent("");
    Bundle extras = new Bundle();
    extras.putBoolean(EXTRA_ENTERING, false);
    intent.putExtras(extras);
    intent.addCategory(String.valueOf(intentId));

    ParcelableGeofence geofence = new ParcelableGeofence("test", 40.0, 70.0, 50.0f, 1000,
        Geofence.GEOFENCE_TRANSITION_EXIT, 0);
    ArrayList<Geofence> allGeofences = new ArrayList<>();
    allGeofences.add(geofence);
    geofencingApi.addGeofences(client, allGeofences, null);

    boolean shouldSendIntent = intentSender.shouldSendIntent(intent);
    assertThat(shouldSendIntent).isTrue();
  }

  @Test public void shouldSendIntent_enterExit_shouldReturnTrueForExit() {
    int intentId = geofenceId;

    Intent intent = new Intent("");
    Bundle extras = new Bundle();
    extras.putBoolean(EXTRA_ENTERING, false);
    intent.putExtras(extras);
    intent.addCategory(String.valueOf(intentId));

    ParcelableGeofence geofence = new ParcelableGeofence("test", 40.0, 70.0, 50.0f, 1000,
        Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT, 0);
    ArrayList<Geofence> allGeofences = new ArrayList<>();
    allGeofences.add(geofence);
    geofencingApi.addGeofences(client, allGeofences, null);

    boolean shouldSendIntent = intentSender.shouldSendIntent(intent);
    assertThat(shouldSendIntent).isTrue();
  }

  @Test public void shouldSendIntent_dwell_shouldReturnFalseForExit() {
    int intentId = geofenceId;

    Intent intent = new Intent("");
    Bundle extras = new Bundle();
    extras.putBoolean(EXTRA_ENTERING, true);
    intent.putExtras(extras);
    intent.addCategory(String.valueOf(intentId));

    ParcelableGeofence geofence = new ParcelableGeofence("test", 40.0, 70.0, 50.0f, 1000,
        Geofence.GEOFENCE_TRANSITION_DWELL, 0);
    ArrayList<Geofence> allGeofences = new ArrayList<>();
    allGeofences.add(geofence);
    geofencingApi.addGeofences(client, allGeofences, null);

    boolean shouldSendIntent = intentSender.shouldSendIntent(intent);
    assertThat(shouldSendIntent).isFalse();
  }

  @Test public void shouldSendIntent_enter_shouldReturnFalseForDwell() {
    int intentId = geofenceId;

    Intent intent = new Intent("");
    Bundle extras = new Bundle();
    intent.putExtras(extras);
    intent.addCategory(String.valueOf(intentId));

    ParcelableGeofence geofence = new ParcelableGeofence("test", 40.0, 70.0, 50.0f, 1000,
        Geofence.GEOFENCE_TRANSITION_ENTER, 0);
    ArrayList<Geofence> allGeofences = new ArrayList<>();
    allGeofences.add(geofence);
    geofencingApi.addGeofences(client, allGeofences, null);

    boolean shouldSendIntent = intentSender.shouldSendIntent(intent);
    assertThat(shouldSendIntent).isFalse();
  }

  @Test public void shouldSendIntent_exit_shouldReturnFalseForDwell() {
    int intentId = geofenceId;

    Intent intent = new Intent("");
    Bundle extras = new Bundle();
    intent.putExtras(extras);
    intent.addCategory(String.valueOf(intentId));

    ParcelableGeofence geofence = new ParcelableGeofence("test", 40.0, 70.0, 50.0f, 1000,
        Geofence.GEOFENCE_TRANSITION_EXIT, 0);
    ArrayList<Geofence> allGeofences = new ArrayList<>();
    allGeofences.add(geofence);
    geofencingApi.addGeofences(client, allGeofences, null);

    boolean shouldSendIntent = intentSender.shouldSendIntent(intent);
    assertThat(shouldSendIntent).isFalse();
  }

  @Test public void shouldSendIntent_enterExitDwell_shouldReturnTrueForDwell() {
    int intentId = geofenceId;

    Intent intent = new Intent("");
    Bundle extras = new Bundle();
    intent.putExtras(extras);
    intent.addCategory(String.valueOf(intentId));

    ParcelableGeofence geofence = new ParcelableGeofence("test", 40.0, 70.0, 50.0f, 1000,
        Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT
            | Geofence.GEOFENCE_TRANSITION_DWELL, 0);
    ArrayList<Geofence> allGeofences = new ArrayList<>();
    allGeofences.add(geofence);
    geofencingApi.addGeofences(client, allGeofences, null);

    boolean shouldSendIntent = intentSender.shouldSendIntent(intent);
    assertThat(shouldSendIntent).isTrue();
  }

  @Test public void shouldSendIntent_dwell_shouldReturnTrueForDwell() {
    int intentId = geofenceId;

    Intent intent = new Intent("");
    Bundle extras = new Bundle();
    intent.putExtras(extras);
    intent.addCategory(String.valueOf(intentId));

    ParcelableGeofence geofence = new ParcelableGeofence("test", 40.0, 70.0, 50.0f, 1000,
        Geofence.GEOFENCE_TRANSITION_DWELL, 0);
    ArrayList<Geofence> allGeofences = new ArrayList<>();
    allGeofences.add(geofence);
    geofencingApi.addGeofences(client, allGeofences, null);

    boolean shouldSendIntent = intentSender.shouldSendIntent(intent);
    assertThat(shouldSendIntent).isTrue();
  }

  private class TestIdGenerator implements IdGenerator {

    @Override public int generateId() {
      return geofenceId;
    }
  }

}
