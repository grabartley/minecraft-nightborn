package com.grahambartley.nightborn.servant;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.entity.Entity;

public final class Servants {

  private Servants() {}

  public static Optional<UUID> masterOf(final Entity entity) {
    return entity instanceof Servant servant ? servant.owner() : Optional.empty();
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
