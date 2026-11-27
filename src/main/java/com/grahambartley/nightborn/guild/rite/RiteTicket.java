package com.grahambartley.nightborn.guild.rite;

import com.grahambartley.nightborn.core.ChunkHold;
import java.util.List;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public final class RiteTicket {

  public static final int RADIUS = 2;
  public static final int EXPIRY_TICKS = 100;

  private static final ChunkHold HOLD = ChunkHold.of("nightborn_rite", RADIUS, EXPIRY_TICKS);

  private RiteTicket() {}

  public static void keep(final ServerWorld world, final BlockPos at) {
    HOLD.keep(world, new ChunkPos(at));
  }

  public static List<ChunkPos> span(final BlockPos at, final int radius) {
    return ChunkPos.stream(
            new ChunkPos(at.add(-radius, 0, -radius)), new ChunkPos(at.add(radius, 0, radius)))
        .toList();
  }

  public static boolean loaded(final ServerWorld world, final BlockPos at, final int radius) {
    return world.shouldTickEntity(at)
        && span(at, radius).stream().allMatch(chunk -> world.isChunkLoaded(chunk.x, chunk.z));
  }
}
