package com.grahambartley.nightborn.servant.job;

import com.grahambartley.nightborn.core.ChunkHold;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;

public final class ScoutTicket {

  public static final int RADIUS = 3;
  public static final int REFRESH_TICKS = 20;
  public static final int EXPIRY_TICKS = REFRESH_TICKS * 5;

  private static final ChunkHold HOLD = ChunkHold.of("nightborn_scout", RADIUS, EXPIRY_TICKS);

  private ScoutTicket() {}

  public static void keep(final ServerWorld world, final Entity scout) {
    HOLD.keep(world, scout.getChunkPos());
  }
}
