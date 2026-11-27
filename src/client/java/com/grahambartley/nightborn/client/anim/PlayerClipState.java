package com.grahambartley.nightborn.client.anim;

import com.grahambartley.nightborn.mob.EndingEdge;
import com.grahambartley.nightborn.mob.LevitationGait;
import com.grahambartley.nightborn.mob.VampirePose;
import java.util.Optional;
import net.minecraft.entity.EntityPose;

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
    double seconds(String animation);
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
  private final OneShot power = new OneShot();
  private final OneShot thrust = new OneShot();
  private final LevitationGait levitationGait = new LevitationGait();
  private VampirePose gesturePose = VampirePose.SMALL_FLINCH;
  private String powerClip = "";
  private boolean powerFresh;
  private VampirePose thrustPose = VampirePose.STAKE_THRUST_RIGHT;
  private boolean thrustFresh;
  private boolean swinging;
  private int swingTicks;

  public void tick(
      final long age,
      final boolean levitating,
      final boolean downed,
      final boolean sprinting,
      final double horizontalSpeed) {
    levitationGait.observe(age, levitating, sprinting, horizontalSpeed);
    follow(landing, land, levitating, age);
    follow(standing, rise, downed, age);
  }

  public double levitationSpread() {
    return levitationGait.spread();
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

  public void power(final String clip, final double age, final Lengths lengths) {
    powerFresh = true;
    powerClip = clip;
    power.start(age);
  }

  public Optional<String> powerClip(final double age, final Lengths lengths) {
    if (!power.playing(lengths.seconds(powerClip), age)) {
      powerFresh = false;
      return Optional.empty();
    }
    return Optional.of(powerClip);
  }

  public double powerSeconds(final double age) {
    return power.seconds(age);
  }

  public void swing(
      final boolean swinging,
      final int swingTicks,
      final boolean reverseGrip,
      final boolean rightArm,
      final double age) {
    final boolean started = swinging && (!this.swinging || swingTicks < this.swingTicks);
    this.swinging = swinging;
    this.swingTicks = swingTicks;
    if (!started) {
      return;
    }
    if (!reverseGrip) {
      thrust.clear();
      return;
    }
    thrustPose = VampirePose.thrust(rightArm);
    thrustFresh = true;
    thrust.start(age);
  }

  public Optional<VampirePose> thrust(final double age, final Lengths lengths) {
    if (!thrust.playing(lengths.seconds(thrustPose.animation()), age)) {
      thrustFresh = false;
      return Optional.empty();
    }
    return Optional.of(thrustPose);
  }

  public double thrustSeconds(final double age) {
    return thrust.seconds(age);
  }

  public boolean takeFreshThrust() {
    final boolean fresh = thrustFresh;
    thrustFresh = false;
    return fresh;
  }

  public static Optional<VampirePose> action(
      final Optional<VampirePose> pose,
      final boolean powerPlaying,
      final Optional<VampirePose> thrust,
      final boolean swinging,
      final boolean using,
      final boolean rightArm) {
    if (!pose.map(VampirePose::allowsActions).orElse(powerPlaying || thrust.isPresent())) {
      return Optional.empty();
    }
    return VampirePose.action(swinging && !powerPlaying && thrust.isEmpty(), using, rightArm);
  }

  public static boolean upright(final EntityPose pose, final boolean riding) {
    return pose == EntityPose.STANDING && !riding;
  }

  public boolean takeFreshPower() {
    final boolean fresh = powerFresh;
    powerFresh = false;
    return fresh;
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
    if (vomit.playing(lengths.seconds(VampirePose.VOMIT.animation()), age)) {
      return Optional.of(VampirePose.VOMIT);
    }
    if (situation.downed()) {
      return Optional.of(VampirePose.DOWNED);
    }
    if (rise.playing(lengths.seconds(VampirePose.RISE.animation()), age)) {
      return Optional.of(VampirePose.RISE);
    }
    if (biting.ended(situation.feeding())) {
      finisher.clear();
    }
    if (finisher.playing(lengths.seconds(VampirePose.FINISHER_BITE.animation()), age)) {
      return Optional.of(VampirePose.FINISHER_BITE);
    }
    if (drain.playing(lengths.seconds(VampirePose.DRAIN.animation()), age)) {
      return Optional.of(VampirePose.DRAIN);
    }
    if (situation.feeding()) {
      return Optional.of(VampirePose.FEED);
    }
    if (situation.levitating()) {
      return Optional.of(levitationGait.pose());
    }
    if (land.playing(lengths.seconds(VampirePose.LAND.animation()), age)) {
      return Optional.of(VampirePose.LAND);
    }
    if (gesture.playing(lengths.seconds(gesturePose.animation()), age)) {
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
