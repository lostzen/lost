package mapzen.com.rxlost;

import com.mapzen.android.lost.api.LostApiClient;

import android.content.Context;

/**
 * Implementation of {@link ApiClientFactory} for use with {@link RxLostApiClient}.
 */
class LostApiClientFactory implements ApiClientFactory {

  @Override public LostApiClient getClient(Context context) {
    return new LostApiClient.Builder(context).build();
  }
}
