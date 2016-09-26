#Requesting Location Updates

Lost provides the ability to request ongoing location updates. You can specify the update interval, minimum displacement, and priority. The priority determines which location providers will be activated.

```java
LocationRequest request = LocationRequest.create().setInterval(5000).setSmallestDisplacement(10).setPriority(LocationRequest.PRIORITY_LOW_POWER);
LocationListener listener = new LocationListener() {
    @Override
    public void onLocationChanged(Location location) {
      // Do stuff
    }
};

LocationServices.FusedLocationApi.requestLocationUpdates(request, listener);
```

For situations where you want to receive location updates in the background, you can request location updates using the `PendingIntent` API

```java
LocationRequest request = LocationRequest.create();
request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
request.setInterval(100);

Intent intent = new Intent(PendingIntentService.ACTION);
PendingIntent pendingIntent = PendingIntent.getService(PendingIntentActivity.this, 1,intent, 0);
LocationServices.FusedLocationApi.requestLocationUpdates(client, request, pendingIntent);
```
