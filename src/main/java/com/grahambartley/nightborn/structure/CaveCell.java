package com.grahambartley.nightborn.structure;

import net.minecraft.block.BlockState;

public enum CaveCell {
  SOLID,
  OPEN,
  FLUID;

  public static CaveCell of(final BlockState state) {
    if (!state.getFluidState().isEmpty()) {
      return FLUID;
    }
    return state.blocksMovement() ? SOLID : OPEN;
  }
}
