package com.grahambartley.nightborn.mob.home;

import java.util.Optional;
import net.minecraft.util.math.BlockPos;

public final class Housemates {

  public static final double ALERT_RANGE = 24.0;

  private Housemates() {}

  public static boolean share(final Optional<BlockPos> one, final Optional<BlockPos> other) {
    return one.isPresent() && one.equals(other);
  }

  public static boolean alerted(
      final boolean sameHome, final boolean attackerIsHousemate, final double distanceSquared) {
    return sameHome && !attackerIsHousemate && distanceSquared <= ALERT_RANGE * ALERT_RANGE;
  }
}
