package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationSettingsRequest;
import com.mapzen.android.lost.api.LocationSettingsResult;
import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.android.lost.api.PendingResult;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;

import static org.fest.assertions.api.Assertions.assertThat;

public class SettingsApiImplTest {

  Context context;
  SettingsApiImpl settingsApi;

  @Before
  public void setup() {
    context = Mockito.mock(Context.class);
    BluetoothAdapter adapter = Mockito.mock(BluetoothAdapter.class);
    settingsApi = new SettingsApiImpl(context, adapter);
  }

  @Test
  public void checkLocationSettings_shouldCreateLocationSettingsResultRequest() {
    LocationSettingsRequest request = new LocationSettingsRequest.Builder()
        .build();
    LostApiClient apiClient = new LostApiClient.Builder(context).build();

    PendingResult<LocationSettingsResult> result = settingsApi.checkLocationSettings(
        apiClient, request);
    assertThat(result).isInstanceOf(LocationSettingsResultRequest.class);
  }
}
