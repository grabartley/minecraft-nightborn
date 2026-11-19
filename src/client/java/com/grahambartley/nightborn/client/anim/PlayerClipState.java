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
      Optional<VampirePose> coffin,
      boolean staked,
      boolean sunBurning) {}

  @FunctionalInterface
  public interface Lengths {
    double seconds(VampirePose pose);
  }

  private final EndingEdge landing = new EndingEdge();
  private final EndingEdge standing = new EndingEdge();
  private final EndingEdge biting = new EndingEdge();
  private final OneShot land = new OneShot();
  private final OneShot rise = new OneShot();
  private final OneShot vomit = new OneShot();
  private final OneShot drain = new OneShot();
  private final OneShot finisher = new OneShot();
  private final OneShot gesture = new OneShot();
  private VampirePose gesturePose = VampirePose.SMALL_FLINCH;

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

  public void gesture(final VampirePose pose, final double age) {
    gesturePose = pose;
    gesture.start(age);
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
    if (situation.staked()) {
      return Optional.of(VampirePose.STAKED_DEATH);
    }
    if (vomit.playing(lengths.seconds(VampirePose.VOMIT), age)) {
      return Optional.of(VampirePose.VOMIT);
    }
    if (situation.downed()) {
      return Optional.of(VampirePose.DOWNED);
    }
    if (rise.playing(lengths.seconds(VampirePose.RISE), age)) {
      return Optional.of(VampirePose.RISE);
    }
    if (biting.ended(situation.feeding())) {
      finisher.clear();
    }
    if (finisher.playing(lengths.seconds(VampirePose.FINISHER_BITE), age)) {
      return Optional.of(VampirePose.FINISHER_BITE);
    }
    if (drain.playing(lengths.seconds(VampirePose.DRAIN), age)) {
      return Optional.of(VampirePose.DRAIN);
    }
    if (situation.feeding()) {
      return Optional.of(VampirePose.FEED);
    }
    if (situation.levitating()) {
      return Optional.of(
          VampirePose.levitating(situation.sprinting(), situation.horizontalSpeed()));
    }
    if (land.playing(lengths.seconds(VampirePose.LAND), age)) {
      return Optional.of(VampirePose.LAND);
    }
    if (gesture.playing(lengths.seconds(gesturePose), age)) {
      return Optional.of(gesturePose);
    }
    if (situation.sunBurning() && !situation.sprinting()) {
      return Optional.of(VampirePose.SUN_BURN);
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
