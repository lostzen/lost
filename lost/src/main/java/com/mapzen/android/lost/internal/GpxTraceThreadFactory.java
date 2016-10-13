package com.mapzen.android.lost.internal;

import android.content.Context;

import java.io.File;

public class GpxTraceThreadFactory implements TraceThreadFactory {
  @Override public TraceThread createTraceThread(Context context, File traceFile, MockEngine engine,
      SleepFactory sleepFactory) {
    return new TraceThread(context, traceFile, engine, sleepFactory);
  }
}
