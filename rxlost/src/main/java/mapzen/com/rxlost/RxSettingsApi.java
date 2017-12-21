package mapzen.com.rxlost;

import com.mapzen.android.lost.api.LocationSettingsRequest;
import com.mapzen.android.lost.api.LocationSettingsResult;
import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.android.lost.api.SettingsApi;

import io.reactivex.Single;

/**
 * RxJava interface for {@link SettingsApi}.
 */
public interface RxSettingsApi {

  Single<LocationSettingsResult> checkLocationSettings(RxLostApiClient apiClient,
      LocationSettingsRequest request);
}
