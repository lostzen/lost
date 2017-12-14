package mapzen.com.rxlost;

import com.mapzen.android.lost.api.Geofence;
import com.mapzen.android.lost.api.GeofencingApi;
import com.mapzen.android.lost.api.GeofencingRequest;
import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.android.lost.api.Status;

import android.app.PendingIntent;
import android.support.annotation.RequiresPermission;

import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * RxJava interface for {@link GeofencingApi}.
 */
public interface RxGeofencingApi {
  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  Single<Status> addGeofences(LostApiClient client, GeofencingRequest geofencingRequest,
      PendingIntent pendingIntent);

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  Single<Status> addGeofences(LostApiClient client, List<Geofence> geofences,
      PendingIntent pendingIntent);

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  Single<Status> removeGeofences(LostApiClient client, List<String> geofenceRequestIds);

  @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
  Single<Status> removeGeofences(LostApiClient client, PendingIntent pendingIntent);

  Observable<Geofence> requestGeofences(LostApiClient client, GeofencingRequest geofencingRequest);

  Observable<Geofence> requestGeofences(LostApiClient client, List<Geofence> geofences);
}
