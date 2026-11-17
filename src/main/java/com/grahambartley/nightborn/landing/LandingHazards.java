package com.grahambartley.nightborn.landing;

import java.util.stream.Stream;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;

public final class LandingHazards {

  private LandingHazards() {}

  public static boolean burns(final BlockState floor, final Stream<BlockState> body) {
    return hotFloor(floor) || body.anyMatch(LandingHazards::flame);
  }

  static boolean hotFloor(final BlockState floor) {
    return floor.isOf(Blocks.MAGMA_BLOCK) || flame(floor);
  }

  static boolean flame(final BlockState state) {
    return state.getBlock() instanceof AbstractFireBlock
        || (state.getBlock() instanceof CampfireBlock && state.get(CampfireBlock.LIT));
  }
}
