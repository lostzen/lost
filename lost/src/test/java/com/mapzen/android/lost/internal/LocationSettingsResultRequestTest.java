package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LocationSettingsRequest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import android.content.Context;

import java.util.ArrayList;

public class LocationSettingsResultRequestTest {

  Context context;
  LocationSettingsRequest request;
  LocationSettingsResultRequest resultRequest;

  @Before
  public void setup() {
    context = Mockito.mock(Context.class);

    ArrayList<LocationRequest> requests = new ArrayList<>();
    LocationRequest highAccuracy = LocationRequest.create().setPriority(
        LocationRequest.PRIORITY_HIGH_ACCURACY); //gps + wifi
    requests.add(highAccuracy);
    request = new LocationSettingsRequest.Builder()
        .addAllLocationRequests(requests)
        .setNeedBle(true)
        .build();

    resultRequest = new LocationSettingsResultRequest(context, request);
  }

  @Test
  public void await_shouldReturnSuccessfullResult() {

    //PackageManager pm = context.getPackageManager();
    ////BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    //LocationManager locationManager = (LocationManager) context.getSystemService(
    // Context.LOCATION_SERVICE);
    //
    //when(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)).thenReturn(true);
    //when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)).thenReturn(true);
    //when(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)).thenReturn(true);
    //when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK)).thenReturn(true);
    ////when(bluetoothAdapter.isEnabled()).thenReturn(true);
    //when(pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)).thenReturn(true);
    //
    //LocationSettingsResult result = resultRequest.await();
    //assertThat(result.getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
    //assertThat(result.getLocationSettingsStates().isGpsPresent()).isTrue();
    //assertThat(result.getLocationSettingsStates().isGpsUsable()).isTrue();
    //assertThat(result.getLocationSettingsStates().isNetworkLocationPresent()).isTrue();
    //assertThat(result.getLocationSettingsStates().isNetworkLocationUsable()).isTrue();
    //assertThat(result.getLocationSettingsStates().isLocationPresent()).isTrue();
    //assertThat(result.getLocationSettingsStates().isLocationUsable()).isTrue();
    //assertThat(result.getLocationSettingsStates().isBlePresent()).isTrue();
    //assertThat(result.getLocationSettingsStates().isBleUsable()).isTrue();
  }

  @Test
  public void await_shouldReturnGpsUsableIfGpsEnabled() {

  }

  @Test
  public void await_shouldReturnNetworkUsableIfNetworkEnabled() {

  }

  @Test
  public void await_shouldReturnLocationUsableIfNetworkOrGpsEnabled() {

  }

  @Test
  public void await_shouldReturnBleUsableIfBleEnabled() {

  }

  @Test
  public void awaitTimeout_shouldTimeoutAfterInterval() {

  }

  @Test
  public void cancel_shouldReturnCancelResult() {

  }

  @Test
  public void cancel_shouldSetIsCanceled() {

  }

  @Test
  public void setResultCallback_shouldReturnResultOnCallback() {

  }

  @Test
  public void setResultCallbackTime_shouldReturnResultOnCallback() {

  }
}
