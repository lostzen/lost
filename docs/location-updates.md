# Requesting Location Updates

Lost provides the ability to request ongoing location updates. You can specify the update interval, minimum displacement, and priority. The priority determines which location providers will be activated.

```java
LocationRequest request = LocationRequest.create()
    .setPriority(LocationRequest.PRIORITY_LOW_POWER)
    .setInterval(5000)
    .setSmallestDisplacement(10);

LocationListener listener = new LocationListener() {
  @Override
  public void onLocationChanged(Location location) {
    // Do stuff
  }
};

LocationServices.FusedLocationApi.requestLocationUpdates(lostApiClient, request, listener);
```

For situations where you want to receive location updates in the background, you can request location updates using the `PendingIntent` API.

```java
LocationRequest request = LocationRequest.create()
    .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    .setInterval(100);

Intent intent = new Intent(MyIntentService.ACTION);
PendingIntent pendingIntent = PendingIntent.getService(MyActivity.this, requestCode, intent, flags);
LocationServices.FusedLocationApi.requestLocationUpdates(lostApiClient, request, pendingIntent);
```
