package com.example.lost;

import android.support.v7.app.AppCompatActivity;

public class Sample {

  private final int titleId;

  private final int detailId;

  private final Class<? extends AppCompatActivity> activityClass;

  public Sample(int titleId, int detailId, Class activityClass) {
    this.titleId = titleId;
    this.detailId = detailId;
    this.activityClass = activityClass;
  }

  public int getTitleId() {
    return titleId;
  }

  public int getDetailId() {
    return detailId;
  }

  public Class<? extends AppCompatActivity> getActivityClass() {
    return activityClass;
  }
}
