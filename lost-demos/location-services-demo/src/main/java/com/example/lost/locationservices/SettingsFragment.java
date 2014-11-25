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
package com.example.lost.locationservices;

import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

/**
 * Settings Fragment
 */
public class SettingsFragment extends PreferenceFragment {
    private SharedPreferences prefs;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        listener = new SettingsListener();

        updateValue(getString(R.string.mock_lat_key));
        updateValue(getString(R.string.mock_lng_key));
        updateValue(getString(R.string.mock_accuracy_key));
        updateValue(getString(R.string.mock_gpx_file_key));
    }

    @Override
    public void onResume() {
        super.onResume();
        prefs.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onPause() {
        super.onPause();
        prefs.unregisterOnSharedPreferenceChangeListener(listener);
        setMockLocation();

        if (prefs.getBoolean(getString(R.string.mock_mode_gpx_key), false)) {
            String filename = getValue(getString(R.string.mock_gpx_file_key));
            LostActivity.getLocationClient().setMockTrace(filename);
        }
    }

    private void updateValue(String key) {
        final String value = getValue(key);
        final Preference pref = findPreference(key);
        pref.setSummary(value);

        if (!getString(R.string.mock_gpx_file_key).equals(key)) {
            try {
                Float.parseFloat(value);
            } catch (NumberFormatException e) {
                Toast.makeText(getActivity(), "Invalid value: " + pref.getTitle(),
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private String getValue(String key) {
        if (getString(R.string.mock_gpx_file_key).equals(key)) {
            return prefs.getString(key, "lost.gpx");
        }

        return prefs.getString(key, "0.0");
    }

    public void setMockLocation() {
        float lat = 0f;
        float lng = 0f;
        float accuracy = 0f;

        try {
            lat = Float.parseFloat(getValue(getString(R.string.mock_lat_key)));
        } catch (NumberFormatException e) {
        }

        try {
            lng = Float.parseFloat(getValue(getString(R.string.mock_lng_key)));
        } catch (NumberFormatException e) {
        }

        try {
            accuracy = Float.parseFloat(getValue(getString(R.string.mock_accuracy_key)));
        } catch (NumberFormatException e) {
        }

        final Location location = new Location("mock");
        location.setLatitude(lat);
        location.setLongitude(lng);
        location.setAccuracy(accuracy);
        location.setTime(System.currentTimeMillis());

        LostActivity.getLocationClient().setMockLocation(location);
    }

    /**
     * Settings Listener
     */
    private class SettingsListener implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            if (!getString(R.string.mock_mode_key).equals(key) &&
                    !getString(R.string.mock_mode_gpx_key).equals(key)) {
                updateValue(key);
            }
        }
    }
}
