package com.grahambartley.nightborn.client.gramophone;

import java.util.Collection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class GramophoneEarshot {

  static final float RECORD_VOLUME = 4.0F;
  static final double BLOCKS_PER_VOLUME = 16.0;
  public static final double RANGE = RECORD_VOLUME * BLOCKS_PER_VOLUME;

  private GramophoneEarshot() {}

  public static boolean hears(final Collection<BlockPos> playing, final Vec3d listener) {
    return playing.stream()
        .anyMatch(pos -> pos.toCenterPos().squaredDistanceTo(listener) <= RANGE * RANGE);
  }
}
