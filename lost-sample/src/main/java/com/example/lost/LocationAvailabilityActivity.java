package com.example.lost;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.mapzen.android.lost.api.LocationAvailability;
import com.mapzen.android.lost.api.LocationServices;
import com.mapzen.android.lost.api.LostApiClient;

public class LocationAvailabilityActivity extends AppCompatActivity {

    LostApiClient client;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_availability);

        setupLocationAvailabilityBtn();
    }

    private void setupLocationAvailabilityBtn() {
        Button connect = (Button) findViewById(R.id.check_availability);
        connect.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (client == null || !client.isConnected()) {
                    connect();
                } else {
                    checkLocationAvailability();
                }
            }
        });
    }

    private void connect() {
        client = new LostApiClient.Builder(this).addConnectionCallbacks(new LostApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected() {
                checkLocationAvailability();
            }

            @Override
            public void onConnectionSuspended() {

            }
        }).build();
        client.connect();
    }

    private void checkLocationAvailability() {
        LocationAvailability availability =
                LocationServices.FusedLocationApi.getLocationAvailability(client);
        boolean isAvailable = availability.isLocationAvailable();
        Toast.makeText(LocationAvailabilityActivity.this, isAvailable ? R.string.location_available
                : R.string.location_unavailable, Toast.LENGTH_SHORT).show();
    }
}
