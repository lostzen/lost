package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.Status;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.fest.assertions.api.Assertions.assertThat;

public class SimplePendingResultTest {

  SimplePendingResult result = new SimplePendingResult(true);

  @Test public void await_shouldReturnSuccess() {
    assertThat(result.await().getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
  }

  @Test public void await_delay_shouldReturnSuccess() {
    assertThat(result.await(1000, TimeUnit.MILLISECONDS).getStatus().getStatusCode()).isEqualTo(
        Status.SUCCESS);
  }

  @Test public void isCancelled_shouldReturnFalse() {
    assertThat(result.isCanceled()).isFalse();
  }

  @Test public void setResultCallback_shouldReturnSuccess() {
    TestResultCallback callback = new TestResultCallback();
    result.setResultCallback(callback);
    assertThat(callback.getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
  }

  @Test public void setResultCallback_delay_shouldReturnSuccess() {
    TestResultCallback callback = new TestResultCallback();
    result.setResultCallback(callback, 1000, TimeUnit.MILLISECONDS);
    assertThat(callback.getStatus().getStatusCode()).isEqualTo(Status.SUCCESS);
  }
}
