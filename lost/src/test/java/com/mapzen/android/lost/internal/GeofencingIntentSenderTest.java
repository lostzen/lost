package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.Geofence;
import com.mapzen.android.lost.api.GeofencingApi;
import com.mapzen.android.lost.api.GeofencingIntentSender;
import com.mapzen.android.lost.api.LocationServices;
import com.mapzen.lost.BuildConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;

import java.util.ArrayList;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, manifest = Config.NONE)
public class GeofencingIntentSenderTest {

  GeofencingIntentSender intentSender;

  @Before public void setup() {
    intentSender = new GeofencingIntentSender(mock(Context.class));
  }

  @Test public void generateIntent_shouldHaveExtras() {
    GeofencingApiImpl geofencingApi = (GeofencingApiImpl) LocationServices.GeofencingApi;
    int intentId = 123;
    ParcelableGeofence geofence = new ParcelableGeofence("test", 40.0, 70.0, 50.0f, 1000,
        Geofence.GEOFENCE_TRANSITION_ENTER);
    geofencingApi.setGeofenceForIntentId(intentId, geofence);

    Intent intent = new Intent("");
    Bundle extras = new Bundle();
    extras.putBoolean(GeofencingIntentSender.EXTRA_ENTERING, true);
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
    int intentId = 123;

    Intent intent = new Intent("");
    Bundle extras = new Bundle();
    extras.putBoolean(GeofencingIntentSender.EXTRA_ENTERING, true);
    intent.putExtras(extras);
    intent.addCategory(String.valueOf(intentId));

    ParcelableGeofence geofence = new ParcelableGeofence("test", 40.0, 70.0, 50.0f, 1000,
        Geofence.GEOFENCE_TRANSITION_ENTER);
    GeofencingApiImpl geofencingApi = (GeofencingApiImpl) LocationServices.GeofencingApi;
    geofencingApi.setGeofenceForIntentId(intentId, geofence);

    boolean shouldSendIntent = intentSender.shouldSendIntent(intent);
    assertThat(shouldSendIntent).isTrue();
  }

  @Test public void shouldSendIntent_exit_shouldReturnFalseForEnter() {
    int intentId = 123;

    Intent intent = new Intent("");
    Bundle extras = new Bundle();
    extras.putBoolean(GeofencingIntentSender.EXTRA_ENTERING, true);
    intent.putExtras(extras);
    intent.addCategory(String.valueOf(intentId));

    ParcelableGeofence geofence = new ParcelableGeofence("test", 40.0, 70.0, 50.0f, 1000,
        Geofence.GEOFENCE_TRANSITION_EXIT);
    GeofencingApiImpl geofencingApi = (GeofencingApiImpl) LocationServices.GeofencingApi;
    geofencingApi.setGeofenceForIntentId(intentId, geofence);

    boolean shouldSendIntent = intentSender.shouldSendIntent(intent);
    assertThat(shouldSendIntent).isFalse();
  }

  @Test public void shouldSendIntent_enterExit_shouldReturnTrueForEnter() {
    int intentId = 123;

    Intent intent = new Intent("");
    Bundle extras = new Bundle();
    extras.putBoolean(GeofencingIntentSender.EXTRA_ENTERING, true);
    intent.putExtras(extras);
    intent.addCategory(String.valueOf(intentId));

    ParcelableGeofence geofence = new ParcelableGeofence("test", 40.0, 70.0, 50.0f, 1000,
        Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT);
    GeofencingApiImpl geofencingApi = (GeofencingApiImpl) LocationServices.GeofencingApi;
    geofencingApi.setGeofenceForIntentId(intentId, geofence);

    boolean shouldSendIntent = intentSender.shouldSendIntent(intent);
    assertThat(shouldSendIntent).isTrue();
  }

  @Test public void shouldSendIntent_dwell_shouldReturnFalseForEnter() {
    int intentId = 123;

    Intent intent = new Intent("");
    Bundle extras = new Bundle();
    extras.putBoolean(GeofencingIntentSender.EXTRA_ENTERING, true);
    intent.putExtras(extras);
    intent.addCategory(String.valueOf(intentId));

    ParcelableGeofence geofence = new ParcelableGeofence("test", 40.0, 70.0, 50.0f, 1000,
        Geofence.GEOFENCE_TRANSITION_DWELL);
    GeofencingApiImpl geofencingApi = (GeofencingApiImpl) LocationServices.GeofencingApi;
    geofencingApi.setGeofenceForIntentId(intentId, geofence);

    boolean shouldSendIntent = intentSender.shouldSendIntent(intent);
    assertThat(shouldSendIntent).isFalse();
  }

  @Test public void shouldSendIntent_enter_shouldReturnFalseForExit() {
    int intentId = 123;

    Intent intent = new Intent("");
    Bundle extras = new Bundle();
    extras.putBoolean(GeofencingIntentSender.EXTRA_ENTERING, false);
    intent.putExtras(extras);
    intent.addCategory(String.valueOf(intentId));

    ParcelableGeofence geofence = new ParcelableGeofence("test", 40.0, 70.0, 50.0f, 1000,
        Geofence.GEOFENCE_TRANSITION_ENTER);
    GeofencingApiImpl geofencingApi = (GeofencingApiImpl) LocationServices.GeofencingApi;
    geofencingApi.setGeofenceForIntentId(intentId, geofence);

    boolean shouldSendIntent = intentSender.shouldSendIntent(intent);
    assertThat(shouldSendIntent).isFalse();
  }

  @Test public void shouldSendIntent_exit_shouldReturnTrueForExit() {
    int intentId = 123;

    Intent intent = new Intent("");
    Bundle extras = new Bundle();
    extras.putBoolean(GeofencingIntentSender.EXTRA_ENTERING, false);
    intent.putExtras(extras);
    intent.addCategory(String.valueOf(intentId));

    ParcelableGeofence geofence = new ParcelableGeofence("test", 40.0, 70.0, 50.0f, 1000,
        Geofence.GEOFENCE_TRANSITION_EXIT);
    GeofencingApiImpl geofencingApi = (GeofencingApiImpl) LocationServices.GeofencingApi;
    geofencingApi.setGeofenceForIntentId(intentId, geofence);

    boolean shouldSendIntent = intentSender.shouldSendIntent(intent);
    assertThat(shouldSendIntent).isTrue();
  }

  @Test public void shouldSendIntent_enterExit_shouldReturnTrueForExit() {
    int intentId = 123;

    Intent intent = new Intent("");
    Bundle extras = new Bundle();
    extras.putBoolean(GeofencingIntentSender.EXTRA_ENTERING, false);
    intent.putExtras(extras);
    intent.addCategory(String.valueOf(intentId));

    ParcelableGeofence geofence = new ParcelableGeofence("test", 40.0, 70.0, 50.0f, 1000,
        Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT);
    GeofencingApiImpl geofencingApi = (GeofencingApiImpl) LocationServices.GeofencingApi;
    geofencingApi.setGeofenceForIntentId(intentId, geofence);

    boolean shouldSendIntent = intentSender.shouldSendIntent(intent);
    assertThat(shouldSendIntent).isTrue();
  }

  @Test public void shouldSendIntent_dwell_shouldReturnFalseForExit() {
    int intentId = 123;

    Intent intent = new Intent("");
    Bundle extras = new Bundle();
    extras.putBoolean(GeofencingIntentSender.EXTRA_ENTERING, true);
    intent.putExtras(extras);
    intent.addCategory(String.valueOf(intentId));

    ParcelableGeofence geofence = new ParcelableGeofence("test", 40.0, 70.0, 50.0f, 1000,
        Geofence.GEOFENCE_TRANSITION_DWELL);
    GeofencingApiImpl geofencingApi = (GeofencingApiImpl) LocationServices.GeofencingApi;
    geofencingApi.setGeofenceForIntentId(intentId, geofence);

    boolean shouldSendIntent = intentSender.shouldSendIntent(intent);
    assertThat(shouldSendIntent).isFalse();
  }

  @Test public void shouldSendIntent_enter_shouldReturnFalseForDwell() {
    int intentId = 123;

    Intent intent = new Intent("");
    Bundle extras = new Bundle();
    intent.putExtras(extras);
    intent.addCategory(String.valueOf(intentId));

    ParcelableGeofence geofence = new ParcelableGeofence("test", 40.0, 70.0, 50.0f, 1000,
        Geofence.GEOFENCE_TRANSITION_ENTER);
    GeofencingApiImpl geofencingApi = (GeofencingApiImpl) LocationServices.GeofencingApi;
    geofencingApi.setGeofenceForIntentId(intentId, geofence);

    boolean shouldSendIntent = intentSender.shouldSendIntent(intent);
    assertThat(shouldSendIntent).isFalse();
  }

  @Test public void shouldSendIntent_exit_shouldReturnFalseForDwell() {
    int intentId = 123;

    Intent intent = new Intent("");
    Bundle extras = new Bundle();
    intent.putExtras(extras);
    intent.addCategory(String.valueOf(intentId));

    ParcelableGeofence geofence = new ParcelableGeofence("test", 40.0, 70.0, 50.0f, 1000,
        Geofence.GEOFENCE_TRANSITION_EXIT);
    GeofencingApiImpl geofencingApi = (GeofencingApiImpl) LocationServices.GeofencingApi;
    geofencingApi.setGeofenceForIntentId(intentId, geofence);

    boolean shouldSendIntent = intentSender.shouldSendIntent(intent);
    assertThat(shouldSendIntent).isFalse();
  }

  @Test public void shouldSendIntent_enterExitDwell_shouldReturnTrueForDwell() {
    int intentId = 123;

    Intent intent = new Intent("");
    Bundle extras = new Bundle();
    intent.putExtras(extras);
    intent.addCategory(String.valueOf(intentId));

    ParcelableGeofence geofence = new ParcelableGeofence("test", 40.0, 70.0, 50.0f, 1000,
        Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT
            | Geofence.GEOFENCE_TRANSITION_DWELL);
    GeofencingApiImpl geofencingApi = (GeofencingApiImpl) LocationServices.GeofencingApi;
    geofencingApi.setGeofenceForIntentId(intentId, geofence);

    boolean shouldSendIntent = intentSender.shouldSendIntent(intent);
    assertThat(shouldSendIntent).isTrue();
  }

  @Test public void shouldSendIntent_dwell_shouldReturnTrueForDwell() {
    int intentId = 123;

    Intent intent = new Intent("");
    Bundle extras = new Bundle();
    intent.putExtras(extras);
    intent.addCategory(String.valueOf(intentId));

    ParcelableGeofence geofence = new ParcelableGeofence("test", 40.0, 70.0, 50.0f, 1000,
        Geofence.GEOFENCE_TRANSITION_DWELL);
    GeofencingApiImpl geofencingApi = (GeofencingApiImpl) LocationServices.GeofencingApi;
    geofencingApi.setGeofenceForIntentId(intentId, geofence);

    boolean shouldSendIntent = intentSender.shouldSendIntent(intent);
    assertThat(shouldSendIntent).isTrue();
  }

}
