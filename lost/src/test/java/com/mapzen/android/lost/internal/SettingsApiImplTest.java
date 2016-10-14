package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationSettingsRequest;
import com.mapzen.android.lost.api.LocationSettingsResult;
import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.android.lost.api.PendingResult;

import org.junit.Before;
import org.junit.Test;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SettingsApiImplTest {

  Context context;
  SettingsApiImpl settingsApi;

  @Before public void setup() {
    context = mock(Context.class);
    BluetoothAdapter adapter = mock(BluetoothAdapter.class);
    settingsApi = new SettingsApiImpl(context, adapter);
  }

  @Test public void checkLocationSettings_shouldCreateLocationSettingsResultRequest() {
    LocationSettingsRequest request = new LocationSettingsRequest.Builder().build();
    LostApiClient apiClient = new LostApiClient.Builder(context).build();

    PendingResult<LocationSettingsResult> result =
        settingsApi.checkLocationSettings(apiClient, request);
    assertThat(result).isInstanceOf(LocationSettingsResultRequest.class);
  }

  @Test public void checkLocationSettings_shouldNotRequireBluetoothPermission() {
    PackageManager packageManager = mock(PackageManager.class);
    String pkgName = "com.test.pkg";
    LocationManager locationManager = mock(LocationManager.class);
    when(context.getPackageName()).thenReturn(pkgName);
    when(context.getPackageManager()).thenReturn(packageManager);
    when(context.getSystemService(Context.LOCATION_SERVICE)).thenReturn(locationManager);

    LocationSettingsRequest request = new LocationSettingsRequest.Builder().build();
    LostApiClient apiClient = new LostApiClient.Builder(context).build();
    PendingResult<LocationSettingsResult> result =
        settingsApi.checkLocationSettings(apiClient, request);
    result.await();
    verify(packageManager).checkPermission(Manifest.permission.BLUETOOTH, pkgName);
  }
}
