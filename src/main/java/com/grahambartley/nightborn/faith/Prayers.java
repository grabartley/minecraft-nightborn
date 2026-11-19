package com.grahambartley.nightborn.faith;

import com.grahambartley.nightborn.boss.ZoneMarkEntity;
import com.grahambartley.nightborn.boss.ZoneShape;
import com.grahambartley.nightborn.mind.MindEffect;
import com.grahambartley.nightborn.occult.Humans;
import com.grahambartley.nightborn.skill.AreaRules;
import com.grahambartley.nightborn.skill.PowerReach;
import com.grahambartley.nightborn.trap.TrapDamage;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class Prayers {

  static final int SHELTER_TICKS = PrayerRules.PULSE_TICKS + 10;
  static final int GLOW_TICKS = 5;

  private record Prayer(
      RegistryKey<World> world,
      UUID caster,
      @Nullable Vec3d spot,
      PrayerPower power,
      List<RegistryEntry<StatusEffect>> clears,
      long startedAt,
      Optional<ZoneMarkEntity> mark) {}

  private static final List<Prayer> PRAYERS = new ArrayList<>();

  private Prayers() {}

  static void register() {
    ServerTickEvents.END_WORLD_TICK.register(Prayers::tick);
    ServerLifecycleEvents.SERVER_STOPPED.register(server -> clearAll());
  }

  public static void clearAll() {
    PRAYERS.clear();
  }

  public static void pray(
      final LivingEntity caster, final PrayerPower power, @Nullable final BlockPos spot) {
    if (!(caster.getWorld() instanceof ServerWorld world)) {
      return;
    }
    final Vec3d at = spot == null ? null : spot.toBottomCenterPos();
    final List<RegistryEntry<StatusEffect>> clears = PrayerHealing.clears(caster);
    final Vec3d centre = at == null ? caster.getPos() : at;
    if (power.health() > 0.0F) {
      for (final LivingEntity human : humans(world, centre, power, at != null)) {
        if (PrayerHealing.heal(caster, human, power.health(), clears)) {
          PrayerSigns.mended(world, human);
        }
      }
      PrayerSigns.ring(world, centre, power.radius());
    }
    if (power.lingers()) {
      final Optional<ZoneMarkEntity> mark =
          power
              .look()
              .filter(look -> at != null)
              .map(
                  look ->
                      ZoneMarkEntity.place(
                          world,
                          ZoneShape.circle(at.x, at.z, power.radius()),
                          at.y + 0.02,
                          Optional.of(look)));
      mark.ifPresent(placed -> placed.linger(power.durationTicks()));
      final Prayer prayer =
          new Prayer(
              world.getRegistryKey(), caster.getUuid(), at, power, clears, world.getTime(), mark);
      PRAYERS.add(prayer);
      pulse(world, prayer, caster, centre);
    }
  }

  private static void tick(final ServerWorld world) {
    final long now = world.getTime();
    final Iterator<Prayer> prayers = PRAYERS.iterator();
    while (prayers.hasNext()) {
      final Prayer prayer = prayers.next();
      if (!prayer.world().equals(world.getRegistryKey())) {
        continue;
      }
      final LivingEntity caster = caster(world, prayer);
      if (!AreaRules.lasts(now, prayer.startedAt() + prayer.power().durationTicks())
          || caster == null
          || !caster.isAlive()) {
        prayer.mark().ifPresent(ZoneMarkEntity::discard);
        prayers.remove();
        continue;
      }
      final Vec3d centre = prayer.spot() == null ? caster.getPos() : prayer.spot();
      if ((now - prayer.startedAt()) % GLOW_TICKS == 0) {
        PrayerSigns.glow(world, centre, prayer.power(), prayer.spot() != null);
      }
      if (PrayerRules.pulses(prayer.startedAt(), now)) {
        pulse(world, prayer, caster, centre);
      }
    }
  }

  private static void pulse(
      final ServerWorld world, final Prayer prayer, final LivingEntity caster, final Vec3d centre) {
    final PrayerPower power = prayer.power();
    for (final LivingEntity human : humans(world, centre, power, prayer.spot() != null)) {
      if (power.healthPerSecond() > 0.0F) {
        PrayerHealing.heal(caster, human, power.healthPerSecond(), prayer.clears());
      }
      if (power.shelters()) {
        shelter(human);
      }
    }
    if (power.harmPerSecond() > 0.0F) {
      power.harmType().ifPresent(type -> harm(world, prayer, caster, centre, type));
    }
  }

  private static void shelter(final LivingEntity human) {
    final boolean fresh = !FaithMarks.SHELTERED.on(human);
    FaithMarks.SHELTERED.set(human, SHELTER_TICKS);
    human.removeStatusEffect(MindEffect.MESMERISED);
    human.removeStatusEffect(MindEffect.COWED);
    if (fresh && human instanceof ServerPlayerEntity player) {
      player.sendMessage(Text.translatable("nightborn.faith.sheltered"), true);
    }
  }

  private static void harm(
      final ServerWorld world,
      final Prayer prayer,
      final LivingEntity caster,
      final Vec3d centre,
      final RegistryKey<DamageType> type) {
    for (final LivingEntity vampire :
        inside(
            world,
            centre,
            prayer.power(),
            prayer.spot() != null,
            candidate -> Vampires.isVampire(candidate) && PowerReach.touches(caster, candidate))) {
      vampire.damage(TrapDamage.of(vampire, type, caster), prayer.power().harmPerSecond());
    }
  }

  private static List<LivingEntity> humans(
      final ServerWorld world, final Vec3d centre, final PrayerPower power, final boolean pillar) {
    return inside(world, centre, power, pillar, Humans::human);
  }

  private static List<LivingEntity> inside(
      final ServerWorld world,
      final Vec3d centre,
      final PrayerPower power,
      final boolean pillar,
      final Predicate<LivingEntity> test) {
    final float radius = power.radius();
    final Box reach =
        pillar
            ? new Box(centre, centre).expand(radius, 1.0, radius).stretch(0.0, power.height(), 0.0)
            : new Box(centre, centre).expand(radius);
    return world.getEntitiesByClass(
        LivingEntity.class,
        reach,
        candidate ->
            !candidate.isSpectator()
                && (pillar
                    ? AreaRules.column(candidate.getPos(), centre, radius, power.height())
                    : AreaRules.inside(candidate.squaredDistanceTo(centre), radius))
                && test.test(candidate));
  }

  @Nullable
  private static LivingEntity caster(final ServerWorld world, final Prayer prayer) {
    final Entity caster = world.getEntity(prayer.caster());
    return caster instanceof LivingEntity living ? living : null;
  }
}
