package com.mapzen.android.lost.api;

import com.mapzen.android.lost.internal.DwellServiceIntentFactory;
import com.mapzen.android.lost.internal.FusedLocationProviderApiImpl;
import com.mapzen.android.lost.internal.FusedLocationServiceCallbackManager;
import com.mapzen.android.lost.internal.FusedLocationServiceConnectionManager;
import com.mapzen.android.lost.internal.GeofencingApiImpl;
import com.mapzen.android.lost.internal.GeofencingServiceIntentFactory;
import com.mapzen.android.lost.internal.LostRequestManager;
import com.mapzen.android.lost.internal.PendingIntentIdGenerator;
import com.mapzen.android.lost.internal.SettingsApiImpl;

/**
 * Entry point for API interaction.
 */
public class LocationServices {

  /**
   * Entry point for APIs concerning location updates.
   */
  public static final FusedLocationProviderApi FusedLocationApi =
      new FusedLocationProviderApiImpl(new FusedLocationServiceConnectionManager(),
          new FusedLocationServiceCallbackManager(), LostRequestManager.shared());

  /**
   * Entry point for APIs concerning geofences.
   */
  public static final GeofencingApi GeofencingApi = new GeofencingApiImpl(
      new GeofencingServiceIntentFactory(), new DwellServiceIntentFactory(),
      new PendingIntentIdGenerator());

  /**
   * Entry point for APIs concerning location settings.
   */
  public static final SettingsApi SettingsApi = new SettingsApiImpl();
}
