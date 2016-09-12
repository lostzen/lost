package com.example.lost;

/**
 * Displays a list of samples available for the
 * {@link com.mapzen.android.lost.api.FusedLocationProviderApi}
 */
public class FusedLocationApiActivity extends SampleListActivity {

  @Override int numOfSamples() {
    return SamplesList.FUSED_API_SAMPLES.length;
  }

  @Override Sample[] getSamples() {
    return SamplesList.FUSED_API_SAMPLES;
  }
}
