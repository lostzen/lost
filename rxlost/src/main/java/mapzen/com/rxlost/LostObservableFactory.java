package mapzen.com.rxlost;

import com.mapzen.android.lost.api.LostApiClient;

import io.reactivex.ObservableOnSubscribe;

/**
 * Implementation of {@link ObservableFactory} used by {@link RxLostApiClient}.
 */
public class LostObservableFactory implements ObservableFactory {

  LostObservableFactory() {
  }

  @Override public ObservableOnSubscribe<ConnectionStatus> getObservable(LostApiClient client) {
    return new DisposableConnectionStatusEmitter(client);
  }
}
