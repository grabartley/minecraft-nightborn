package com.grahambartley.nightborn.house;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.age.AgeLadder;
import com.grahambartley.nightborn.age.StageRanks;
import com.grahambartley.nightborn.vampire.Vampires;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.WorldSavePath;

public final class HouseRanks {

  static final int REFRESH_TICKS = 100;

  private HouseRanks() {}

  static void register() {
    ServerPlayConnectionEvents.JOIN.register(
        (handler, sender, server) -> refreshHouseOf(server, handler.getPlayer().getUuid()));
    ServerPlayConnectionEvents.DISCONNECT.register(
        (handler, server) -> refreshHouseOf(server, handler.getPlayer().getUuid()));
    ServerTickEvents.END_SERVER_TICK.register(
        server -> {
          if (server.getTicks() % REFRESH_TICKS == 0) {
            final Houses houses = Houses.get(server);
            houses.all().forEach(house -> houses.put(refreshed(server, house)));
          }
        });
  }

  private static void refreshHouseOf(final MinecraftServer server, final UUID member) {
    final Houses houses = Houses.get(server);
    houses.of(member).ifPresent(house -> houses.put(refreshed(server, house)));
  }

  public static House refreshed(final MinecraftServer server, final House house) {
    House known = house;
    for (final UUID member : house.everyone().toList()) {
      final Optional<LivingEntity> found = find(server, member);
      if (found.isPresent() && Vampires.isVampire(found.get())) {
        known = known.ranked(member, StageRanks.of(found.get()));
      } else if (found.isEmpty()
          && house.players().contains(member)
          && !house.ranks().containsKey(member)) {
        final int rank =
            saved(server, member)
                .map(age -> AgeLadder.of(server.getRegistryManager()).stageRank(age))
                .orElse(0);
        known = known.ranked(member, rank);
      }
    }
    return known;
  }

  private static Optional<Integer> saved(final MinecraftServer server, final UUID player) {
    final Path file =
        server.getSavePath(WorldSavePath.PLAYERDATA).resolve(player.toString() + ".dat");
    if (!Files.isRegularFile(file)) {
      return Optional.empty();
    }
    try {
      return OfflineStage.ageRank(NbtIo.readCompressed(file, NbtSizeTracker.ofUnlimitedBytes()));
    } catch (final IOException unreadable) {
      Nightborn.LOGGER.warn("Could not read the saved stage of {}", player, unreadable);
      return Optional.empty();
    }
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
