package com.grahambartley.nightborn.mob.home;

import java.util.Optional;
import net.minecraft.util.math.BlockPos;

public final class Housemates {

  private Housemates() {}

  public static boolean share(final Optional<BlockPos> one, final Optional<BlockPos> other) {
    return one.isPresent() && one.equals(other);
  }

  public static boolean rallied(
      final boolean sameHome,
      final boolean foeIsVampire,
      final boolean asleep,
      final boolean helpless,
      final double distanceSquared,
      final int range) {
    return !asleep && !helpless && alerted(sameHome, foeIsVampire, distanceSquared, range);
  }

  public static boolean alerted(
      final boolean sameHome,
      final boolean attackerIsVampire,
      final double distanceSquared,
      final int alertRange) {
    return sameHome && !attackerIsVampire && distanceSquared <= (double) alertRange * alertRange;
  }
}
