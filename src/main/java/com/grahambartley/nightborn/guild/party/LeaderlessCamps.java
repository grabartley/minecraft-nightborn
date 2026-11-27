package com.grahambartley.nightborn.guild.party;

import com.grahambartley.nightborn.guild.GuildMasterEntity;
import com.grahambartley.nightborn.village.VillageGuard;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.world.ServerWorld;

public final class LeaderlessCamps {

  public static final int CHECK_TICKS = 100;

  private LeaderlessCamps() {}

  public static void register() {
    ServerTickEvents.END_WORLD_TICK.register(LeaderlessCamps::tick);
  }

  public static boolean clears(final boolean over, final boolean loaded, final boolean led) {
    return over && loaded && !led;
  }

  public static boolean holds(final boolean alive, final Optional<HunterParty> party) {
    return alive && party.filter(HunterParty::pitched).isPresent();
  }

  private static void tick(final ServerWorld world) {
    if (world.getTime() % CHECK_TICKS != 0) {
      return;
    }
    final PitchedCamps pitched = PitchedCamps.of(world);
    for (final Map.Entry<UUID, PitchedCamps.Pitched> entry : pitched.all().entrySet()) {
      final HunterParty.Camp camp = entry.getValue().camp();
      final boolean led =
          world.getEntity(entry.getKey()) instanceof GuildMasterEntity master
              && holds(master.isAlive(), Optional.ofNullable(master.party()));
      final CampStay stay =
          led ? entry.getValue().stay() : relieved(world, entry.getValue().stay());
      pitched.kept(entry.getKey(), stay);
      if (clears(stay.over(world.getTimeOfDay(), world.getTime()), loaded(world, camp), led)) {
        PartyCamp.strike(world, camp);
        pitched.struck(entry.getKey());
      }
    }
  }

  private static CampStay relieved(final ServerWorld world, final CampStay stay) {
    return !stay.released()
            && stay.guards().filter(bell -> !VillageGuard.stillGuarded(world, bell)).isPresent()
        ? stay.released(world.getTime())
        : stay;
  }

  private static boolean loaded(final ServerWorld world, final HunterParty.Camp camp) {
    return Stream.concat(Stream.of(camp.fire()), camp.blocks().stream())
        .allMatch(block -> world.isPosLoaded(block.getX(), block.getZ()));
  }
}
