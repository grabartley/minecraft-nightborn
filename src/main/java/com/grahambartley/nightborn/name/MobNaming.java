package com.grahambartley.nightborn.name;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class MobNaming {

  public static final double KIN_RANGE = 48.0;

  private MobNaming() {}

  public static void ensure(final MobEntity mob) {
    if (mob.hasCustomName()
        || !(mob instanceof NameBearer bearer)
        || !(mob.getWorld() instanceof ServerWorld world)) {
      return;
    }
    NameStyles.of(mob).ifPresent(style -> name(mob, bearer, world, style));
  }

  public static Optional<String> suggestion(final MobEntity mob) {
    if (!(mob instanceof NameBearer bearer) || !(mob.getWorld() instanceof ServerWorld world)) {
      return Optional.empty();
    }
    return NameStyles.of(mob)
        .flatMap(
            style ->
                NameDraw.pick(
                    names(world.getRegistryManager(), style.poolsFor(bearer.bearsWomansName())),
                    KinNames.taken(kin(mob, world, style)).givens(),
                    mob.getRandom()::nextInt));
  }

  public static void give(final MobEntity mob, final String name) {
    mob.setCustomName(Text.literal(name));
    mob.setCustomNameVisible(true);
  }

  private static void name(
      final MobEntity mob,
      final NameBearer bearer,
      final ServerWorld world,
      final NameStyle style) {
    final DynamicRegistryManager registries = world.getRegistryManager();
    final List<String> pool = names(registries, style.poolsFor(bearer.bearsWomansName()));
    if (pool.isEmpty()) {
      return;
    }
    final KinNames.Taken taken = KinNames.taken(kin(mob, world, style));
    NameDraw.pick(pool, taken.givens(), mob.getRandom()::nextInt)
        .ifPresent(
            given -> {
              final String epithet =
                  style
                      .epithets()
                      .flatMap(
                          id ->
                              NameDraw.pick(
                                  NamePools.pool(registries, id).names(),
                                  taken.epithets(),
                                  mob.getRandom()::nextInt))
                      .orElse("");
              bearer.bearEpithet(epithet);
              give(mob, Epithets.join(given, epithet));
            });
  }

  private static List<KinNames.Held> kin(
      final MobEntity mob, final ServerWorld world, final NameStyle style) {
    return world
        .getEntitiesByClass(
            MobEntity.class,
            mob.getBoundingBox().expand(KIN_RANGE),
            other ->
                other != mob
                    && other.hasCustomName()
                    && other instanceof NameBearer
                    && NameStyles.of(other).filter(style::kin).isPresent())
        .stream()
        .map(
            other -> new KinNames.Held(other.getName().getString(), ((NameBearer) other).epithet()))
        .toList();
  }

  private static List<String> names(
      final DynamicRegistryManager registries, final List<Identifier> pools) {
    final List<String> names = new ArrayList<>();
    pools.forEach(id -> names.addAll(NamePools.pool(registries, id).names()));
    return names;
  }
}
