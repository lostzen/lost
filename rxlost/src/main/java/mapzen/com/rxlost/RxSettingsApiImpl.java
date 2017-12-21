package mapzen.com.rxlost;

import com.mapzen.android.lost.api.LocationSettingsRequest;
import com.mapzen.android.lost.api.LocationSettingsResult;
import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.android.lost.api.SettingsApi;

import io.reactivex.Single;

/**
 * RxJava implementation of {@link SettingsApi}.
 */
public class RxSettingsApiImpl implements RxSettingsApi {

  @Override public Single<LocationSettingsResult> checkLocationSettings(RxLostApiClient apiClient,
      LocationSettingsRequest request) {
    throw new RuntimeException("Sorry, not yet implemented");
  }
}
