package com.grahambartley.nightborn.house.duel;

import com.grahambartley.nightborn.boss.Arena;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class DuelRules {

  public static final double OUTSIDE = 1.5;

  private DuelRules() {}

  public static boolean allowsHarm(
      final boolean targetDueling,
      final boolean attackerDueling,
      final boolean attackerIsFoe,
      final boolean hasAttacker) {
    if (!targetDueling && !attackerDueling) {
      return true;
    }
    if (attackerIsFoe) {
      return true;
    }
    return targetDueling && !hasAttacker;
  }

  public static Arena circle(final Vec3d lord, final Vec3d challenger, final int radius) {
    return new Arena(BlockPos.ofFloored(lord.add(challenger).multiply(0.5)), radius);
  }

  public static Vec3d pushOut(final Arena arena, final Vec3d pos) {
    final double dx = pos.x - arena.midX();
    final double dz = pos.z - arena.midZ();
    final double out = Math.sqrt(dx * dx + dz * dz);
    final double reach = arena.radius() + OUTSIDE;
    if (out < 1.0E-6) {
      return new Vec3d(arena.midX() + reach, pos.y, arena.midZ());
    }
    return new Vec3d(arena.midX() + dx / out * reach, pos.y, arena.midZ() + dz / out * reach);
  }
}
