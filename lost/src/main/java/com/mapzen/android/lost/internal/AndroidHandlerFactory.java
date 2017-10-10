package com.mapzen.android.lost.internal;

import android.os.Handler;
import android.os.Looper;

/**
 *
 */
class AndroidHandlerFactory implements HandlerFactory {

  @Override public void run(Looper looper, Runnable runnable) {
    Handler handler = new Handler(looper);
    handler.post(runnable);
  }
}
