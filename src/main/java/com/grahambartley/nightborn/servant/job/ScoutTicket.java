package com.grahambartley.nightborn.servant.job;

import java.util.Comparator;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;

public final class ScoutTicket {

  public static final int RADIUS = 3;
  public static final int REFRESH_TICKS = 20;
  public static final int EXPIRY_TICKS = REFRESH_TICKS * 5;

  public static final ChunkTicketType<ChunkPos> TYPE =
      ChunkTicketType.create(
          "nightborn_scout", Comparator.comparingLong(ChunkPos::toLong), EXPIRY_TICKS);

  private ScoutTicket() {}

  public static void keep(final ServerWorld world, final Entity scout) {
    final ChunkPos chunk = scout.getChunkPos();
    world.getChunkManager().addTicket(TYPE, chunk, RADIUS, chunk);
  }
}
