package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LostApiClient;

public interface ClientManagerListener {
    void onClientAdded(LostApiClient client);
}
