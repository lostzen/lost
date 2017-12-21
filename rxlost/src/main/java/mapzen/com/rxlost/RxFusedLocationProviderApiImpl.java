package mapzen.com.rxlost;

import com.mapzen.android.lost.api.FusedLocationProviderApi;
import com.mapzen.android.lost.api.LocationAvailability;
import com.mapzen.android.lost.api.LocationCallback;
import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.Status;

import android.app.PendingIntent;
import android.location.Location;
import android.os.Looper;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * RxJava interface for {@link FusedLocationProviderApi}.
 */
class RxFusedLocationProviderApiImpl implements RxFusedLocationProviderApi {

  @Override public Maybe<Location> getLastLocation(RxLostApiClient client) {
    throw new RuntimeException("Sorry, not yet implemented");
  }

  @Override public Maybe<LocationAvailability> getLocationAvailability(RxLostApiClient client) {
    throw new RuntimeException("Sorry, not yet implemented");
  }

  @Override
  public Single<Status> requestLocationUpdates(RxLostApiClient client, LocationRequest request,
      LocationListener listener) {
    throw new RuntimeException("Sorry, not yet implemented");
  }

  @Override
  public Single<Status> requestLocationUpdates(RxLostApiClient client, LocationRequest request,
      LocationListener listener, Looper looper) {
    throw new RuntimeException("Sorry, not yet implemented");
  }

  @Override
  public Single<Status> requestLocationUpdates(RxLostApiClient client, LocationRequest request,
      LocationCallback callback, Looper looper) {
    throw new RuntimeException("Sorry, not yet implemented");
  }

  @Override
  public Single<Status> requestLocationUpdates(RxLostApiClient client, LocationRequest request,
      PendingIntent callbackIntent) {
    throw new RuntimeException("Sorry, not yet implemented");
  }

  @Override
  public Single<Status> removeLocationUpdates(RxLostApiClient client, LocationListener listener) {
    throw new RuntimeException("Sorry, not yet implemented");
  }

  @Override public Single<Status> removeLocationUpdates(RxLostApiClient client,
      PendingIntent callbackIntent) {
    throw new RuntimeException("Sorry, not yet implemented");
  }

  @Override
  public Single<Status> removeLocationUpdates(RxLostApiClient client, LocationCallback callback) {
    throw new RuntimeException("Sorry, not yet implemented");
  }

  @Override public Single<Status> setMockMode(RxLostApiClient client, boolean isMockMode) {
    throw new RuntimeException("Sorry, not yet implemented");
  }

  @Override public Single<Status> setMockLocation(RxLostApiClient client, Location mockLocation) {
    throw new RuntimeException("Sorry, not yet implemented");
  }

  @Override
  public Single<Status> setMockTrace(RxLostApiClient client, String path, String filename) {
    throw new RuntimeException("Sorry, not yet implemented");
  }

  @Override public Observable<Location> requestLocationUpdates(RxLostApiClient client,
      LocationRequest request) {
    throw new RuntimeException("Sorry, not yet implemented");
  }
}
