package com.grahambartley.nightborn.client.gramophone;

import java.util.Collection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class GramophoneEarshot {

  public static final double RANGE = 64.0;

  private GramophoneEarshot() {}

  public static boolean hears(final Collection<BlockPos> playing, final Vec3d listener) {
    return playing.stream()
        .anyMatch(pos -> pos.toCenterPos().squaredDistanceTo(listener) <= RANGE * RANGE);
  }
}
