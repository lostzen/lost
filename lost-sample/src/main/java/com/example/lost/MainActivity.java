package com.example.lost;

/**
 * Displays a list of samples available for the {@link com.mapzen.android.lost.api.LocationServices}
 * APIs
 */
public class MainActivity extends SampleListActivity {

  @Override int numOfSamples() {
    return SamplesList.SAMPLES.length;
  }

  @Override Sample[] getSamples() {
    return SamplesList.SAMPLES;
  }
}
