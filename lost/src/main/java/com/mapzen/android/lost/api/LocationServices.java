package com.mapzen.android.lost.api;

import com.mapzen.android.lost.internal.FusedLocationProviderApiImpl;
import com.mapzen.android.lost.internal.GeofencingApiImpl;

public class LocationServices {

    public static FusedLocationProviderApi FusedLocationApi = new FusedLocationProviderApiImpl();

    public static com.mapzen.android.lost.api.GeofencingApi GeofencingApi = new GeofencingApiImpl();
}
