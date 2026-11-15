package com.grahambartley.nightborn.coffin;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class CoffinBlockEntity extends BlockEntity {

  private final LidMotion lid = new LidMotion();

  public CoffinBlockEntity(final BlockPos pos, final BlockState state) {
    super(Coffins.BLOCK_ENTITY, pos, state);
  }

  public void clientTick(final BlockState state) {
    this.lid.tick(state.get(CoffinBlock.OPEN));
  }

  public float openness(final float tickDelta) {
    return this.lid.openness(tickDelta);
  }

  public boolean isShut() {
    return this.lid.openness(1.0F) <= 0.0F;
  }
}
