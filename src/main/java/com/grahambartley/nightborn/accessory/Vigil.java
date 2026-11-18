package com.grahambartley.nightborn.accessory;

import com.grahambartley.nightborn.vampire.Vampires;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;

public final class Vigil {

  static final int CHECK_TICKS = 20;
  static final int CHILL_FROST_TICKS = 100;
  static final String CHILL = "nightborn.vigil.chill";

  private static final Map<UUID, Set<UUID>> FELT = new HashMap<>();

  private Vigil() {}

  static void tick(final MinecraftServer server) {
    if (server.getTicks() % CHECK_TICKS != 0) {
      return;
    }
    for (final ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
      watch(player);
    }
  }

  private static void watch(final ServerPlayerEntity watcher) {
    final double range = GiftAttributes.value(watcher, GiftAttributes.VIGIL_RANGE);
    if (range <= 0.0 || !watcher.isAlive() || watcher.isSpectator()) {
      FELT.remove(watcher.getUuid());
      return;
    }
    final Set<UUID> near =
        watcher
            .getServerWorld()
            .getEntitiesByClass(
                LivingEntity.class,
                new Box(watcher.getBlockPos()).expand(range),
                vampire ->
                    awakeVampireMob(
                            Vampires.isVampire(vampire),
                            vampire instanceof PlayerEntity,
                            vampire.isAlive(),
                            vampire.isSleeping())
                        && vampire.squaredDistanceTo(watcher) <= range * range)
            .stream()
            .map(LivingEntity::getUuid)
            .collect(Collectors.toSet());
    final Set<UUID> felt = FELT.getOrDefault(watcher.getUuid(), Set.of());
    if (newcomer(near, felt)) {
      chill(watcher);
    }
    FELT.put(watcher.getUuid(), new HashSet<>(near));
  }

  public static boolean awakeVampireMob(
      final boolean vampire, final boolean player, final boolean alive, final boolean sleeping) {
    return vampire && !player && alive && !sleeping;
  }

  public static boolean newcomer(final Set<UUID> near, final Set<UUID> felt) {
    return near.stream().anyMatch(id -> !felt.contains(id));
  }

  private static void chill(final ServerPlayerEntity watcher) {
    watcher.setFrozenTicks(Math.max(watcher.getFrozenTicks(), CHILL_FROST_TICKS));
    watcher.playSoundToPlayer(
        SoundEvents.ENTITY_PLAYER_HURT_FREEZE, SoundCategory.PLAYERS, 0.7F, 0.6F);
    watcher.sendMessage(Text.translatable(CHILL), true);
    watcher
        .getServerWorld()
        .spawnParticles(
            watcher,
            ParticleTypes.SNOWFLAKE,
            false,
            watcher.getX(),
            watcher.getBodyY(0.6),
            watcher.getZ(),
            12,
            0.4,
            0.5,
            0.4,
            0.01);
  }

  public static void clearAll() {
    FELT.clear();
  }
}
