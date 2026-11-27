package com.grahambartley.nightborn.mob;

import net.minecraft.entity.Entity;

public final class LevitationGait {

  static final double SMOOTHING = 0.25;
  static final double MOVE_ENTER_SPEED = 0.04;
  static final double MOVE_EXIT_SPEED = 0.02;
  static final double SPRINT_ENTER_SPEED = 0.26;
  static final double SPRINT_EXIT_SPEED = 0.2;
  static final double FULL_SPREAD_SPEED = 0.24;
  static final int HOLD_TICKS = 10;
  static final int SPRINT_SETTLE_TICKS = 2;
  static final int STALE_TICKS = 20;

  private double speed;
  private boolean sprinting;
  private long sprintFlipSince = Long.MIN_VALUE;
  private VampirePose pose = VampirePose.LEVITATE;
  private long changedAt = Long.MIN_VALUE / 2;
  private long lastTick = Long.MIN_VALUE / 2;

  public static double horizontalSpeed(final Entity entity) {
    return Math.hypot(entity.getX() - entity.prevX, entity.getZ() - entity.prevZ);
  }

  public void observe(
      final long tick,
      final boolean levitating,
      final boolean nowSprinting,
      final double horizontalSpeed) {
    if (tick == lastTick) {
      return;
    }
    final double sample = Math.max(0.0, horizontalSpeed);
    if (tick - lastTick > STALE_TICKS) {
      speed = sample;
      sprinting = nowSprinting;
      sprintFlipSince = Long.MIN_VALUE;
    } else {
      speed += (sample - speed) * SMOOTHING;
      settleSprint(tick, nowSprinting);
    }
    lastTick = tick;
    final VampirePose wanted = wanted(pose, sprinting, speed);
    if (!levitating) {
      pose = wanted;
      changedAt = tick - HOLD_TICKS;
      return;
    }
    if (wanted != pose && tick - changedAt >= HOLD_TICKS) {
      pose = wanted;
      changedAt = tick;
    }
  }

  public VampirePose pose() {
    return pose;
  }

  public double spread() {
    return pose == VampirePose.LEVITATE_SPRINT ? 1.0 : Math.min(1.0, speed / FULL_SPREAD_SPEED);
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

  private void settleSprint(final long tick, final boolean nowSprinting) {
    if (nowSprinting == sprinting) {
      sprintFlipSince = Long.MIN_VALUE;
      return;
    }
    if (sprintFlipSince == Long.MIN_VALUE) {
      sprintFlipSince = tick;
    }
    if (tick - sprintFlipSince >= SPRINT_SETTLE_TICKS) {
      sprinting = nowSprinting;
      sprintFlipSince = Long.MIN_VALUE;
    }
  }
}
