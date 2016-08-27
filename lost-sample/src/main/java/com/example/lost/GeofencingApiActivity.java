package com.example.lost;

import com.mapzen.android.lost.api.Geofence;
import com.mapzen.android.lost.api.GeofencingRequest;
import com.mapzen.android.lost.api.LocationServices;
import com.mapzen.android.lost.api.LostApiClient;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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

  private TextView inputRequestId;
  private TextView inputLatitude;
  private TextView inputLongitude;
  private TextView inputRadius;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_geofencing);
    if (client == null) {
      client = new LostApiClient.Builder(this).addConnectionCallbacks(this).build();
    }
    initConnectButton();
    initDisconnectButton();
    initInputFields();
    initCreateButton();
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

  private void initInputFields() {
    inputRequestId = (TextView) findViewById(R.id.input_request_id);
    inputLatitude = (TextView) findViewById(R.id.input_latitude);
    inputLongitude = (TextView) findViewById(R.id.input_longitude);
    inputRadius = (TextView) findViewById(R.id.input_radius);
  }

  private void initCreateButton() {
    Button createButton = (Button) findViewById(R.id.button_create);
    if (createButton != null) {
      createButton.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          createGeofence();
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

  @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    if (grantResults[0] == PERMISSION_GRANTED) {
      client.connect();
    } else {
      finish();
    }
  }

  private void disconnect() {
    Log.d(TAG, "Disconnecting...");
    client.disconnect();
    Toast.makeText(this, "LOST client disconnected", LENGTH_SHORT).show();
  }

  private void createGeofence() {
    String requestId = inputRequestId.getText().toString();
    double latitude = Double.valueOf(inputLatitude.getText().toString());
    double longitude = Double.valueOf(inputLongitude.getText().toString());
    float radius = Float.valueOf(inputRadius.getText().toString());

    Geofence geofence = new Geofence.Builder()
        .setRequestId(requestId)
        .setCircularRegion(latitude, longitude, radius)
        .build();
    GeofencingRequest request = new GeofencingRequest.Builder()
        .addGeofence(geofence)
        .build();
    Intent serviceIntent = new Intent(getApplicationContext(), GeofenceIntentService.class);
    PendingIntent pendingIntent = PendingIntent.getService(this, 0, serviceIntent, 0);

    LocationServices.GeofencingApi.addGeofences(request, pendingIntent);
  }
}
