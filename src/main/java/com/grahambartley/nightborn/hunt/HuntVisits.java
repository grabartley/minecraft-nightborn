package com.grahambartley.nightborn.hunt;

import com.grahambartley.nightborn.config.HuntSettings;
import com.grahambartley.nightborn.inquisitor.InquisitorArrival;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

final class HuntVisits {

  private HuntVisits() {}

  static HuntRecord check(
      final ServerPlayerEntity player,
      final HuntRecord record,
      final long now,
      final HuntSettings settings) {
    return switch (record.phase()) {
      case DUE -> arriving(player, now, settings) ? arrive(player, record) : record;
      case VISIT -> gone(player, record) ? record.returning() : record;
      case NONE, WARNING -> record;
    };
  }

  static boolean underOpenSky(final ServerPlayerEntity player) {
    final ServerWorld world = player.getServerWorld();
    return world.getRegistryKey() == World.OVERWORLD && world.isSkyVisible(player.getBlockPos());
  }

  private static boolean arriving(
      final ServerPlayerEntity player, final long now, final HuntSettings settings) {
    return player.isAlive()
        && !player.isSpectator()
        && !player.hasAttached(HuntAttachment.CALL)
        && HuntRules.canArrive(
            player.getServerWorld().getRegistryKey() == World.OVERWORLD,
            underOpenSky(player),
            HuntRules.inArrivalWindow(now, settings));
  }

  private static HuntRecord arrive(final ServerPlayerEntity player, final HuntRecord record) {
    return InquisitorArrival.arrive(player, false, record.man())
        .map(man -> record.visitedBy(man.getUuid(), man.givenName()))
        .orElse(record);
  }

  private static boolean gone(final ServerPlayerEntity player, final HuntRecord record) {
    final Entity man =
        record.visitor().map(id -> player.server.getOverworld().getEntity(id)).orElse(null);
    final int missing = man == null ? player.getAttachedOrElse(HuntAttachment.MISSING, 0) + 1 : 0;
    final boolean lost = HuntRules.lost(man != null && !man.isAlive(), missing);
    if (lost || missing == 0) {
      player.removeAttached(HuntAttachment.MISSING);
    } else {
      player.setAttached(HuntAttachment.MISSING, missing);
    }
    return lost;
  }
}
