#Upgrading from Lost 1.x

##Add Connection Callbacks
Lost 2.0 introduces an underlying `Service` in the `FusedLocationProviderApi`. Because of this, connecting a `LostApiClient` requires that developers add `ConnectionCallbacks` so that they know when the client has connected and is ready to use.

1.x:
```java
LostApiClient client = new LostApiClient.Builder(context).addConnectionCallbacks(callbacks).build();
client.connect(); //Client is ready for use
```

2.0:
```java
LostApiClient.ConnectionCallbacks callbacks = new LostApiClient.ConnectionCallbacks() {
    @Override public void onConnected() {
        //Client is ready for use
    }

    @Override public void onConnectionSuspended() {

    }
};
LostApiClient client = new LostApiClient.Builder(context).addConnectionCallbacks(callbacks).build();
client.connect(); //Client is NOT ready for use
```
