package mapzen.com.rxlost;

import com.mapzen.android.lost.api.LostApiClient;

import org.junit.Test;

import io.reactivex.ObservableEmitter;
import static mapzen.com.rxlost.ConnectionStatus.CONNECTED;
import static mapzen.com.rxlost.ConnectionStatus.SUSPENDED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class DisposableConnectionStatusEmitterTest {

  LostApiClient client = mock(LostApiClient.class);
  DisposableConnectionStatusEmitter emitter = new DisposableConnectionStatusEmitter(client);

  @Test public void subscribe_shouldSetDisposable() throws Exception {
    ObservableEmitter<ConnectionStatus> e = mock(ObservableEmitter.class);
    emitter.subscribe(e);
    verify(e).setDisposable(emitter);
  }

  @Test public void subscribe_shouldRegisterCallbacks() throws Exception {
    ObservableEmitter<ConnectionStatus> e = mock(ObservableEmitter.class);
    emitter.subscribe(e);
    verify(client).registerConnectionCallbacks(emitter);
  }

  @Test public void subscribe_shouldConnectClient() throws Exception {
    ObservableEmitter<ConnectionStatus> e = mock(ObservableEmitter.class);
    emitter.subscribe(e);
    verify(client).connect();
  }

  @Test public void dispose_shouldUnregisterCallbacks() throws Exception {
    emitter.dispose();
    verify(client).unregisterConnectionCallbacks(emitter);
  }

  @Test public void dispose_disconnect() throws Exception {
    emitter.dispose();
    verify(client).disconnect();
  }

  @Test public void dispose_shouldUpdateDisposed() throws Exception {
    assertThat(emitter.isDisposed()).isFalse();
    emitter.dispose();
    assertThat(emitter.isDisposed()).isTrue();
  }

  @Test public void isDisposed_shouldReturnFalseByDefault() throws Exception {
    assertThat(emitter.isDisposed()).isFalse();
  }

  @Test public void onConnected_shouldEmitConnectedStatus() throws Exception {
    ObservableEmitter<ConnectionStatus> e = mock(ObservableEmitter.class);
    emitter.subscribe(e);
    emitter.onConnected();
    verify(e).onNext(CONNECTED);
  }

  @Test public void onConnected_disposed_shouldDoNothing() throws Exception {
    ObservableEmitter<ConnectionStatus> e = mock(ObservableEmitter.class);
    emitter.subscribe(e);
    emitter.dispose();
    emitter.onConnected();
    verify(e, never()).onNext(CONNECTED);
  }

  @Test public void onConnectionSuspended_shouldEmitConnectedStatus() throws Exception {
    ObservableEmitter<ConnectionStatus> e = mock(ObservableEmitter.class);
    emitter.subscribe(e);
    emitter.onConnectionSuspended();
    verify(e).onNext(SUSPENDED);
  }

  @Test public void onConnectionSuspended_disposed_shouldDoNothing() throws Exception {
    ObservableEmitter<ConnectionStatus> e = mock(ObservableEmitter.class);
    emitter.subscribe(e);
    emitter.dispose();
    emitter.onConnectionSuspended();
    verify(e, never()).onNext(SUSPENDED);
  }
}
