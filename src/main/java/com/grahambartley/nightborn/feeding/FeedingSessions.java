package com.grahambartley.nightborn.feeding;

import com.grahambartley.nightborn.blood.BloodQuality;
import com.grahambartley.nightborn.config.NightbornConfig;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.ward.Wards;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

public final class FeedingSessions {

  static final int RESIST_COOLDOWN_TICKS = 40;
  static final double REACH_SLACK = 1.5;

  private static final Map<UUID, FeedingSession> SESSIONS = new HashMap<>();
  private static final Map<UUID, Long> LAST_FED = new HashMap<>();
  private static final Map<UUID, Long> SHAKEN_OFF_UNTIL = new HashMap<>();

  private FeedingSessions() {}

  public static void register() {
    UseEntityCallback.EVENT.register(FeedingInteraction::onUse);
    ServerTickEvents.END_SERVER_TICK.register(FeedingSessions::tick);
    ServerLifecycleEvents.SERVER_STOPPED.register(server -> clearAll());
    ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> end(handler.getPlayer()));
    ServerEntityEvents.ENTITY_UNLOAD.register(
        (entity, world) -> {
          if (!(entity instanceof PlayerEntity)) {
            LAST_FED.remove(entity.getUuid());
          }
        });
  }

  public static void fed(final Entity vampire, final long now) {
    LAST_FED.put(vampire.getUuid(), now);
  }

  public static boolean isFeeding(final Entity vampire) {
    return SESSIONS.containsKey(vampire.getUuid());
  }

  public static long ticksSinceFed(final Entity vampire, final long now) {
    final Long last = LAST_FED.get(vampire.getUuid());
    return last == null ? -1 : now - last;
  }

  public static void end(final LivingEntity vampire) {
    final FeedingSession session = SESSIONS.remove(vampire.getUuid());
    if (session != null && vampire instanceof ServerPlayerEntity player) {
      finish(player, session, ServerConfigService.current());
    }
  }

  static void hold(
      final ServerPlayerEntity vampire, final LivingEntity victim, final BloodQuality quality) {
    final long now = vampire.getServerWorld().getTime();
    if (now < SHAKEN_OFF_UNTIL.getOrDefault(vampire.getUuid(), Long.MIN_VALUE)) {
      return;
    }
    final FeedingSession current = SESSIONS.get(vampire.getUuid());
    if (current != null && current.victim().equals(victim.getUuid())) {
      current.held(now);
      return;
    }
    end(vampire);
    SESSIONS.put(vampire.getUuid(), new FeedingSession(victim.getUuid(), quality, now));
  }

  private static void tick(final MinecraftServer server) {
    if (SESSIONS.isEmpty()) {
      return;
    }
    final NightbornConfig config = ServerConfigService.current();
    final Iterator<Map.Entry<UUID, FeedingSession>> sessions = SESSIONS.entrySet().iterator();
    while (sessions.hasNext()) {
      final Map.Entry<UUID, FeedingSession> entry = sessions.next();
      final ServerPlayerEntity vampire = server.getPlayerManager().getPlayer(entry.getKey());
      final FeedingSession session = entry.getValue();
      if (vampire == null) {
        sessions.remove();
        continue;
      }
      final ServerWorld world = vampire.getServerWorld();
      final long now = world.getTime();
      final LivingEntity victim = victimOf(world, session);
      if (victim == null
          || !victim.isAlive()
          || session.released(now)
          || Wards.wards(victim)
          || !inReach(vampire, victim)) {
        sessions.remove();
        finish(vampire, victim, session, config);
        continue;
      }
      if (session.drinkDue(now)) {
        final Feeder.Result result = Feeder.drink(world, vampire, victim, session, config);
        if (result != Feeder.Result.CONTINUE) {
          sessions.remove();
          finish(vampire, victim, session, config);
          if (result == Feeder.Result.RESISTED) {
            SHAKEN_OFF_UNTIL.put(vampire.getUuid(), now + RESIST_COOLDOWN_TICKS);
          }
          continue;
        }
      }
      VictimHold.apply(victim, session.anchorAt(victim.getPos()));
    }
  }

  private static void finish(
      final ServerPlayerEntity vampire,
      final FeedingSession session,
      final NightbornConfig config) {
    finish(vampire, victimOf(vampire.getServerWorld(), session), session, config);
  }

  private static void finish(
      final ServerPlayerEntity vampire,
      @Nullable final LivingEntity victim,
      final FeedingSession session,
      final NightbornConfig config) {
    fed(vampire, vampire.getServerWorld().getTime());
    Feeder.finish(vampire, victim, session, config);
  }

  @Nullable
  private static LivingEntity victimOf(final ServerWorld world, final FeedingSession session) {
    return world.getEntity(session.victim()) instanceof LivingEntity living ? living : null;
  }

  private static boolean inReach(final ServerPlayerEntity vampire, final LivingEntity victim) {
    final double reach = vampire.getEntityInteractionRange() + REACH_SLACK;
    return vampire.squaredDistanceTo(victim) <= reach * reach;
  }

  public static void clearAll() {
    SESSIONS.clear();
    LAST_FED.clear();
    SHAKEN_OFF_UNTIL.clear();
  }
}
