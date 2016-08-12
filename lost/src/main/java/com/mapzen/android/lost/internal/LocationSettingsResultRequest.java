package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LocationSettingsRequest;
import com.mapzen.android.lost.api.LocationSettingsResult;
import com.mapzen.android.lost.api.LocationSettingsStates;
import com.mapzen.android.lost.api.PendingResult;
import com.mapzen.android.lost.api.ResultCallback;
import com.mapzen.android.lost.api.Status;

import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.mapzen.android.lost.api.Status.*;

public class LocationSettingsResultRequest extends PendingResult<LocationSettingsResult> {

  private final Context context;
  private final LocationSettingsRequest settingsRequest;
  private ResultCallback<? super LocationSettingsResult> resultCallback;
  private final LocationManager locationManager;

  AsyncTask locationResultTask = new AsyncTask() {

    FutureTask<LocationSettingsResult> locationSettingTask = new FutureTask(new Callable() {
      @Override public Object call() throws Exception {
        return generateLocationSettingsResult();
      }
    });

    @Override protected Object doInBackground(Object[] params) {
      long time = -1;
      TimeUnit timeUnit = TimeUnit.MILLISECONDS;
      if (params != null) {
        time = (long) params[0];
        timeUnit = (TimeUnit) params[1];
      }

      LocationSettingsResult result;
      try {
        if (params != null) {
          result = locationSettingTask.get(time, timeUnit);
        } else {
          result = locationSettingTask.get();
        }
      } catch (InterruptedException e) {
        if (isCancelled()) {
          result = createResultForStatus(CANCELLED);
        } else {
          result = createResultForStatus(INTERRUPTED);
        }
      } catch (ExecutionException e) {
        result = createResultForStatus(INTERNAL_ERROR);
      } catch (TimeoutException e) {
        result = createResultForStatus(TIMEOUT);
      }
      postLocationSettingsResult(result);
      return null;
    }

    @Override protected void onCancelled() {
      super.onCancelled();
      locationSettingTask.cancel(true);
    }
  };

  public LocationSettingsResultRequest(Context ctx, LocationSettingsRequest request) {
    context = ctx;
    settingsRequest = request;
    locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
  }

  @NonNull @Override public LocationSettingsResult await() {
    return generateLocationSettingsResult();
  }

  @NonNull @Override public LocationSettingsResult await(long time, @NonNull TimeUnit timeUnit) {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Future<LocationSettingsResult> future = executor.submit(new Callable() {
      public LocationSettingsResult call() throws Exception {
        return generateLocationSettingsResult();
      }
    });
    LocationSettingsResult result;
    try {
      result = future.get(time, timeUnit);
    } catch (TimeoutException e) {
      result = createResultForStatus(TIMEOUT);
    } catch (InterruptedException e) {
      result = createResultForStatus(INTERRUPTED);
    } catch (ExecutionException e) {
      result = createResultForStatus(INTERNAL_ERROR);
    }
    executor.shutdownNow();

    return result;
  }

  private LocationSettingsResult createResultForStatus(int statusType) {
    Status status = new Status(statusType);
    return new LocationSettingsResult(status, null);
  }

  @Override public void cancel() {
    if (locationResultTask != null && !locationResultTask.isCancelled()) {
      locationResultTask.cancel(true);
    }
  }


  @Override public boolean isCanceled() {
    return locationResultTask.isCancelled();
  }

  @Override
  public void setResultCallback(@NonNull ResultCallback<? super LocationSettingsResult> callback) {
    resultCallback = callback;
    locationResultTask.execute();
  }

  @Override
  public void setResultCallback(@NonNull ResultCallback<? super LocationSettingsResult> callback,
      long time, @NonNull TimeUnit timeUnit) {
    resultCallback = callback;
    locationResultTask.execute(time, timeUnit);
  }

  private LocationSettingsResult generateLocationSettingsResult() {
    boolean needGps = false;
    boolean needNetwork = false;
    for (LocationRequest request : settingsRequest.getLocationRequests()) {
      switch (request.getPriority()) {
        case LocationRequest.PRIORITY_HIGH_ACCURACY:
          needGps = true;
          needNetwork = true;
          break;
        case LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY:
        case LocationRequest.PRIORITY_LOW_POWER:
          needNetwork = true;
          break;
      }
    }
    boolean needBle = settingsRequest.getNeedBle();

    PackageManager pm = context.getPackageManager();
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    boolean gpsUsable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    boolean gpsPresent = pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
    boolean networkUsable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    boolean networkPresent = pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK);
    boolean bleUsable = bluetoothAdapter.isEnabled();
    boolean blePresent = pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);

    boolean hasGpsResolution = needGps && gpsPresent && !gpsUsable;
    boolean hasNetworkResolution = needNetwork && networkPresent && !networkUsable;
    boolean hasBleResolution = needBle && blePresent && !bleUsable;
    boolean hasResolution = hasGpsResolution || hasNetworkResolution || hasBleResolution;

    boolean gpsResolutionUnavailable = needGps && !gpsPresent;
    boolean networkResolutionUnavailable = needNetwork && !networkPresent;
    boolean bleResolutionUnavailable = needBle && !blePresent;
    boolean resolutionUnavailable = gpsResolutionUnavailable || networkResolutionUnavailable
        || bleResolutionUnavailable;

    final Status status;
    if (hasResolution) {
      Intent intent;
      if (hasBleResolution) {
        intent = new Intent(context, ResolveLocationActivity.class);
      } else {
        intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
      }
      PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
      status = new Status(RESOLUTION_REQUIRED, pendingIntent);
    } else if(resolutionUnavailable) {
      status = new Status(SETTINGS_CHANGE_UNAVAILABLE);
    } else {
      status = new Status(SUCCESS);
    }
    final LocationSettingsStates states = new LocationSettingsStates(gpsUsable,networkUsable,
        bleUsable, gpsPresent, networkPresent, blePresent);
    return new LocationSettingsResult(status, states);
  }

  private void postLocationSettingsResult(final LocationSettingsResult result) {
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        resultCallback.onResult(result);
      }
    };
    new Handler(context.getMainLooper()).post(runnable);
  }
}
