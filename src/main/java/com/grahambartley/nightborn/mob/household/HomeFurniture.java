package com.grahambartley.nightborn.mob.household;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;

public final class HomeFurniture {

  static final int REACH_UP = 8;
  static final int REACH_DOWN = 4;

  private HomeFurniture() {}

  public static Map<FurnitureKind, List<BlockPos>> scan(
      final ServerWorld world, final BlockPos home, final int radius) {
    final Map<FurnitureKind, List<BlockPos>> found = new EnumMap<>(FurnitureKind.class);
    final BlockPos.Mutable at = new BlockPos.Mutable();
    for (int x = home.getX() - radius; x <= home.getX() + radius; x++) {
      for (int z = home.getZ() - radius; z <= home.getZ() + radius; z++) {
        if (!world.isChunkLoaded(
            ChunkSectionPos.getSectionCoord(x), ChunkSectionPos.getSectionCoord(z))) {
          continue;
        }
        for (int y = home.getY() - REACH_DOWN; y <= home.getY() + REACH_UP; y++) {
          at.set(x, y, z);
          final BlockPos pos = at.toImmutable();
          FurnitureKind.of(world.getBlockState(at))
              .ifPresent(kind -> found.computeIfAbsent(kind, k -> new ArrayList<>()).add(pos));
        }
      }
    }
    return found;
  }
}
