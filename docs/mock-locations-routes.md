# Mock Locations

With Lost you can mock not just individual locations but also entire routes.

By loading a [GPX trace file](http://www.topografix.com/gpx.asp) onto the device you can configure Lost to replay locations from the trace file including latitude, longitude, speed, and bearing.

## Mocking a single location
To mock a single location with Lost you must first enable mock mode. Then create a mock location object and pass it to the API.

```java
Location mockLocation = new Location("mock");
mockLocation.setLatitude(40.7484);
mockLocation.setLongitude(-73.9857);

LocationServices.FusedLocationApi.setMockMode(lostApiClient, true);
LocationServices.FusedLocationApi.setMockLocation(lostApiClient, mockLocation);
```

The mock location object you set will be immediately returned to all registered listeners and will be returned the next time `getLastLocation(lostApiClient)` is called.

## Mocking an entire route
To mock an entire route you must first transfer a [GPX trace file](http://www.topografix.com/gpx.asp) to the device using [adb](http://developer.android.com/tools/help/adb.html). Sample GPX traces can be found on the [public GPS traces page](http://www.openstreetmap.org/traces) for OpenStreetMap.

### Install GPX File
To install your gpx file onto your device, run the `install-gpx-trace.sh` script provided.

Example:

```bash
scripts/install-gpx-trace.sh lost.gpx com.example.myapp
```

Once the trace file is loaded on the device you can tell Lost to replay the locations in the trace by issuing a `LocationRequest`. Mock locations will be emitted according to the fastest interval value.

```java
File file = new File(context.getExternalFilesDir(null), "lost.gpx");
LocationServices.FusedLocationApi.setMockMode(lostApiClient, true);
LocationServices.FusedLocationApi.setMockTrace(lostApiClient, file);
LocationRequest locationRequest = LocationRequest.create()
    .setInterval(1000)
    .setFastestInterval(1000) // Mock locations will replay at this interval.
    .setSmallestDisplacement(0)
    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
FusedLocationApi.requestLocationUpdates(lostApiClient, locationRequest, locationListener);
```
