package com.grahambartley.nightborn.occult;

import net.minecraft.util.math.Vec3d;

public final class FamiliarRules {

  static final double START_DISTANCE = 4.0;
  static final double STOP_DISTANCE = 2.0;
  static final double TELEPORT_DISTANCE = 20.0;
  static final double HOME_DISTANCE = 2.5;
  static final double ARRIVED_DISTANCE = 1.0;
  static final int FADE_TICKS = 20;

  private FamiliarRules() {}

  public static boolean follows(final boolean ownerHere, final double distance) {
    return ownerHere && distance > START_DISTANCE;
  }

  public static boolean keepsFollowing(final boolean ownerHere, final double distance) {
    return ownerHere && distance > STOP_DISTANCE;
  }

  public static boolean teleports(final FamiliarMode mode, final double distance) {
    return !mode.passesThroughBlocks() && distance > TELEPORT_DISTANCE;
  }

  public static boolean fights(final FamiliarMode mode) {
    return mode == FamiliarMode.FIGHT;
  }

  public static Vec3d scoutTarget(final Vec3d eye, final Vec3d look, final double range) {
    final Vec3d flat = look.lengthSquared() < 1.0E-6 ? new Vec3d(0.0, 0.0, 1.0) : look.normalize();
    return eye.add(flat.multiply(range));
  }

  public static boolean scoutTurnsBack(
      final int ticksOut, final int scoutTicks, final double distanceToTarget) {
    return ticksOut >= scoutTicks / 2 || distanceToTarget <= ARRIVED_DISTANCE;
  }

  public static boolean scoutHome(
      final boolean ownerHere,
      final double distanceToOwner,
      final int ticksOut,
      final int scoutTicks) {
    return !ownerHere || distanceToOwner <= HOME_DISTANCE || ticksOut >= scoutTicks;
  }

  public static boolean marks(final double distance, final double radius) {
    return distance <= radius;
  }

  public static boolean fading(final int ticksLeft) {
    return ticksLeft <= FADE_TICKS;
  }

  public static int toDismiss(final int owned, final int limit) {
    return Math.max(0, owned - Math.max(1, limit) + 1);
  }
}
