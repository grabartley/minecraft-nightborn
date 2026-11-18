package com.grahambartley.nightborn.core;

import java.util.Optional;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;

public final class SurfaceSpots {

  private SurfaceSpots() {}

  public static Optional<BlockPos> around(
      final ServerWorld world,
      final BlockPos near,
      final int min,
      final int max,
      final Random random) {
    return at(
        world, offset(near, random.nextFloat() * MathHelper.TAU, random.nextBetween(min, max)));
  }

  public static Optional<BlockPos> at(final ServerWorld world, final BlockPos column) {
    final int x = column.getX();
    final int z = column.getZ();
    if (!loaded(world, x, z)) {
      return Optional.empty();
    }
    final BlockPos spot =
        new BlockPos(x, world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z), z);
    return world.getFluidState(spot.down()).isEmpty() ? Optional.of(spot) : Optional.empty();
  }

  public static boolean loaded(final ServerWorld world, final int x, final int z) {
    return world.isChunkLoaded(
        ChunkSectionPos.getSectionCoord(x), ChunkSectionPos.getSectionCoord(z));
  }

  public static BlockPos offset(final BlockPos near, final float angle, final int distance) {
    return new BlockPos(
        near.getX() + Math.round(MathHelper.cos(angle) * distance),
        near.getY(),
        near.getZ() + Math.round(MathHelper.sin(angle) * distance));
  }
}
