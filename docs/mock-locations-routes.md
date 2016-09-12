#Mock Locations

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
