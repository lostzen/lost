package mapzen.com.rxlost;

import com.mapzen.android.lost.api.LostApiClient;

import io.reactivex.ObservableOnSubscribe;

/**
 * Interface for creating {@link io.reactivex.Observable}s used by {@link RxLostApiClient}.
 */
interface ObservableFactory {
  ObservableOnSubscribe<ConnectionStatus> getObservable(LostApiClient client);
}
