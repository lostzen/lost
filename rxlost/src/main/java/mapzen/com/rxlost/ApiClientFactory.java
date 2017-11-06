package mapzen.com.rxlost;

import com.mapzen.android.lost.api.LostApiClient;

import android.content.Context;

/**
 * Interface for {@link LostApiClient} factory generators.
 */
interface ApiClientFactory {
  LostApiClient getClient(Context context);
}
