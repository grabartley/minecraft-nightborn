package com.grahambartley.nightborn.client.anim;

import com.grahambartley.nightborn.mob.LandingEdge;
import com.grahambartley.nightborn.mob.VampirePose;
import java.util.Optional;

public final class PlayerClipState {

  public static final double TICKS_PER_SECOND = 20.0;

  private final LandingEdge landing = new LandingEdge();
  private double landedAt = Double.NaN;
  private double vomitAt = Double.NaN;

  public void tick(final double age, final boolean levitating) {
    if (landing.landed(levitating)) {
      landedAt = age;
    }
    if (levitating) {
      landedAt = Double.NaN;
    }
  }

  public void vomit(final double age) {
    vomitAt = age;
  }

  public double vomitingSeconds(final double age) {
    return seconds(vomitAt, age);
  }

  public Optional<VampirePose> clip(
      final double age,
      final boolean levitating,
      final boolean sprinting,
      final double horizontalSpeed,
      final double landSeconds,
      final double vomitSeconds) {
    if (playing(vomitAt, vomitSeconds, age)) {
      return Optional.of(VampirePose.VOMIT);
    }
    if (levitating) {
      return Optional.of(VampirePose.levitating(sprinting, horizontalSpeed));
    }
    if (playing(landedAt, landSeconds, age)) {
      return Optional.of(VampirePose.LAND);
    }
    return Optional.empty();
  }

  private static double seconds(final double from, final double age) {
    return Double.isNaN(from) ? Double.NaN : (age - from) / TICKS_PER_SECOND;
  }

  private static boolean playing(final double from, final double length, final double age) {
    final double seconds = seconds(from, age);
    return !Double.isNaN(seconds) && seconds >= 0.0 && seconds < length;
  }
}
