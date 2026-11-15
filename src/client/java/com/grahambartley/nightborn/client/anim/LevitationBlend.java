package com.grahambartley.nightborn.client.anim;

import com.grahambartley.nightborn.mob.VampirePose;

public record LevitationBlend(double move, double sprint) {

  public static final LevitationBlend STILL = new LevitationBlend(0.0, 0.0);

  static final double SPRINT_FROM = VampirePose.LEVITATE_MOVE_SPEED * 4.0;

  public static LevitationBlend target(final double horizontalSpeed, final boolean sprinting) {
    return new LevitationBlend(
        smoothstep(
            VampirePose.LEVITATE_MOVE_SPEED * 0.3,
            VampirePose.LEVITATE_MOVE_SPEED * 3.0,
            horizontalSpeed),
        sprinting
            ? smoothstep(SPRINT_FROM, VampirePose.LEVITATE_SPRINT_SPEED, horizontalSpeed)
            : 0.0);
  }

  public LevitationBlend approach(final LevitationBlend target, final double share) {
    return new LevitationBlend(
        move + (target.move - move) * share, sprint + (target.sprint - sprint) * share);
  }

  static double smoothstep(final double from, final double to, final double value) {
    final double t = Math.max(0.0, Math.min(1.0, (value - from) / (to - from)));
    return t * t * (3.0 - 2.0 * t);
  }
}
