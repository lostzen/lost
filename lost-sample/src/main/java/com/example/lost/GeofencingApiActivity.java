package com.example.lost;

import com.mapzen.android.lost.api.LostApiClient;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.widget.Toast.LENGTH_SHORT;

/**
 * Geofencing demo
 */
public class GeofencingApiActivity extends AppCompatActivity
    implements LostApiClient.ConnectionCallbacks {
  private static final String TAG = GeofencingApiActivity.class.getSimpleName();

  private static LostApiClient client;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_geofencing);
    if (client == null) {
      client = new LostApiClient.Builder(this).addConnectionCallbacks(this).build();
    }
    initConnectButton();
    initDisconnectButton();
  }

  private void initConnectButton() {
    Button connectButton = (Button) findViewById(R.id.button_connect);
    if (connectButton != null) {
      connectButton.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          connect();
        }
      });
    }
  }

  private void initDisconnectButton() {
    Button disconnectButton = (Button) findViewById(R.id.button_disconnect);
    if (disconnectButton != null) {
      disconnectButton.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          disconnect();
        }
      });
    }
  }

  @Override public void onConnected() {
    Toast.makeText(this, "LOST client connected", LENGTH_SHORT).show();
  }

  @Override public void onConnectionSuspended() {
    Toast.makeText(this, "LOST client suspended", LENGTH_SHORT).show();
  }

  private void connect() {
    Log.d(TAG, "Connecting...");
    if (!isFineLocationPermissionGranted()) {
      requestFineLocationPermission();
    } else {
      client.connect();
    }
  }

  private boolean isFineLocationPermissionGranted() {
    return ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED;
  }

  private void requestFineLocationPermission() {
    ActivityCompat.requestPermissions(this, new String[] { ACCESS_FINE_LOCATION }, 101);
  }

  private void disconnect() {
    Log.d(TAG, "Disconnecting...");
    client.disconnect();
    Toast.makeText(this, "LOST client disconnected", LENGTH_SHORT).show();
  }

  @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    if (grantResults[0] == PERMISSION_GRANTED) {
      client.connect();
    } else {
      finish();
    }
  }
}
