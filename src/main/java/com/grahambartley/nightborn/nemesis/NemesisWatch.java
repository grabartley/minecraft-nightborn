package com.grahambartley.nightborn.nemesis;

import com.grahambartley.nightborn.nemesis.NemesisLines.Moment;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public final class NemesisWatch {

  static final double SIGHT = 32.0;

  private static final Map<UUID, MobEntity> LOADED = new HashMap<>();
  private static final Set<UUID> SIGHTED = new HashSet<>();

  private NemesisWatch() {}

  static void loaded(final Entity entity, final ServerWorld world) {
    if (entity instanceof MobEntity mob && NemesisAttachment.get(mob).isPresent()) {
      LOADED.put(mob.getUuid(), mob);
    }
  }

  static void unloaded(final Entity entity, final ServerWorld world) {
    if (!(entity instanceof MobEntity mob) || LOADED.get(mob.getUuid()) != mob) {
      return;
    }
    LOADED.remove(mob.getUuid());
    SIGHTED.remove(mob.getUuid());
    NemesisAttachment.get(mob)
        .ifPresent(
            mark -> {
              final NemesisLedger ledger = NemesisLedger.of(world.getServer());
              ledger
                  .entry(mark.quarry(), mob.getUuid())
                  .filter(entry -> entry.current(mark) && mob.isAlive())
                  .ifPresent(
                      entry -> {
                        final var snapshot = NemesisMaking.snapshot(mob);
                        if (!snapshot.isEmpty()) {
                          ledger.put(
                              mark.quarry(),
                              entry.withSnapshot(mob.getName().getString(), snapshot));
                        }
                      });
            });
  }

  static void track(final MobEntity mob) {
    LOADED.put(mob.getUuid(), mob);
  }

  static void tick(final MinecraftServer server) {
    final NemesisLedger ledger = NemesisLedger.of(server);
    for (final MobEntity mob : List.copyOf(LOADED.values())) {
      if (mob.isRemoved()) {
        LOADED.remove(mob.getUuid());
        continue;
      }
      final Optional<NemesisMark> mark = NemesisAttachment.get(mob);
      final Optional<NemesisEntry> entry =
          mark.flatMap(held -> ledger.entry(held.quarry(), mob.getUuid()));
      if (mark.isEmpty() || entry.isEmpty()) {
        NemesisAttachment.clear(mob);
        LOADED.remove(mob.getUuid());
      } else if (!entry.get().current(mark.get())) {
        LOADED.remove(mob.getUuid());
        mob.remove(Entity.RemovalReason.CHANGED_DIMENSION);
      } else {
        watch(mob, server.getPlayerManager().getPlayer(mark.get().quarry()));
      }
    }
  }

  static void clear() {
    LOADED.clear();
    SIGHTED.clear();
  }

  private static void watch(final MobEntity mob, final ServerPlayerEntity quarry) {
    final boolean near =
        quarry != null
            && quarry.getWorld() == mob.getWorld()
            && quarry.isAlive()
            && !quarry.isSpectator()
            && !quarry.isCreative()
            && mob.squaredDistanceTo(quarry) <= SIGHT * SIGHT
            && mob.canSee(quarry);
    if (near && mob.getTarget() == null && mob.canTarget(quarry)) {
      mob.setTarget(quarry);
    }
    if (near && mob.getTarget() == quarry) {
      if (SIGHTED.add(mob.getUuid())) {
        NemesisTaunts.say(mob, quarry, Moment.SIGHTED);
      }
    } else if (mob.getTarget() == null) {
      SIGHTED.remove(mob.getUuid());
    }
  }
}
