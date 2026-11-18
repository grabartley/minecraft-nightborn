package com.grahambartley.nightborn.house;

import com.grahambartley.nightborn.age.StageRanks;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

public final class HouseRanks {

  static final int REFRESH_TICKS = 100;

  private HouseRanks() {}

  static void register() {
    ServerTickEvents.END_SERVER_TICK.register(
        server -> {
          if (server.getTicks() % REFRESH_TICKS == 0) {
            final Houses houses = Houses.get(server);
            houses.all().forEach(house -> houses.put(refreshed(server, house)));
          }
        });
  }

  public static House refreshed(final MinecraftServer server, final House house) {
    House known = house;
    for (final UUID member : house.everyone().toList()) {
      final Optional<LivingEntity> found = find(server, member);
      if (found.isPresent() && Vampires.isVampire(found.get())) {
        known = known.ranked(member, StageRanks.of(found.get()));
      }
    }
    return known;
  }

  public static Optional<LivingEntity> find(final MinecraftServer server, final UUID member) {
    final Entity player = server.getPlayerManager().getPlayer(member);
    if (player instanceof LivingEntity living) {
      return Optional.of(living);
    }
    for (final ServerWorld world : server.getWorlds()) {
      if (world.getEntity(member) instanceof LivingEntity living) {
        return Optional.of(living);
      }
    }
    return Optional.empty();
  }
}
