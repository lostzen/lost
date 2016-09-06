package com.mapzen.android.lost.api;

import com.mapzen.android.lost.internal.FusedLocationProviderApiImpl;
import com.mapzen.android.lost.internal.GeofencingApiImpl;
import com.mapzen.android.lost.internal.SettingsApiImpl;

public class LocationServices {

  public static final FusedLocationProviderApi FusedLocationApi =
      new FusedLocationProviderApiImpl();

  public static final GeofencingApi GeofencingApi = new GeofencingApiImpl();

  public static final SettingsApi SettingsApi = new SettingsApiImpl();
}
