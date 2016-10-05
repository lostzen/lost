package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.Geofence;

import android.content.Intent;
import android.os.Bundle;

import java.util.Set;

/**
 * Helper class for extracting {@link Geofence} information from an {@link Intent}.
 */
public class GeofenceIntentHelper {

  public static final String EXTRA_ENTERING = "entering";

  public int transitionForIntent(Intent intent) {
    Bundle extras = intent.getExtras();
    int transition;
    if (extras.containsKey(EXTRA_ENTERING)) {
      if (extras.getBoolean(EXTRA_ENTERING)) {
        transition = Geofence.GEOFENCE_TRANSITION_ENTER;
      } else {
        transition = Geofence.GEOFENCE_TRANSITION_EXIT;
      }
    } else {
      transition = Geofence.GEOFENCE_TRANSITION_DWELL;
    }
    return transition;
  }

  public int extractIntentId(Intent intent) {
    Set<String> categories = intent.getCategories();
    String intentStr = categories.iterator().next();
    return Integer.valueOf(intentStr);
  }
}
