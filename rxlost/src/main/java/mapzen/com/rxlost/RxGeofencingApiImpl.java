package mapzen.com.rxlost;

import com.mapzen.android.lost.api.Geofence;
import com.mapzen.android.lost.api.GeofencingApi;
import com.mapzen.android.lost.api.GeofencingRequest;
import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.android.lost.api.Status;

import android.app.PendingIntent;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * RxJava implementation of {@link GeofencingApi}.
 */
public class RxGeofencingApiImpl implements RxGeofencingApi {
  @Override
  public Single<Status> addGeofences(LostApiClient client, GeofencingRequest geofencingRequest,
      PendingIntent pendingIntent) {
    throw new RuntimeException("Sorry, not yet implemented");
  }

  @Override public Single<Status> addGeofences(LostApiClient client, List<Geofence> geofences,
      PendingIntent pendingIntent) {
    throw new RuntimeException("Sorry, not yet implemented");
  }

  @Override
  public Single<Status> removeGeofences(LostApiClient client, List<String> geofenceRequestIds) {
    throw new RuntimeException("Sorry, not yet implemented");
  }

  @Override
  public Single<Status> removeGeofences(LostApiClient client, PendingIntent pendingIntent) {
    throw new RuntimeException("Sorry, not yet implemented");
  }

  @Override public Observable<Geofence> requestGeofences(LostApiClient client,
      GeofencingRequest geofencingRequest) {
    throw new RuntimeException("Sorry, not yet implemented");
  }

  @Override
  public Observable<Geofence> requestGeofences(LostApiClient client, List<Geofence> geofences) {
    throw new RuntimeException("Sorry, not yet implemented");
  }
}
