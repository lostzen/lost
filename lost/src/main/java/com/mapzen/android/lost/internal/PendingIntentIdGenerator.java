package com.mapzen.android.lost.internal;

public class PendingIntentIdGenerator implements IdGenerator {
  @Override public int generateId() {
    return (int) System.currentTimeMillis();
  }
}
