package com.grahambartley.nightborn.batform;

import java.util.Optional;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class BatTurnBack {

  static final double EPSILON = 1.0E-4;

  private BatTurnBack() {}

  public static Optional<Vec3d> feet(
      final Vec3d bat,
      final Optional<BlockPos> anchor,
      final double batHeight,
      final double standingHeight,
      final double ceiling,
      final double floor) {
    if (anchor.isPresent()) {
      return Optional.of(BatHangRules.standingFeet(anchor.get(), standingHeight));
    }
    if (ceiling >= bat.y + standingHeight - EPSILON) {
      return Optional.of(bat);
    }
    final double lowered = ceiling - standingHeight;
    if (bat.y - lowered > standingHeight - batHeight + EPSILON || lowered < floor - EPSILON) {
      return Optional.empty();
    }
    return Optional.of(new Vec3d(bat.x, lowered, bat.z));
  }
}
