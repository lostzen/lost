package mapzen.com.rxlost;

import com.mapzen.android.lost.api.LostApiClient;

import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;
import static mapzen.com.rxlost.ConnectionStatus.CONNECTED;
import static mapzen.com.rxlost.ConnectionStatus.SUSPENDED;

/**
 * Observable class returned by {@link RxLostApiClient#getConnectionStatus()}.
 */
class DisposableConnectionStatusEmitter implements ObservableOnSubscribe<ConnectionStatus>,
    Disposable, LostApiClient.ConnectionCallbacks {

  private LostApiClient client;
  private ObservableEmitter<ConnectionStatus> emitter;
  private boolean disposed = false;

  DisposableConnectionStatusEmitter(LostApiClient client) {
    this.client = client;
  }

  @Override public void subscribe(final ObservableEmitter<ConnectionStatus> emitter) throws Exception {
    this.emitter = emitter;
    emitter.setDisposable(this);
    client.registerConnectionCallbacks(this);
    client.connect();
  }

  @Override public void dispose() {
    disposed = true;
    client.unregisterConnectionCallbacks(this);
    client.disconnect();
  }

  @Override public boolean isDisposed() {
    return disposed;
  }

  @Override public void onConnected() {
    if (!isDisposed()) {
      emitter.onNext(CONNECTED);
    }
  }

  @Override public void onConnectionSuspended() {
    if (!isDisposed()) {
      emitter.onNext(SUSPENDED);
    }
  }
}
