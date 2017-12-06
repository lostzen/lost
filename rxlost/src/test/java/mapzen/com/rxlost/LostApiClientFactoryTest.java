package mapzen.com.rxlost;

import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.android.lost.internal.LostApiClientImpl;

import org.junit.Test;

import android.content.Context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class LostApiClientFactoryTest {

  LostApiClientFactory factory = new LostApiClientFactory();

  @Test public void getClient_shouldReturnLostApiClient() throws Exception {
    LostApiClient client = factory.getClient(mock(Context.class));
    assertThat(client.getClass()).isEqualTo(LostApiClientImpl.class);
  }
}
