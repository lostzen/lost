package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LostApiClient;

import org.junit.Test;

import android.content.Context;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class ClientManagerTest {

  ClientManager manager = ClientManager.shared();
  Context context = mock(Context.class);

  @Test public void shouldHaveZeroClientCount() {
    assertThat(manager.numberOfClients()).isEqualTo(0);
  }

  @Test public void addClient_shouldIncreaseClientCount() {
    LostApiClient client = new LostApiClient.Builder(context).build();
    manager.addClient(client);
    assertThat(manager.numberOfClients()).isEqualTo(1);
    manager.removeClient(client);
  }

  @Test public void removeClient_shouldDecreaseClientCount() {
    LostApiClient client = new LostApiClient.Builder(context).build();
    manager.addClient(client);
    LostApiClient anotherClient = new LostApiClient.Builder(context).build();
    manager.addClient(anotherClient);
    assertThat(manager.numberOfClients()).isEqualTo(2);
    manager.removeClient(client);
    assertThat(manager.numberOfClients()).isEqualTo(1);
    manager.removeClient(anotherClient);
  }
}
