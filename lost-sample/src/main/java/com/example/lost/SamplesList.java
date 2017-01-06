package com.example.lost;

public class SamplesList {

  private SamplesList() {
  }

  public static final Sample[] SAMPLES = {
      new Sample(R.string.sample_fused_location_api_title,
          R.string.sample_fused_location_api_description, FusedLocationApiActivity.class),
      new Sample(R.string.sample_geofencing_api_title, R.string.sample_geofencing_api_description,
          GeofencingApiActivity.class),
      new Sample(R.string.sample_settings_api_title, R.string.sample_settings_api_description,
          SettingsApiActivity.class)
  };

  public static final Sample[] FUSED_API_SAMPLES = {
      new Sample(R.string.sample_location_listener_title,
          R.string.sample_location_listener_description, LocationListenerActivity.class),
      new Sample(R.string.sample_multiple_clients_diff_intervals_title,
          R.string.sample_multiple_clients_diff_intervals_description,
          MultipleLocationListenerMultipleClientsActivity.class),
      new Sample(R.string.sample_single_client_diff_intervals_title,
          R.string.sample_single_client_diff_intervals_description,
          MultipleLocationListenerSingleClientActivity.class),
      new Sample(R.string.sample_pending_intent_title,
          R.string.sample_pending_intent_description, PendingIntentActivity.class),
      new Sample(R.string.sample_location_availability_title,
          R.string.sample_location_availability_description, LocationAvailabilityActivity.class)
  };
}
