package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationListener;

import android.location.Location;

import java.util.ArrayList;
import java.util.List;

public class TestLocationListener implements LocationListener {
  private ArrayList<Location> locations = new ArrayList<>();
  private boolean isGpsEnabled = true;
  private boolean isNetworkEnabled = true;

  @Override public void onLocationChanged(Location location) {
    locations.add(location);
  }

  public List<Location> getAllLocations() {
    return locations;
  }

  public Location getMostRecentLocation() {
    return locations.get(locations.size() - 1);
  }

  public void setIsGpsEnabled(boolean enabled) {
    isGpsEnabled = enabled;
  }

  public boolean getIsGpsEnabled() {
    return isGpsEnabled;
  }

  public void setIsNetworkEnabled(boolean enabled) {
    isNetworkEnabled = enabled;
  }

  public boolean getIsNetworkEnabled() {
    return isNetworkEnabled;
  }
}
