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
import android.support.annotation.RequiresPermission;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * RxJava interface for {@link FusedLocationProviderApi}.
 */
public interface RxFusedLocationProviderApi {

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  Maybe<Location> getLastLocation(RxLostApiClient client);

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  Maybe<LocationAvailability> getLocationAvailability(RxLostApiClient client);

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  Single<Status> requestLocationUpdates(RxLostApiClient client, LocationRequest request,
      LocationListener listener);

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  Single<Status> requestLocationUpdates(RxLostApiClient client, LocationRequest request,
      LocationListener listener, Looper looper);

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  Single<Status> requestLocationUpdates(RxLostApiClient client, LocationRequest request,
      LocationCallback callback, Looper looper);

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  Single<Status> requestLocationUpdates(RxLostApiClient client, LocationRequest request,
      PendingIntent callbackIntent);

  Single<Status> removeLocationUpdates(RxLostApiClient client, LocationListener listener);

  Single<Status> removeLocationUpdates(RxLostApiClient client, PendingIntent callbackIntent);

  Single<Status> removeLocationUpdates(RxLostApiClient client, LocationCallback callback);

  Single<Status> setMockMode(RxLostApiClient client, boolean isMockMode);

  Single<Status> setMockLocation(RxLostApiClient client, Location mockLocation);

  Single<Status> setMockTrace(RxLostApiClient client, final String path,
      final String filename);

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  Observable<Location> requestLocationUpdates(RxLostApiClient client, LocationRequest request);
}
