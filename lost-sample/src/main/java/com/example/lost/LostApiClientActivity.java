package com.example.lost;

import com.mapzen.android.lost.api.LostApiClient;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.widget.Toast.LENGTH_SHORT;

/**
 * Creates a {@link LostApiClient} and handles requesting permissions
 */
public class LostApiClientActivity extends AppCompatActivity implements
    LostApiClient.ConnectionCallbacks {

  private static final String TAG = LostApiClientActivity.class.getSimpleName();

  protected LostApiClient client;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    client = new LostApiClient.Builder(this).addConnectionCallbacks(this).build();
  }

  @Override public void onConnected() {
    Toast.makeText(this, "LOST client connected", LENGTH_SHORT).show();
  }

  @Override public void onConnectionSuspended() {
    Toast.makeText(this, "LOST client suspended", LENGTH_SHORT).show();
  }

  private boolean isFineLocationPermissionGranted() {
    return ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED;
  }

  private void requestFineLocationPermission() {
    ActivityCompat.requestPermissions(this, new String[] { ACCESS_FINE_LOCATION }, 101);
  }

  @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    if (grantResults[0] == PERMISSION_GRANTED) {
      client.connect();
    } else {
      finish();
    }
  }

  protected void connect() {
    Log.d(TAG, "Connecting...");
    if (!isFineLocationPermissionGranted()) {
      requestFineLocationPermission();
    } else {
      client.connect();
    }
  }

  protected void disconnect() {
    Log.d(TAG, "Disconnecting...");
    client.disconnect();
    Toast.makeText(this, "LOST client disconnected", LENGTH_SHORT).show();
  }
}
