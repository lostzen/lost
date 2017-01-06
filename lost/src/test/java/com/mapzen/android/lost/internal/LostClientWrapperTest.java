package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LostApiClient;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import static org.fest.assertions.api.Assertions.assertThat;

public class LostClientWrapperTest {
  private LostClientWrapper wrapper;
  private LostApiClient client;

  @Before public void setUp() throws Exception {
    client = new LostApiClient.Builder(RuntimeEnvironment.application).build();
    wrapper = new LostClientWrapper(client);
  }

  @Test public void shouldNotBeNull() throws Exception {
    assertThat(wrapper).isNotNull();
  }

  @Test public void shouldExposeClient() throws Exception {
    assertThat(wrapper.client()).isEqualTo(client);
  }
}
