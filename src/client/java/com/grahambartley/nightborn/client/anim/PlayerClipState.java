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
      boolean weakened,
      Optional<VampirePose> coffin) {}

  public record Lengths(
      double land, double vomit, double drain, double rise, double flinch, double finisher) {}

  private final EndingEdge landing = new EndingEdge();
  private final EndingEdge standing = new EndingEdge();
  private final OneShot land = new OneShot();
  private final OneShot rise = new OneShot();
  private final OneShot vomit = new OneShot();
  private final OneShot drain = new OneShot();
  private final OneShot flinch = new OneShot();
  private final OneShot finisher = new OneShot();

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

  public void flinch(final double age) {
    flinch.start(age);
  }

  public void finisher(final double age) {
    finisher.start(age);
  }

  public double vomitingSeconds(final double age) {
    return vomit.seconds(age);
  }

  public Optional<VampirePose> clip(
      final double age, final Situation situation, final Lengths lengths) {
    if (situation.coffin().isPresent()) {
      return situation.coffin();
    }
    if (vomit.playing(lengths.vomit(), age)) {
      return Optional.of(VampirePose.VOMIT);
    }
    if (situation.downed()) {
      return Optional.of(VampirePose.DOWNED);
    }
    if (rise.playing(lengths.rise(), age)) {
      return Optional.of(VampirePose.RISE);
    }
    if (!situation.feeding()) {
      finisher.clear();
    }
    if (finisher.playing(lengths.finisher(), age)) {
      return Optional.of(VampirePose.FINISHER_BITE);
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
    if (flinch.playing(lengths.flinch(), age)) {
      return Optional.of(VampirePose.FLINCH);
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
