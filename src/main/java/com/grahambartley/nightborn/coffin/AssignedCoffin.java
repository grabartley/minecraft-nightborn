package com.grahambartley.nightborn.coffin;

import java.util.Optional;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class AssignedCoffin {

  private AssignedCoffin() {}

  public static Optional<BlockPos> in(final ServerPlayerEntity player, final ServerWorld world) {
    final BlockPos spawn = player.getSpawnPointPosition();
    return counts(
            spawn != null,
            player.getSpawnPointDimension() == world.getRegistryKey(),
            spawn != null && world.getBlockState(spawn).getBlock() instanceof CoffinBlock)
        ? Optional.of(spawn.toImmutable())
        : Optional.empty();
  }

  public static boolean counts(
      final boolean hasSpawnPoint, final boolean sameWorld, final boolean coffinThere) {
    return hasSpawnPoint && sameWorld && coffinThere;
  }
}
