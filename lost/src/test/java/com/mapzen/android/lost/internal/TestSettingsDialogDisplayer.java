package com.mapzen.android.lost.internal;

import android.app.Activity;
import android.app.PendingIntent;
import android.os.Parcel;

public class TestSettingsDialogDisplayer implements DialogDisplayer {

  boolean displayed = false;

  public TestSettingsDialogDisplayer() {
  }

  protected TestSettingsDialogDisplayer(Parcel in) {
  }

  public static final Creator<TestSettingsDialogDisplayer> CREATOR =
      new Creator<TestSettingsDialogDisplayer>() {
        @Override public TestSettingsDialogDisplayer createFromParcel(Parcel in) {
          return new TestSettingsDialogDisplayer(in);
        }

        @Override public TestSettingsDialogDisplayer[] newArray(int size) {
          return new TestSettingsDialogDisplayer[size];
        }
      };

  @Override
  public void displayDialog(Activity activity, int requestCode, PendingIntent pendingIntent) {
    displayed = true;
  }

  public boolean isDisplayed() {
    return displayed;
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel parcel, int i) {

  }
}
