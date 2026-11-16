package com.grahambartley.nightborn.servant;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

public final class Servants {

  private Servants() {}

  public static Optional<UUID> masterOf(final Entity entity) {
    return entity instanceof Servant servant ? servant.owner() : Optional.empty();
  }

  @Nullable
  public static LivingEntity masterHere(final Entity servant) {
    if (!(servant.getWorld() instanceof ServerWorld world)) {
      return null;
    }
    return masterOf(servant)
        .map(world::getEntity)
        .filter(entity -> entity instanceof LivingEntity living && living.isAlive())
        .map(LivingEntity.class::cast)
        .orElse(null);
  }

  public static boolean serves(final Entity servant, final Entity master) {
    return serves(masterOf(servant), master.getUuid());
  }

  public static boolean sameMaster(final Entity one, final Entity other) {
    return sameMaster(masterOf(one), masterOf(other));
  }

  public static boolean serves(final Optional<UUID> master, final UUID candidate) {
    return master.filter(candidate::equals).isPresent();
  }

  public static boolean sameMaster(final Optional<UUID> one, final Optional<UUID> other) {
    return one.isPresent() && one.equals(other);
  }
}
