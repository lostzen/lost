package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationSettingsRequest;
import com.mapzen.android.lost.api.LocationSettingsResult;
import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.android.lost.api.PendingResult;

import org.junit.Before;
import org.junit.Test;

import android.content.Context;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class SettingsApiImplTest {

  Context context;
  SettingsApiImpl settingsApi;

  @Before public void setup() {
    context = mock(Context.class);
    settingsApi = new SettingsApiImpl();
    settingsApi.connect(context);
  }

  @Test public void checkLocationSettings_shouldCreateLocationSettingsResultRequest() {
    LocationSettingsRequest request = new LocationSettingsRequest.Builder().build();
    LostApiClient apiClient = new LostApiClient.Builder(context).build();

    PendingResult<LocationSettingsResult> result =
        settingsApi.checkLocationSettings(apiClient, request);
    assertThat(result).isInstanceOf(LocationSettingsResultRequest.class);
  }
}
