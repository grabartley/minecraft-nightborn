package com.grahambartley.nightborn.mob;

public final class LevitationGait {

  static final double SMOOTHING = 0.25;
  static final double MOVE_ENTER_SPEED = 0.04;
  static final double MOVE_EXIT_SPEED = 0.02;
  static final double SPRINT_ENTER_SPEED = 0.26;
  static final double SPRINT_EXIT_SPEED = 0.2;
  static final double FULL_SPREAD_SPEED = 0.24;
  static final int HOLD_TICKS = 10;

  private double speed;
  private boolean sprinting;
  private VampirePose pose = VampirePose.LEVITATE;
  private int held = HOLD_TICKS;
  private long lastTick = Long.MIN_VALUE;

  public void observe(
      final long tick,
      final boolean levitating,
      final boolean nowSprinting,
      final double horizontalSpeed) {
    if (tick == lastTick) {
      return;
    }
    lastTick = tick;
    speed += (Math.max(0.0, horizontalSpeed) - speed) * SMOOTHING;
    sprinting = nowSprinting;
    final VampirePose wanted = wanted(pose, sprinting, speed);
    if (!levitating) {
      pose = wanted;
      held = HOLD_TICKS;
      return;
    }
    held++;
    if (wanted != pose && held >= HOLD_TICKS) {
      pose = wanted;
      held = 0;
    }
  }

  public VampirePose pose() {
    return pose;
  }

  public double spread() {
    return sprinting ? 1.0 : Math.min(1.0, speed / FULL_SPREAD_SPEED);
  }

  static VampirePose wanted(final VampirePose shown, final boolean sprinting, final double speed) {
    if (sprinting) {
      return VampirePose.LEVITATE_SPRINT;
    }
    final boolean fast =
        speed > (shown == VampirePose.LEVITATE_SPRINT ? SPRINT_EXIT_SPEED : SPRINT_ENTER_SPEED);
    if (fast) {
      return VampirePose.LEVITATE_SPRINT;
    }
    final boolean moving =
        speed > (shown == VampirePose.LEVITATE ? MOVE_ENTER_SPEED : MOVE_EXIT_SPEED);
    return moving ? VampirePose.LEVITATE_MOVE : VampirePose.LEVITATE;
  }
}
