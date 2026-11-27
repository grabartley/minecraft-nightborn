package com.grahambartley.nightborn.core;

import java.util.Comparator;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;

public record ChunkHold(ChunkTicketType<ChunkPos> type, int radius) {

  public static ChunkHold of(final String name, final int radius, final int expiryTicks) {
    return new ChunkHold(
        ChunkTicketType.create(name, Comparator.comparingLong(ChunkPos::toLong), expiryTicks),
        radius);
  }

  public void keep(final ServerWorld world, final ChunkPos chunk) {
    world.getChunkManager().addTicket(type, chunk, radius, chunk);
  }
}
