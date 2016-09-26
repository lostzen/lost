# Lost

Location Open Source Tracker (for Android)

[![Circle CI Build Status](https://circleci.com/gh/mapzen/lost.png?circle-token=87063f053ef960fa184031157ec01aa5549fd4ce)](https://circleci.com/gh/mapzen/lost)

# Usage

Lost is a drop-in replacement for Google Play Services that makes calls directly to the [LocationManger][1].

This project seeks to provide an open source alternative to the [Google APIs][2] that depends only on the Android SDK. It provides 1:1 replacements for the [FusedLocationProviderApi][3], [GeofencingApi][4], and [SettingsApi][5].

Set up:
- [Installation](https://github.com/mapzen/lost/blob/master/docs/installation.md)
- [Getting Started](https://github.com/mapzen/lost/blob/master/docs/getting-started.md)
- [Getting the Last Known Location](https://github.com/mapzen/lost/blob/master/docs/last-known-location.md)
- [Receiving Location Updates](https://github.com/mapzen/lost/blob/master/docs/location-updates.md)
- [Mock Locations & Routes](https://github.com/mapzen/lost/blob/master/docs/mock-locations-routes.md)
- [Changing Location Settings](https://github.com/mapzen/lost/blob/master/docs/location-settings.md)
- [Creating and Monitoring Geofences](https://github.com/mapzen/lost/blob/master/docs/geofences.md)

# Sample
For a working example please refer to the [Sample App](https://github.com/mapzen/lost/tree/master/lost-sample).

[1]: https://developer.android.com/reference/android/location/LocationManager.html
[2]: http://developer.android.com/google/play-services/location.html
[3]: https://developer.android.com/reference/com/google/android/gms/location/FusedLocationProviderApi.html
[4]: https://developers.google.com/android/reference/com/google/android/gms/location/GeofencingApi.html
[5]: https://developers.google.com/android/reference/com/google/android/gms/location/SettingsApi.html
