package mapzen.com.rxlost;

import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.android.lost.internal.LostApiClientImpl;

import org.junit.Before;
import org.junit.Test;

import android.content.Context;

import io.reactivex.Observable;

import io.reactivex.ObservableOnSubscribe;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RxLostApiClientTest {

  Context context = mock(Context.class);
  ApiClientFactory clientFactory = mock(ApiClientFactory.class);
  ObservableFactory observableFactory = mock(ObservableFactory.class);
  LostApiClient apiClient;
  RxLostApiClient client;

  @Before public void setup() throws Exception {
    apiClient = mock(LostApiClient.class);
    when(clientFactory.getClient(context)).thenReturn(apiClient);
    client = new RxLostApiClient(context, clientFactory, observableFactory);
  }

  @Test public void rxClientShouldCreateClient() throws Exception {
    verify(clientFactory).getClient(context);
  }

  @Test public void getConnectionStatus() throws Exception {
    when(observableFactory.getObservable(apiClient)).thenReturn(
        new DisposableConnectionStatusEmitter(apiClient));
    Observable observable = client.getConnectionStatus();
    assertThat(observable).isNotNull();
    verify(observableFactory).getObservable(clientFactory.getClient(context));
  }

}
