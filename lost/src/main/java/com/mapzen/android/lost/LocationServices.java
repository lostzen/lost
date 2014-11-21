package com.mapzen.android.lost;

public class LocationServices {

    public static FusedLocationProviderApi FusedLocationApi = new FusedLocationProviderApiImpl();

    public static GeofencingApi GeofencingApi = new GeofencingApiImpl();
}
