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
        if (LocationServices.FusedLocationApi == null) {
            LocationServices.FusedLocationApi = new FusedLocationProviderApiImpl(context);
        }
        if (LocationServices.GeofencingApi == null) {
            LocationServices.GeofencingApi = new GeofencingApiImpl();
        }
    }

    @Override
    public void disconnect() {
        if (LocationServices.FusedLocationApi != null && LocationServices.FusedLocationApi
                instanceof FusedLocationProviderApiImpl) {
            FusedLocationProviderApiImpl fusedProvider = (FusedLocationProviderApiImpl)
                    LocationServices.FusedLocationApi;
            fusedProvider.shutdown();
        }
        LocationServices.FusedLocationApi = null;
        LocationServices.GeofencingApi = null;
    }

    @Override
    public boolean isConnected() {
        return LocationServices.FusedLocationApi != null && LocationServices.GeofencingApi != null;
    }
    
}
