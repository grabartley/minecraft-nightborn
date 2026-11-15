package com.grahambartley.nightborn.client.anim;

public final class OneShot {

  public static final double TICKS_PER_SECOND = 20.0;

  private double startedAt = Double.NaN;

  public void start(final double age) {
    startedAt = age;
  }

  public void clear() {
    startedAt = Double.NaN;
  }

  public double seconds(final double age) {
    return Double.isNaN(startedAt) ? Double.NaN : (age - startedAt) / TICKS_PER_SECOND;
  }

  public boolean playing(final double length, final double age) {
    final double seconds = seconds(age);
    return !Double.isNaN(seconds) && seconds >= 0.0 && seconds < length;
  }
}
