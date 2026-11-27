package com.grahambartley.nightborn.guild.rite;

import java.util.Comparator;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public final class RiteTicket {

  public static final int RADIUS = 2;
  public static final int EXPIRY_TICKS = 100;

  public static final ChunkTicketType<ChunkPos> TYPE =
      ChunkTicketType.create(
          "nightborn_rite", Comparator.comparingLong(ChunkPos::toLong), EXPIRY_TICKS);

  private RiteTicket() {}

  public static void keep(final ServerWorld world, final BlockPos at) {
    final ChunkPos chunk = new ChunkPos(at);
    world.getChunkManager().addTicket(TYPE, chunk, RADIUS, chunk);
  }

  public static boolean loaded(final ServerWorld world, final BlockPos at, final int radius) {
    return ChunkPos.stream(
            new ChunkPos(at.add(-radius, 0, -radius)), new ChunkPos(at.add(radius, 0, radius)))
        .allMatch(chunk -> world.isChunkLoaded(chunk.x, chunk.z));
  }
}
