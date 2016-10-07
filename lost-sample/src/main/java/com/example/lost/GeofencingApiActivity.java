package com.example.lost;

import com.mapzen.android.lost.api.Geofence;
import com.mapzen.android.lost.api.GeofencingRequest;
import com.mapzen.android.lost.api.LocationServices;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import static com.mapzen.android.lost.api.Geofence.GEOFENCE_TRANSITION_DWELL;
import static com.mapzen.android.lost.api.Geofence.GEOFENCE_TRANSITION_ENTER;
import static com.mapzen.android.lost.api.Geofence.GEOFENCE_TRANSITION_EXIT;
import static com.mapzen.android.lost.api.Geofence.NEVER_EXPIRE;

/**
 * Geofencing demo
 */
public class GeofencingApiActivity extends LostApiClientActivity {

  private TextView inputRequestId;
  private TextView inputLatitude;
  private TextView inputLongitude;
  private TextView inputRadius;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_geofencing);

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

  private void createGeofence() {
    String requestId = inputRequestId.getText().toString();
    double latitude = Double.valueOf(inputLatitude.getText().toString());
    double longitude = Double.valueOf(inputLongitude.getText().toString());
    float radius = Float.valueOf(inputRadius.getText().toString());

    Geofence geofence = new Geofence.Builder()
        .setRequestId(requestId)
        .setCircularRegion(latitude, longitude, radius)
        .setTransitionTypes(GEOFENCE_TRANSITION_ENTER | GEOFENCE_TRANSITION_EXIT |
            GEOFENCE_TRANSITION_DWELL)
        .setLoiteringDelay(10000)
        .setExpirationDuration(NEVER_EXPIRE)
        .build();
    GeofencingRequest request = new GeofencingRequest.Builder()
        .addGeofence(geofence)
        .build();
    Intent serviceIntent = new Intent(getApplicationContext(), GeofenceIntentService.class);
    PendingIntent pendingIntent = PendingIntent.getService(this, 0, serviceIntent, 0);

    LocationServices.GeofencingApi.addGeofences(client, request, pendingIntent);
  }
}
