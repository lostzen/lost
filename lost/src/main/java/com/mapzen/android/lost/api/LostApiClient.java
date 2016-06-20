package com.mapzen.android.lost.api;

import com.mapzen.android.lost.internal.LostApiClientImpl;

import android.content.Context;

public interface LostApiClient {

    public void connect();

    public void disconnect();

    public boolean isConnected();

    public int numberOfListeners();

    public static final class Builder {
        private final Context context;

        public Builder(Context context) {
            this.context = context;
        }

        public LostApiClient build() {
            return new LostApiClientImpl(context);
        }
    }
}
