# LOST

[![Circle CI Build Status](https://circleci.com/gh/mapzen/LOST.png?circle-token=87063f053ef960fa184031157ec01aa5549fd4ce)](https://circleci.com/gh/mapzen/LOST)

Location Open Source Tracker for Android

# Usage

## FusedLocationProviderApi

LOST is a drop-in replacement for Google Play Services [FusedLocationProviderApi][1] that makes calls directly to the [LocationManger][2].

This project seeks to provide an open source alternative to the [Fused Location Provider][3] that depends only on the Android SDK. Operations supported at this time include getting the last known location and registering for location updates.

**Connecting to the LOST API Client**

When using LOST, [`GoogleApiClient`](https://developer.android.com/reference/com/google/android/gms/common/api/GoogleApiClient.html) is replaced by [`LostApiClient`](https://github.com/mapzen/LOST/blob/master/lost/src/main/java/com/mapzen/android/lost/api/LostApiClient.java). Connecting to LOST is even easier since there are no [`ConnectionCallbacks`](https://developer.android.com/reference/com/google/android/gms/common/api/GoogleApiClient.ConnectionCallbacks.html) or [`OnConnectionFailedListener`](https://developer.android.com/reference/com/google/android/gms/common/api/GoogleApiClient.OnConnectionFailedListener.html) objects to manage.

```java
LostApiClient lostApiClient = new LostApiClient.Builder(this).build();
lostApiClient.connect();
```

LOST instantly connects to the [`LocationManager`](https://developer.android.com/reference/android/location/LocationManager.html) and can immediately retrieve that last known location or begin sending location updates.

**Getting the Last Known Location**

Once connected you can request the last known location. The actual logic to determine the best most recent location is based this classic [blog post by Reto Meier](http://android-developers.blogspot.com/2011/06/deep-dive-into-location.html).


```java
Location location = LocationServices.FusedLocationApi.getLastLocation();
if (location != null) {
  // Do stuff
}
```

**Requesting Location Updates**

LOST also provides the ability to request ongoing location updates. You can specify the update interval, minimum displacement, and priority. The priority determines which location providers will be activated.

```java
LocationRequest request = LocationRequest.create()
    .setInterval(5000)
    .setSmallestDisplacement(10)
    .setPriority(LocationRequest.PRIORITY_LOW_POWER);

LocationListener listener = new LocationListener() {
  @Override
  public void onLocationChanged(Location location) {
    // Do stuff
  }
};

LocationServices.FusedLocationApi.requestLocationUpdates(request, listener);
```

Currently location updates can only be requested with a [`LocationListener`](https://developer.android.com/reference/com/google/android/gms/location/LocationListener.html) object. In the future we are planning to add location updates via a [`PendingIntent`](http://developer.android.com/reference/android/app/PendingIntent.html) as well.

**Mock Locations**

With LOST you can mock not just individual locations but also entire routes. By loading a [GPX trace file](http://www.topografix.com/gpx.asp) onto the device you can configure LOST to replay locations from the trace file including latitude, longitude, speed, and bearing.

**Mocking a single location**

To mock a single location with LOST you must first enable mock mode. Then you simply create a mock location object and pass it to the API.

```java
Location mockLocation = new Location("mock");
mockLocation.setLatitude(40.7484);
mockLocation.setLongitude(-73.9857);
LocationServices.FusedLocationApi.setMockMode(true);
LocationServices.FusedLocationApi.setMockLocation(mockLocation);
```

The mock location object you set will be immediately returned to all registered listeners and will be returned the next time `getLastLocation()` is called.

**Mocking an entire route**

To mock an entire route you must first transfer a [GPX trace file](http://www.topografix.com/gpx.asp) to the device using [adb](http://developer.android.com/tools/help/adb.html). Sample GPX traces can be found on the [public GPS traces page](http://www.openstreetmap.org/traces) for OpenStreetMap. Once the trace file is loaded on the device you can tell LOST to replay the locations in the trace at the requested update interval.

```java
File file = new File(Environment.getExternalStorageDirectory(), "mock_track.gpx");
LocationServices.FusedLocationApi.setMockMode(true);
LocationServices.FusedLocationApi.setMockTrace(file);
```

For more in-depth examples, please refer to the [sample application](https://github.com/mapzen/LOST/tree/master/lost-sample).

## SettingsApi

This api is a drop in replacement for Google Play Services' corresponding [SettingsApi] [5]. It can be used to check whether location and bluetooth settings are satisfied as well as provide a mechanism for resolving unsatisfied settings.

First create and connect a `LostApiClient` for use:
```java
LostApiClient apiClient = new LostApiClient.Builder(this).build();
apiClient.connect();
```

Next, create a `LocationSettingsRequest` specifying the location priority and whether or not the user needs BLE:
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
@Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      case REQUEST_CHECK_SETTINGS:
        // Check the location settings again and continue
        break;
      default:
        break;
    }
  }
```

When using this API, you must declare that your app uses the Bluetooth permission:
```java
<uses-permission android:name="android.permission.BLUETOOTH"/>
```

And if your app requires Bluetooth, you must also request the Bluetooth admin permission:
```java
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
```

# Install

**Download Jar**

Download the [latest JAR][4].

**Maven**

Include dependency using Maven.

```xml
<dependency>
  <groupId>com.mapzen.android</groupId>
  <artifactId>lost</artifactId>
  <version>1.1.1</version>
</dependency>
```

**Gradle**

Include dependency using Gradle.

```groovy
compile 'com.mapzen.android:lost:1.1.1'
```

[1]: https://developer.android.com/reference/com/google/android/gms/location/FusedLocationProviderApi.html
[2]: https://developer.android.com/reference/android/location/LocationManager.html
[3]: http://developer.android.com/google/play-services/location.html
[4]: http://search.maven.org/remotecontent?filepath=com/mapzen/android/lost/1.1.1/lost-1.1.1.jar
[5]: https://developers.google.com/android/reference/com/google/android/gms/location/SettingsApi