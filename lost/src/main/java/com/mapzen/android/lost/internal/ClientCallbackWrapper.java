package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LostApiClient;


/**
 * Wraps a {@link LostApiClient} to either a {@link com.mapzen.android.lost.api.LocationListener},
 * {@link android.app.PendingIntent}, or {@link com.mapzen.android.lost.api.LocationCallback}.
 */
class ClientCallbackWrapper {

  private LostApiClient client;
  private Object callback;

  public <T> ClientCallbackWrapper(LostApiClient client, T callback) {
    this.client = client;
    this.callback = callback;
  }

  @Override public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ClientCallbackWrapper wrapper = (ClientCallbackWrapper) o;

    if (!client.equals(wrapper.client)) {
      return false;
    }
    return callback.equals(wrapper.callback);
  }

  @Override public int hashCode() {
    int result = client.hashCode();
    result = 31 * result + callback.hashCode();
    return result;
  }
}
