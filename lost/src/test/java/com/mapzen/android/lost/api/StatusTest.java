package com.mapzen.android.lost.api;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import android.app.PendingIntent;

import static org.fest.assertions.api.Assertions.assertThat;

public class StatusTest {

  PendingIntent pendingIntent;
  Status status;

  @Before
  public void setup() {
    pendingIntent = Mockito.mock(PendingIntent.class);
    status = new Status(Status.SUCCESS, pendingIntent);
  }

  @Test
  public void shouldHaveSuccessStatus() {
    assertThat(status.getStatusCode()).isEqualTo(Status.SUCCESS);
  }

  @Test
  public void shouldHavePendingIntent() {
    assertThat(status.getResolution()).isEqualTo(pendingIntent);
  }

  @Test
  public void shouldHaveStatusMessage() {
    assertThat(status.getStatusMessage()).isEqualTo("SUCCESS");
  }
}
