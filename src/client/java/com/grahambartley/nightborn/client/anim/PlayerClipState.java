package com.grahambartley.nightborn.client.anim;

import com.grahambartley.nightborn.mob.EndingEdge;
import com.grahambartley.nightborn.mob.VampirePose;
import java.util.Optional;

public final class PlayerClipState {

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
  private final OneShot land = new OneShot();
  private final OneShot rise = new OneShot();
  private final OneShot vomit = new OneShot();
  private final OneShot drain = new OneShot();

  public void tick(final double age, final boolean levitating, final boolean downed) {
    follow(landing, land, levitating, age);
    follow(standing, rise, downed, age);
  }

  public void vomit(final double age) {
    vomit.start(age);
  }

  public void drain(final double age) {
    drain.start(age);
  }

  public double vomitingSeconds(final double age) {
    return vomit.seconds(age);
  }

  public Optional<VampirePose> clip(
      final double age, final Situation situation, final Lengths lengths) {
    if (vomit.playing(lengths.vomit(), age)) {
      return Optional.of(VampirePose.VOMIT);
    }
    if (situation.downed()) {
      return Optional.of(VampirePose.DOWNED);
    }
    if (rise.playing(lengths.rise(), age)) {
      return Optional.of(VampirePose.RISE);
    }
    if (drain.playing(lengths.drain(), age)) {
      return Optional.of(VampirePose.DRAIN);
    }
    if (situation.feeding()) {
      return Optional.of(VampirePose.FEED);
    }
    if (situation.levitating()) {
      return Optional.of(
          VampirePose.levitating(situation.sprinting(), situation.horizontalSpeed()));
    }
    if (land.playing(lengths.land(), age)) {
      return Optional.of(VampirePose.LAND);
    }
    if (situation.weakened()) {
      return Optional.of(VampirePose.weakened(situation.horizontalSpeed()));
    }
    return Optional.empty();
  }

  private static void follow(
      final EndingEdge edge, final OneShot shot, final boolean active, final double age) {
    if (edge.ended(active)) {
      shot.start(age);
    }
    if (active) {
      shot.clear();
    }
  }
}
