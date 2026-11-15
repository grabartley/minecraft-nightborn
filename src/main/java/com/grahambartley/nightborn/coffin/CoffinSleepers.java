package com.grahambartley.nightborn.coffin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public final class CoffinSleepers {

  public static final double LYING_HEIGHT = 3.0 / 16.0;

  private CoffinSleepers() {}

  public static boolean isCoffin(final BlockView world, final BlockPos pos) {
    return world.getBlockState(pos).getBlock() instanceof CoffinBlock;
  }

  public static boolean inCoffin(final LivingEntity sleeper) {
    return sleeper
        .getSleepingPosition()
        .map(pos -> isCoffin(sleeper.getWorld(), pos))
        .orElse(false);
  }

  public static boolean shelteredBy(final LivingEntity sleeper) {
    return sleeper
        .getSleepingPosition()
        .map(pos -> shelters(sleeper.getWorld().getBlockState(pos)))
        .orElse(false);
  }

  public static boolean shelters(final BlockState state) {
    return state.getBlock() instanceof CoffinBlock && !state.get(CoffinBlock.OPEN);
  }
}
