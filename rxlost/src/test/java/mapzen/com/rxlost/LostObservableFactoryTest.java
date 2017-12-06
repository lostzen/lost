package mapzen.com.rxlost;

import com.mapzen.android.lost.api.LostApiClient;

import org.junit.Test;

import io.reactivex.ObservableOnSubscribe;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class LostObservableFactoryTest {

  LostObservableFactory factory = new LostObservableFactory();

  @Test public void getObservable_shouldReturnCorrectClass() throws Exception {
    ObservableOnSubscribe<ConnectionStatus> observable = factory.getObservable(mock(
        LostApiClient.class));
    assertThat(observable.getClass()).isEqualTo(DisposableConnectionStatusEmitter.class);
  }
}
