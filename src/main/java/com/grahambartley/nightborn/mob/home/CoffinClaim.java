package com.grahambartley.nightborn.mob.home;

import com.grahambartley.nightborn.coffin.CoffinRest;
import java.util.Optional;
import java.util.UUID;

public final class CoffinClaim {

  private CoffinClaim() {}

  public static boolean keeps(
      final boolean coffinHead, final Optional<UUID> owner, final UUID vampire) {
    return coffinHead && CoffinRest.of(owner, vampire) == CoffinRest.RETURN;
  }

  public static boolean mayClaim(final boolean coffinHead, final Optional<UUID> owner) {
    return coffinHead && owner.isEmpty();
  }
}
