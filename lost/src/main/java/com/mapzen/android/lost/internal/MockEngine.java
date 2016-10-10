package com.mapzen.android.lost.internal;

import android.content.Context;
import android.location.Location;

import java.io.File;

/**
 * Mock implementation of LocationEngine that reports single locations and/or full GPX traces.
 */
public class MockEngine extends LocationEngine {
  public static final String MOCK_PROVIDER = "mock";

  private Location location;
  private File traceFile;
  private TraceThreadFactory traceThreadFactory;
  protected TraceThread traceThread;

  public MockEngine(Context context, FusionEngine.Callback callback,
      TraceThreadFactory traceThreadFactory) {
    super(context, callback);
    this.traceThreadFactory = traceThreadFactory;
  }

  @Override public Location getLastLocation() {
    return location;
  }

  @Override public boolean isProviderEnabled(String provider) {
    return false;
  }

  @Override protected void enable() {
    if (traceFile != null) {
      traceThread = traceThreadFactory.createTraceThread(getContext(), traceFile, this,
          new ThreadSleepFactory());
      traceThread.start();
    }
  }

  @Override protected void disable() {
    if (traceThread != null) {
      traceThread.cancel();
    }
  }

  public void setLocation(Location location) {
    this.location = location;
    if (getCallback() != null) {
      getCallback().reportLocation(location);
    }
  }

  /**
   * Set a GPX trace file to be replayed as mock locations.
   */
  public void setTrace(File file) {
    traceFile = file;
  }

  /**
   * Returns the current file used to replay mock locations.
   * @return the current file used to replay mock locations.
   */
  public File getTrace() {
    return traceFile;
  }
}
