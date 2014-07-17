/*
 * Copyright 2014 Mapzen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.lost;

import com.mapzen.android.lost.LocationClient;
import com.mapzen.android.lost.LocationListener;
import com.mapzen.android.lost.LocationRequest;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    LostFragment fragment;
    LocationClient client;
    SharedPreferences sharedPreferences;

    LocationClient.ConnectionCallbacks callbacks = new LocationClient.ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle connectionHint) {
            Toast.makeText(getApplication(), "Location client connected",
                    Toast.LENGTH_SHORT).show();

            client.setMockMode(isMockMode());

            Location location = client.getLastLocation();
            fragment.setLastKnownLocation(location);

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setInterval(5000);
            locationRequest.setSmallestDisplacement(0);
            client.requestLocationUpdates(locationRequest, listener);
        }

        @Override
        public void onDisconnected() {
            Toast.makeText(getApplication(), "Location client disconnected",
                    Toast.LENGTH_SHORT).show();
        }
    };

    LocationListener listener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            fragment.updateLocation(location);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final FragmentManager fragmentManager = getFragmentManager();
        fragment = (LostFragment) fragmentManager.findFragmentByTag(LostFragment.TAG);
        if (fragment == null) {
            fragment = new LostFragment();
            fragmentManager.beginTransaction().add(android.R.id.content, fragment,
                    LostFragment.TAG).commit();
        }

        client = new LocationClient(this, callbacks);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplication());
    }

    @Override
    protected void onResume() {
        super.onResume();
        client.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        client.disconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        startActivity(new Intent(this, SettingsActivity.class));
        return true;
    }

    private boolean isMockMode() {
        return sharedPreferences.getBoolean(getString(R.string.mock_mode_key), false);
    }

    public static class LostFragment extends Fragment {
        public static final String TAG = LostFragment.class.getSimpleName();

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_lost, container, false);
        }

        public void setLastKnownLocation(Location location) {
            TextView lastLocation = (TextView) getView().findViewById(R.id.last_location_value);
            lastLocation.setText(location.toString());
        }

        public void updateLocation(Location location) {
            TextView textView = new TextView(getActivity());
            textView.setText(location.toString());
            ((ViewGroup) getView().findViewById(R.id.container)).addView(textView);
        }
    }
}
