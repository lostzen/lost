package com.mapzen.android.lost.api;

import com.mapzen.android.lost.internal.FusedLocationProviderApiImpl;
import com.mapzen.android.lost.internal.GeofencingApiImpl;
import com.mapzen.lost.BuildConfig;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, manifest = Config.NONE)
public class LocationServicesTest {
  private LostApiClient client;

  @Before public void setUp() throws Exception {
    client = new LostApiClient.Builder(RuntimeEnvironment.application).build();
    client.connect();
  }

  @After public void tearDown() throws Exception {
    client.disconnect();
  }

  @Test public void shouldCreateFusedLocationProviderApiImpl() throws Exception {
    assertThat(LocationServices.FusedLocationApi).isInstanceOf(FusedLocationProviderApiImpl.class);
  }

  @Test public void shouldCreateGeofencingApiImpl() throws Exception {
    assertThat(LocationServices.GeofencingApi).isInstanceOf(GeofencingApiImpl.class);
  }
}
