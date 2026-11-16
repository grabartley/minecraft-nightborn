package com.grahambartley.nightborn.mob.household;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;

public final class HomeFurniture {

  static final int REACH_UP = 8;
  static final int REACH_DOWN = 4;
  static final long RESCAN_TICKS = 1200L;

  private record Home(BlockPos heart, int radius) {}

  private record Scan(Map<FurnitureKind, List<BlockPos>> found, long at) {}

  private static final Map<ServerWorld, Map<Home, Scan>> SCANS = new WeakHashMap<>();

  private HomeFurniture() {}

  public static Map<FurnitureKind, List<BlockPos>> around(
      final ServerWorld world, final BlockPos home, final int radius) {
    final long now = world.getTime();
    final Map<Home, Scan> scans = SCANS.computeIfAbsent(world, w -> new HashMap<>());
    final Home key = new Home(home.toImmutable(), radius);
    final Scan known = scans.get(key);
    if (known != null && !Rescan.due(known.at(), now, RESCAN_TICKS)) {
      return known.found();
    }
    scans.values().removeIf(scan -> Rescan.due(scan.at(), now, RESCAN_TICKS * 2));
    final Scan fresh = new Scan(scan(world, home, radius), now);
    scans.put(key, fresh);
    return fresh.found();
  }

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
          FurnitureKind.of(world.getBlockState(at))
              .ifPresent(
                  kind ->
                      found.computeIfAbsent(kind, k -> new ArrayList<>()).add(at.toImmutable()));
        }
      }
    }
    final Map<FurnitureKind, List<BlockPos>> frozen = new EnumMap<>(FurnitureKind.class);
    found.forEach((kind, spots) -> frozen.put(kind, List.copyOf(spots)));
    return Collections.unmodifiableMap(frozen);
  }
}
