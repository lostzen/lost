# Getting the Last Known Location

Once the `LostApiClient` is connected you can request the last known location.

The actual logic to determine the best most recent location is based this classic [blog post by Reto Meier](http://android-developers.blogspot.com/2011/06/deep-dive-into-location.html).

```java
Location location = LocationServices.FusedLocationApi.getLastLocation(lostApiClient);
if (location != null) {
  // Do stuff
}
```
