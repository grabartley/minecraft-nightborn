package com.grahambartley.nightborn.structure;

import net.minecraft.block.BlockState;

public final class StandingSpot {

  private StandingSpot() {}

  public static boolean fits(final BlockState floor, final BlockState feet, final BlockState head) {
    return floor.blocksMovement()
        && floor.getFluidState().isEmpty()
        && isOpen(feet)
        && isOpen(head);
  }

  private static boolean isOpen(final BlockState state) {
    return !state.blocksMovement() && state.getFluidState().isEmpty();
  }
}
