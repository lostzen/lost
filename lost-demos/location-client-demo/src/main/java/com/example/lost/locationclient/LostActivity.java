package com.example.lost.locationclient;

import com.mapzen.android.lost.api.LocationClient;
import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

/**
 * LOST Activity
 */
public class LostActivity extends Activity {
    private LostFragment fragment;
    private SharedPreferences sharedPreferences;

    private static LocationClient client;

    public static LocationClient getLocationClient() {
        return client;
    }

    LocationClient.ConnectionCallbacks callbacks = new LocationClient.ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle connectionHint) {
            Toast.makeText(LostActivity.this, "Location client connected",
                    Toast.LENGTH_SHORT).show();

            if (fragment.lastKnownLocation == null) {
                fragment.setLastKnownLocation(client.getLastLocation());
            }

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setInterval(1000);
            locationRequest.setSmallestDisplacement(0);
            client.requestLocationUpdates(locationRequest, listener);
        }

        @Override
        public void onDisconnected() {
            Toast.makeText(LostActivity.this, "Location client disconnected",
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
        File file = Environment.getExternalStorageDirectory();
        System.out.println("External storage directory: " + file.toString());
        final FragmentManager fragmentManager = getFragmentManager();
        fragment = (LostFragment) fragmentManager.findFragmentByTag(LostFragment.TAG);
        if (fragment == null) {
            fragment = new LostFragment();
            fragmentManager.beginTransaction().add(android.R.id.content, fragment,
                    LostFragment.TAG).commit();
            fragment.setListAdapter(new LostAdapter(this));
            client = new LocationClient(this, callbacks);
            fragment.client = client;
        } else {
            client = fragment.client;
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplication());
    }

    @Override
    protected void onResume() {
        super.onResume();
        client.setMockMode(isMockModePrefEnabled());
        connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        disconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.reset:
                onResetOptionSelected();
                break;
            case R.id.settings:
                onSettingsOptionSelected();
                break;
        }

        return true;
    }

    private void onResetOptionSelected() {
        disconnect();
        reset();
        connect();
    }

    private void onSettingsOptionSelected() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private boolean isMockModePrefEnabled() {
        return sharedPreferences.getBoolean(getString(R.string.mock_mode_key), false);
    }

    private void connect() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                client.connect();
            }
        }, 300);
    }

    private void disconnect() {
        client.disconnect();
    }

    private void reset() {
        fragment.reset();
    }

    public static void populateLocationView(View view, Location location) {
        if (location == null) {
            clearLocationView(view);
            return;
        }

        final TextView provider = (TextView) view.findViewById(R.id.provider);
        final TextView coordinates = (TextView) view.findViewById(R.id.coordinates);
        final TextView accuracy = (TextView) view.findViewById(R.id.accuracy);
        final TextView speed = (TextView) view.findViewById(R.id.speed);
        final TextView time = (TextView) view.findViewById(R.id.time);

        provider.setText(location.getProvider() + " provider");
        coordinates.setText(String.format("%.4f, %.4f", location.getLatitude(),
                location.getLongitude()));
        accuracy.setText("within " + Math.round(location.getAccuracy()) + " meters");
        speed.setText(location.getSpeed() + " m/s");
        time.setText(new Date(location.getTime()).toString());
    }

    public static void clearLocationView(View view) {
        final TextView provider = (TextView) view.findViewById(R.id.provider);
        final TextView coordinates = (TextView) view.findViewById(R.id.coordinates);
        final TextView accuracy = (TextView) view.findViewById(R.id.accuracy);
        final TextView speed = (TextView) view.findViewById(R.id.speed);
        final TextView time = (TextView) view.findViewById(R.id.time);

        provider.setText("");
        coordinates.setText("");
        accuracy.setText("");
        speed.setText("");
        time.setText("");
    }

    /**
     * LOST Fragment
     */
    private static class LostFragment extends ListFragment {
        public static final String TAG = LostFragment.class.getSimpleName();

        private LocationClient client;
        private Location lastKnownLocation;
        private View headerView;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            final View view = inflater.inflate(R.layout.fragment_lost, container, false);
            final ListView listView = (ListView) view.findViewById(android.R.id.list);
            headerView = inflater.inflate(R.layout.list_header, null);
            listView.addHeaderView(headerView);
            listView.setHeaderDividersEnabled(false);
            if (lastKnownLocation != null) {
                populateLocationView(headerView, lastKnownLocation);
            }
            return view;
        }

        public void setLastKnownLocation(Location location) {
            lastKnownLocation = location;
            populateLocationView(headerView, location);
        }

        public void updateLocation(Location location) {
            final LostAdapter adapter = (LostAdapter) getListAdapter();
            adapter.addLocation(location);
            adapter.notifyDataSetChanged();
        }

        public void reset() {
            final LostAdapter adapter = (LostAdapter) getListAdapter();
            adapter.clear();
            clearLocationView(headerView);
            lastKnownLocation = null;
        }
    }

    /**
     * LOST Adapter
     */
    private static class LostAdapter extends BaseAdapter {
        private final ArrayList<Location> locations = new ArrayList<Location>();
        private final Context context;

        public LostAdapter(Context context) {
            this.context = context;
        }

        public void addLocation(Location location) {
            locations.add(location);
        }

        public void clear() {
            locations.clear();
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return locations.size();
        }

        @Override
        public Object getItem(int i) {
            return locations.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = View.inflate(context, R.layout.list_item, null);
            }

            populateLocationView(view, locations.get(i));
            return view;
        }
    }
}
