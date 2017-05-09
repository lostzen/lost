package com.example.lost;

import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LocationServices;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Demonstrates how to register for {@link android.app.PendingIntent}s.
 */
public class PendingIntentActivity extends LostApiClientActivity {

  PendingIntent pendingIntent;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_pending_intent);

    setupConnectBtn();
    setupRequestBtn();
    setupDisconnectBtn();
  }

  private void setupConnectBtn() {
    Button connect = (Button) findViewById(R.id.connect);
    connect.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        connect();
      }
    });
  }

  private void setupRequestBtn() {
    Button requestUpdates = (Button) findViewById(R.id.request_updates);
    requestUpdates.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        requestLocationUpdates();
      }
    });
  }

  private void setupDisconnectBtn() {
    Button disconnect = (Button) findViewById(R.id.disconnect);
    disconnect.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        LocationServices.FusedLocationApi.removeLocationUpdates(client, pendingIntent);
        disconnect();
      }
    });
  }

  private void requestLocationUpdates() {
    LocationRequest request = LocationRequest.create();
    request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    request.setInterval(100);

    Intent intent = new Intent(PendingIntentService.ACTION);
    pendingIntent = PendingIntent.getService(PendingIntentActivity.this, 1,
        intent, 0);
    LocationServices.FusedLocationApi.requestLocationUpdates(client, request, pendingIntent);

    Toast.makeText(PendingIntentActivity.this, R.string.requested, Toast.LENGTH_SHORT).show();
  }
}
