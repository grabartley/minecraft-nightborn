package com.grahambartley.nightborn.occult;

import com.grahambartley.nightborn.thrall.ThrallEntity;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.entity.Entity;

public final class Servants {

  private Servants() {}

  public static Optional<UUID> masterOf(final Entity entity) {
    if (entity instanceof GhostlyFamiliarEntity familiar) {
      return familiar.owner();
    }
    if (entity instanceof ThrallEntity thrall) {
      return thrall.owner();
    }
    return Optional.empty();
  }

  public static boolean serves(final Entity servant, final Entity master) {
    return serves(masterOf(servant), master.getUuid());
  }

  public static boolean sameMaster(final Entity one, final Entity other) {
    return sameMaster(masterOf(one), masterOf(other));
  }

  static boolean serves(final Optional<UUID> master, final UUID candidate) {
    return master.filter(candidate::equals).isPresent();
  }

  static boolean sameMaster(final Optional<UUID> one, final Optional<UUID> other) {
    return one.isPresent() && one.equals(other);
  }
}
