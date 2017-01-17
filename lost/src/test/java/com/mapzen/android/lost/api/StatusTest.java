package com.mapzen.android.lost.api;

import com.mapzen.android.lost.internal.TestSettingsDialogDisplayer;

import org.junit.Before;
import org.junit.Test;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.IntentSender;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class StatusTest {

  PendingIntent pendingIntent;
  Status status;

  @Before public void setup() {
    pendingIntent = mock(PendingIntent.class);
    status = new Status(Status.SUCCESS, new TestSettingsDialogDisplayer(), pendingIntent);
  }

  @Test public void shouldHaveSuccessStatus() {
    assertThat(status.getStatusCode()).isEqualTo(Status.SUCCESS);
  }

  @Test public void shouldHavePendingIntent() {
    assertThat(status.getResolution()).isEqualTo(pendingIntent);
  }

  @Test public void shouldHaveStatusMessage() {
    assertThat(status.getStatusMessage()).isEqualTo("SUCCESS");
  }

  @Test public void resolveSettings_shouldInvokeDialogDisplayer()
      throws IntentSender.SendIntentException {
    TestSettingsDialogDisplayer dialogDisplayer = new TestSettingsDialogDisplayer();
    Status s = new Status(Status.RESOLUTION_REQUIRED, dialogDisplayer, pendingIntent);
    s.startResolutionForResult(mock(Activity.class), 1);
    assertThat(dialogDisplayer.isDisplayed()).isTrue();
  }

  @Test public void shouldNotHaveResolution() {
    Status s = new Status(Status.RESOLUTION_REQUIRED);
    assertThat(s.hasResolution()).isFalse();
  }
}
