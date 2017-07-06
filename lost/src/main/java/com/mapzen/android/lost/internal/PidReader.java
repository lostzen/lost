package com.mapzen.android.lost.internal;

/**
 * Generic interface for returning process id.
 */
public interface PidReader {
  /**
   * Gets the process id.
   * @return
   */
  long getPid();
}
