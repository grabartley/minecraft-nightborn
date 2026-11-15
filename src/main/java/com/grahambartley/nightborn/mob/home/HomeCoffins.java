package com.grahambartley.nightborn.mob.home;

import com.grahambartley.nightborn.coffin.CoffinBlock;
import com.grahambartley.nightborn.coffin.CoffinBlockEntity;
import com.grahambartley.nightborn.coffin.CoffinOwnership;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BedPart;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

public final class HomeCoffins {

  private HomeCoffins() {}

  public static boolean loaded(final ServerWorld world, final BlockPos pos) {
    return world.getChunkManager().getWorldChunk(pos.getX() >> 4, pos.getZ() >> 4) != null;
  }

  public static Optional<Boolean> keeps(
      final ServerWorld world, final BlockPos coffin, final UUID vampire) {
    if (!loaded(world, coffin)) {
      return Optional.empty();
    }
    return Optional.of(
        CoffinClaim.keeps(
            head(world, coffin), CoffinOwnership.ownerAt(world, coffin).orElse(null), vampire));
  }

  public static Optional<BlockPos> claim(
      final ServerWorld world, final BlockPos home, final int radius, final UUID vampire) {
    final ChunkPos min = new ChunkPos(home.add(-radius, 0, -radius));
    final ChunkPos max = new ChunkPos(home.add(radius, 0, radius));
    Optional<CoffinBlockEntity> best = Optional.empty();
    for (int x = min.x; x <= max.x; x++) {
      for (int z = min.z; z <= max.z; z++) {
        final WorldChunk chunk = world.getChunkManager().getWorldChunk(x, z);
        if (chunk == null) {
          continue;
        }
        final Optional<CoffinBlockEntity> nearest =
            chunk.getBlockEntities().values().stream()
                .filter(CoffinBlockEntity.class::isInstance)
                .map(CoffinBlockEntity.class::cast)
                .filter(coffin -> coffin.getPos().isWithinDistance(home, radius))
                .filter(
                    coffin ->
                        CoffinClaim.mayClaim(
                            head(world, coffin.getPos()),
                            CoffinOwnership.ownerAt(world, coffin.getPos()).orElse(null)))
                .min(
                    Comparator.comparingDouble(coffin -> coffin.getPos().getSquaredDistance(home)));
        if (nearest.isPresent()
            && (best.isEmpty()
                || nearest.get().getPos().getSquaredDistance(home)
                    < best.get().getPos().getSquaredDistance(home))) {
          best = nearest;
        }
      }
    }
    return best.map(CoffinBlockEntity::getPos)
        .filter(coffin -> CoffinOwnership.claim(world, coffin, vampire));
  }

  public static void release(final ServerWorld world, final UUID vampire) {
    CoffinOwnership.release(world.getServer(), vampire);
  }

  public static boolean free(final ServerWorld world, final BlockPos coffin) {
    final BlockState state = world.getBlockState(coffin);
    return state.getBlock() instanceof CoffinBlock && !state.get(BedBlock.OCCUPIED);
  }

  private static boolean head(final ServerWorld world, final BlockPos pos) {
    final BlockState state = world.getBlockState(pos);
    return state.getBlock() instanceof CoffinBlock && state.get(BedBlock.PART) == BedPart.HEAD;
  }
}
