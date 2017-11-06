package mapzen.com.rxlost;

import com.mapzen.android.lost.api.LostApiClient;

import io.reactivex.ObservableOnSubscribe;
import static org.mockito.Mockito.mock;

public class TestObservableFactory implements ObservableFactory {

  @Override public ObservableOnSubscribe<ConnectionStatus> getObservable(LostApiClient client) {
    return mock(DisposableConnectionStatusEmitter.class);
  }
}
