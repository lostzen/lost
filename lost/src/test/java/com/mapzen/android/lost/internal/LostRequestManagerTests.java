package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LocationCallback;
import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LostApiClient;

import org.junit.Before;
import org.junit.Test;

import android.app.PendingIntent;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class LostRequestManagerTests {

  LostRequestManager requestManager;

  @Before
  public void setup() {
    requestManager = new LostRequestManager();
  }

  @Test
  public void requestLocationUpdates_clientListener_oneRequest_shouldUpdateMap() {
    LostApiClient client = mock(LostApiClient.class);
    LocationRequest request = LocationRequest.create();
    LocationListener listener = mock(LocationListener.class);
    requestManager.requestLocationUpdates(client, request, listener);

    assertThat(requestManager.getClientCallbackMap().size()).isEqualTo(1);
  }

  @Test
  public void requestLocationUpdates_clientListener_twoRequests_shouldUpdateMap() {
    LostApiClient client = mock(LostApiClient.class);
    LocationRequest request = LocationRequest.create();
    LocationListener listener = mock(LocationListener.class);
    requestManager.requestLocationUpdates(client, request, listener);
    LocationRequest anotherRequest = LocationRequest.create();
    requestManager.requestLocationUpdates(client, anotherRequest, listener);

    assertThat(requestManager.getClientCallbackMap().size()).isEqualTo(1);

    ClientCallbackWrapper wrapper = new ClientCallbackWrapper(client, listener);
    assertThat(requestManager.getClientCallbackMap().get(wrapper)).contains(request);
    assertThat(requestManager.getClientCallbackMap().get(wrapper)).contains(anotherRequest);
  }

  @Test
  public void requestLocationUpdates_twoClientListeners_twoRequests_shouldUpdateMap() {
    LostApiClient client = mock(LostApiClient.class);
    LocationRequest request = LocationRequest.create();
    LocationListener listener = mock(LocationListener.class);
    requestManager.requestLocationUpdates(client, request, listener);
    LostApiClient anotherClient = mock(LostApiClient.class);
    LocationRequest anotherRequest = LocationRequest.create();
    requestManager.requestLocationUpdates(anotherClient, anotherRequest, listener);

    assertThat(requestManager.getClientCallbackMap().size()).isEqualTo(2);

    ClientCallbackWrapper wrapper = new ClientCallbackWrapper(client, listener);
    assertThat(requestManager.getClientCallbackMap().get(wrapper).size()).isEqualTo(1);
    assertThat(requestManager.getClientCallbackMap().get(wrapper)).contains(request);

    ClientCallbackWrapper anotherWrapper = new ClientCallbackWrapper(anotherClient, listener);
    assertThat(requestManager.getClientCallbackMap().get(anotherWrapper).size()).isEqualTo(1);
    assertThat(requestManager.getClientCallbackMap().get(anotherWrapper)).contains(anotherRequest);
  }

  @Test
  public void requestLocationUpdates_clientPendingIntent_oneRequest_shouldUpdateMap() {
    LostApiClient client = mock(LostApiClient.class);
    LocationRequest request = LocationRequest.create();
    PendingIntent intent = mock(PendingIntent.class);
    requestManager.requestLocationUpdates(client, request, intent);

    assertThat(requestManager.getClientCallbackMap().size()).isEqualTo(1);
  }

  @Test
  public void requestLocationUpdates_clientPendingIntent_twoRequests_shouldUpdateMap() {
    LostApiClient client = mock(LostApiClient.class);
    LocationRequest request = LocationRequest.create();
    PendingIntent intent = mock(PendingIntent.class);
    requestManager.requestLocationUpdates(client, request, intent);
    LocationRequest anotherRequest = LocationRequest.create();
    requestManager.requestLocationUpdates(client, anotherRequest, intent);

    assertThat(requestManager.getClientCallbackMap().size()).isEqualTo(1);

    ClientCallbackWrapper wrapper = new ClientCallbackWrapper(client, intent);
    assertThat(requestManager.getClientCallbackMap().get(wrapper)).contains(request);
    assertThat(requestManager.getClientCallbackMap().get(wrapper)).contains(anotherRequest);
  }

  @Test
  public void requestLocationUpdates_twoClientPendingIntents_twoRequests_shouldUpdateMap() {
    LostApiClient client = mock(LostApiClient.class);
    LocationRequest request = LocationRequest.create();
    PendingIntent intent = mock(PendingIntent.class);
    requestManager.requestLocationUpdates(client, request, intent);
    LostApiClient anotherClient = mock(LostApiClient.class);
    LocationRequest anotherRequest = LocationRequest.create();
    requestManager.requestLocationUpdates(anotherClient, anotherRequest, intent);

    assertThat(requestManager.getClientCallbackMap().size()).isEqualTo(2);

    ClientCallbackWrapper wrapper = new ClientCallbackWrapper(client, intent);
    assertThat(requestManager.getClientCallbackMap().get(wrapper).size()).isEqualTo(1);
    assertThat(requestManager.getClientCallbackMap().get(wrapper)).contains(request);

    ClientCallbackWrapper anotherWrapper = new ClientCallbackWrapper(anotherClient, intent);
    assertThat(requestManager.getClientCallbackMap().get(anotherWrapper).size()).isEqualTo(1);
    assertThat(requestManager.getClientCallbackMap().get(anotherWrapper)).contains(anotherRequest);
  }

  @Test
  public void requestLocationUpdates_clientCallback_oneRequest_shouldUpdateMap() {
    LostApiClient client = mock(LostApiClient.class);
    LocationRequest request = LocationRequest.create();
    LocationCallback callback = mock(LocationCallback.class);
    requestManager.requestLocationUpdates(client, request, callback);

    assertThat(requestManager.getClientCallbackMap().size()).isEqualTo(1);
  }

  @Test
  public void requestLocationUpdates_clientCallback_twoRequests_shouldUpdateMap() {
    LostApiClient client = mock(LostApiClient.class);
    LocationRequest request = LocationRequest.create();
    LocationCallback callback = mock(LocationCallback.class);
    requestManager.requestLocationUpdates(client, request, callback);
    LocationRequest anotherRequest = LocationRequest.create();
    requestManager.requestLocationUpdates(client, anotherRequest, callback);

    assertThat(requestManager.getClientCallbackMap().size()).isEqualTo(1);

    ClientCallbackWrapper wrapper = new ClientCallbackWrapper(client, callback);
    assertThat(requestManager.getClientCallbackMap().get(wrapper)).contains(request);
    assertThat(requestManager.getClientCallbackMap().get(wrapper)).contains(anotherRequest);
  }

  @Test
  public void requestLocationUpdates_twoClientCallbacks_twoRequests_shouldUpdateMap() {
    LostApiClient client = mock(LostApiClient.class);
    LocationRequest request = LocationRequest.create();
    LocationCallback callback = mock(LocationCallback.class);
    requestManager.requestLocationUpdates(client, request, callback);
    LostApiClient anotherClient = mock(LostApiClient.class);
    LocationRequest anotherRequest = LocationRequest.create();
    requestManager.requestLocationUpdates(anotherClient, anotherRequest, callback);

    assertThat(requestManager.getClientCallbackMap().size()).isEqualTo(2);

    ClientCallbackWrapper wrapper = new ClientCallbackWrapper(client, callback);
    assertThat(requestManager.getClientCallbackMap().get(wrapper).size()).isEqualTo(1);
    assertThat(requestManager.getClientCallbackMap().get(wrapper)).contains(request);

    ClientCallbackWrapper anotherWrapper = new ClientCallbackWrapper(anotherClient, callback);
    assertThat(requestManager.getClientCallbackMap().get(anotherWrapper).size()).isEqualTo(1);
    assertThat(requestManager.getClientCallbackMap().get(anotherWrapper)).contains(anotherRequest);
  }

  @Test
  public void removeLocationUpdates_clientListener_oneRequest_shouldRemoveAll() {
    LostApiClient client = mock(LostApiClient.class);
    LocationRequest request = LocationRequest.create();
    LocationListener listener = mock(LocationListener.class);
    requestManager.requestLocationUpdates(client, request, listener);
    requestManager.removeLocationUpdates(client, listener);

    assertThat(requestManager.getClientCallbackMap()).isEmpty();
  }

  @Test
  public void removeLocationUpdates_clientListener_twoRequests_shouldRemoveAll() {
    LostApiClient client = mock(LostApiClient.class);
    LocationRequest request = LocationRequest.create();
    LocationListener listener = mock(LocationListener.class);
    requestManager.requestLocationUpdates(client, request, listener);
    LocationRequest anotherRequest = LocationRequest.create();
    requestManager.requestLocationUpdates(client, anotherRequest, listener);
    requestManager.removeLocationUpdates(client, listener);

    assertThat(requestManager.getClientCallbackMap()).isEmpty();
  }

  @Test
  public void removeLocationUpdates_twoClientListeners_twoRequests_shouldRemoveOne() {
    LostApiClient client = mock(LostApiClient.class);
    LocationRequest request = LocationRequest.create();
    LocationListener listener = mock(LocationListener.class);
    requestManager.requestLocationUpdates(client, request, listener);
    LostApiClient anotherClient = mock(LostApiClient.class);
    LocationRequest anotherRequest = LocationRequest.create();
    requestManager.requestLocationUpdates(anotherClient, anotherRequest, listener);
    requestManager.removeLocationUpdates(client, listener);

    assertThat(requestManager.getClientCallbackMap().size()).isEqualTo(1);

    ClientCallbackWrapper anotherWrapper = new ClientCallbackWrapper(anotherClient, listener);
    assertThat(requestManager.getClientCallbackMap().get(anotherWrapper)).contains(anotherRequest);
  }

  @Test
  public void removeLocationUpdates_clientPendingIntent_oneRequest_shouldRemoveAll() {
    LostApiClient client = mock(LostApiClient.class);
    LocationRequest request = LocationRequest.create();
    PendingIntent intent = mock(PendingIntent.class);
    requestManager.requestLocationUpdates(client, request, intent);
    requestManager.removeLocationUpdates(client, intent);

    assertThat(requestManager.getClientCallbackMap()).isEmpty();
  }

  @Test
  public void removeLocationUpdates_clientPendingIntent_twoRequests_shouldRemoveAll() {
    LostApiClient client = mock(LostApiClient.class);
    LocationRequest request = LocationRequest.create();
    PendingIntent intent = mock(PendingIntent.class);
    requestManager.requestLocationUpdates(client, request, intent);
    LocationRequest anotherRequest = LocationRequest.create();
    requestManager.requestLocationUpdates(client, anotherRequest, intent);
    requestManager.removeLocationUpdates(client, intent);

    assertThat(requestManager.getClientCallbackMap()).isEmpty();
  }

  @Test
  public void removeLocationUpdates_twoClientPendingIntents_twoRequests_shouldRemoveOne() {
    LostApiClient client = mock(LostApiClient.class);
    LocationRequest request = LocationRequest.create();
    PendingIntent intent = mock(PendingIntent.class);
    requestManager.requestLocationUpdates(client, request, intent);
    LostApiClient anotherClient = mock(LostApiClient.class);
    LocationRequest anotherRequest = LocationRequest.create();
    requestManager.requestLocationUpdates(anotherClient, anotherRequest, intent);
    requestManager.removeLocationUpdates(client, intent);

    assertThat(requestManager.getClientCallbackMap().size()).isEqualTo(1);

    ClientCallbackWrapper anotherWrapper = new ClientCallbackWrapper(anotherClient, intent);
    assertThat(requestManager.getClientCallbackMap().get(anotherWrapper)).contains(anotherRequest);
  }

  @Test
  public void removeLocationUpdates_clientCallback_oneRequest_shouldRemoveAll() {
    LostApiClient client = mock(LostApiClient.class);
    LocationRequest request = LocationRequest.create();
    LocationCallback callback = mock(LocationCallback.class);
    requestManager.requestLocationUpdates(client, request, callback);
    requestManager.removeLocationUpdates(client, callback);

    assertThat(requestManager.getClientCallbackMap()).isEmpty();
  }

  @Test
  public void removeLocationUpdates_clientCallback_twoRequests_shouldRemoveAll() {
    LostApiClient client = mock(LostApiClient.class);
    LocationRequest request = LocationRequest.create();
    LocationCallback callback = mock(LocationCallback.class);
    requestManager.requestLocationUpdates(client, request, callback);
    LocationRequest anotherRequest = LocationRequest.create();
    requestManager.requestLocationUpdates(client, anotherRequest, callback);
    requestManager.removeLocationUpdates(client, callback);

    assertThat(requestManager.getClientCallbackMap()).isEmpty();
  }

  @Test
  public void removeLocationUpdates_twoClientCallbacks_twoRequests_shouldRemoveOne() {
    LostApiClient client = mock(LostApiClient.class);
    LocationRequest request = LocationRequest.create();
    LocationCallback callback = mock(LocationCallback.class);
    requestManager.requestLocationUpdates(client, request, callback);
    LostApiClient anotherClient = mock(LostApiClient.class);
    LocationRequest anotherRequest = LocationRequest.create();
    requestManager.requestLocationUpdates(anotherClient, anotherRequest, callback);
    requestManager.removeLocationUpdates(client, callback);

    assertThat(requestManager.getClientCallbackMap().size()).isEqualTo(1);

    ClientCallbackWrapper anotherWrapper = new ClientCallbackWrapper(anotherClient, callback);
    assertThat(requestManager.getClientCallbackMap().get(anotherWrapper)).contains(anotherRequest);
  }
}
