package com.example.lost;

import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LocationServices;
import com.mapzen.android.lost.api.LostApiClient;

import android.Manifest;
import android.app.FragmentManager;
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
 * Demonstrates two {@link LostApiClient}s receiving location updates at difference intervals
 */
public class MultipleLocationListenerMultipleClientsActivity extends ListActivity
    implements LocationListener {

  private static final int LOCATION_PERMISSION_REQUEST = 1;

  LostApiClient lostApiClient;
  LostClientFragment fragment;
  List<Item> items = new ArrayList<>();

  @Override int numOfItems() {
    return items.size();
  }

  @Override List<Item> getItems() {
    return items;
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    final FragmentManager fragmentManager = getFragmentManager();
    final String fragTag = "Frag";
    fragment = (LostClientFragment) fragmentManager.findFragmentByTag(fragTag);
    if (fragment == null) {
      fragment = new LostClientFragment();
      fragmentManager.beginTransaction()
          .add(android.R.id.content, fragment, fragTag)
          .commit();
    }

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
    LocationServices.FusedLocationApi.removeLocationUpdates(lostApiClient,
        MultipleLocationListenerMultipleClientsActivity.this);
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

    LocationServices.FusedLocationApi.requestLocationUpdates(lostApiClient, request, this);
  }

  @Override public void onLocationChanged(Location location) {
    addItem("Activity");
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

  public static class LostClientFragment extends android.app.Fragment implements LocationListener {

    private static final int LOCATION_PERMISSION_REQUEST = 2;

    LostApiClient fragmentClient;

    @Override public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      fragmentClient = new LostApiClient.Builder(this.getActivity()).addConnectionCallbacks(
          new LostApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected() {
              fragmentInitLocationTracking();
            }

            @Override
            public void onConnectionSuspended() {

            }
          }).build();
    }

    @Override public void onStart() {
      super.onStart();
      fragmentClient.connect();
    }

    @Override public void onStop() {
      super.onStop();
      LocationServices.FusedLocationApi.removeLocationUpdates(fragmentClient, this);
      fragmentClient.disconnect();
    }

    private void fragmentInitLocationTracking() {
      if (ActivityCompat.checkSelfPermission(this.getActivity(),
          Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(this.getActivity(), new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION }, LOCATION_PERMISSION_REQUEST);
        return;
      }

      long interval = 15 * 1000; // 15 seconds
      LocationRequest request = LocationRequest.create()
          .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
          .setFastestInterval(interval)
          .setInterval(interval);

      LocationServices.FusedLocationApi.requestLocationUpdates(fragmentClient, request, this);
    }

    @Override public void onLocationChanged(Location location) {
      MultipleLocationListenerMultipleClientsActivity a =
          (MultipleLocationListenerMultipleClientsActivity) getActivity();
      a.addItem("Fragment");
    }
  }
}
