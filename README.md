# LOST

[![Circle CI Build Status](https://circleci.com/gh/mapzen/LOST.png?circle-token=87063f053ef960fa184031157ec01aa5549fd4ce)](https://circleci.com/gh/mapzen/LOST)

Location Open Source Tracker for Android

## Usage

LOST is a drop-in replacement for Google Play Services [LocationClient][1] that makes calls directly to the [LocationManger][2].

This project seeks to provide an open source alternative to the [Fused Location Provider][3] that depends only on the Android SDK. Operations supported at this time include getting the last known location and registering for location updates.

## Install

### Download Jar

Download the [latest JAR][4].

### Maven

Include dependency using Maven.

```xml
<dependency>
  <groupId>com.mapzen.android</groupId>
  <artifactId>lost</artifactId>
  <version>0.1</version>
</dependency>
```

### Gradle

Include dependency using Gradle.

```groovy
compile 'com.mapzen.android:lost:0.1'
```

[1]: https://developer.android.com/reference/com/google/android/gms/location/LocationClient.html
[2]: https://developer.android.com/reference/android/location/LocationManager.html
[3]: http://developer.android.com/google/play-services/location.html
[4]: http://search.maven.org/remotecontent?filepath=com/mapzen/android/lost/0.1/lost-0.1.jar
