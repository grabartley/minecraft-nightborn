package com.grahambartley.nightborn.seeker;

import com.grahambartley.nightborn.sight.Sights;
import com.grahambartley.nightborn.skill.PowerReach;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public final class Revelations {

  private static final Map<UUID, Revealing> ACTIVE = new HashMap<>();

  private Revelations() {}

  static void register() {
    Sights.standing(Revelations::show);
    ServerTickEvents.END_SERVER_TICK.register(Revelations::tick);
    ServerLifecycleEvents.SERVER_STOPPED.register(server -> ACTIVE.clear());
  }

  public static void reveal(final LivingEntity caster, final RevealPower power) {
    if (!(caster instanceof ServerPlayerEntity hunter)) {
      return;
    }
    final List<LivingEntity> found = vampires(hunter, power.range());
    ACTIVE.put(
        hunter.getUuid(),
        new Revealing(
            found.stream().map(Entity::getUuid).collect(Collectors.toSet()),
            hunter.getServerWorld().getTime() + power.durationTicks(),
            power));
    hunter.sendMessage(
        Text.translatable("nightborn.seeker.revelation", found.size()).formatted(Formatting.GOLD),
        true);
    for (final LivingEntity vampire : found) {
      if (vampire instanceof ServerPlayerEntity player) {
        player.sendMessage(
            Text.translatable("nightborn.seeker.revealed_you").formatted(Formatting.DARK_RED),
            true);
      }
    }
  }

  public static boolean bars(final LivingEntity caster, final Identifier power) {
    if (caster.getServer() == null) {
      return false;
    }
    for (final Map.Entry<UUID, Revealing> entry : ACTIVE.entrySet()) {
      final ServerPlayerEntity hunter =
          caster.getServer().getPlayerManager().getPlayer(entry.getKey());
      if (hunter != null
          && entry.getValue().lasts(caster.getWorld().getTime())
          && entry.getValue().bars(caster.getUuid(), power)
          && PowerReach.touches(hunter, caster)) {
        return true;
      }
    }
    return false;
  }

  static Optional<Revealing> of(final UUID hunter) {
    return Optional.ofNullable(ACTIVE.get(hunter));
  }

  static Set<UUID> hunters() {
    return ACTIVE.keySet();
  }

  static List<LivingEntity> vampires(final ServerPlayerEntity hunter, final float range) {
    return hunter
        .getServerWorld()
        .getEntitiesByClass(
            LivingEntity.class,
            hunter.getBoundingBox().expand(range),
            candidate ->
                candidate != hunter
                    && candidate.isAlive()
                    && !candidate.isSpectator()
                    && Vampires.isVampire(candidate)
                    && PowerReach.marks(candidate)
                    && candidate.squaredDistanceTo(hunter) <= (double) range * range);
  }

  private static void tick(final MinecraftServer server) {
    if (ACTIVE.isEmpty()) {
      return;
    }
    ACTIVE
        .entrySet()
        .removeIf(
            entry -> {
              final ServerPlayerEntity hunter = server.getPlayerManager().getPlayer(entry.getKey());
              if (hunter == null || !entry.getValue().lasts(hunter.getServerWorld().getTime())) {
                return true;
              }
              lift(hunter, entry.getValue());
              return false;
            });
  }

  private static void lift(final ServerPlayerEntity hunter, final Revealing revealing) {
    final ServerWorld world = hunter.getServerWorld();
    for (final UUID id : revealing.vampires()) {
      if (world.getEntity(id) instanceof LivingEntity vampire
          && vampire.isAlive()
          && PowerReach.touches(hunter, vampire)
          && Veils.veiled(vampire)) {
        Veils.lift(vampire);
      }
    }
  }

  private static void show(final ServerPlayerEntity viewer, final Map<Integer, Integer> shown) {
    final boolean human = !Vampires.isVampire(viewer);
    for (final Map.Entry<UUID, Revealing> entry : ACTIVE.entrySet()) {
      final Revealing revealing = entry.getValue();
      final ServerPlayerEntity hunter =
          viewer.getServer().getPlayerManager().getPlayer(entry.getKey());
      if (hunter == null
          || hunter.getServerWorld() != viewer.getServerWorld()
          || !revealing.lasts(viewer.getServerWorld().getTime())
          || !revealing.sharedWith(hunter == viewer, human, hunter.squaredDistanceTo(viewer))) {
        continue;
      }
      for (final UUID id : revealing.vampires()) {
        if (viewer.getServerWorld().getEntity(id) instanceof LivingEntity vampire
            && vampire.isAlive()) {
          shown.putIfAbsent(vampire.getId(), revealing.power().colour());
        }
      }
    }
  }
}
