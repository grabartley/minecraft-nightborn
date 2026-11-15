package com.grahambartley.nightborn.sickness;

public final class SicknessClock {

  private static final long UNKNOWN = Long.MIN_VALUE;

  private long lastTimeOfDay = UNKNOWN;

  public int advance(final long timeOfDay) {
    final long last = lastTimeOfDay;
    lastTimeOfDay = timeOfDay;
    return step(last, timeOfDay);
  }

  public void reset() {
    lastTimeOfDay = UNKNOWN;
  }

  static int step(final long lastTimeOfDay, final long timeOfDay) {
    if (lastTimeOfDay == UNKNOWN || timeOfDay <= lastTimeOfDay) {
      return 1;
    }
    return (int) Math.min(Integer.MAX_VALUE, timeOfDay - lastTimeOfDay);
  }
}
