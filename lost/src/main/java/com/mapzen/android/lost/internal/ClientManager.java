package com.mapzen.android.lost.internal;

import com.mapzen.android.lost.api.LostApiClient;

import java.util.HashSet;

/**
 *
 */
public class ClientManager {

  private static ClientManager instance;
  private HashSet<LostApiClient> clients;

  private ClientManager() {
    clients = new HashSet<>();
  }

  public static ClientManager shared() {
    if (instance == null) {
      instance = new ClientManager();
    }
    return instance;
  }

  public void addClient(LostApiClient client) {
    clients.add(client);
  }

  public void removeClient(LostApiClient client) {
    clients.remove(client);
  }

  public int numberOfClients() {
    return clients.size();
  }
}
