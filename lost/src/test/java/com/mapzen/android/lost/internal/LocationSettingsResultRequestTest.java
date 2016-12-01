package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LocationSettingsRequest;
import com.mapzen.android.lost.api.LocationSettingsResult;
import com.mapzen.android.lost.api.ResultCallback;
import com.mapzen.android.lost.api.Status;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SuppressWarnings("MissingPermission") public class LocationSettingsResultRequestTest {

  Context context;
  PackageManager pm;
  LocationManager locationManager;
  PendingIntentGenerator generator;
  LocationSettingsRequest request;
  LocationSettingsResultRequest resultRequest;

  @Before public void setup() {
    context = Mockito.mock(Context.class);
    pm = Mockito.mock(PackageManager.class);
    locationManager = Mockito.mock(LocationManager.class);
    generator = new TestPendingIntentGenerator(context);

    when(context.getPackageManager()).thenReturn(pm);
    when(context.getSystemService(Context.LOCATION_SERVICE)).thenReturn(locationManager);

    ArrayList<LocationRequest> requests = new ArrayList<>();
    LocationRequest highAccuracy =
        LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); //gps + wifi
    requests.add(highAccuracy);
    request = new LocationSettingsRequest.Builder().addAllLocationRequests(requests)
        .setNeedBle(true)
        .build();

    resultRequest = new LocationSettingsResultRequest(context, generator, request);
  }

  @Test public void await_shouldReturnSuccessfulResult() {
    when(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)).thenReturn(true);
    when(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)).thenReturn(true);

    LocationSettingsResult result = resultRequest.await();
    assertThat(result.getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
    assertThat(result.getLocationSettingsStates().isGpsPresent()).isTrue();
    assertThat(result.getLocationSettingsStates().isGpsUsable()).isTrue();
    assertThat(result.getLocationSettingsStates().isNetworkLocationPresent()).isTrue();
    assertThat(result.getLocationSettingsStates().isNetworkLocationUsable()).isTrue();
    assertThat(result.getLocationSettingsStates().isLocationPresent()).isTrue();
    assertThat(result.getLocationSettingsStates().isLocationUsable()).isTrue();
    assertThat(result.getLocationSettingsStates().isBlePresent()).isTrue();
    assertThat(result.getLocationSettingsStates().isBleUsable()).isTrue();
  }

  @Test public void await_shouldReturnGpsUsableIfGpsEnabled() {
    when(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)).thenReturn(true);

    LocationSettingsResult result = resultRequest.await();
    assertThat(result.getStatus().getStatusCode()).isEqualTo(Status.RESOLUTION_REQUIRED);
    assertThat(result.getLocationSettingsStates().isGpsPresent()).isTrue();
    assertThat(result.getLocationSettingsStates().isGpsUsable()).isTrue();
    assertThat(result.getLocationSettingsStates().isNetworkLocationPresent()).isTrue();
    assertThat(result.getLocationSettingsStates().isNetworkLocationUsable()).isFalse();
    assertThat(result.getLocationSettingsStates().isLocationPresent()).isTrue();
    assertThat(result.getLocationSettingsStates().isLocationUsable()).isTrue();
    assertThat(result.getLocationSettingsStates().isBlePresent()).isTrue();
    assertThat(result.getLocationSettingsStates().isBleUsable()).isFalse();
  }

  @Test public void await_shouldReturnNetworkUsableIfNetworkEnabled() {
    when(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)).thenReturn(true);

    LocationSettingsResult result = resultRequest.await();
    assertThat(result.getStatus().getStatusCode()).isEqualTo(Status.RESOLUTION_REQUIRED);
    assertThat(result.getLocationSettingsStates().isGpsPresent()).isTrue();
    assertThat(result.getLocationSettingsStates().isGpsUsable()).isFalse();
    assertThat(result.getLocationSettingsStates().isNetworkLocationPresent()).isTrue();
    assertThat(result.getLocationSettingsStates().isNetworkLocationUsable()).isTrue();
    assertThat(result.getLocationSettingsStates().isLocationPresent()).isTrue();
    assertThat(result.getLocationSettingsStates().isLocationUsable()).isTrue();
    assertThat(result.getLocationSettingsStates().isBlePresent()).isTrue();
    assertThat(result.getLocationSettingsStates().isBleUsable()).isTrue();
  }

  @Test public void await_shouldReturnLocationUsableIfNetworkOrGpsEnabled() {
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)).thenReturn(true);
    when(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)).thenReturn(true);
    when(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)).thenReturn(false);

    LocationSettingsResult result = resultRequest.await();
    assertThat(result.getStatus().getStatusCode()).isEqualTo(Status.RESOLUTION_REQUIRED);
    assertThat(result.getLocationSettingsStates().isGpsPresent()).isTrue();
    assertThat(result.getLocationSettingsStates().isGpsUsable()).isTrue();
    assertThat(result.getLocationSettingsStates().isNetworkLocationPresent()).isTrue();
    assertThat(result.getLocationSettingsStates().isNetworkLocationUsable()).isFalse();
    assertThat(result.getLocationSettingsStates().isLocationPresent()).isTrue();
    assertThat(result.getLocationSettingsStates().isLocationUsable()).isTrue();
    assertThat(result.getLocationSettingsStates().isBlePresent()).isTrue();
    assertThat(result.getLocationSettingsStates().isBleUsable()).isFalse();

    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)).thenReturn(true);
    when(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)).thenReturn(false);
    when(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)).thenReturn(true);

    result = resultRequest.await();
    assertThat(result.getStatus().getStatusCode()).isEqualTo(Status.RESOLUTION_REQUIRED);
    assertThat(result.getLocationSettingsStates().isGpsPresent()).isTrue();
    assertThat(result.getLocationSettingsStates().isGpsUsable()).isFalse();
    assertThat(result.getLocationSettingsStates().isNetworkLocationPresent()).isTrue();
    assertThat(result.getLocationSettingsStates().isNetworkLocationUsable()).isTrue();
    assertThat(result.getLocationSettingsStates().isLocationPresent()).isTrue();
    assertThat(result.getLocationSettingsStates().isLocationUsable()).isTrue();
    assertThat(result.getLocationSettingsStates().isBlePresent()).isTrue();
    assertThat(result.getLocationSettingsStates().isBleUsable()).isTrue();
  }

  @Test public void await_shouldReturnBleUsableIfNetworkIsEnabled() {
    when(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)).thenReturn(true);

    LocationSettingsResult result = resultRequest.await();
    assertThat(result.getStatus().getStatusCode()).isEqualTo(Status.RESOLUTION_REQUIRED);
    assertThat(result.getLocationSettingsStates().isGpsPresent()).isTrue();
    assertThat(result.getLocationSettingsStates().isGpsUsable()).isFalse();
    assertThat(result.getLocationSettingsStates().isNetworkLocationPresent()).isTrue();
    assertThat(result.getLocationSettingsStates().isNetworkLocationUsable()).isTrue();
    assertThat(result.getLocationSettingsStates().isLocationPresent()).isTrue();
    assertThat(result.getLocationSettingsStates().isLocationUsable()).isTrue();
    assertThat(result.getLocationSettingsStates().isBlePresent()).isTrue();
    assertThat(result.getLocationSettingsStates().isBleUsable()).isTrue();
  }

  @Test public void await_balancedLowPower_noBle_shouldReturnStatusResolutionRequired() {
    when(pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)).thenReturn(true);

    ArrayList<LocationRequest> requests = new ArrayList<>();
    LocationRequest balanced =
        LocationRequest.create().setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    LocationRequest lowPower =
        LocationRequest.create().setPriority(LocationRequest.PRIORITY_LOW_POWER);
    requests.add(balanced);
    requests.add(lowPower);
    LocationSettingsRequest settingsRequest =
        new LocationSettingsRequest.Builder().addAllLocationRequests(requests)
            .setNeedBle(false)
            .build();
    LocationSettingsResultRequest settingsResultRequest =
        new LocationSettingsResultRequest(context, generator, settingsRequest);

    LocationSettingsResult result = settingsResultRequest.await();
    assertThat(result.getStatus().getStatusCode()).isEqualTo(Status.RESOLUTION_REQUIRED);
  }

  @Test public void await_balancedLowPower_needBle_shouldReturnStatusResolutionRequired() {
    when(pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)).thenReturn(true);

    ArrayList<LocationRequest> requests = new ArrayList<>();
    LocationRequest balanced =
        LocationRequest.create().setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    LocationRequest lowPower =
        LocationRequest.create().setPriority(LocationRequest.PRIORITY_LOW_POWER);
    requests.add(balanced);
    requests.add(lowPower);
    LocationSettingsRequest settingsRequest =
        new LocationSettingsRequest.Builder().addAllLocationRequests(requests)
            .setNeedBle(true)
            .build();
    LocationSettingsResultRequest settingsResultRequest =
        new LocationSettingsResultRequest(context, generator, settingsRequest);

    LocationSettingsResult result = settingsResultRequest.await();
    assertThat(result.getStatus().getStatusCode()).isEqualTo(Status.RESOLUTION_REQUIRED);
  }

  @Test public void await_balancedLowPower_noBle_shouldReturnStatusSuccess() {
    when(pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)).thenReturn(true);
    when(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)).thenReturn(true);

    ArrayList<LocationRequest> requests = new ArrayList<>();
    LocationRequest balanced =
        LocationRequest.create().setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    LocationRequest lowPower =
        LocationRequest.create().setPriority(LocationRequest.PRIORITY_LOW_POWER);
    requests.add(balanced);
    requests.add(lowPower);
    LocationSettingsRequest settingsRequest =
        new LocationSettingsRequest.Builder().addAllLocationRequests(requests)
            .setNeedBle(false)
            .build();
    LocationSettingsResultRequest settingsResultRequest =
        new LocationSettingsResultRequest(context, generator, settingsRequest);

    LocationSettingsResult result = settingsResultRequest.await();
    assertThat(result.getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
  }

  @Test public void await_noPower_noBle_shouldReturnStatusSuccess() {
    when(pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)).thenReturn(true);

    ArrayList<LocationRequest> requests = new ArrayList<>();
    LocationRequest noPower =
        LocationRequest.create().setPriority(LocationRequest.PRIORITY_NO_POWER);
    requests.add(noPower);
    LocationSettingsRequest settingsRequest =
        new LocationSettingsRequest.Builder().addAllLocationRequests(requests)
            .setNeedBle(false)
            .build();
    LocationSettingsResultRequest settingsResultRequest =
        new LocationSettingsResultRequest(context, generator, settingsRequest);

    LocationSettingsResult result = settingsResultRequest.await();
    assertThat(result.getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
  }

  @Test public void await_noPower_needBle_shouldReturnStatusResolutionRequired() {
    when(pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)).thenReturn(true);

    ArrayList<LocationRequest> requests = new ArrayList<>();
    LocationRequest noPower =
        LocationRequest.create().setPriority(LocationRequest.PRIORITY_NO_POWER);
    requests.add(noPower);
    LocationSettingsRequest settingsRequest =
        new LocationSettingsRequest.Builder().addAllLocationRequests(requests)
            .setNeedBle(true)
            .build();
    LocationSettingsResultRequest settingsResultRequest =
        new LocationSettingsResultRequest(context, generator, settingsRequest);

    LocationSettingsResult result = settingsResultRequest.await();
    assertThat(result.getStatus().getStatusCode()).isEqualTo(Status.RESOLUTION_REQUIRED);
  }

  @Test public void awaitTimeout_shouldTimeoutAfterInterval() {
    when(pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)).thenReturn(true);

    DelayTestPendingIntentGenerator delayedGenerator = new DelayTestPendingIntentGenerator(context);
    LocationSettingsResultRequest settingsResultRequest =
        new LocationSettingsResultRequest(context, delayedGenerator, request);

    LocationSettingsResult resultRequest = settingsResultRequest.await(1000, TimeUnit.MILLISECONDS);
    assertThat(resultRequest.getStatus().getStatusCode()).isEqualTo(Status.TIMEOUT);
  }

  @Test public void setResultCallback_shouldReturnSuccessfulResult() {
    when(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)).thenReturn(true);
    when(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)).thenReturn(true);

    resultRequest.setResultCallback(new ResultCallback<LocationSettingsResult>() {
      @Override public void onResult(@NonNull LocationSettingsResult result) {
        assertThat(result.getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
        assertThat(result.getLocationSettingsStates().isGpsPresent()).isTrue();
        assertThat(result.getLocationSettingsStates().isGpsUsable()).isTrue();
        assertThat(result.getLocationSettingsStates().isNetworkLocationPresent()).isTrue();
        assertThat(result.getLocationSettingsStates().isNetworkLocationUsable()).isTrue();
        assertThat(result.getLocationSettingsStates().isLocationPresent()).isTrue();
        assertThat(result.getLocationSettingsStates().isLocationUsable()).isTrue();
        assertThat(result.getLocationSettingsStates().isBlePresent()).isTrue();
        assertThat(result.getLocationSettingsStates().isBleUsable()).isTrue();
      }
    });
  }

  @Test public void setResultCallback_shouldReturnGpsUsableIfGpsEnabled() {
    when(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)).thenReturn(true);

    resultRequest.setResultCallback(new ResultCallback<LocationSettingsResult>() {
      @Override public void onResult(@NonNull LocationSettingsResult result) {
        assertThat(result.getStatus().getStatusCode()).isEqualTo(Status.RESOLUTION_REQUIRED);
        assertThat(result.getLocationSettingsStates().isGpsPresent()).isTrue();
        assertThat(result.getLocationSettingsStates().isGpsUsable()).isTrue();
        assertThat(result.getLocationSettingsStates().isNetworkLocationPresent()).isTrue();
        assertThat(result.getLocationSettingsStates().isNetworkLocationUsable()).isFalse();
        assertThat(result.getLocationSettingsStates().isLocationPresent()).isTrue();
        assertThat(result.getLocationSettingsStates().isLocationUsable()).isTrue();
        assertThat(result.getLocationSettingsStates().isBlePresent()).isTrue();
        assertThat(result.getLocationSettingsStates().isBleUsable()).isFalse();
      }
    });
  }

  @Test public void setResultCallback_shouldReturnNetworkUsableIfNetworkEnabled() {
    when(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)).thenReturn(true);

    resultRequest.setResultCallback(new ResultCallback<LocationSettingsResult>() {
      @Override public void onResult(@NonNull LocationSettingsResult result) {
        assertThat(result.getStatus().getStatusCode()).isEqualTo(Status.RESOLUTION_REQUIRED);
        assertThat(result.getLocationSettingsStates().isGpsPresent()).isTrue();
        assertThat(result.getLocationSettingsStates().isGpsUsable()).isFalse();
        assertThat(result.getLocationSettingsStates().isNetworkLocationPresent()).isTrue();
        assertThat(result.getLocationSettingsStates().isNetworkLocationUsable()).isTrue();
        assertThat(result.getLocationSettingsStates().isLocationPresent()).isTrue();
        assertThat(result.getLocationSettingsStates().isLocationUsable()).isTrue();
        assertThat(result.getLocationSettingsStates().isBlePresent()).isTrue();
        assertThat(result.getLocationSettingsStates().isBleUsable()).isTrue();
      }
    });
  }

  @Test public void setResultCallback_shouldReturnLocationUsableIfNetworkOrGpsEnabled() {
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)).thenReturn(true);
    when(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)).thenReturn(true);
    when(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)).thenReturn(false);

    resultRequest.setResultCallback(new ResultCallback<LocationSettingsResult>() {
      @Override public void onResult(@NonNull LocationSettingsResult result) {
        assertThat(result.getStatus().getStatusCode()).isEqualTo(Status.RESOLUTION_REQUIRED);
        assertThat(result.getLocationSettingsStates().isGpsPresent()).isTrue();
        assertThat(result.getLocationSettingsStates().isGpsUsable()).isTrue();
        assertThat(result.getLocationSettingsStates().isNetworkLocationPresent()).isTrue();
        assertThat(result.getLocationSettingsStates().isNetworkLocationUsable()).isFalse();
        assertThat(result.getLocationSettingsStates().isLocationPresent()).isTrue();
        assertThat(result.getLocationSettingsStates().isLocationUsable()).isTrue();
        assertThat(result.getLocationSettingsStates().isBlePresent()).isTrue();
        assertThat(result.getLocationSettingsStates().isBleUsable()).isFalse();
      }
    });

    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)).thenReturn(true);
    when(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)).thenReturn(false);
    when(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)).thenReturn(true);

    resultRequest.setResultCallback(new ResultCallback<LocationSettingsResult>() {
      @Override public void onResult(@NonNull LocationSettingsResult result) {
        assertThat(result.getStatus().getStatusCode()).isEqualTo(Status.RESOLUTION_REQUIRED);
        assertThat(result.getLocationSettingsStates().isGpsPresent()).isTrue();
        assertThat(result.getLocationSettingsStates().isGpsUsable()).isFalse();
        assertThat(result.getLocationSettingsStates().isNetworkLocationPresent()).isTrue();
        assertThat(result.getLocationSettingsStates().isNetworkLocationUsable()).isTrue();
        assertThat(result.getLocationSettingsStates().isLocationPresent()).isTrue();
        assertThat(result.getLocationSettingsStates().isLocationUsable()).isTrue();
        assertThat(result.getLocationSettingsStates().isBlePresent()).isTrue();
        assertThat(result.getLocationSettingsStates().isBleUsable()).isTrue();
      }
    });
  }

  @Test public void setResultCallback_shouldReturnBleUsableIfNetworkEnabled() {
    when(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)).thenReturn(true);

    resultRequest.setResultCallback(new ResultCallback<LocationSettingsResult>() {
      @Override public void onResult(@NonNull LocationSettingsResult result) {
        assertThat(result.getStatus().getStatusCode()).isEqualTo(Status.RESOLUTION_REQUIRED);
        assertThat(result.getLocationSettingsStates().isGpsPresent()).isTrue();
        assertThat(result.getLocationSettingsStates().isGpsUsable()).isFalse();
        assertThat(result.getLocationSettingsStates().isNetworkLocationPresent()).isTrue();
        assertThat(result.getLocationSettingsStates().isNetworkLocationUsable()).isTrue();
        assertThat(result.getLocationSettingsStates().isLocationPresent()).isTrue();
        assertThat(result.getLocationSettingsStates().isLocationUsable()).isTrue();
        assertThat(result.getLocationSettingsStates().isBlePresent()).isTrue();
        assertThat(result.getLocationSettingsStates().isBleUsable()).isTrue();
      }
    });
  }

  @Test
  public void setResultCallback_balancedLowPower_noBle_shouldReturnStatusResolutionRequired() {
    when(pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)).thenReturn(true);

    ArrayList<LocationRequest> requests = new ArrayList<>();
    LocationRequest balanced =
        LocationRequest.create().setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    LocationRequest lowPower =
        LocationRequest.create().setPriority(LocationRequest.PRIORITY_LOW_POWER);
    requests.add(balanced);
    requests.add(lowPower);
    LocationSettingsRequest settingsRequest =
        new LocationSettingsRequest.Builder().addAllLocationRequests(requests)
            .setNeedBle(false)
            .build();
    LocationSettingsResultRequest settingsResultRequest =
        new LocationSettingsResultRequest(context, generator, settingsRequest);

    settingsResultRequest.setResultCallback(new ResultCallback<LocationSettingsResult>() {
      @Override public void onResult(@NonNull LocationSettingsResult result) {
        assertThat(result.getStatus().getStatusCode()).isEqualTo(Status.RESOLUTION_REQUIRED);
      }
    });
  }

  @Test
  public void setResultCallback_balancedLowPower_needBle_shouldReturnStatusResolutionRequired() {
    when(pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)).thenReturn(true);

    ArrayList<LocationRequest> requests = new ArrayList<>();
    LocationRequest balanced =
        LocationRequest.create().setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    LocationRequest lowPower =
        LocationRequest.create().setPriority(LocationRequest.PRIORITY_LOW_POWER);
    requests.add(balanced);
    requests.add(lowPower);
    LocationSettingsRequest settingsRequest =
        new LocationSettingsRequest.Builder().addAllLocationRequests(requests)
            .setNeedBle(true)
            .build();
    LocationSettingsResultRequest settingsResultRequest =
        new LocationSettingsResultRequest(context, generator, settingsRequest);

    settingsResultRequest.setResultCallback(new ResultCallback<LocationSettingsResult>() {
      @Override public void onResult(@NonNull LocationSettingsResult result) {
        assertThat(result.getStatus().getStatusCode()).isEqualTo(Status.RESOLUTION_REQUIRED);
      }
    });
  }

  @Test public void setResultCallback_balancedLowPower_noBle_shouldReturnStatusSuccess() {
    when(pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)).thenReturn(true);
    when(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)).thenReturn(true);

    ArrayList<LocationRequest> requests = new ArrayList<>();
    LocationRequest balanced =
        LocationRequest.create().setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    LocationRequest lowPower =
        LocationRequest.create().setPriority(LocationRequest.PRIORITY_LOW_POWER);
    requests.add(balanced);
    requests.add(lowPower);
    LocationSettingsRequest settingsRequest =
        new LocationSettingsRequest.Builder().addAllLocationRequests(requests)
            .setNeedBle(false)
            .build();
    LocationSettingsResultRequest settingsResultRequest =
        new LocationSettingsResultRequest(context, generator, settingsRequest);

    settingsResultRequest.setResultCallback(new ResultCallback<LocationSettingsResult>() {
      @Override public void onResult(@NonNull LocationSettingsResult result) {
        assertThat(result.getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
      }
    });
  }

  @Test public void setResultCallback_noPower_noBle_shouldReturnStatusSuccess() {
    when(pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)).thenReturn(true);

    ArrayList<LocationRequest> requests = new ArrayList<>();
    LocationRequest noPower =
        LocationRequest.create().setPriority(LocationRequest.PRIORITY_NO_POWER);
    requests.add(noPower);
    LocationSettingsRequest settingsRequest =
        new LocationSettingsRequest.Builder().addAllLocationRequests(requests)
            .setNeedBle(false)
            .build();
    LocationSettingsResultRequest settingsResultRequest =
        new LocationSettingsResultRequest(context, generator, settingsRequest);

    settingsResultRequest.setResultCallback(new ResultCallback<LocationSettingsResult>() {
      @Override public void onResult(@NonNull LocationSettingsResult result) {
        assertThat(result.getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
      }
    });
  }

  @Test public void setResultCallback_noPower_needBle_shouldReturnStatusResolutionRequired() {
    when(pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)).thenReturn(true);

    ArrayList<LocationRequest> requests = new ArrayList<>();
    LocationRequest noPower =
        LocationRequest.create().setPriority(LocationRequest.PRIORITY_NO_POWER);
    requests.add(noPower);
    LocationSettingsRequest settingsRequest =
        new LocationSettingsRequest.Builder().addAllLocationRequests(requests)
            .setNeedBle(true)
            .build();
    LocationSettingsResultRequest settingsResultRequest =
        new LocationSettingsResultRequest(context, generator, settingsRequest);

    settingsResultRequest.setResultCallback(new ResultCallback<LocationSettingsResult>() {
      @Override public void onResult(@NonNull LocationSettingsResult result) {
        assertThat(result.getStatus().getStatusCode()).isEqualTo(Status.RESOLUTION_REQUIRED);
      }
    });
  }

  @Test public void setResultCallbackTimeout_shouldTimeoutAfterInterval() {
    when(pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK)).thenReturn(true);
    when(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)).thenReturn(true);

    DelayTestPendingIntentGenerator delayedGenerator = new DelayTestPendingIntentGenerator(context);
    LocationSettingsResultRequest settingsResultRequest =
        new LocationSettingsResultRequest(context, delayedGenerator, request);

    settingsResultRequest.setResultCallback(new ResultCallback<LocationSettingsResult>() {
      @Override public void onResult(@NonNull LocationSettingsResult result) {
        assertThat(result.getStatus().getStatusCode()).isEqualTo(Status.TIMEOUT);
      }
    }, 1000, TimeUnit.MILLISECONDS);
  }
}
