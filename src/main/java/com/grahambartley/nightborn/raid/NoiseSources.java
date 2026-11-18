package com.grahambartley.nightborn.raid;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;

public final class NoiseSources {

  static final int SAMPLE_TICKS = 20;

  private record Steps(int walked, int sprinted, int jumps) {}

  private static final Map<UUID, Steps> LAST = new HashMap<>();

  private NoiseSources() {}

  static void register() {
    ServerTickEvents.END_SERVER_TICK.register(NoiseSources::tick);
    PlayerBlockBreakEvents.AFTER.register(
        (world, player, pos, state, entity) -> {
          if (player instanceof ServerPlayerEntity breaker) {
            HouseNoise.make(breaker, NoiseAct.BREAK);
          }
        });
    ServerLivingEntityEvents.AFTER_DAMAGE.register(
        (entity, source, base, taken, blocked) -> blow(entity, source));
    ServerPlayConnectionEvents.DISCONNECT.register(
        (handler, server) -> LAST.remove(handler.getPlayer().getUuid()));
    ServerLifecycleEvents.SERVER_STOPPED.register(
        server -> {
          LAST.clear();
          HouseNoise.clear();
        });
  }

  private static void blow(final LivingEntity hurt, final DamageSource source) {
    if (source.getAttacker() instanceof ServerPlayerEntity attacker) {
      HouseNoise.make(attacker, NoiseAct.BLOW);
    } else if (source.getAttacker() != null && hurt instanceof ServerPlayerEntity victim) {
      HouseNoise.make(victim, NoiseAct.BLOW);
    }
  }

  private static void tick(final MinecraftServer server) {
    if (server.getTicks() % SAMPLE_TICKS != 0) {
      return;
    }
    for (final ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
      final Steps now = steps(player);
      final Steps before = LAST.put(player.getUuid(), now);
      if (before == null) {
        continue;
      }
      for (final NoiseAct act :
          Footsteps.of(
              now.walked() - before.walked(),
              now.sprinted() - before.sprinted(),
              now.jumps() - before.jumps())) {
        HouseNoise.make(player, act);
      }
    }
    HouseNoise.settle(server.getOverworld().getTime());
  }

  private static Steps steps(final ServerPlayerEntity player) {
    final ServerStatHandler stats = player.getStatHandler();
    return new Steps(
        count(stats, Stats.WALK_ONE_CM),
        count(stats, Stats.SPRINT_ONE_CM),
        count(stats, Stats.JUMP));
  }

  private static int count(final ServerStatHandler stats, final Identifier stat) {
    return stats.getStat(Stats.CUSTOM.getOrCreateStat(stat));
  }
}
