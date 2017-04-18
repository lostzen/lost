package com.example.lost;

import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LocationServices;
import com.mapzen.android.lost.api.LostApiClient;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.widget.BaseAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Demonstrates one {@link LostApiClient}s receiving location updates at difference intervals
 */
public class MultipleLocationListenerSingleClientActivity extends ListActivity {

  private static final int LOCATION_PERMISSION_REQUEST = 1;

  LostApiClient lostApiClient;
  List<Item> items = new ArrayList<>();

  LocationListener listener = new LocationListener() {
    @Override public void onLocationChanged(Location location) {
      addItem("Listener");
    }
  };

  LocationListener otherListener = new LocationListener() {
    @Override public void onLocationChanged(Location location) {
      addItem("Other Listener");
    }
  };

  @Override int numOfItems() {
    return items.size();
  }

  @Override List<Item> getItems() {
    return items;
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    lostApiClient = new LostApiClient.Builder(this).addConnectionCallbacks(
        new LostApiClient.ConnectionCallbacks() {
          @Override
          public void onConnected() {
            initLocationTracking();
          }

          @Override
          public void onConnectionSuspended() {

          }
        }).build();
  }

  @Override public void onStart() {
    super.onStart();
    lostApiClient.connect();
  }

  @Override public void onStop() {
    super.onStop();
    LocationServices.FusedLocationApi.removeLocationUpdates(lostApiClient, listener);
    LocationServices.FusedLocationApi.removeLocationUpdates(lostApiClient, otherListener);
    lostApiClient.disconnect();
  }

  private void initLocationTracking() {
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(
          this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION },
          LOCATION_PERMISSION_REQUEST);
      return;
    }

    long interval = 30 * 1000; // 30 seconds
    LocationRequest request = LocationRequest.create()
        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        .setFastestInterval(interval)
        .setInterval(interval);

    LocationServices.FusedLocationApi.requestLocationUpdates(lostApiClient, request, listener);

    interval = 15 * 1000; // 15 seconds
    request = LocationRequest.create()
        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        .setFastestInterval(interval)
        .setInterval(interval);

    LocationServices.FusedLocationApi.requestLocationUpdates(lostApiClient, request, otherListener);
  }

  public void addItem(String title) {
    Date date = new Date(System.currentTimeMillis());
    SimpleDateFormat dateformat = new SimpleDateFormat("HH:mm:ss");
    StringBuilder dateString = new StringBuilder(dateformat.format(date));
    String description = dateString.toString();
    Item item = new Item(title, description);
    items.add(item);
    BaseAdapter adapter = (BaseAdapter) listView.getAdapter();
    adapter.notifyDataSetChanged();
  }
}
