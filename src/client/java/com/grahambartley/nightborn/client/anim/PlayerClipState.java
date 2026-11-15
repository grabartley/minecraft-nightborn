package com.grahambartley.nightborn.client.anim;

import com.grahambartley.nightborn.mob.EndingEdge;
import com.grahambartley.nightborn.mob.VampirePose;
import java.util.Optional;

public final class PlayerClipState {

  public static final double TICKS_PER_SECOND = 20.0;

  public record Situation(
      boolean levitating,
      boolean sprinting,
      double horizontalSpeed,
      boolean downed,
      boolean feeding,
      boolean weakened) {}

  public record Lengths(double land, double vomit, double drain, double rise) {}

  private final EndingEdge landing = new EndingEdge();
  private final EndingEdge standing = new EndingEdge();
  private double landedAt = Double.NaN;
  private double roseAt = Double.NaN;
  private double vomitAt = Double.NaN;
  private double drainAt = Double.NaN;

  public void tick(final double age, final boolean levitating, final boolean downed) {
    if (landing.ended(levitating)) {
      landedAt = age;
    }
    if (levitating) {
      landedAt = Double.NaN;
    }
    if (standing.ended(downed)) {
      roseAt = age;
    }
    if (downed) {
      roseAt = Double.NaN;
    }
  }

  public void vomit(final double age) {
    vomitAt = age;
  }

  public void drain(final double age) {
    drainAt = age;
  }

  public double vomitingSeconds(final double age) {
    return seconds(vomitAt, age);
  }

  public Optional<VampirePose> clip(
      final double age, final Situation situation, final Lengths lengths) {
    if (playing(vomitAt, lengths.vomit(), age)) {
      return Optional.of(VampirePose.VOMIT);
    }
    if (situation.downed()) {
      return Optional.of(VampirePose.DOWNED);
    }
    if (playing(roseAt, lengths.rise(), age)) {
      return Optional.of(VampirePose.RISE);
    }
    if (playing(drainAt, lengths.drain(), age)) {
      return Optional.of(VampirePose.DRAIN);
    }
    if (situation.feeding()) {
      return Optional.of(VampirePose.FEED);
    }
    if (situation.levitating()) {
      return Optional.of(
          VampirePose.levitating(situation.sprinting(), situation.horizontalSpeed()));
    }
    if (playing(landedAt, lengths.land(), age)) {
      return Optional.of(VampirePose.LAND);
    }
    if (situation.weakened()) {
      return Optional.of(VampirePose.weakened(situation.horizontalSpeed()));
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
