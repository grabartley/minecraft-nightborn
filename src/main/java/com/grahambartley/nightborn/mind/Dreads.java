package com.grahambartley.nightborn.mind;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.faith.FaithMarks;
import com.grahambartley.nightborn.hunter.HunterAttributes;
import com.grahambartley.nightborn.skill.Boost;
import com.grahambartley.nightborn.skill.Boosts;
import com.grahambartley.nightborn.skill.PowerReach;
import com.grahambartley.nightborn.skill.SkillAttribute;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class Dreads {

  static final int PULSE_TICKS = 10;
  static final int HOLD_TICKS = 20;
  static final int FLEE_DISTANCE = 16;
  static final Identifier FALTER = Identifier.of(Nightborn.MOD_ID, "dread_falter");

  private record Dread(
      UUID caster, RegistryKey<World> world, DreadPower power, long endsAt, Set<UUID> resisted) {}

  private static final List<Dread> DREADS = new ArrayList<>();

  private Dreads() {}

  static void register() {
    ServerTickEvents.END_SERVER_TICK.register(Dreads::tick);
    ServerLifecycleEvents.SERVER_STOPPED.register(server -> DREADS.clear());
  }

  public static void start(final LivingEntity caster, final DreadPower power) {
    DREADS.removeIf(dread -> dread.caster().equals(caster.getUuid()));
    DREADS.add(
        new Dread(
            caster.getUuid(),
            caster.getWorld().getRegistryKey(),
            power,
            caster.getWorld().getTime() + power.durationTicks(),
            new HashSet<>()));
  }

  private static void tick(final MinecraftServer server) {
    if (DREADS.isEmpty() || server.getTicks() % PULSE_TICKS != 0) {
      return;
    }
    final Iterator<Dread> dreads = DREADS.iterator();
    while (dreads.hasNext()) {
      final Dread dread = dreads.next();
      final ServerWorld world = server.getWorld(dread.world());
      final Entity caster = world == null ? null : world.getEntity(dread.caster());
      if (!(caster instanceof LivingEntity vampire)
          || !vampire.isAlive()
          || world.getTime() >= dread.endsAt()) {
        dreads.remove();
        continue;
      }
      pulse(world, vampire, dread);
    }
  }

  private static void pulse(final ServerWorld world, final LivingEntity caster, final Dread dread) {
    final DreadPower power = dread.power();
    final double radius = power.radius();
    world.spawnParticles(
        ParticleTypes.SQUID_INK,
        caster.getX(),
        caster.getY() + 0.2,
        caster.getZ(),
        (int) Math.ceil(radius * 2),
        radius * 0.5,
        0.1,
        radius * 0.5,
        0.0);
    for (final LivingEntity living :
        world.getEntitiesByClass(
            LivingEntity.class,
            new Box(caster.getBlockPos()).expand(radius),
            candidate ->
                candidate.squaredDistanceTo(caster) <= radius * radius
                    && PowerReach.touches(caster, candidate)
                    && !Vampires.isVampire(candidate))) {
      touch(caster, living, dread);
    }
  }

  private static void touch(
      final LivingEntity caster, final LivingEntity living, final Dread dread) {
    if (FaithMarks.SHELTERED.on(living)) {
      return;
    }
    final DreadPower power = dread.power();
    if (living instanceof PlayerEntity player) {
      if (dread.resisted().contains(player.getUuid())) {
        return;
      }
      final double resistance =
          player.getAttributes().hasAttribute(HunterAttributes.MESMERISE_RESISTANCE)
              ? player.getAttributeValue(HunterAttributes.MESMERISE_RESISTANCE)
              : 0.0;
      if (MesmeriseRules.land(false, resistance, caster.getRandom().nextFloat())
          == MesmeriseRules.Outcome.RESISTED) {
        dread.resisted().add(player.getUuid());
        return;
      }
      for (final StatusEffectInstance effect : power.playerEffects()) {
        player.addStatusEffect(
            new StatusEffectInstance(
                effect.getEffectType(), HOLD_TICKS, effect.getAmplifier(), false, true, true),
            caster);
      }
      return;
    }
    if (living.getType().isIn(power.cowers())) {
      living.addStatusEffect(new StatusEffectInstance(MindEffect.COWED, HOLD_TICKS), caster);
    } else if (living.getType().isIn(power.falters())) {
      Boosts.start(
          living,
          FALTER,
          Boost.of(
              living.getWorld().getTime(),
              HOLD_TICKS,
              List.of(
                  new SkillAttribute(
                      EntityAttributes.GENERIC_ATTACK_DAMAGE,
                      power.falterDamage() - 1.0,
                      EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL))));
    } else if (living.getType().isIn(power.flees()) && living instanceof PathAwareEntity mob) {
      final Vec3d away = NoPenaltyTargeting.findFrom(mob, FLEE_DISTANCE, 7, caster.getPos());
      if (away != null) {
        mob.setTarget(null);
        mob.getNavigation().startMovingTo(away.x, away.y, away.z, 1.4);
      }
    }
  }
}
