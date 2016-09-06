package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationSettingsRequest;
import com.mapzen.android.lost.api.LocationSettingsResult;
import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.android.lost.api.PendingResult;
import com.mapzen.android.lost.api.SettingsApi;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;

public class SettingsApiImpl implements SettingsApi {

  private Context context;
  private BluetoothAdapter bluetoothAdapter;

  public SettingsApiImpl() {
    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
  }

  public void connect(Context context) {
    this.context = context;
  }

  public boolean isConnected() {
    return context != null;
  }

  public void disconnect() {
    context = null;
  }

  public SettingsApiImpl(Context context, BluetoothAdapter adapter) {
    this.context = context;
    bluetoothAdapter = adapter;
  }

  @Override
  public PendingResult<LocationSettingsResult> checkLocationSettings(LostApiClient client,
      LocationSettingsRequest request) {
    PackageManager pm = context.getPackageManager();
    LocationManager locationManager =
        (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    PendingIntentGenerator generator = new PendingIntentGenerator(context);
    return new LocationSettingsResultRequest(bluetoothAdapter, pm, locationManager, generator,
        request);
  }
}
