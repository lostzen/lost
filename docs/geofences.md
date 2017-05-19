# Creating and Monitoring Geofences

With Lost you can create `Geofence`s to monitor when a user enters or exits an area and get notifications when this occurs.

First create a `Geofence` object:
```java
Geofence geofence = new Geofence.Builder()
        .setRequestId(requestId)
        .setCircularRegion(latitude, longitude, radius)
        .setExpirationDuration(NEVER_EXPIRE)
        .build();
```

Then create a `GeofencingRequest` from all relevant `Geofences`:

```java
GeofencingRequest request = new GeofencingRequest.Builder()
        .addGeofence(geofence)
        .build();
```

After this is done, you can create a `PendingIntent` to fire when the user enters/exits the `Geofence`. Usually this will send an `Intent` to an `IntentService`:
```java
Intent serviceIntent = new Intent(getApplicationContext(), GeofenceIntentService.class);
PendingIntent pendingIntent = PendingIntent.getService(this, 0, serviceIntent, 0);
```

Finally, invoke the `GeofencingApi` method to register for updates:

```java
LocationServices.GeofencingApi.addGeofences(client, request, pendingIntent);
```

Your `IntentService` will continue to receive updates as the user enters/exits the relevant `Geofences` until you remove the `PendingIntent`.
