#Location Settings

Location and bluetooth settings can be checked to see if the requirements are satisfied for a `LocationRequest`. In addition, a mechanism for resolving unsatisfied settings is provided.

After creating a `LostApiClient`, create a `LocationSettingsRequest` specifying the location priority and whether or not the user needs BLE:
```java
ArrayList<LocationRequest> requests = new ArrayList<>();
LocationRequest highAccuracy = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
requests.add(highAccuracy);

boolean needBle = false;
LocationSettingsRequest request = new LocationSettingsRequest.Builder()
          .addAllLocationRequests(requests)
          .setNeedBle(needBle)
          .build();
```

Then, use the `SettingsApi` to get a `PendingResult`:
```java
PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(apiClient, request);
```

Once you have a `PendingResult`, invoke either `await()` or `setResultCallback(ResultCallback)` to obtain a `LocationSettingsResult`. With this object, access the `LocationSettingsStates` to determine whether or not location settings have been satisfied:
```java
private static final int REQUEST_CHECK_SETTINGS = 100;
LocationSettingsResult locationSettingsResult = result.await();
LocationSettingsStates states = locationSettingsResult.getLocationSettingsStates();
Status status = locationSettingsResult.getStatus();
    switch (status.getStatusCode()) {
      case Status.SUCCESS:
        // All location and BLE settings are satisfied. The client can initialize location
        // requests here.
        break;
      case Status.RESOLUTION_REQUIRED:
        // Location settings are not satisfied but can be resolved by show the user the Location Settings activity
        status.startResolutionForResult(SettingsApiActivity.this, REQUEST_CHECK_SETTINGS);
        break;
      case Status.INTERNAL_ERROR:
      case Status.INTERRUPTED:
      case Status.TIMEOUT:
      case Status.CANCELLED:
        // Location settings are not satisfied but cannot be resolved
        break;
      default:
        break;
    }
```

If the status code is `RESOLUTION_REQUIRED`, the client can call `startResolutionForResult(Activity, int)` to bring up an `Activity`, asking for user's permission to modify the location settings to satisfy those requests. The result of the `Activity` will be returned via `onActivityResult(int, int, Intent)`.
```java
@Override  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      case REQUEST_CHECK_SETTINGS:
        // Check the location settings again and continue
        break;
      default:
        break;
      }
    }
```
