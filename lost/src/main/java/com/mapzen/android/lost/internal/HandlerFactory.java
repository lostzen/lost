package com.mapzen.android.lost.internal;

import android.os.Looper;

/**
 *
 */
interface HandlerFactory {
  void run(Looper looper, Runnable runnable);
}
