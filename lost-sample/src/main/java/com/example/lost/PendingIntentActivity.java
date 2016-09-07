package com.example.lost;

import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LocationServices;
import com.mapzen.android.lost.api.LostApiClient;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Demonstrates how to register for {@link android.app.PendingIntent}s.
 */
public class PendingIntentActivity extends AppCompatActivity implements
    LostApiClient.ConnectionCallbacks {

  LostApiClient client;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_pending_intent);

    client = new LostApiClient.Builder(this).addConnectionCallbacks(this).build();

    setupConnectBtn();
    setupRequestBtn();
    setupDisconnectBtn();
  }

  @Override public void onConnected() {
    Toast.makeText(this, R.string.connected, Toast.LENGTH_SHORT).show();
  }

  @Override public void onConnectionSuspended() {

  }

  private void setupConnectBtn() {
    Button connect = (Button) findViewById(R.id.connect);
    connect.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        client.connect();
      }
    });
  }

  private void setupRequestBtn() {

    Button requestUpdates = (Button) findViewById(R.id.request_updates);
    requestUpdates.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        request.setInterval(100);

        Intent intent = new Intent(PendingIntentService.ACTION);
        PendingIntent pendingIntent = PendingIntent.getService(PendingIntentActivity.this, 1,
            intent, 0);
        LocationServices.FusedLocationApi.requestLocationUpdates(client, request, pendingIntent);

        Toast.makeText(PendingIntentActivity.this, R.string.requested, Toast.LENGTH_SHORT).show();
      }
    });
  }

  private void setupDisconnectBtn() {
    Button disconnect = (Button) findViewById(R.id.disconnect);
    disconnect.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        client.disconnect();
        Toast.makeText(PendingIntentActivity.this, R.string.disconnected, Toast.LENGTH_SHORT)
            .show();
      }
    });
  }
}