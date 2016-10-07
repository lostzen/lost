package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.Geofence;

import org.junit.Before;
import org.junit.Test;

import android.content.Intent;
import android.os.Bundle;

import java.util.HashSet;

import static com.mapzen.android.lost.internal.GeofenceIntentHelper.EXTRA_ENTERING;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by sarahlensing on 10/5/16.
 */
public class GeofencingDwellManagerTest {

  GeofencingDwellManager dwellManager;
  GeofencingApiImpl geofencingApi;

  @Before public void setup() {
    geofencingApi = mock(GeofencingApiImpl.class);
    dwellManager = new GeofencingDwellManager(geofencingApi);
  }

  @Test public void handleIntent_shouldRegisterGeofenceEntered() {
    Intent intent = mock(Intent.class);
    when(intent.getExtras()).thenReturn(mock(Bundle.class));
    when(intent.getExtras().containsKey(EXTRA_ENTERING)).thenReturn(true);
    when(intent.getExtras().getBoolean(EXTRA_ENTERING)).thenReturn(true);
    HashSet<String> categories = new HashSet<>();
    categories.add("123");
    when(intent.getCategories()).thenReturn(categories);
    dwellManager.handleIntent(intent);
    verify(geofencingApi).geofenceEntered(any(Geofence.class), anyInt());
  }

  @Test public void handleIntent_shouldRegisterGeofenceExited() {
    Intent intent = mock(Intent.class);
    when(intent.getExtras()).thenReturn(mock(Bundle.class));
    when(intent.getExtras().containsKey(EXTRA_ENTERING)).thenReturn(true);
    when(intent.getExtras().getBoolean(EXTRA_ENTERING)).thenReturn(false);
    HashSet<String> categories = new HashSet<>();
    categories.add("123");
    when(intent.getCategories()).thenReturn(categories);
    dwellManager.handleIntent(intent);
    verify(geofencingApi).geofenceExited(any(Geofence.class));
  }
}
