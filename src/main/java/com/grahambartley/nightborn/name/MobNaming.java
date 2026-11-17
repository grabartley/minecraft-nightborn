package com.grahambartley.nightborn.name;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public final class MobNaming {

  public static final double KIN_RANGE = 48.0;

  private MobNaming() {}

  public static void ensure(final MobEntity mob) {
    if (mob.hasCustomName()
        || !(mob instanceof NameBearer bearer)
        || !(mob.getWorld() instanceof ServerWorld world)) {
      return;
    }
    final Set<String> givens = new HashSet<>();
    final Set<String> epithets = new HashSet<>();
    for (final MobEntity other :
        world.getEntitiesByClass(
            MobEntity.class,
            mob.getBoundingBox().expand(KIN_RANGE),
            other ->
                other != mob
                    && other.hasCustomName()
                    && other instanceof NameBearer kin
                    && kin.nameStyle().kin(bearer.nameStyle()))) {
      final NameBearer kin = (NameBearer) other;
      final String full = other.getName().getString();
      givens.add(Epithets.given(full, kin.epithet()).orElse(full));
      if (!kin.epithet().isEmpty()) {
        epithets.add(kin.epithet());
      }
    }
    final DynamicRegistryManager registries = world.getRegistryManager();
    final NameStyle style = bearer.nameStyle();
    final List<String> pool =
        style.equals(NameStyle.FAMILIAR)
            ? familiarPool(registries)
            : NamePools.pool(registries, bearer.bearsWomansName() ? style.women() : style.men())
                .names();
    NameDraw.pick(pool, givens, mob.getRandom()::nextInt)
        .ifPresent(
            given -> {
              final String epithet =
                  style
                      .epithets()
                      .flatMap(
                          id ->
                              NameDraw.pick(
                                  NamePools.pool(registries, id).names(),
                                  epithets,
                                  mob.getRandom()::nextInt))
                      .orElse("");
              bearer.bearEpithet(epithet);
              give(mob, Epithets.join(given, epithet));
            });
  }

  public static Optional<String> suggestion(final ServerWorld world) {
    return NameDraw.pick(familiarPool(world.getRegistryManager()), Set.of(), world.random::nextInt);
  }

  public static void give(final MobEntity mob, final String name) {
    mob.setCustomName(Text.literal(name));
    mob.setCustomNameVisible(true);
  }

  private static List<String> familiarPool(final DynamicRegistryManager registries) {
    return NameDraw.both(
        NamePools.pool(registries, NameStyle.FAMILIAR.men()).names(),
        NamePools.pool(registries, NameStyle.FAMILIAR.women()).names());
  }
}
