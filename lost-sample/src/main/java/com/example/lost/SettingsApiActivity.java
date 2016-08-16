package com.example.lost;

import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LocationServices;
import com.mapzen.android.lost.api.LocationSettingsRequest;
import com.mapzen.android.lost.api.LocationSettingsResult;
import com.mapzen.android.lost.api.LocationSettingsStates;
import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.android.lost.api.PendingResult;
import com.mapzen.android.lost.api.Status;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class SettingsApiActivity extends AppCompatActivity {

  private static final int REQUEST_CHECK_SETTINGS = 100;
  private static final String TAG = SettingsApiActivity.class.getSimpleName();

  Button checkLocationSettings;
  Button resolveLocationSettings;
  Button cancelBtn;

  TextView gpsPresent;
  TextView gpsUsable;
  TextView networkPresent;
  TextView networkUsable;
  TextView locationPresent;
  TextView locationUsable;
  TextView blePresent;
  TextView bleUsable;

  Status requestStatus;

  PendingResult<LocationSettingsResult> result;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings_api);

    checkLocationSettings = (Button) findViewById(R.id.check_location_settings);
    checkLocationSettings.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        checkLocationSettings();
      }
    });
    resolveLocationSettings = (Button) findViewById(R.id.resolve_location_settings);
    resolveLocationSettings.setEnabled(false);
    resolveLocationSettings.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        resolveLocationSettings();
      }
    });
    cancelBtn = (Button) findViewById(R.id.cancel);
    cancelBtn.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        if (result != null) {
          result.cancel();
        }
      }
    });

    gpsPresent = (TextView) findViewById(R.id.gps_present);
    gpsUsable = (TextView) findViewById(R.id.gps_usable);
    networkPresent = (TextView) findViewById(R.id.network_present);
    networkUsable = (TextView) findViewById(R.id.network_usable);
    locationPresent = (TextView) findViewById(R.id.location_present);
    locationUsable = (TextView) findViewById(R.id.location_usable);
    blePresent = (TextView) findViewById(R.id.ble_present);
    bleUsable = (TextView) findViewById(R.id.ble_usable);
  }

  private void checkLocationSettings() {
    ArrayList<LocationRequest> requests = new ArrayList<>();
    LocationRequest highAccuracy =
        LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); //gps + wifi
    requests.add(highAccuracy);

    boolean needBle = true;
    LocationSettingsRequest request =
        new LocationSettingsRequest.Builder().addAllLocationRequests(requests)
            .setNeedBle(needBle)
            .build();
    LostApiClient apiClient = new LostApiClient.Builder(this).build();
    apiClient.connect();
    result = LocationServices.SettingsApi.checkLocationSettings(apiClient, request);

    LocationSettingsResult locationSettingsResult = result.await();

    LocationSettingsStates states = locationSettingsResult.getLocationSettingsStates();
    if (states != null) {
      updateTextViewStates(states);
    }

    Status status = locationSettingsResult.getStatus();
    switch (status.getStatusCode()) {
      case Status.SUCCESS:
        resolveLocationSettings.setEnabled(false);
        break;
      case Status.RESOLUTION_REQUIRED:
        requestStatus = status;
        resolveLocationSettings.setEnabled(true);
        break;
      case Status.INTERNAL_ERROR:
        Log.d(TAG, "INTERNAL ERROR");
        break;
      case Status.INTERRUPTED:
        Log.d(TAG, "INTERRUPTED");
        break;
      case Status.TIMEOUT:
        Log.d(TAG, "TIMEOUT");
        break;
      case Status.CANCELLED:
        Log.d(TAG, "CANCELLED");
        break;
      default:
        break;
    }
  }

  private void resolveLocationSettings() {
    try {
      requestStatus.startResolutionForResult(SettingsApiActivity.this, REQUEST_CHECK_SETTINGS);
    } catch (IntentSender.SendIntentException e) {
      e.printStackTrace();
    }
  }

  private void updateTextViewStates(LocationSettingsStates states) {
    gpsPresent.setText(states.isGpsPresent() ? "Y" : "N");
    gpsUsable.setText(states.isGpsUsable() ? "Y" : "N");
    networkPresent.setText(states.isNetworkLocationPresent() ? "Y" : "N");
    networkUsable.setText(states.isNetworkLocationUsable() ? "Y" : "N");
    locationPresent.setText(states.isLocationPresent() ? "Y" : "N");
    locationUsable.setText(states.isLocationUsable() ? "Y" : "N");
    blePresent.setText(states.isBlePresent() ? "Y" : "N");
    bleUsable.setText(states.isBleUsable() ? "Y" : "N");
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      case REQUEST_CHECK_SETTINGS:
        checkLocationSettings();
        break;
      default:
        break;
    }
  }
}
