package com.grahambartley.nightborn.hunt;

import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;

public final class ArrivalSpot {

  static final double NEAREST = 16.0;
  static final double FARTHEST = 24.0;
  static final int TRIES = 12;

  private ArrivalSpot() {}

  public static BlockPos offset(final BlockPos quarry, final double turn, final double reach) {
    final double angle = turn * MathHelper.TAU;
    final double distance = MathHelper.lerp(MathHelper.clamp(reach, 0.0, 1.0), NEAREST, FARTHEST);
    return quarry.add(
        (int) Math.round(Math.cos(angle) * distance),
        0,
        (int) Math.round(Math.sin(angle) * distance));
  }

  public static Optional<BlockPos> find(
      final ServerWorld world, final BlockPos quarry, final Random random) {
    for (int attempt = 0; attempt < TRIES; attempt++) {
      final BlockPos column = offset(quarry, random.nextDouble(), random.nextDouble());
      if (!world.isChunkLoaded(column.getX() >> 4, column.getZ() >> 4)) {
        continue;
      }
      final BlockPos feet = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, column);
      final BlockState ground = world.getBlockState(feet.down());
      if (ground.isSideSolidFullSquare(world, feet.down(), Direction.UP)
          && world.getFluidState(feet).isEmpty()
          && world.isSkyVisible(feet)) {
        return Optional.of(feet);
      }
    }
    return Optional.empty();
  }
}
