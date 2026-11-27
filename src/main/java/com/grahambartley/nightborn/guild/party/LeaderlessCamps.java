package com.grahambartley.nightborn.guild.party;

import com.grahambartley.nightborn.guild.GuildMasterEntity;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;

public final class LeaderlessCamps {

  public static final int CHECK_TICKS = 100;

  private LeaderlessCamps() {}

  public static void register() {
    ServerTickEvents.END_WORLD_TICK.register(LeaderlessCamps::tick);
  }

  public static boolean clears(final boolean over, final boolean loaded, final boolean led) {
    return over && loaded && !led;
  }

  private static void tick(final ServerWorld world) {
    if (world.getTime() % CHECK_TICKS != 0) {
      return;
    }
    final PitchedCamps pitched = PitchedCamps.of(world);
    for (final Map.Entry<UUID, PitchedCamps.Pitched> entry : pitched.all().entrySet()) {
      final HunterParty.Camp camp = entry.getValue().camp();
      if (clears(
          entry.getValue().stay().over(world.getTimeOfDay(), world.getTime()),
          loaded(world, camp),
          world.getEntity(entry.getKey()) instanceof GuildMasterEntity master
              && master.isAlive())) {
        PartyCamp.strike(world, camp);
        pitched.struck(entry.getKey());
      }
    }
  }

  private static boolean loaded(final ServerWorld world, final HunterParty.Camp camp) {
    return Stream.concat(Stream.of(camp.fire()), camp.blocks().stream())
        .allMatch(block -> loaded(world, block));
  }

  private static boolean loaded(final ServerWorld world, final BlockPos block) {
    return world.isChunkLoaded(
        ChunkSectionPos.getSectionCoord(block.getX()),
        ChunkSectionPos.getSectionCoord(block.getZ()));
  }
}
