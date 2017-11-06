package mapzen.com.rxlost;

import com.mapzen.android.lost.api.LostApiClient;

import android.content.Context;

import static org.mockito.Mockito.mock;

public class TestApiClientFactory implements ApiClientFactory {

  LostApiClient client = mock(LostApiClient.class);

  @Override public LostApiClient getClient(Context context) {
    return client;
  }
}
