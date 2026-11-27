package com.grahambartley.nightborn.client.anim;

import com.grahambartley.nightborn.mob.VampirePose;
import java.util.Optional;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;

public final class ThrustState {

  static final double EASE_IN_SECONDS = 0.05;
  static final double EASE_OUT_SECONDS = 0.15;

  private final OneShot thrust = new OneShot();
  private VampirePose pose = VampirePose.STAKE_THRUST_RIGHT;
  private boolean fresh;
  private boolean swinging;
  private int swingTicks;

  public void swing(
      final boolean swinging,
      final int swingTicks,
      final boolean reverseGrip,
      final boolean rightArm,
      final boolean showable,
      final double age,
      final PlayerClipState.Lengths lengths) {
    final boolean started = swinging && (!this.swinging || swingTicks < this.swingTicks);
    this.swinging = swinging;
    this.swingTicks = swingTicks;
    if (!reverseGrip) {
      thrust.clear();
      return;
    }
    if (!started || !showable || playing(age, lengths).isPresent()) {
      return;
    }
    pose = VampirePose.thrust(rightArm);
    fresh = true;
    thrust.start(age);
  }

  public Optional<VampirePose> playing(final double age, final PlayerClipState.Lengths lengths) {
    if (!thrust.playing(lengths.seconds(pose.animation()), age)) {
      fresh = false;
      return Optional.empty();
    }
    return Optional.of(pose);
  }

  public double seconds(final double age) {
    return thrust.seconds(age);
  }

  public boolean takeFresh() {
    final boolean wasFresh = fresh;
    fresh = false;
    return wasFresh;
  }

  public static boolean swingsRightArm(final Hand swinging, final Arm mainArm) {
    return (swinging != Hand.OFF_HAND) == (mainArm == Arm.RIGHT);
  }

  public static boolean showable(
      final Optional<VampirePose> pose, final boolean powerPlaying, final boolean upright) {
    return upright && !powerPlaying && pose.map(VampirePose::allowsActions).orElse(true);
  }

  public static Hand swingingHand(final Hand preferred) {
    return preferred == null ? Hand.MAIN_HAND : preferred;
  }

  public static float swingProgress(
      final boolean reverseGrip, final boolean thrustShown, final float vanilla) {
    return reverseGrip && thrustShown ? 0.0F : vanilla;
  }

  public static boolean sways(final VampirePose thrust, final Arm arm) {
    return thrust == VampirePose.thrust(arm == Arm.RIGHT);
  }

  public static double swayWeight(final double seconds, final double lengthSeconds) {
    final double in = seconds / EASE_IN_SECONDS;
    final double out = (lengthSeconds - seconds) / EASE_OUT_SECONDS;
    return Math.max(0.0, Math.min(1.0, Math.min(in, out)));
  }
}
