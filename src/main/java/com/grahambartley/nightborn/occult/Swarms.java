package com.grahambartley.nightborn.occult;

import com.grahambartley.nightborn.downed.DownedTargeting;
import com.grahambartley.nightborn.fight.RecentBlows;
import com.grahambartley.nightborn.skill.AreaRules;
import com.grahambartley.nightborn.skill.PowerReach;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public final class Swarms {

  private record Swarm(RegistryKey<World> world, UUID caster, SwarmPower power, long startedAt) {}

  private static final List<Swarm> SWARMS = new ArrayList<>();

  private Swarms() {}

  static void register() {
    ServerTickEvents.END_SERVER_TICK.register(Swarms::tick);
    ServerLifecycleEvents.SERVER_STOPPED.register(server -> SWARMS.clear());
  }

  public static boolean bites(
      final boolean hunterMob, final boolean foughtRecently, final boolean reachable) {
    return reachable && (hunterMob || foughtRecently);
  }

  public static void start(final LivingEntity caster, final SwarmPower power) {
    SWARMS.removeIf(swarm -> swarm.caster().equals(caster.getUuid()));
    SWARMS.add(
        new Swarm(
            caster.getWorld().getRegistryKey(),
            caster.getUuid(),
            power,
            caster.getWorld().getTime()));
  }

  private static void tick(final MinecraftServer server) {
    final Iterator<Swarm> swarms = SWARMS.iterator();
    while (swarms.hasNext()) {
      final Swarm swarm = swarms.next();
      final ServerWorld world = server.getWorld(swarm.world());
      final Entity found = world == null ? null : world.getEntity(swarm.caster());
      if (!(found instanceof LivingEntity caster)
          || !caster.isAlive()
          || !AreaRules.lasts(world.getTime(), swarm.startedAt() + swarm.power().durationTicks())) {
        swarms.remove();
        continue;
      }
      final long age = world.getTime() - swarm.startedAt();
      if (age % 4 == 0) {
        flutter(world, caster, swarm.power());
      }
      if (age % swarm.power().everyTicks() == 0) {
        sting(world, caster, swarm.power());
      }
    }
  }

  private static void flutter(
      final ServerWorld world, final LivingEntity caster, final SwarmPower power) {
    final double radius = power.radius();
    world.spawnParticles(
        ParticleTypes.SQUID_INK,
        caster.getX(),
        caster.getY() + 2.0,
        caster.getZ(),
        (int) Math.ceil(radius),
        radius * 0.5,
        1.0,
        radius * 0.5,
        0.05);
    if (caster.getRandom().nextInt(4) == 0) {
      world.playSound(
          null,
          caster.getBlockPos(),
          SoundEvents.ENTITY_BAT_AMBIENT,
          SoundCategory.HOSTILE,
          1.0F,
          0.8F);
    }
  }

  private static void sting(
      final ServerWorld world, final LivingEntity caster, final SwarmPower power) {
    final double radius = power.radius();
    for (final LivingEntity living :
        world.getEntitiesByClass(
            LivingEntity.class,
            new Box(caster.getBlockPos()).expand(radius),
            candidate ->
                candidate.squaredDistanceTo(caster) <= radius * radius
                    && bites(
                        candidate.getType().isIn(DownedTargeting.HUNTERS),
                        RecentBlows.fought(caster, candidate, power.recentTicks()),
                        PowerReach.touches(caster, candidate)))) {
      living.damage(world.getDamageSources().indirectMagic(caster, caster), power.damage());
    }
  }
}
