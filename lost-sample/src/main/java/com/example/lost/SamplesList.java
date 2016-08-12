package com.example.lost;

public class SamplesList {

  private SamplesList() {
  }

  public static final Sample[] SAMPLES = {
      new Sample(R.string.sample_fused_location_api_title,
          R.string.sample_fused_location_api_description,
          FusedLocationApiActivity.class),
      new Sample(R.string.sample_settings_api_title,
          R.string.sample_settings_api_description,
          SettingsApiActivity.class)
  };
}
