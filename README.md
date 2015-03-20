# LOST

[![Circle CI Build Status](https://circleci.com/gh/mapzen/LOST.png?circle-token=87063f053ef960fa184031157ec01aa5549fd4ce)](https://circleci.com/gh/mapzen/LOST)

Location Open Source Tracker for Android

## Usage

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


## Install

**Download Jar**

Download the [latest JAR][4].

**Maven**

Include dependency using Maven.

```xml
<dependency>
  <groupId>com.mapzen.android</groupId>
  <artifactId>lost</artifactId>
  <version>1.0.1</version>
</dependency>
```

**Gradle**

Include dependency using Gradle.

```groovy
compile 'com.mapzen.android:lost:1.0.1'
```

[1]: https://developer.android.com/reference/com/google/android/gms/location/FusedLocationProviderApi.html
[2]: https://developer.android.com/reference/android/location/LocationManager.html
[3]: http://developer.android.com/google/play-services/location.html
[4]: http://search.maven.org/remotecontent?filepath=com/mapzen/android/lost/1.0.1/lost-1.0.1.jar
