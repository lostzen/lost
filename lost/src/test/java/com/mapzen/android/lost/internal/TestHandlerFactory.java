package com.mapzen.android.lost.internal;

import android.os.Looper;

public class TestHandlerFactory implements HandlerFactory {

  @Override public void run(Looper looper, Runnable runnable) {
    runnable.run();
  }
}
