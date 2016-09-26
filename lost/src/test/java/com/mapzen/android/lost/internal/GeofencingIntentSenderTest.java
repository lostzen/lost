package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.Geofence;
import com.mapzen.android.lost.api.GeofencingApi;
import com.mapzen.android.lost.api.GeofencingIntentSender;
import com.mapzen.android.lost.api.GeofencingIntentService;
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
    Intent intent = new Intent("");
    Bundle extras = new Bundle();
    extras.putInt(GeofencingIntentSender.EXTRA_ENTERING, 1);
    ParcelableGeofence geofence = new ParcelableGeofence("test", 40.0, 70.0, 50.0f, 1000);
    extras.putParcelable(GeofencingIntentService.EXTRA_GEOFENCE, geofence);
    intent.putExtras(extras);

    Location location = new Location("");
    Intent generated = intentSender.generateIntent(intent, location);
    ArrayList geofences = (ArrayList) generated.getExtras().get(GeofencingApi.EXTRA_GEOFENCE_LIST);
    assertThat(geofences.get(0)).isEqualTo(geofence);
    assertThat(generated.getExtras().get(GeofencingApi.EXTRA_TRANSITION)).isEqualTo(
        Geofence.GEOFENCE_TRANSITION_ENTER);
    assertThat(generated.getExtras().get(GeofencingApi.EXTRA_TRIGGERING_LOCATION)).isEqualTo(
        location);
  }
}
