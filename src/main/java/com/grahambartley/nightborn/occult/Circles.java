package com.grahambartley.nightborn.occult;

import com.grahambartley.nightborn.boss.ZoneMarkEntity;
import com.grahambartley.nightborn.boss.ZoneShape;
import com.grahambartley.nightborn.skill.AreaRules;
import com.grahambartley.nightborn.skill.PowerReach;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f;

public final class Circles {

  static final int PULSE_TICKS = 20;
  static final int DRAW_TICKS = 5;
  static final int EFFECT_TICKS = 30;
  static final DustParticleEffect BLOOD_DUST =
      new DustParticleEffect(new Vector3f(0.45F, 0.02F, 0.05F), 1.2F);

  private record Circle(
      RegistryKey<World> world, Vec3d centre, UUID caster, CirclePower power, long startedAt) {}

  private static final List<Circle> CIRCLES = new ArrayList<>();

  private Circles() {}

  static void register() {
    ServerTickEvents.END_WORLD_TICK.register(Circles::tick);
    ServerLifecycleEvents.SERVER_STOPPED.register(server -> clearAll());
  }

  public static void clearAll() {
    CIRCLES.clear();
  }

  public static void draw(final LivingEntity caster, final CirclePower power) {
    if (caster.getWorld() instanceof ServerWorld world) {
      final Vec3d at = caster.getPos();
      CIRCLES.add(new Circle(world.getRegistryKey(), at, caster.getUuid(), power, world.getTime()));
      power
          .look()
          .ifPresent(
              look ->
                  ZoneMarkEntity.place(
                          world,
                          ZoneShape.circle(at.x, at.z, power.radius()),
                          at.y + 0.02,
                          Optional.of(look))
                      .linger(power.durationTicks()));
    }
  }

  public static boolean stopsPrayers(final World world, final Vec3d at) {
    final long now = world.getTime();
    return CIRCLES.stream()
        .anyMatch(
            circle ->
                circle.power().stopsPrayers()
                    && circle.world().equals(world.getRegistryKey())
                    && now < circle.startedAt() + circle.power().durationTicks()
                    && AreaRules.inside(
                        at.squaredDistanceTo(circle.centre()), circle.power().radius()));
  }

  private static void tick(final ServerWorld world) {
    final long now = world.getTime();
    final Iterator<Circle> circles = CIRCLES.iterator();
    while (circles.hasNext()) {
      final Circle circle = circles.next();
      if (!circle.world().equals(world.getRegistryKey())) {
        continue;
      }
      if (!AreaRules.lasts(now, circle.startedAt() + circle.power().durationTicks())) {
        circles.remove();
        continue;
      }
      if ((now - circle.startedAt()) % DRAW_TICKS == 0) {
        ring(world, circle);
      }
      if ((now - circle.startedAt()) % PULSE_TICKS == 0) {
        bleed(world, circle);
      }
    }
  }

  private static void ring(final ServerWorld world, final Circle circle) {
    final double radius = circle.power().radius();
    final int points = (int) Math.ceil(radius * 8);
    for (int i = 0; i < points; i++) {
      final double angle = Math.PI * 2 * i / points;
      world.spawnParticles(
          BLOOD_DUST,
          circle.centre().x + Math.cos(angle) * radius,
          circle.centre().y + 0.1,
          circle.centre().z + Math.sin(angle) * radius,
          1,
          0.0,
          0.0,
          0.0,
          0.0);
    }
  }

  private static void bleed(final ServerWorld world, final Circle circle) {
    final float radius = circle.power().radius();
    final Entity owner = world.getEntity(circle.caster());
    final LivingEntity caster = owner instanceof LivingEntity living ? living : null;
    for (final LivingEntity living :
        world.getEntitiesByClass(
            LivingEntity.class,
            new Box(circle.centre(), circle.centre()).expand(radius, 2.0, radius),
            candidate ->
                AreaRules.inside(candidate.squaredDistanceTo(circle.centre()), radius)
                    && Humans.human(candidate)
                    && (caster == null || PowerReach.touches(caster, candidate)))) {
      for (final StatusEffectInstance effect : circle.power().effects()) {
        living.addStatusEffect(
            new StatusEffectInstance(
                effect.getEffectType(), EFFECT_TICKS, effect.getAmplifier(), false, true, true),
            caster);
      }
      if (circle.power().damage() > 0.0F) {
        living.damage(
            caster == null
                ? world.getDamageSources().magic()
                : world.getDamageSources().indirectMagic(caster, caster),
            circle.power().damage());
      }
    }
  }
}
