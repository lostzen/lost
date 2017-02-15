package com.mapzen.android.lost.api;

import com.mapzen.android.lost.BaseRobolectricTest;
import com.mapzen.android.lost.internal.FusedLocationProviderApiImpl;
import com.mapzen.android.lost.internal.GeofencingApiImpl;
import com.mapzen.android.lost.internal.SettingsApiImpl;
import com.mapzen.lost.BuildConfig;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, manifest = Config.NONE)
public class LocationServicesTest extends BaseRobolectricTest {

  @Test public void shouldCreateFusedLocationProviderApiImpl() throws Exception {
    assertThat(LocationServices.FusedLocationApi).isInstanceOf(FusedLocationProviderApiImpl.class);
  }

  @Test public void shouldCreateGeofencingApiImpl() throws Exception {
    assertThat(LocationServices.GeofencingApi).isInstanceOf(GeofencingApiImpl.class);
  }

  @Test public void shouldCreateSettingApiImpl() throws Exception {
    assertThat(LocationServices.SettingsApi).isInstanceOf(SettingsApiImpl.class);
  }
}
