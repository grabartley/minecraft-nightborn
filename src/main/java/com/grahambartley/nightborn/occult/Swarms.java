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
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public final class Swarms {

  private record Swarm(
      RegistryKey<World> world, UUID caster, SwarmPower power, long startedAt, UUID bats) {}

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

  static boolean ends(final boolean casterAlive, final boolean lasts) {
    return !casterAlive || !lasts;
  }

  public static void start(final LivingEntity caster, final SwarmPower power) {
    if (!(caster.getWorld() instanceof ServerWorld world)) {
      return;
    }
    SWARMS.removeIf(
        swarm -> {
          if (!swarm.caster().equals(caster.getUuid())) {
            return false;
          }
          disperse(world.getServer(), swarm);
          return true;
        });
    final BatSwarmEntity bats = new BatSwarmEntity(OccultEntities.BAT_SWARM, world);
    bats.refreshPositionAndAngles(
        caster.getX(), caster.getY(), caster.getZ(), caster.getYaw(), 0.0F);
    world.spawnEntity(bats);
    SWARMS.add(
        new Swarm(
            world.getRegistryKey(), caster.getUuid(), power, world.getTime(), bats.getUuid()));
  }

  private static void disperse(final MinecraftServer server, final Swarm swarm) {
    final ServerWorld world = server.getWorld(swarm.world());
    if (world != null && world.getEntity(swarm.bats()) instanceof BatSwarmEntity bats) {
      bats.discard();
    }
  }

  private static void tick(final MinecraftServer server) {
    final Iterator<Swarm> swarms = SWARMS.iterator();
    while (swarms.hasNext()) {
      final Swarm swarm = swarms.next();
      final ServerWorld world = server.getWorld(swarm.world());
      final Entity found = world == null ? null : world.getEntity(swarm.caster());
      if (!(found instanceof LivingEntity caster)
          || ends(
              caster.isAlive(),
              AreaRules.lasts(
                  world.getTime(), swarm.startedAt() + swarm.power().durationTicks()))) {
        disperse(server, swarm);
        swarms.remove();
        continue;
      }
      if (world.getEntity(swarm.bats()) instanceof BatSwarmEntity bats) {
        bats.setPosition(caster.getPos());
      }
      final long age = world.getTime() - swarm.startedAt();
      if (age % 4 == 0) {
        flutter(world, caster);
      }
      if (age % swarm.power().everyTicks() == 0) {
        sting(world, caster, swarm.power());
      }
    }
  }

  private static void flutter(final ServerWorld world, final LivingEntity caster) {
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
