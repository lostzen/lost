package com.example.lost;

import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LocationServices;

import android.content.Intent;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

/**
 * Demonstrates two different processes requesting location updates.
 */
public class MultipleProcessesActivity extends PendingIntentActivity {

  private static final String TAG = MultipleProcessesActivity.class.getSimpleName();

  LocationListener listener = new LocationListener() {
    @Override public void onLocationChanged(Location location) {
      Log.d(TAG, "Location Changed");
    }
  };

  @Override protected void onResume() {
    super.onResume();
    Intent intent = new Intent(this, ListenerService.class);
    startService(intent);
  }

  @Override protected void onPause() {
    super.onPause();
    Intent intent = new Intent(this, ListenerService.class);
    stopService(intent);
  }

  void requestLocationUpdates() {
    LocationRequest request = LocationRequest.create();
    request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    request.setInterval(100);

    LocationServices.FusedLocationApi.requestLocationUpdates(client, request, listener);

    Toast.makeText(this, R.string.requested, Toast.LENGTH_SHORT).show();
  }

  void unregisterAndDisconnectClient() {
    LocationServices.FusedLocationApi.removeLocationUpdates(client, listener);
    disconnect();
  }
}
