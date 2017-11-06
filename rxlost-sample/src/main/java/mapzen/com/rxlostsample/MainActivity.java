package mapzen.com.rxlostsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import mapzen.com.rxlost.ConnectionStatus;
import mapzen.com.rxlost.RxLostApiClient;

public class MainActivity extends AppCompatActivity {

  CompositeDisposable compositeDisposable;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    compositeDisposable = new CompositeDisposable();

    final RxLostApiClient client = new RxLostApiClient(this);
    Disposable disposable = client.getConnectionStatus().subscribe(new Consumer<ConnectionStatus>() {
      @Override public void accept(ConnectionStatus connectionStatus) throws Exception {
        if (connectionStatus == ConnectionStatus.CONNECTED) {
          // TODO
        }
      }
    });
    compositeDisposable.add(disposable);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    compositeDisposable.dispose();
  }
}
