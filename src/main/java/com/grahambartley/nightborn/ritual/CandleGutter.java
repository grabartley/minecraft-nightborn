package com.grahambartley.nightborn.ritual;

import com.grahambartley.nightborn.mob.household.Chore;
import com.grahambartley.nightborn.mob.household.FurnitureKind;
import com.grahambartley.nightborn.mob.household.FurnitureUse;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public final class CandleGutter {

  static final int RADIUS = 6;

  private CandleGutter() {}

  public static boolean burning(final boolean tended, final boolean hasFlame, final boolean lit) {
    return tended && hasFlame && lit;
  }

  public static boolean douseOne(
      final ServerWorld world, final BlockPos centre, final Random random) {
    final List<BlockPos> flames = new ArrayList<>();
    for (final BlockPos pos :
        BlockPos.iterate(
            centre.add(-RADIUS, -RADIUS, -RADIUS), centre.add(RADIUS, RADIUS, RADIUS))) {
      final BlockState state = world.getBlockState(pos);
      final boolean hasFlame = state.contains(Properties.LIT);
      if (burning(
          state.isIn(FurnitureKind.TENDED_LIGHTS),
          hasFlame,
          hasFlame && state.get(Properties.LIT))) {
        flames.add(pos.toImmutable());
      }
    }
    if (flames.isEmpty()) {
      return false;
    }
    FurnitureUse.tend(world, flames.get(random.nextInt(flames.size())), null, Chore.DOUSE);
    return true;
  }
}
