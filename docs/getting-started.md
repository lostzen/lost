# Getting Started

When using Lost, [`GoogleApiClient`](https://developer.android.com/reference/com/google/android/gms/common/api/GoogleApiClient.html) is replaced by [`LostApiClient`](https://github.com/mapzen/lost/blob/master/lost/src/main/java/com/mapzen/android/lost/api/LostApiClient.java).

## Create and connect a LostApiClient

```java
LostApiClient lostApiClient = new LostApiClient.Builder(this).addConnectionCallbacks(this).build();
lostApiClient.connect();

@Override public void onConnected() {
  //client is ready to for use
}

@Override public void onConnectionSuspended() {

}
```
