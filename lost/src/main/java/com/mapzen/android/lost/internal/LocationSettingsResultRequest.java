package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LocationSettingsRequest;
import com.mapzen.android.lost.api.LocationSettingsResult;
import com.mapzen.android.lost.api.LocationSettingsStates;
import com.mapzen.android.lost.api.PendingResult;
import com.mapzen.android.lost.api.ResultCallback;
import com.mapzen.android.lost.api.Status;

import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.support.annotation.NonNull;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.mapzen.android.lost.api.Status.INTERNAL_ERROR;
import static com.mapzen.android.lost.api.Status.INTERRUPTED;
import static com.mapzen.android.lost.api.Status.RESOLUTION_REQUIRED;
import static com.mapzen.android.lost.api.Status.SETTINGS_CHANGE_UNAVAILABLE;
import static com.mapzen.android.lost.api.Status.SUCCESS;
import static com.mapzen.android.lost.api.Status.TIMEOUT;

class LocationSettingsResultRequest extends PendingResult<LocationSettingsResult> {

  private final PackageManager packageManager;
  private final LocationManager locationManager;
  private final PendingIntentGenerator pendingIntentGenerator;
  private final LocationSettingsRequest settingsRequest;
  private ResultCallback<? super LocationSettingsResult> resultCallback;
  private Future<LocationSettingsResult> future;
  private SettingsDialogDisplayer dialogDisplayer = new SettingsDialogDisplayer();

  LocationSettingsResultRequest(Context context, PendingIntentGenerator generator,
      LocationSettingsRequest request) {
    packageManager = context.getPackageManager();
    locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    pendingIntentGenerator = generator;
    settingsRequest = request;
  }

  @NonNull @Override public LocationSettingsResult await() {
    return generateLocationSettingsResult();
  }

  @NonNull @Override public LocationSettingsResult await(long time, @NonNull TimeUnit timeUnit) {
    return generateLocationSettingsResult(time, timeUnit);
  }

  @Override public void cancel() {
    if (future != null && !future.isCancelled()) {
      future.cancel(true);
    }
  }

  @Override public boolean isCanceled() {
    if (future == null) {
      return false;
    }
    return future.isCancelled();
  }

  @Override
  public void setResultCallback(@NonNull ResultCallback<? super LocationSettingsResult> callback) {
    resultCallback = callback;
    LocationSettingsResult result = generateLocationSettingsResult();
    resultCallback.onResult(result);
  }

  @Override
  public void setResultCallback(@NonNull ResultCallback<? super LocationSettingsResult> callback,
      long time, @NonNull TimeUnit timeUnit) {
    resultCallback = callback;
    LocationSettingsResult result = generateLocationSettingsResult(time, timeUnit);
    resultCallback.onResult(result);
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
        default:
          break;
      }
    }
    boolean needBle = settingsRequest.getNeedBle();

    boolean gpsUsable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    boolean gpsPresent = packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
    boolean networkUsable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    boolean networkPresent =
        packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK);

    boolean bleUsable = networkUsable;
    boolean blePresent = packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);

    boolean hasGpsResolution = needGps && gpsPresent && !gpsUsable;
    boolean hasNetworkResolution = needNetwork && networkPresent && !networkUsable;
    boolean hasBleResolution = needBle && blePresent && !bleUsable;
    boolean hasResolution = hasGpsResolution || hasNetworkResolution || hasBleResolution;

    boolean gpsResolutionUnavailable = needGps && !gpsPresent;
    boolean networkResolutionUnavailable = needNetwork && !networkPresent;
    boolean bleResolutionUnavailable = needBle && !blePresent;
    boolean resolutionUnavailable =
        gpsResolutionUnavailable || networkResolutionUnavailable || bleResolutionUnavailable;

    final Status status;
    if (hasResolution) {
      PendingIntent pendingIntent = pendingIntentGenerator.generatePendingIntent();
      status = new Status(RESOLUTION_REQUIRED, dialogDisplayer, pendingIntent);
    } else if (resolutionUnavailable) {
      status = new Status(SETTINGS_CHANGE_UNAVAILABLE, dialogDisplayer);
    } else {
      status = new Status(SUCCESS, dialogDisplayer);
    }
    final LocationSettingsStates states =
        new LocationSettingsStates(gpsUsable, networkUsable, bleUsable, gpsPresent, networkPresent,
            blePresent);
    return new LocationSettingsResult(status, states);
  }

  private LocationSettingsResult generateLocationSettingsResult(long time, TimeUnit timeUnit) {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    future = executor.submit(new Callable() {
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
    Status status = new Status(statusType, dialogDisplayer);
    return new LocationSettingsResult(status, null);
  }
}
