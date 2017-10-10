package com.mapzen.android.lost.internal;

import android.os.Handler;
import android.os.Looper;

/**
 * Concrete implementation of {@link HandlerFactory} used by {@link LostClientManager}.
 */
class AndroidHandlerFactory implements HandlerFactory {

  @Override public void run(Looper looper, Runnable runnable) {
    Handler handler = new Handler(looper);
    handler.post(runnable);
  }
}
