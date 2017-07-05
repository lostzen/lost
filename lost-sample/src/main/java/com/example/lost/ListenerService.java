package com.example.lost;

import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LocationServices;
import com.mapzen.android.lost.api.LostApiClient;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Service running in separate process to request location updates.
 */
public class ListenerService extends Service {

  private static final String TAG = ListenerService.class.getSimpleName();

  LostApiClient client;
  LocationListener listener = new LocationListener() {
    @Override public void onLocationChanged(Location location) {
      Log.d(TAG, "Location Changed");
    }
  };

  @Nullable @Override public IBinder onBind(Intent intent) {
    return null;
  }

  @Override public void onCreate() {
    super.onCreate();
    client = new LostApiClient.Builder(this).addConnectionCallbacks(
        new LostApiClient.ConnectionCallbacks() {
      @Override public void onConnected() {
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(100);

        LocationServices.FusedLocationApi.requestLocationUpdates(client, request, listener);
      }

      @Override public void onConnectionSuspended() {

      }
    }).build();
    client.connect();
  }

  @Override public void onDestroy() {
    super.onDestroy();
    LocationServices.FusedLocationApi.removeLocationUpdates(client, listener);
    client.disconnect();
  }
}
