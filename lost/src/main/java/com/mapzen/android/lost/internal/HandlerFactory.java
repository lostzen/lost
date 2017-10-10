package com.mapzen.android.lost.internal;

import android.os.Looper;

/**
 * Generic interface for {@link AndroidHandlerFactory} which can be seamlessly replaced for test
 * class when running test suite.
 */
interface HandlerFactory {
  void run(Looper looper, Runnable runnable);
}
