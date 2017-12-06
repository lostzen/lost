package mapzen.com.rxlost;

import com.mapzen.android.lost.api.LostApiClient;

import android.content.Context;

import io.reactivex.Observable;

public class RxLostApiClient {

  private LostApiClient client;
  private ObservableFactory observableFactory;

  public RxLostApiClient(Context context) {
    this(context, new LostApiClientFactory(), new LostObservableFactory());
  }

  RxLostApiClient(Context context, ApiClientFactory clientFactory,
      ObservableFactory observableFactory) {
    client = clientFactory.getClient(context);
    this.observableFactory = observableFactory;
  }

  public Observable<ConnectionStatus> getConnectionStatus() {
    return Observable.create(observableFactory.getObservable(client));
  }
}
