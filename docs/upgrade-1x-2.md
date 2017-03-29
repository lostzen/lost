# Upgrading from Lost 1.x

### Add Connection Callbacks
Lost 2.0 introduces an underlying `Service` in the `FusedLocationProviderApi`. Because of this, connecting a `LostApiClient` requires that developers add `ConnectionCallbacks` so that they know when the client has connected and is ready to use.

1.x:
```java
LostApiClient client = new LostApiClient.Builder(context).addConnectionCallbacks(callbacks).build();
client.connect(); // Client is ready for use
```

2.0:
```java
LostApiClient.ConnectionCallbacks callbacks = new LostApiClient.ConnectionCallbacks() {
    @Override public void onConnected() {
        // Client is ready for use
    }

    @Override public void onConnectionSuspended() {

    }
};
LostApiClient client = new LostApiClient.Builder(context).addConnectionCallbacks(callbacks).build();
client.connect(); // Client is NOT ready for use
```

### Explicitly Request Permissions
We have removed all permissions from Lost's `AndroidManifest.xml`, allowing developers to declare only the permissions they need in their client applications.

In addition, developers targeting Android M (API 23) and above also need to [request runtime permissions](https://developer.android.com/training/permissions/requesting.html).

The permissions that Lost requires are as follows:

### FusedLocationProviderApi & GeofencingApi
```xml
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
```

### SettingsApi
```xml
<uses-permission android:name="android.permission.BLUETOOTH"/>
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
```

### Limitations

#### Multiple Processes
Currently LOST 2.x does not support being run in multiple processes due to the way we bind to the underlying service. This will be resolved in the near future and can be tracked in [this issue](https://github.com/mapzen/lost/issues/173)
