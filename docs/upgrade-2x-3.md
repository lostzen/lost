# Upgrading from Lost 2.x

### Explicitly Unregister Location Updates
`LocationRequest`s are no longer automatically removed when a client disconnects, therefore you should ensure that you unregister all requests registered:

2.x
```
// onConnected

LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, listener);

client.disconnect();
```

3.x
```
// onConnected

LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, listener);

// when disconnecting and shutting down client
LocationServices.FusedLocationApi.removeLocationUpdates(client, listener); // NOW REQUIRED

client.disconnect();
```

### Remove Deprecated Methods
`onProviderDisabled` and `onProviderEnabled` have been removed from `LocationListener`:

2.x
```
LocationListener listener = new LocationListener() {
    @Override public void onLocationChanged(Location location) {

    }

    @Override public void onProviderDisabled(Location location) {

    }

    @Override public void onProviderEnabled(Location location) {

    }
  };
```

3.x
```
LocationListener listener = new LocationListener() {
    @Override public void onLocationChanged(Location location) {

    }
  };
```

`isProviderEnabled` has been removed from `FusedLocationProviderApi` in favor of `SettingsApi#checkLocationSettings(LostApiClient, LocationSettingsRequest)`

2.x
```
boolean enabled = LocationServices.FusedLocationProviderApi.isProviderEnabled(client, GPS_PROVIDER);
```

3.x
```
PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(apiClient, request);

LocationSettingsResult locationSettingsResult = result.await();

LocationSettingsStates states = locationSettingsResult.getLocationSettingsStates();

boolean enabled = states.isGpsUsable();
```

### Ensure Application Process

The underlying Lost `Service` now runs in it's own process. Because of this, `Application` subclasses will receive two calls to `onCreate`(https://developer.android.com/reference/android/app/Application.html#onCreate()). To ensure unnecessary initialization isn't done, you should use the current `Context` to determine if `onCreate` is being called for the `Service` and if so, handle flow accordingly.

LeakCanary(https://github.com/square/leakcanary) uses this method:

```
public static boolean isInServiceProcess(Context context, Class<? extends Service> serviceClass) {
    PackageManager packageManager = context.getPackageManager();
    PackageInfo packageInfo;
    try {
      packageInfo = packageManager.getPackageInfo(context.getPackageName(), GET_SERVICES);
    } catch (Exception e) {
      CanaryLog.d(e, "Could not get package info for %s", context.getPackageName());
      return false;
    }
    String mainProcess = packageInfo.applicationInfo.processName;

    ComponentName component = new ComponentName(context, serviceClass);
    ServiceInfo serviceInfo;
    try {
      serviceInfo = packageManager.getServiceInfo(component, 0);
    } catch (PackageManager.NameNotFoundException ignored) {
      // Service is disabled.
      return false;
    }

    if (serviceInfo.processName.equals(mainProcess)) {
      CanaryLog.d("Did not expect service %s to run in main process %s", serviceClass, mainProcess);
      // Technically we are in the service process, but we're not in the service dedicated process.
      return false;
    }

    int myPid = android.os.Process.myPid();
    ActivityManager activityManager =
        (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    ActivityManager.RunningAppProcessInfo myProcess = null;
    List<ActivityManager.RunningAppProcessInfo> runningProcesses =
        activityManager.getRunningAppProcesses();
    if (runningProcesses != null) {
      for (ActivityManager.RunningAppProcessInfo process : runningProcesses) {
        if (process.pid == myPid) {
          myProcess = process;
          break;
        }
      }
    }
    if (myProcess == null) {
      CanaryLog.d("Could not find running process for %d", myPid);
      return false;
    }

    return myProcess.processName.equals(serviceInfo.processName);
  }
```
