package com.grahambartley.nightborn.coffin;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class CoffinRespawn {

  private CoffinRespawn() {}

  public static void keepOnlyAtOwnCoffin(final ServerPlayerEntity player) {
    final BlockPos pos = player.getSpawnPointPosition();
    final ServerWorld world = player.server.getWorld(player.getSpawnPointDimension());
    if (pos != null
        && world != null
        && !holds(
            player.isSpawnForced(),
            CoffinSleepers.isCoffin(world, pos),
            CoffinOwnership.owner(world, pos),
            player.getUuid())) {
      clear(player);
    }
  }

  public static void clear(final ServerPlayerEntity player) {
    player.setSpawnPoint(World.OVERWORLD, null, 0.0F, false, false);
  }

  static void set(final ServerPlayerEntity player, final BlockPos head) {
    player.setSpawnPoint(
        player.getServerWorld().getRegistryKey(), head, player.getYaw(), false, true);
  }

  static void forget(final MinecraftServer server, final UUID owner, final GlobalPos coffin) {
    final ServerPlayerEntity player = server.getPlayerManager().getPlayer(owner);
    if (player != null
        && pointsAt(player.getSpawnPointDimension(), player.getSpawnPointPosition(), coffin)) {
      clear(player);
    }
  }

  static boolean holds(
      final boolean forced, final boolean coffin, final Optional<UUID> owner, final UUID player) {
    return forced || !coffin || CoffinRest.of(owner, player) == CoffinRest.RETURN;
  }

  static boolean pointsAt(
      final RegistryKey<World> dimension, @Nullable final BlockPos pos, final GlobalPos coffin) {
    return coffin.dimension().equals(dimension) && coffin.pos().equals(pos);
  }
}
