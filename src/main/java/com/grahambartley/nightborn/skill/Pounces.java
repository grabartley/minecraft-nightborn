package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.hold.HoldKind;
import com.grahambartley.nightborn.hold.Holds;
import com.grahambartley.nightborn.slayer.Guards;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public final class Pounces {

  static final int SLACK_TICKS = 10;

  private record Pounce(RegistryKey<World> world, UUID target, PouncePower power, long until) {}

  private static final Map<UUID, Pounce> POUNCES = new HashMap<>();

  private Pounces() {}

  static void register() {
    ServerTickEvents.END_SERVER_TICK.register(Pounces::tick);
    ServerLifecycleEvents.SERVER_STOPPED.register(server -> POUNCES.clear());
  }

  public static void start(
      final LivingEntity caster, final LivingEntity target, final PouncePower power) {
    caster.setVelocity(PounceArc.launch(caster.getPos(), target.getPos()));
    caster.velocityModified = true;
    caster.fallDistance = 0.0F;
    final long now = caster.getWorld().getTime();
    POUNCES.put(
        caster.getUuid(),
        new Pounce(
            caster.getWorld().getRegistryKey(),
            target.getUuid(),
            power,
            now + PounceArc.flightTicks(caster.distanceTo(target)) + SLACK_TICKS));
  }

  public static void stop(final Entity caster) {
    POUNCES.remove(caster.getUuid());
  }

  private static void tick(final MinecraftServer server) {
    if (POUNCES.isEmpty()) {
      return;
    }
    final Iterator<Map.Entry<UUID, Pounce>> pounces = POUNCES.entrySet().iterator();
    while (pounces.hasNext()) {
      final Map.Entry<UUID, Pounce> entry = pounces.next();
      final Pounce pounce = entry.getValue();
      final ServerWorld world = server.getWorld(pounce.world());
      final Entity caster = world == null ? null : world.getEntity(entry.getKey());
      final Entity target = world == null ? null : world.getEntity(pounce.target());
      if (!(caster instanceof LivingEntity hunter)
          || !(target instanceof LivingEntity prey)
          || world.getTime() > pounce.until()) {
        pounces.remove();
        continue;
      }
      hunter.fallDistance = 0.0F;
      if (PounceArc.caught(hunter.distanceTo(prey))) {
        pounces.remove();
        if (Guards.turns(prey, hunter)) {
          continue;
        }
        Holds.hold(hunter, prey, HoldKind.PIN, pounce.power().pinFor(prey instanceof PlayerEntity));
      }
    }
  }
}
