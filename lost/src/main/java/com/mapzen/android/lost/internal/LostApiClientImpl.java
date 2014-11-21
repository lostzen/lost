package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationServices;
import com.mapzen.android.lost.api.LostApiClient;

import android.content.Context;

/**
 * Implementation for {@link LostApiClient}. Constructs API implementations with {@link Context}.
 */
public class LostApiClientImpl implements LostApiClient {
    private final Context context;

    public LostApiClientImpl(Context context) {
        this.context = context;
    }

    @Override
    public void connect() {
        LocationServices.FusedLocationApi = new FusedLocationProviderApiImpl(context);
        LocationServices.GeofencingApi = new GeofencingApiImpl();
    }
}
