package com.example.lost;

import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LocationServices;
import com.mapzen.android.lost.api.LostApiClient;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

/**
 * Demonstrates two {@link LostApiClient}s receiving location updates with different request
 * priorities.
 */
public class MultiplePriorityMultipleClientsActivity extends LostApiClientActivity {

  private static final String TAG = MultiplePriorityMultipleClientsActivity.class.getSimpleName();

  LostApiClient otherClient;

  LocationListener noPowerListener = new LocationListener() {
    @Override public void onLocationChanged(Location location) {
      Log.d(TAG, "No Power Client Location:" + location.getLongitude() + " " +
          location.getLatitude());
    }
  };

  LocationListener highPriorityListener = new LocationListener() {
    @Override public void onLocationChanged(Location location) {
      Log.d(TAG, "High Priority Client Location:" + location.getLongitude() + " " +
          location.getLatitude());
    }
  };

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_multiple_clients_requests);
    createOtherClient();
    setupBtns();
  }

  @Override public void onConnected() {
    super.onConnected();
    requestNoPowerUpdates();
  }

  private void setupBtns() {
    findViewById(R.id.connect_no_power_btn).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        client.connect();
      }
    });
    findViewById(R.id.connect_high_priority_btn).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        otherClient.connect();
      }
    });
    findViewById(R.id.rm_no_power_btn).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        LocationServices.FusedLocationApi.removeLocationUpdates(client, noPowerListener);
      }
    });
    findViewById(R.id.rm_high_priority_btn).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        LocationServices.FusedLocationApi.removeLocationUpdates(otherClient, highPriorityListener);
      }
    });
  }

  private void requestNoPowerUpdates() {
    LocationRequest request = LocationRequest.create()
        .setPriority(LocationRequest.PRIORITY_NO_POWER)
        .setFastestInterval(0)
        .setSmallestDisplacement(0)
        .setInterval(1000); // 1 sec
    LocationServices.FusedLocationApi.requestLocationUpdates(client, request, noPowerListener);
  }

  private void createOtherClient() {
    otherClient = new LostApiClient.Builder(this)
        .addConnectionCallbacks(new LostApiClient.ConnectionCallbacks() {

          @Override public void onConnected() {
            Log.d("Test", "High priority onConnected");

            LocationRequest request = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setFastestInterval(0)
                .setSmallestDisplacement(0)
                .setInterval(1000); // 1 sec

            LocationServices.FusedLocationApi.requestLocationUpdates(otherClient, request,
                highPriorityListener);
          }

          @Override public void onConnectionSuspended() {

          }
        })
        .build();
  }
}
